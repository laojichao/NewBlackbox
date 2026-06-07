package com.vcore.core.system;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vcore.BlackBoxCore;

import com.vcore.core.IBActivityThread;
import com.vcore.core.env.BEnvironment;
import com.vcore.core.system.notification.BNotificationManagerService;
import com.vcore.core.system.pm.BPackageManagerService;
import com.vcore.core.system.user.BUserHandle;
import com.vcore.entity.AppConfig;
import com.vcore.proxy.ProxyManifest;
import com.vcore.utils.FileUtils;
import com.vcore.utils.Slog;
import com.vcore.utils.compat.ApplicationThreadCompat;
import com.vcore.utils.compat.BundleCompat;
import com.vcore.utils.provider.ProviderCall;

/**
 * Manages the lifecycle of virtual application processes within the BlackBox environment.
 * <p>
 * Responsible for starting, tracking, restarting, and killing virtual app processes.
 * Each virtual process is identified by a unique bPID (BlackBox Process ID) and
 * associated with a bUID (BlackBox User ID). This service maintains a mapping from
 * UIDs to process records and handles process initialization via provider calls.
 */
public class BProcessManagerService implements ISystemService {
    public static final String TAG = "BProcessManager";

    /** Singleton instance of the process manager service. */
    public static final BProcessManagerService sBProcessManagerService = new BProcessManagerService();

    /** Mapping from bUID to a map of process name to ProcessRecord. */
    private final Map<Integer, Map<String, ProcessRecord>> mProcessMap = new HashMap<>();

    /** List of all active ProcessRecord instances, guarded by its own lock for pid lookups. */
    private final List<ProcessRecord> mPidsSelfLocked = new ArrayList<>();

    /** Lock object guarding process start/kill operations. */
    private final Object mProcessLock = new Object();

    /**
     * Returns the singleton instance of BProcessManagerService.
     *
     * @return the singleton BProcessManagerService instance
     */
    public static BProcessManagerService get() {
        return sBProcessManagerService;
    }

    /**
     * Starts a new virtual application process or returns an existing one.
     * <p>
     * If a process with the given name already exists and is initialized, it is returned
     * directly. Otherwise, a new bPID is allocated, the process is initialized via a
     * provider call, and the resulting ProcessRecord is registered.
     *
     * @param packageName the package name of the application to start
     * @param processName the process name within the application
     * @param userId      the virtual user ID under which the process runs
     * @param bPID        the BlackBox process ID to use, or -1 to auto-allocate
     * @param callingPid  the PID of the caller initiating the start
     * @return the {@link ProcessRecord} for the started process, or null if the app
     *         could not be found or initialized
     * @throws RuntimeException if no free process slots are available when bPID is -1
     */
    public ProcessRecord startProcessLocked(String packageName, String processName, int userId, int bPID, int callingPid) {
        ApplicationInfo info = BPackageManagerService.get().getApplicationInfo(packageName, 0, userId);
        if (info == null) {
            return null;
        }

        ProcessRecord app;
        int bUID = BUserHandle.getUid(userId, BPackageManagerService.get().getAppId(packageName));
        synchronized (mProcessLock) {
            Map<String, ProcessRecord> bProcess = mProcessMap.get(bUID);

            if (bProcess == null) {
                bProcess = new HashMap<>();
            }

            if (bPID == -1) {
                app = bProcess.get(processName);
                if (app != null) {
                    if (app.initLock != null) {
                        app.initLock.block();
                    }

                    if (app.bActivityThread != null) {
                        return app;
                    }
                }
                bPID = getUsingBPidL();
                Slog.d(TAG, "init bUid = " + bUID + ", bPid = " + bPID);
            }

            if (bPID == -1) {
                throw new RuntimeException("No processes available");
            }

            app = new ProcessRecord(info, processName);
            app.uid = Process.myUid();
            app.bPID = bPID;
            app.bUID = BPackageManagerService.get().getAppId(packageName);
            app.callingBUid = getBUidByPidOrPackageName(callingPid, packageName);
            app.userId = userId;

            bProcess.put(processName, app);
            mPidsSelfLocked.add(app);

            synchronized (mProcessMap) {
                mProcessMap.put(bUID, bProcess);
            }

            if (!initAppProcessL(app)) {
                bProcess.remove(processName);
                mPidsSelfLocked.remove(app);
                app = null;
            } else {
                app.pid = getPid(BlackBoxCore.getContext(), ProxyManifest.getProcessName(app.bPID));

                Slog.d(TAG, "init pid = " + app.pid);
            }
        }
        return app;
    }

    /**
     * Kills the given virtual process by its real PID or by iterating running processes
     * to find a matching bPID.
     *
     * @param app the ProcessRecord of the process to kill
     */
    private void killProcess(final ProcessRecord app) {
        if (app.pid > 0) {
            Process.killProcess(app.pid);
        } else {
            try {
                ActivityManager manager = (ActivityManager) BlackBoxCore.getContext().getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = manager.getRunningAppProcesses();
                for (ActivityManager.RunningAppProcessInfo runningAppProcess : runningAppProcesses) {
                    int bPID = parseBPid(runningAppProcess.processName);
                    if (bPID != -1 && app.bPID == bPID) {
                        Slog.d(TAG, "force kill process: " + app.processName + ", pid: " + runningAppProcess.pid + ", bPID: " + bPID);
                        Process.killProcess(runningAppProcess.pid);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Finds the first available (unused) bPID by scanning currently running processes.
     *
     * @return an available bPID in the range [0, FREE_COUNT), or -1 if all slots are occupied
     */
    private int getUsingBPidL() {
        ActivityManager manager = (ActivityManager) BlackBoxCore.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = manager.getRunningAppProcesses();
        Set<Integer> usingPs = new HashSet<>();

        for (ActivityManager.RunningAppProcessInfo runningAppProcess : runningAppProcesses) {
            int i = parseBPid(runningAppProcess.processName);
            usingPs.add(i);
        }

        for (int i = 0; i < ProxyManifest.FREE_COUNT; i++) {
            if (usingPs.contains(i)) {
                continue;
            }
            return i;
        }
        return -1;
    }

    /**
     * Restarts a virtual application process. Kills the current process associated with
     * the calling PID and reuses its bPID to start a new process instance.
     *
     * @param packageName the package name of the application
     * @param processName the process name within the application
     * @param userId      the virtual user ID
     */
    public void restartAppProcess(String packageName, String processName, int userId) {
        synchronized (mProcessLock) {
            int callingPid = Binder.getCallingPid();
            ProcessRecord app = findProcessByPid(callingPid);
            if (app != null) {
                killProcess(app);
            }

            String stubProcessName = getProcessName(BlackBoxCore.getContext(), callingPid);
            int bPID = parseBPid(stubProcessName);
            startProcessLocked(packageName, processName, userId, bPID, callingPid);
        }
    }

    /**
     * Parses the bPID from a stub process name by extracting the numeric suffix
     * after the host package prefix.
     *
     * @param stubProcessName the full stub process name (e.g., "com.host:p0")
     * @return the parsed bPID, or -1 if the name does not match the expected format
     */
    private int parseBPid(String stubProcessName) {
        String prefix;
        if (stubProcessName == null) {
            return -1;
        } else {
            prefix = BlackBoxCore.getHostPkg() + ":p";
        }

        if (stubProcessName.startsWith(prefix)) {
            try {
                return Integer.parseInt(stubProcessName.substring(prefix.length()));
            } catch (NumberFormatException ignored) { }
        }
        return -1;
    }

    /**
     * Initializes a virtual application process by sending an init command via the
     * content provider and attaching the client binder.
     *
     * @param record the ProcessRecord to initialize
     * @return true if initialization succeeded and the client thread was attached
     */
    private boolean initAppProcessL(ProcessRecord record) {
        Slog.d(TAG, "initProcess: " + record.processName);
        AppConfig appConfig = record.getClientConfig();
        Bundle bundle = new Bundle();
        bundle.putParcelable(AppConfig.KEY, appConfig);
        Bundle init = ProviderCall.callSafely(record.getProviderAuthority(), "_Black_|_init_process_", null, bundle);

        IBinder appThread = BundleCompat.getBinder(init, "_Black_|_client_");
        if (appThread == null || !appThread.isBinderAlive()) {
            return false;
        }

        attachClientL(record, appThread);
        createProc(record);
        return true;
    }

    /**
     * Attaches the client-side activity thread to the given ProcessRecord. Sets up
     * a death recipient to handle process termination and opens the init lock.
     *
     * @param app      the ProcessRecord to attach to
     * @param appThread the IBinder of the client's activity thread
     */
    private void attachClientL(final ProcessRecord app, final IBinder appThread) {
        IBActivityThread activityThread = IBActivityThread.Stub.asInterface(appThread);
        if (activityThread == null) {
            killProcess(app);
            return;
        }

        try {
            appThread.linkToDeath(new IBinder.DeathRecipient() {
                @Override
                public void binderDied() {
                    Slog.d(TAG, "App Died: " + app.processName);
                    appThread.unlinkToDeath(this, 0);
                    onProcessDie(app);
                }
            }, 0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        app.bActivityThread = activityThread;
        try {
            app.appThread = ApplicationThreadCompat.asInterface(activityThread.getActivityThread());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        app.initLock.open();
    }

    /**
     * Callback invoked when a virtual process dies. Removes the process from all
     * tracking structures, cleans up the proc directory, and deletes associated notifications.
     *
     * @param record the ProcessRecord of the dead process
     */
    public void onProcessDie(ProcessRecord record) {
        synchronized (mProcessLock) {
            killProcess(record);
            Map<String, ProcessRecord> process = mProcessMap.get(record.bUID);
            if (process != null) {
                process.remove(record.processName);
                if (process.isEmpty()) {
                    mProcessMap.remove(record.bUID);
                }
            }

            mPidsSelfLocked.remove(record);
            removeProc(record);
            BNotificationManagerService.get().deletePackageNotification(record.getPackageName(), record.userId);
        }
    }

    /**
     * Finds a ProcessRecord by package name, process name, and user ID.
     *
     * @param packageName the package name of the application
     * @param processName the process name within the application
     * @param userId      the virtual user ID
     * @return the matching ProcessRecord, or null if not found
     */
    public ProcessRecord findProcessRecord(String packageName, String processName, int userId) {
        synchronized (mProcessMap) {
            int appId = BPackageManagerService.get().getAppId(packageName);
            int bUID = BUserHandle.getUid(userId, appId);

            Map<String, ProcessRecord> processRecordMap = mProcessMap.get(bUID);
            if (processRecordMap == null) {
                return null;
            }
            return processRecordMap.get(processName);
        }
    }

    /**
     * Kills all processes belonging to the given package across all users.
     *
     * @param packageName the package name whose processes should be killed
     */
    public void killAllByPackageName(String packageName) {
        synchronized (mProcessLock) {
            synchronized (mPidsSelfLocked) {
                List<ProcessRecord> tmp = new ArrayList<>(mPidsSelfLocked);
                int appId = BPackageManagerService.get().getAppId(packageName);
                for (ProcessRecord processRecord : mPidsSelfLocked) {
                    int appId1 = BUserHandle.getAppId(processRecord.bUID);
                    if (appId == appId1) {
                        mProcessMap.remove(processRecord.bUID);
                        killProcess(processRecord);
                        tmp.remove(processRecord);
                    }
                }
                mPidsSelfLocked.clear();
                mPidsSelfLocked.addAll(tmp);
            }
        }
    }

    /**
     * Kills all processes of a specific package for a specific user.
     *
     * @param packageName the package name whose processes should be killed
     * @param userId      the virtual user ID
     */
    public void killPackageAsUser(String packageName, int userId) {
        synchronized (mProcessLock) {
            int bUID = BUserHandle.getUid(userId, BPackageManagerService.get().getAppId(packageName));
            Map<String, ProcessRecord> process = mProcessMap.get(bUID);
            if (process == null) {
                return;
            }

            for (ProcessRecord value : process.values()) {
                killProcess(value);
                mPidsSelfLocked.remove(value);
            }
            mProcessMap.remove(bUID);
        }
    }

    /**
     * Returns a list of all active ProcessRecords for a given package and user.
     *
     * @param packageName the package name to query
     * @param userId      the virtual user ID
     * @return a list of ProcessRecord instances, empty if none found
     */
    public List<ProcessRecord> getPackageProcessAsUser(String packageName, int userId) {
        synchronized (mProcessMap) {
            int bUID = BUserHandle.getUid(userId, BPackageManagerService.get().getAppId(packageName));
            Map<String, ProcessRecord> process = mProcessMap.get(bUID);
            if (process == null) {
                return new ArrayList<>();
            }
            return new ArrayList<>(process.values());
        }
    }

    /**
     * Returns the bUID (app ID portion) for the caller identified by PID, falling
     * back to the app ID of the given package name if no process record is found.
     *
     * @param pid         the calling PID to look up
     * @param packageName fallback package name for app ID lookup
     * @return the bUID (app ID) of the calling process or the package
     */
    public int getBUidByPidOrPackageName(int pid, String packageName) {
        ProcessRecord callingProcess = findProcessByPid(pid);
        if (callingProcess == null) {
            return BPackageManagerService.get().getAppId(packageName);
        }
        return BUserHandle.getAppId(callingProcess.bUID);
    }

    /**
     * Returns the virtual user ID for the process identified by the given calling PID.
     *
     * @param callingPid the PID to look up
     * @return the user ID of the process, or 0 if not found
     */
    public int getUserIdByCallingPid(int callingPid) {
        ProcessRecord callingProcess = findProcessByPid(callingPid);
        if (callingProcess == null) {
            return 0;
        }
        return callingProcess.userId;
    }

    /**
     * Finds a ProcessRecord by its real host PID.
     *
     * @param pid the real process PID to search for
     * @return the matching ProcessRecord, or null if not found
     */
    public ProcessRecord findProcessByPid(int pid) {
        synchronized (mPidsSelfLocked) {
            for (ProcessRecord processRecord : mPidsSelfLocked) {
                if (processRecord.pid == pid) {
                    return processRecord;
                }
            }
            return null;
        }
    }

    /**
     * Retrieves the process name for a given PID from the ActivityManager.
     *
     * @param context the application context
     * @param pid     the PID to look up
     * @return the process name
     * @throws RuntimeException if no running process matches the given PID
     */
    private static String getProcessName(Context context, int pid) {
        String processName = null;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
            if (info.pid == pid) {
                processName = info.processName;
                break;
            }
        }
        if (processName == null) {
            throw new RuntimeException("processName = null");
        }
        return processName;
    }

    /**
     * Retrieves the real host PID for a given process name by scanning running processes.
     *
     * @param context     the application context
     * @param processName the process name to search for
     * @return the PID of the matching process, or -1 if not found
     */
    public static int getPid(Context context, String processName) {
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = manager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo runningAppProcess : runningAppProcesses) {
                if (runningAppProcess.processName.equals(processName)) {
                    return runningAppProcess.pid;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Creates the /proc/cmdline file for the given process record, used for process
     * identification within the BlackBox environment.
     *
     * @param record the ProcessRecord whose proc entry should be created
     */
    private static void createProc(ProcessRecord record) {
        File cmdline = new File(BEnvironment.getProcDir(record.bPID), "cmdline");
        try {
            FileUtils.writeToFile(record.processName.getBytes(), cmdline);
        } catch (IOException ignored) { }
    }

    /**
     * Removes the /proc directory entry for the given process record.
     *
     * @param record the ProcessRecord whose proc entry should be removed
     */
    private static void removeProc(ProcessRecord record) {
        FileUtils.deleteDir(BEnvironment.getProcDir(record.bPID));
    }

    /**
     * Called when the system is ready. Cleans up the proc directory.
     */
    @Override
    public void systemReady() {
        FileUtils.deleteDir(BEnvironment.getProcDir());
    }
}
