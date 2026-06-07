package com.vcore.fake.frameworks;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ProviderInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.vcore.app.BActivityThread;
import com.vcore.core.system.ServiceManager;
import com.vcore.core.system.am.IBActivityManagerService;
import com.vcore.entity.AppConfig;
import com.vcore.entity.UnbindRecord;
import com.vcore.entity.am.PendingResultData;
import com.vcore.entity.am.RunningAppProcessInfo;
import com.vcore.entity.am.RunningServiceInfo;

/**
 * Virtual environment manager for activity, service, and broadcast operations.
 *
 * <p>Wraps {@link IBActivityManagerService} to provide Activity Manager functionality
 * scoped to the virtual environment. Handles activity lifecycle management, service
 * binding/unbinding, broadcast dispatching, and content provider acquisition within
 * the virtual space.</p>
 *
 * @see BlackManager
 * @see IBActivityManagerService
 */
public class BActivityManager extends BlackManager<IBActivityManagerService> {
    private static final BActivityManager sActivityManager = new BActivityManager();

    /**
     * Returns the singleton instance of {@link BActivityManager}.
     *
     * @return the global BActivityManager instance
     */
    public static BActivityManager get() {
        return sActivityManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getServiceName() {
        return ServiceManager.ACTIVITY_MANAGER;
    }

    /**
     * Initializes a virtual process for the given package.
     *
     * @param packageName the package name
     * @param processName the process name
     * @param userId      the virtual user ID
     * @return the app configuration, or {@code null} on error
     */
    public AppConfig initProcess(String packageName, String processName, int userId) {
        try {
            return getService().initProcess(packageName, processName, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Restarts a virtual process for the given package.
     *
     * @param packageName the package name
     * @param processName the process name
     * @param userId      the virtual user ID
     */
    public void restartProcess(String packageName, String processName, int userId) {
        try {
            getService().restartProcess(packageName, processName, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts an activity within the virtual environment.
     *
     * @param intent the activity intent
     * @param userId the virtual user ID
     */
    public void startActivity(Intent intent, int userId) {
        try {
            getService().startActivity(intent, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts an activity with full AMS parameters within the virtual environment.
     *
     * @param userId        the virtual user ID
     * @param intent        the activity intent
     * @param resolvedType  the resolved MIME type
     * @param resultTo      the token of the activity to receive the result
     * @param resultWho     the identifier of the requesting activity
     * @param requestCode   the request code for the result
     * @param flags         additional flags
     * @param options       activity options bundle
     */
    public void startActivityAms(int userId, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags, Bundle options) {
        try {
            getService().startActivityAms(userId, intent, resolvedType, resultTo, resultWho, requestCode, flags, options);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts multiple activities atomically within the virtual environment.
     *
     * @param userId       the virtual user ID
     * @param intent       the activity intents
     * @param resolvedType the resolved MIME types
     * @param resultTo     the token of the activity to receive the result
     * @param options      activity options bundle
     * @return the result code, or -1 on error
     */
    public int startActivities(int userId, Intent[] intent, String[] resolvedType, IBinder resultTo, Bundle options) {
        try {
            return getService().startActivities(userId, intent, resolvedType, resultTo, options);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Starts a service within the virtual environment.
     *
     * @param intent            the service intent
     * @param resolvedType      the resolved MIME type
     * @param requireForeground whether foreground execution is required
     * @param userId            the virtual user ID
     * @return the service component name, or {@code null} on error
     */
    public ComponentName startService(Intent intent, String resolvedType, boolean requireForeground, int userId) {
        try {
            return getService().startService(intent, resolvedType, requireForeground, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Stops a service within the virtual environment.
     *
     * @param intent       the service intent
     * @param resolvedType the resolved MIME type
     * @param userId       the virtual user ID
     * @return the result code, or -1 on error
     */
    public int stopService(Intent intent, String resolvedType, int userId) {
        try {
            return getService().stopService(intent, resolvedType, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Binds a service within the virtual environment.
     *
     * @param service       the service intent
     * @param binder        the connection binder
     * @param resolvedType  the resolved MIME type
     * @param userId        the virtual user ID
     * @return the bind intent, or {@code null} on error
     */
    public Intent bindService(Intent service, IBinder binder, String resolvedType, int userId) {
        try {
            return getService().bindService(service, binder, resolvedType, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Unbinds a service within the virtual environment.
     *
     * @param binder the connection binder
     * @param userId the virtual user ID
     */
    public void unbindService(IBinder binder, int userId) {
        try {
            getService().unbindService(binder, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stops a service by its token within the virtual environment.
     *
     * @param componentName the service component name
     * @param token         the service token
     * @param userId        the virtual user ID
     */
    public void stopServiceToken(ComponentName componentName, IBinder token, int userId) {
        try {
            getService().stopServiceToken(componentName, token, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when a service is unbound. Returns unbind record information.
     *
     * @param proxyIntent the proxy intent for the service
     * @param userId      the virtual user ID
     * @return the unbind record, or {@code null} on error
     */
    public UnbindRecord onServiceUnbind(Intent proxyIntent, int userId) {
        try {
            return getService().onServiceUnbind(proxyIntent, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Called when a service is destroyed.
     *
     * @param proxyIntent the proxy intent for the service
     * @param userId      the virtual user ID
     */
    public void onServiceDestroy(Intent proxyIntent, int userId) {
        try {
            getService().onServiceDestroy(proxyIntent, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Acquires a content provider client binder for the given provider info.
     *
     * @param providerInfo the provider information
     * @return the provider binder, or {@code null} on error
     */
    public IBinder acquireContentProviderClient(ProviderInfo providerInfo) {
        try {
            return getService().acquireContentProviderClient(providerInfo);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Sends a broadcast within the virtual environment.
     *
     * @param intent       the broadcast intent
     * @param resolvedType the resolved MIME type
     * @param userId       the virtual user ID
     * @return the proxy broadcast intent, or {@code null} on error
     */
    public Intent sendBroadcast(Intent intent, String resolvedType, int userId) {
        try {
            return getService().sendBroadcast(intent, resolvedType, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Peeks at a running service's binder.
     *
     * @param intent       the service intent
     * @param resolvedType the resolved MIME type
     * @param userId       the virtual user ID
     * @return the service binder, or {@code null} on error
     */
    public IBinder peekService(Intent intent, String resolvedType, int userId) {
        try {
            return getService().peekService(intent, resolvedType, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Notifies the virtual AM that an activity has been created.
     *
     * @param taskId       the task ID
     * @param token        the activity token
     * @param activityToken the parent activity token
     */
    public void onActivityCreated(int taskId, IBinder token, String activityToken) {
        try {
            getService().onActivityCreated(taskId, token, activityToken);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Notifies the virtual AM that an activity has been resumed.
     * Contains a workaround for WeChat's focus issue.
     *
     * @param token the activity token
     */
    public void onActivityResumed(IBinder token) {
        try {
            // Fix https://github.com/FBlackBox/BlackBox/issues/28
            if ("com.tencent.mm".equals(BActivityThread.getAppPackageName())) {
                Activity activityByToken = BActivityThread.getActivityByToken(token);
                if (activityByToken != null) {
                    activityByToken.getWindow().getDecorView().clearFocus();
                }
            }
        } catch (Throwable ignored) { }

        try {
            getService().onActivityResumed(token);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Notifies the virtual AM that an activity has been destroyed.
     *
     * @param token the activity token
     */
    public void onActivityDestroyed(IBinder token) {
        try {
            getService().onActivityDestroyed(token);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Notifies the virtual AM that an activity has finished.
     *
     * @param token the activity token
     */
    public void onFinishActivity(IBinder token) {
        try {
            getService().onFinishActivity(token);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns running app process information in the virtual environment.
     *
     * @param callerPackage the caller's package name
     * @param userId        the virtual user ID
     * @return the running app process info, or {@code null} on error
     */
    public RunningAppProcessInfo getRunningAppProcesses(String callerPackage, int userId) {
        try {
            return getService().getRunningAppProcesses(callerPackage, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns running service information in the virtual environment.
     *
     * @param callerPackage the caller's package name
     * @param userId        the virtual user ID
     * @return the running service info, or {@code null} on error
     */
    public RunningServiceInfo getRunningServices(String callerPackage, int userId) {
        try {
            return getService().getRunningServices(callerPackage, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Schedules a broadcast receiver to run within the virtual environment.
     *
     * @param intent          the broadcast intent
     * @param pendingResultData the pending result data
     * @param userId          the virtual user ID
     * @throws RemoteException if the remote call fails
     */
    public void scheduleBroadcastReceiver(Intent intent, PendingResultData pendingResultData, int userId) throws RemoteException {
        getService().scheduleBroadcastReceiver(intent, pendingResultData, userId);
    }

    /**
     * Finishes a broadcast receiver's pending result.
     *
     * @param data the pending result data
     */
    public void finishBroadcast(PendingResultData data) {
        try {
            getService().finishBroadcast(data);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the calling package name for the given activity token.
     *
     * @param token  the activity token
     * @param userId the virtual user ID
     * @return the calling package name, or {@code null} on error
     */
    public String getCallingPackage(IBinder token, int userId) {
        try {
            return getService().getCallingPackage(token, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the calling activity component for the given activity token.
     *
     * @param token  the activity token
     * @param userId the virtual user ID
     * @return the calling activity component name, or {@code null} on error
     */
    public ComponentName getCallingActivity(IBinder token, int userId) {
        try {
            return getService().getCallingActivity(token, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Registers an intent sender with the virtual AM.
     *
     * @param target      the intent sender binder
     * @param packageName the package name
     * @param uid         the UID
     */
    public void getIntentSender(IBinder target, String packageName, int uid) {
        try {
            getService().getIntentSender(target, packageName, uid, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the package name associated with an intent sender.
     *
     * @param target the intent sender binder
     * @return the package name, or {@code null} on error
     */
    public String getPackageForIntentSender(IBinder target) {
        try {
            return getService().getPackageForIntentSender(target, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the UID associated with an intent sender.
     *
     * @param target the intent sender binder
     * @return the UID, or -1 on error
     */
    public int getUidForIntentSender(IBinder target) {
        try {
            return getService().getUidForIntentSender(target, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
