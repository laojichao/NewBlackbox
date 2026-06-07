package com.vcore.core.system.am;

import static android.content.pm.PackageManager.GET_ACTIVITIES;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import black.android.app.ActivityManagerNative;
import black.android.app.IActivityManager;

import com.vcore.BlackBoxCore;
import com.vcore.core.system.BProcessManagerService;
import com.vcore.core.system.ProcessRecord;
import com.vcore.core.system.pm.BPackageManagerService;
import com.vcore.core.system.pm.PackageManagerCompat;
import com.vcore.proxy.ProxyActivity;
import com.vcore.proxy.ProxyManifest;
import com.vcore.proxy.record.ProxyActivityRecord;
import com.vcore.utils.ArrayUtils;
import com.vcore.utils.ComponentUtils;
import com.vcore.utils.Slog;
import com.vcore.utils.compat.ActivityManagerCompat;

/**
 * Manages the virtual activity stack and handles activity launch modes within the virtual environment.
 *
 * <p>This class implements the activity management logic for the virtual container, including
 * task affinity resolution, launch mode handling (standard, singleTop, singleTask, singleInstance),
 * and activity lifecycle callbacks. Activities are launched through proxy activities in the host
 * process, with theme detection for transparent activities.</p>
 */
public class ActivityStack {
    public static final String TAG = "ActivityStack";

    /** The system ActivityManager used for task operations. */
    private final ActivityManager mAms;

    /** Maps task IDs to their TaskRecord, maintaining insertion order. */
    private final Map<Integer, TaskRecord> mTasks = new LinkedHashMap<>();

    /** Maps activity tokens to ActivityRecords for activities currently being launched. */
    private final Map<String, ActivityRecord> mLaunchingActivities = new HashMap<>();

    /** Message ID for launch timeout handler. */
    public static final int LAUNCH_TIME_OUT = 0;

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == LAUNCH_TIME_OUT) {
                String token = (String) msg.obj;
                if (token != null) {
                    mLaunchingActivities.remove(token);
                }
            }
        }
    };

    /**
     * Creates a new ActivityStack instance and initializes the system ActivityManager.
     */
    public ActivityStack() {
        this.mAms = (ActivityManager) BlackBoxCore.getContext().getSystemService(Context.ACTIVITY_SERVICE);
    }

    /**
     * Checks whether an intent contains a specific flag.
     *
     * @param intent the intent to check
     * @param flag   the flag bit to test
     * @return true if the intent contains the specified flag
     */
    public boolean containsFlag(Intent intent, int flag) {
        return (intent.getFlags() & flag) != 0;
    }

    /**
     * Starts multiple activities sequentially within the virtual environment.
     *
     * @param userId        the virtual user ID
     * @param intents       array of intents to start
     * @param resolvedTypes array of resolved MIME types corresponding to each intent
     * @param resultTo      the token of the activity receiving the result
     * @param options       additional launch options bundle
     * @return always returns 0
     * @throws NullPointerException     if intents or resolvedTypes is null
     * @throws IllegalArgumentException if intents and resolvedTypes have different lengths
     */
    public int startActivitiesLocked(int userId, Intent[] intents, String[] resolvedTypes, IBinder resultTo, Bundle options) {
        if (intents == null) {
            throw new NullPointerException("intents is null");
        }

        if (resolvedTypes == null) {
            throw new NullPointerException("resolvedTypes is null");
        }

        if (intents.length != resolvedTypes.length) {
            throw new IllegalArgumentException("intents are length different than resolvedTypes");
        }

        for (int i = 0; i < intents.length; i++) {
            startActivityLocked(userId, intents[i], resolvedTypes[i], resultTo, null, -1, 0, options);
        }
        return 0;
    }

    /**
     * Starts a single activity within the virtual environment, handling all launch modes.
     *
     * <p>This method resolves the activity, determines the appropriate task based on task affinity
     * and launch mode flags, and either launches into an existing task, creates a new task,
     * or delivers a new intent to an existing activity instance. Supports all standard
     * Android launch modes: standard, singleTop, singleTask, and singleInstance.</p>
     *
     * @param userId       the virtual user ID
     * @param intent       the intent describing the activity to start
     * @param resolvedType the resolved MIME type of the intent
     * @param resultTo     the token of the activity receiving the result
     * @param resultWho    the identifier of the result recipient
     * @param requestCode  the request code for startActivityForResult, or -1 if not used
     * @param flags        additional flags for the activity launch
     * @param options      additional launch options bundle
     * @return always returns 0
     */
    public int startActivityLocked(int userId, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags, Bundle options) {
        synchronized (mTasks) {
            synchronizeTasks();
        }

        ResolveInfo resolveInfo = BPackageManagerService.get().resolveActivity(intent, GET_ACTIVITIES, resolvedType, userId);
        if (resolveInfo == null || resolveInfo.activityInfo == null) {
            return 0;
        }

        Slog.d(TAG, "startActivityLocked : " + resolveInfo.activityInfo);
        ActivityInfo activityInfo = resolveInfo.activityInfo;

        ActivityRecord sourceRecord = findActivityRecordByToken(userId, resultTo);
        if (sourceRecord == null) {
            resultTo = null;
        }

        TaskRecord sourceTask = null;
        if (sourceRecord != null) {
            sourceTask = sourceRecord.task;
        }

        String taskAffinity = ComponentUtils.getTaskAffinity(activityInfo);

        int launchModeFlags = 0;
        boolean singleTop = containsFlag(intent, Intent.FLAG_ACTIVITY_SINGLE_TOP) || activityInfo.launchMode == ActivityInfo.LAUNCH_SINGLE_TOP;
        boolean newTask = containsFlag(intent, Intent.FLAG_ACTIVITY_NEW_TASK);
        boolean clearTop = containsFlag(intent, Intent.FLAG_ACTIVITY_CLEAR_TOP);
        boolean clearTask = containsFlag(intent, Intent.FLAG_ACTIVITY_CLEAR_TASK);

        TaskRecord taskRecord = null;
        switch (activityInfo.launchMode) {
            case ActivityInfo.LAUNCH_SINGLE_TOP:
            case ActivityInfo.LAUNCH_MULTIPLE:
            case ActivityInfo.LAUNCH_SINGLE_TASK:
                taskRecord = findTaskRecordByTaskAffinityLocked(userId, taskAffinity);
                if (taskRecord == null && !newTask) {
                    taskRecord = sourceTask;
                }
                break;
            case ActivityInfo.LAUNCH_SINGLE_INSTANCE:
                taskRecord = findTaskRecordByTaskAffinityLocked(userId, taskAffinity);
                break;
        }

        // 如果还没有task则新启动一个task
        if (taskRecord == null || taskRecord.needNewTask()) {
            return startActivityInNewTaskLocked(userId, intent, activityInfo, resultTo, launchModeFlags);
        }
        // 移至前台
        mAms.moveTaskToFront(taskRecord.id, 0);

        boolean notStartToFront = clearTop || singleTop || clearTask;
        boolean startTaskToFront = !notStartToFront && ComponentUtils.intentFilterEquals(taskRecord.rootIntent, intent)
                && taskRecord.rootIntent.getFlags() == intent.getFlags();
        if (startTaskToFront)
            return 0;

        ActivityRecord topActivityRecord = taskRecord.getTopActivityRecord();
        ActivityRecord targetActivityRecord = findActivityRecordByComponentName(userId, ComponentUtils.toComponentName(activityInfo));
        ActivityRecord newIntentRecord = null;

        if (clearTop) {
            if (targetActivityRecord != null) {
                // 目标栈上面所有activity出栈
                synchronized (Objects.requireNonNull(targetActivityRecord.task).activities) {
                    for (int i = targetActivityRecord.task.activities.size() - 1; i >= 0; i--) {
                        ActivityRecord next = targetActivityRecord.task.activities.get(i);
                        if (next != targetActivityRecord) {
                            next.finished = true;
                            Slog.d(TAG, "makerFinish: " + Objects.requireNonNull(next.component));
                        } else {
                            if (singleTop) {
                                newIntentRecord = targetActivityRecord;
                            } else {
                                // clearTop并且不是singleTop，目标也finish，重建。
                                targetActivityRecord.finished = true;
                            }
                            break;
                        }
                    }
                }
            }
        }

        if (singleTop && !clearTop) {
            if (ComponentUtils.intentFilterEquals(topActivityRecord.intent, intent)) {
                newIntentRecord = topActivityRecord;
            }
        }

        if (activityInfo.launchMode == ActivityInfo.LAUNCH_SINGLE_TASK && !clearTop) {
            if (ComponentUtils.intentFilterEquals(topActivityRecord.intent, intent)) {
                newIntentRecord = topActivityRecord;
            } else {
                ActivityRecord record = findActivityRecordByComponentName(userId, ComponentUtils.toComponentName(activityInfo));
                if (record != null) {
                    // 需要调用目标onNewIntent
                    newIntentRecord = record;
                    // 目标栈上面所有activity出栈
                    synchronized (taskRecord.activities) {
                        for (int i = taskRecord.activities.size() - 1; i >= 0; i--) {
                            ActivityRecord next = taskRecord.activities.get(i);
                            if (next != record) {
                                next.finished = true;
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (activityInfo.launchMode == ActivityInfo.LAUNCH_SINGLE_INSTANCE) {
            newIntentRecord = topActivityRecord;
        }

        // clearTask finish All
        if (clearTask && newTask) {
            for (ActivityRecord activity : taskRecord.activities) {
                activity.finished = true;
            }
        }

        finishAllActivity(userId);

        if (newIntentRecord != null) {
            // 通知onNewIntent
            deliverNewIntentLocked(newIntentRecord, intent);
            return 0;
        }

        if (resultTo == null) {
            ActivityRecord top = taskRecord.getTopActivityRecord();
            if (top != null) {
                resultTo = top.token;
            }
        } else if (sourceTask != null) {
            ActivityRecord top = sourceTask.getTopActivityRecord();
            if (top != null) {
                resultTo = top.token;
            }
        }
        return startActivityInSourceTask(intent, resolvedType, resultTo, resultWho, requestCode, flags, options, userId, topActivityRecord, activityInfo,
                launchModeFlags);
    }

    /**
     * Delivers a new intent notification to an existing activity via its activity thread.
     *
     * @param activityRecord the activity record to receive the new intent
     * @param intent         the new intent to deliver
     */
    private void deliverNewIntentLocked(ActivityRecord activityRecord, Intent intent) {
        try {
            Objects.requireNonNull(activityRecord.processRecord).bActivityThread.handleNewIntent(activityRecord.token, intent);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the target activity's process and returns a stub intent for launching via proxy.
     *
     * @param userId  the virtual user ID
     * @param intent  the original activity intent
     * @param info    the resolved ActivityInfo
     * @param record  the ActivityRecord for the activity being launched
     * @return a stub intent pointing to the proxy activity in the host
     * @throws RuntimeException if the target process cannot be created
     */
    private Intent startActivityProcess(int userId, Intent intent, ActivityInfo info, ActivityRecord record) {
        ProxyActivityRecord stubRecord = new ProxyActivityRecord(userId, info, intent, record.mBToken);
        ProcessRecord targetApp = BProcessManagerService.get().startProcessLocked(info.packageName, info.processName, userId, -1, Binder.getCallingPid());
        if (targetApp == null) {
            throw new RuntimeException("Unable to create process, name:" + info.name);
        }
        return getStartStubActivityIntentInner(intent, targetApp.bPID, stubRecord, info);
    }

    /**
     * Starts an activity in a new task with NEW_TASK, MULTIPLE_TASK, and NEW_DOCUMENT flags.
     *
     * @param userId       the virtual user ID
     * @param intent       the original activity intent
     * @param activityInfo the resolved ActivityInfo
     * @param resultTo     the token of the calling activity
     * @param launchMode   additional launch mode flags
     * @return always returns 0
     */
    private int startActivityInNewTaskLocked(int userId, Intent intent, ActivityInfo activityInfo, IBinder resultTo, int launchMode) {
        ActivityRecord record = newActivityRecord(intent, activityInfo, resultTo, userId);
        Intent shadow = startActivityProcess(userId, intent, activityInfo, record);

        shadow.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        shadow.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shadow.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shadow.addFlags(launchMode);

        BlackBoxCore.getContext().startActivity(shadow);
        return 0;
    }

    /**
     * Starts an activity within the source task by delegating to the real activity manager.
     *
     * @param intent          the original activity intent
     * @param resolvedType    the resolved MIME type
     * @param resultTo        the token of the activity receiving the result
     * @param resultWho       the identifier of the result recipient
     * @param requestCode     the request code for startActivityForResult
     * @param flags           additional launch flags
     * @param options         additional launch options
     * @param userId          the virtual user ID
     * @param sourceRecord    the source activity record
     * @param activityInfo    the resolved ActivityInfo
     * @param launchMode      additional launch mode flags
     * @return always returns 0
     */
    private int startActivityInSourceTask(Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags, Bundle options,
                                          int userId, ActivityRecord sourceRecord, ActivityInfo activityInfo, int launchMode) {
        ActivityRecord selfRecord = newActivityRecord(intent, activityInfo, resultTo, userId);
        Intent shadow = startActivityProcess(userId, intent, activityInfo, selfRecord);
        shadow.setAction(UUID.randomUUID().toString());
        shadow.addFlags(launchMode);
        if (resultTo == null) {
            shadow.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return realStartActivityLocked(Objects.requireNonNull(sourceRecord.processRecord).appThread, shadow, resolvedType, resultTo, resultWho, requestCode, flags, options);
    }

    /**
     * Performs the actual activity start by invoking the system's IActivityManager.
     * Strips debug-related flags before the call.
     *
     * @param appThread    the application thread interface of the target process
     * @param intent       the stub intent to start
     * @param resolvedType the resolved MIME type
     * @param resultTo     the result recipient token
     * @param resultWho    the result recipient identifier
     * @param requestCode  the request code
     * @param flags        launch flags (debug flags will be stripped)
     * @param options      additional launch options
     * @return always returns 0
     */
    private int realStartActivityLocked(IInterface appThread, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags,
                                        Bundle options) {
        try {
            flags &= ~ActivityManagerCompat.START_FLAG_DEBUG;
            flags &= ~ActivityManagerCompat.START_FLAG_NATIVE_DEBUGGING;
            flags &= ~ActivityManagerCompat.START_FLAG_TRACK_ALLOCATION;

            IActivityManager.startActivity.call(ActivityManagerNative.getDefault.call(), appThread, BlackBoxCore.getHostPkg(), intent, resolvedType, resultTo, resultWho, requestCode, flags, null, options);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Creates a stub activity intent, detecting whether the target activity uses a transparent
     * theme to select the appropriate proxy activity (transparent or standard).
     *
     * @param intent         the original activity intent
     * @param vPID           the virtual process ID for proxy selection
     * @param target         the ProxyActivityRecord containing activity metadata
     * @param activityInfo   the resolved ActivityInfo for theme inspection
     * @return a stub intent configured to launch the appropriate proxy activity
     */
    private Intent getStartStubActivityIntentInner(Intent intent, int vPID, ProxyActivityRecord target, ActivityInfo activityInfo) {
        Intent shadow = new Intent();
        TypedArray typedArray = null;

        try {
            Resources resources = PackageManagerCompat.getResources(BlackBoxCore.getContext(), activityInfo.applicationInfo);
            int id;
            if (activityInfo.theme != 0) {
                id = activityInfo.theme;
            } else {
                id = activityInfo.applicationInfo.theme;
            }

            assert resources != null;
            typedArray = resources.newTheme().obtainStyledAttributes(id, ArrayUtils.toInt(black.com.android.internal.R.styleable.Window.get()));
            boolean windowIsTranslucent = typedArray.getBoolean(black.com.android.internal.R.styleable.Window_windowIsTranslucent.get(), false);
            if (windowIsTranslucent) {
                shadow.setComponent(new ComponentName(BlackBoxCore.getHostPkg(), ProxyManifest.TransparentProxyActivity(vPID)));
            } else {
                shadow.setComponent(new ComponentName(BlackBoxCore.getHostPkg(), ProxyManifest.getProxyActivity(vPID)));
            }

            Slog.d(TAG, activityInfo + ", windowIsTranslucent: " + windowIsTranslucent);
        } catch (Throwable e) {
            e.printStackTrace();
            shadow.setComponent(new ComponentName(BlackBoxCore.getHostPkg(), ProxyManifest.getProxyActivity(vPID)));
        } finally {
            if (typedArray != null) {
                typedArray.recycle();
            }
        }
        ProxyActivityRecord.saveStub(shadow, intent, target.mActivityInfo, target.mActivityToken, target.mUserId);
        return shadow;
    }

    /**
     * Finishes all activities marked as finished for the given user by notifying their processes.
     *
     * @param userId the virtual user ID
     */
    private void finishAllActivity(int userId) {
        for (TaskRecord task : mTasks.values()) {
            for (ActivityRecord activity : task.activities) {
                if (activity.userId == userId) {
                    if (activity.finished) {
                        try {
                            assert activity.processRecord != null;
                            activity.processRecord.bActivityThread.finishActivity(activity.token);
                        } catch (RemoteException ignored) { }
                    }
                }
            }
        }
    }

    /**
     * Creates a new ActivityRecord and registers it in the launching activities map with a timeout.
     *
     * @param intent   the activity intent
     * @param info     the resolved ActivityInfo
     * @param resultTo the token of the calling activity
     * @param userId   the virtual user ID
     * @return the newly created ActivityRecord
     */
    ActivityRecord newActivityRecord(Intent intent, ActivityInfo info, IBinder resultTo, int userId) {
        ActivityRecord targetRecord = ActivityRecord.create(intent, info, resultTo, userId);
        synchronized (mLaunchingActivities) {
            mLaunchingActivities.put(targetRecord.mBToken, targetRecord);
            Message obtain = Message.obtain(mHandler, LAUNCH_TIME_OUT, targetRecord.mBToken);
            mHandler.sendMessageDelayed(obtain, 15000);
        }
        return targetRecord;
    }

    /**
     * Finds an activity record by its component name for the given user.
     *
     * @param userId      the virtual user ID
     * @param componentName the component name to search for
     * @return the matching ActivityRecord, or null if not found
     */
    private ActivityRecord findActivityRecordByComponentName(int userId, ComponentName componentName) {
        ActivityRecord record = null;
        for (TaskRecord next : mTasks.values()) {
            if (userId == next.userId) {
                for (ActivityRecord activity : next.activities) {
                    if (Objects.equals(activity.component, componentName)) {
                        record = activity;
                        break;
                    }
                }
            }
        }
        return record;
    }

    /**
     * Finds an activity record by its binder token for the given user.
     *
     * @param userId the virtual user ID
     * @param token  the IBinder token to search for
     * @return the matching ActivityRecord, or null if not found
     */
    private ActivityRecord findActivityRecordByToken(int userId, IBinder token) {
        ActivityRecord record = null;
        if (token != null) {
            for (TaskRecord next : mTasks.values()) {
                if (userId == next.userId) {
                    for (ActivityRecord activity : next.activities) {
                        if (activity.token == token) {
                            record = activity;
                            break;
                        }
                    }
                }
            }
        }
        return record;
    }

    /**
     * Finds a task record by its task affinity for the given user.
     *
     * @param userId      the virtual user ID
     * @param taskAffinity the task affinity string to match
     * @return the matching TaskRecord, or null if not found
     */
    private TaskRecord findTaskRecordByTaskAffinityLocked(int userId, String taskAffinity) {
        synchronized (mTasks) {
            for (TaskRecord next : mTasks.values()) {
                if (userId == next.userId && next.taskAffinity.equals(taskAffinity)) {
                    return next;
                }
            }
            return null;
        }
    }

    /**
     * Called when an activity is created. Registers the activity in the appropriate task
     * and removes it from the launching activities map.
     *
     * @param processRecord the process hosting the activity
     * @param taskId        the system task ID
     * @param token         the activity's IBinder token
     * @param activityToken the unique activity launch token
     */
    public void onActivityCreated(ProcessRecord processRecord, int taskId, IBinder
            token, String activityToken) {
        ActivityRecord record = mLaunchingActivities.get(activityToken);
        if (record == null) {
            return;
        }

        synchronized (mLaunchingActivities) {
            mLaunchingActivities.remove(activityToken);
            mHandler.removeMessages(LAUNCH_TIME_OUT, activityToken);
        }

        synchronized (mTasks) {
            synchronizeTasks();
            TaskRecord taskRecord = mTasks.get(taskId);
            if (taskRecord == null) {
                taskRecord = new TaskRecord(taskId, record.userId, ComponentUtils.getTaskAffinity(Objects.requireNonNull(record.info)));
                taskRecord.rootIntent = record.intent;
                mTasks.put(taskId, taskRecord);
            }

            record.token = token;
            record.processRecord = processRecord;
            record.task = taskRecord;
            taskRecord.addTopActivity(record);
            Slog.d(TAG, "onActivityCreated : " + Objects.requireNonNull(record.component));
        }
    }

    /**
     * Called when an activity is resumed. Moves the activity to the top of its task stack.
     *
     * @param userId the virtual user ID
     * @param token  the activity's IBinder token
     */
    // FIXME: Multiple activities belonged to same app.
    public void onActivityResumed(int userId, IBinder token) {
        synchronized (mTasks) {
            synchronizeTasks();
            ActivityRecord activityRecord = findActivityRecordByToken(userId, token);
            if (activityRecord == null) {
                return;
            }

            Slog.d(TAG, "onActivityResumed : " + Objects.requireNonNull(activityRecord.component));
            synchronized (Objects.requireNonNull(activityRecord.task).activities) {
                activityRecord.task.removeActivity(activityRecord);
                activityRecord.task.addTopActivity(activityRecord);
            }
        }
    }

    /**
     * Called when an activity is destroyed. Marks it as finished and removes it from its task.
     *
     * @param userId the virtual user ID
     * @param token  the activity's IBinder token
     */
    public void onActivityDestroyed(int userId, IBinder token) {
        synchronized (mTasks) {
            synchronizeTasks();
            ActivityRecord activityRecord = findActivityRecordByToken(userId, token);
            if (activityRecord == null) {
                return;
            }

            activityRecord.finished = true;
            Slog.d(TAG, "onActivityDestroyed : " + Objects.requireNonNull(activityRecord.component));
            synchronized (Objects.requireNonNull(activityRecord.task).activities) {
                activityRecord.task.removeActivity(activityRecord);
            }
        }
    }

    /**
     * Called when an activity finishes. Marks the activity as finished.
     *
     * @param userId the virtual user ID
     * @param token  the activity's IBinder token
     */
    public void onFinishActivity(int userId, IBinder token) {
        synchronized (mTasks) {
            synchronizeTasks();
            ActivityRecord activityRecord = findActivityRecordByToken(userId, token);
            if (activityRecord == null) {
                return;
            }

            activityRecord.finished = true;
            Slog.d(TAG, "onFinishActivity : " + Objects.requireNonNull(activityRecord.component));
        }
    }

    /**
     * Returns the package name of the activity that started the given activity.
     *
     * @param token  the IBinder token of the activity
     * @param userId the virtual user ID
     * @return the calling package name, or the host package name if not found
     */
    public String getCallingPackage(IBinder token, int userId) {
        synchronized (mTasks) {
            synchronizeTasks();
            ActivityRecord activityRecordByToken = findActivityRecordByToken(userId, token);
            if (activityRecordByToken != null) {
                ActivityRecord resultTo = findActivityRecordByToken(userId, activityRecordByToken.resultTo);
                if (resultTo != null) {
                    return Objects.requireNonNull(resultTo.info).packageName;
                }
            }
            return BlackBoxCore.getHostPkg();
        }
    }

    /**
     * Returns the component name of the activity that started the given activity.
     *
     * @param token  the IBinder token of the activity
     * @param userId the virtual user ID
     * @return the calling activity's ComponentName, or a default proxy activity if not found
     */
    public ComponentName getCallingActivity(IBinder token, int userId) {
        synchronized (mTasks) {
            synchronizeTasks();
            ActivityRecord activityRecordByToken = findActivityRecordByToken(userId, token);
            if (activityRecordByToken != null) {
                ActivityRecord resultTo = findActivityRecordByToken(userId, activityRecordByToken.resultTo);
                if (resultTo != null) {
                    return resultTo.component;
                }
            }
            return new ComponentName(BlackBoxCore.getHostPkg(), ProxyActivity.P0.class.getName());
        }
    }

    /**
     * Synchronizes the internal task list with the system's recent tasks.
     *
     * <p>Queries the system for recent tasks and reorders the internal task map
     * to match the system's ordering, removing any tasks that no longer exist in the system.</p>
     */
    private void synchronizeTasks() {
        List<ActivityManager.RecentTaskInfo> recentTasks = mAms.getRecentTasks(100, 0);
        Map<Integer, TaskRecord> newTacks = new LinkedHashMap<>();

        for (int i = recentTasks.size() - 1; i >= 0; i--) {
            ActivityManager.RecentTaskInfo next = recentTasks.get(i);
            TaskRecord taskRecord = mTasks.get(next.id);
            if (taskRecord == null) {
                continue;
            }
            newTacks.put(next.id, taskRecord);
        }

        mTasks.clear();
        mTasks.putAll(newTacks);
    }
}
