package com.vcore.core.system.am;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.vcore.BlackBoxCore;
import com.vcore.core.IEmpty;
import com.vcore.core.system.BProcessManagerService;
import com.vcore.core.system.ProcessRecord;
import com.vcore.core.system.pm.BPackageManagerService;
import com.vcore.entity.UnbindRecord;
import com.vcore.entity.am.RunningServiceInfo;
import com.vcore.proxy.ProxyManifest;
import com.vcore.proxy.record.ProxyServiceRecord;

/**
 * Manages the lifecycle of services running within the virtual environment.
 *
 * <p>This class handles starting, stopping, binding, and unbinding services by resolving
 * service intents through the virtual package manager and delegating actual execution to
 * proxy services running in the host process. It tracks running services and their
 * connection counts to manage lifecycle events properly.</p>
 */
@SuppressLint("NewApi")
public class ActiveServices {
    public static final String TAG = "ActiveServices";

    /** Maps intent comparisons to their corresponding running service records. */
    private final Map<Intent.FilterComparison, RunningServiceRecord> mRunningServiceRecords = new HashMap<>();

    /** Maps service tokens (IBinder) to their corresponding running service records. */
    private final Map<IBinder, RunningServiceRecord> mRunningTokens = new HashMap<>();

    /** Maps client binders to their connected service records for tracking bind connections. */
    private final Map<IBinder, ConnectedServiceRecord> mConnectedServices = new HashMap<>();

    /**
     * Starts a service in the virtual environment.
     *
     * <p>Resolves the service intent, starts the target process if needed, creates
     * a stub service intent pointing to a proxy service, and launches it on the host.</p>
     *
     * @param intent        the intent describing the service to start
     * @param resolvedType  the resolved MIME type of the intent
     * @param userId        the virtual user ID
     */
    public void startService(Intent intent, String resolvedType, int userId) {
        ResolveInfo resolveInfo = resolveService(intent, resolvedType, userId);
        if (resolveInfo == null) {
            return;
        }

        ServiceInfo serviceInfo = resolveInfo.serviceInfo;
        ProcessRecord processRecord = BProcessManagerService.get().startProcessLocked(serviceInfo.packageName, serviceInfo.processName, userId, -1, Binder.getCallingPid());
        if (processRecord == null) {
            throw new RuntimeException("Unable to create " + serviceInfo.name);
        }

        RunningServiceRecord runningServiceRecord = getOrCreateRunningServiceRecord(intent);
        runningServiceRecord.mServiceInfo = serviceInfo;
        runningServiceRecord.getAndIncrementStartId();

        final Intent stubServiceIntent = createStubServiceIntent(intent, serviceInfo, processRecord, runningServiceRecord);
        new Thread(() -> {
            try {
                BlackBoxCore.getContext().startService(stubServiceIntent);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Stops a running service in the virtual environment.
     *
     * <p>Resolves the service, checks for active bindings, and delegates the stop
     * command to the target process via its activity thread.</p>
     *
     * @param intent        the intent describing the service to stop
     * @param resolvedType  the resolved MIME type of the intent
     * @param userId        the virtual user ID
     * @return always returns 0
     */
    public int stopService(Intent intent, String resolvedType, int userId) {
        synchronized (mRunningServiceRecords) {
            RunningServiceRecord runningServiceRecord = findRunningServiceRecord(intent);
            if (runningServiceRecord == null) {
                return 0;
            }

            if (runningServiceRecord.mBindCount.get() > 0) {
                Log.d(TAG, "There are also connections");
                return 0;
            }

            runningServiceRecord.mStartId.set(0);
            ResolveInfo resolveInfo = resolveService(intent, resolvedType, userId);
            if (resolveInfo == null) {
                return 0;
            }

            ServiceInfo serviceInfo = resolveInfo.serviceInfo;
            ProcessRecord processRecord = BProcessManagerService.get().startProcessLocked(serviceInfo.packageName, serviceInfo.processName, userId, -1, Binder.getCallingPid());
            if (processRecord == null) {
                return 0;
            }

            try {
                processRecord.bActivityThread.stopService(intent);
            } catch (RemoteException ignored) { }
        }
        return 0;
    }

    /**
     * Binds a client to a service in the virtual environment.
     *
     * <p>Resolves the service, starts the target process, tracks the bind connection
     * with death recipient for cleanup, and returns a stub service intent for binding.</p>
     *
     * @param intent        the intent describing the service to bind
     * @param binder        the client's IBinder for tracking the connection
     * @param resolvedType  the resolved MIME type of the intent
     * @param userId        the virtual user ID
     * @return a stub intent pointing to the proxy service for actual binding
     */
    public Intent bindService(Intent intent, final IBinder binder, String resolvedType, int userId) {
        ResolveInfo resolveInfo = resolveService(intent, resolvedType, userId);
        if (resolveInfo == null) {
            return intent;
        }

        ServiceInfo serviceInfo = resolveInfo.serviceInfo;
        ProcessRecord processRecord = BProcessManagerService.get().startProcessLocked(serviceInfo.packageName, serviceInfo.processName,
                userId, -1, Binder.getCallingPid());
        if (processRecord == null) {
            throw new RuntimeException("Unable to create " + serviceInfo.name);
        }

        RunningServiceRecord runningServiceRecord;
        synchronized (mRunningServiceRecords) {
            runningServiceRecord = getOrCreateRunningServiceRecord(intent);
            runningServiceRecord.mServiceInfo = serviceInfo;

            if (binder != null) {
                ConnectedServiceRecord connectedService = mConnectedServices.get(binder);
                boolean isBound = false;
                if (connectedService != null) {
                    isBound = true;
                } else {
                    connectedService = new ConnectedServiceRecord();
                    try {
                        binder.linkToDeath(new IBinder.DeathRecipient() {
                            @Override
                            public void binderDied() {
                                binder.unlinkToDeath(this, 0);
                                mConnectedServices.remove(binder);
                            }
                        }, 0);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    connectedService.mIntent = intent;
                    mConnectedServices.put(binder, connectedService);
                }

                if (!isBound) {
                    runningServiceRecord.incrementBindCountAndGet();
                }
            }
        }
        return createStubServiceIntent(intent, serviceInfo, processRecord, runningServiceRecord);
    }

    /**
     * Unbinds a client from a previously bound service.
     *
     * @param binder the client's IBinder that was used during bindService
     */
    public void unbindService(IBinder binder) {
        ConnectedServiceRecord connectedService = mConnectedServices.get(binder);
        if (connectedService == null) {
            return;
        }

        RunningServiceRecord runningServiceRecord = getOrCreateRunningServiceRecord(connectedService.mIntent);
        runningServiceRecord.mBindCount.decrementAndGet();
        mConnectedServices.remove(binder);
    }

    /**
     * Stops a service identified by its token.
     *
     * @param token   the IBinder token of the running service
     * @param userId  the virtual user ID
     */
    public void stopServiceToken(IBinder token, int userId) {
        RunningServiceRecord runningServiceByToken = findRunningServiceByToken(token);
        if (runningServiceByToken != null) {
            stopService(runningServiceByToken.mIntent, null, userId);
        }
    }

    /**
     * Called when a proxy service is destroyed. Cleans up the running service record
     * associated with the given proxy intent.
     *
     * @param proxyIntent the intent of the proxy service that was destroyed
     */
    public void onServiceDestroy(Intent proxyIntent) {
        if (proxyIntent == null) {
            return;
        }

        ProxyServiceRecord proxyServiceRecord = ProxyServiceRecord.create(proxyIntent);
        if (proxyServiceRecord.mServiceIntent != null) {
            proxyIntent = proxyServiceRecord.mServiceIntent;
        }

        RunningServiceRecord remove = mRunningServiceRecords.remove(new Intent.FilterComparison(proxyIntent));
        if (remove != null) {
            mRunningTokens.remove(remove);
        }
    }

    /**
     * Called when a proxy service is unbound. Returns an unbind record containing
     * the current bind count and start ID for the service.
     *
     * @param proxyIntent the intent of the proxy service that was unbound
     * @return an {@link UnbindRecord} with service state, or null if the service is not found
     */
    public UnbindRecord onServiceUnbind(Intent proxyIntent) {
        if (proxyIntent == null) {
            return null;
        }

        ProxyServiceRecord proxyServiceRecord = ProxyServiceRecord.create(proxyIntent);
        ComponentName component = proxyServiceRecord.mServiceIntent.getComponent();
        RunningServiceRecord runningServiceRecord = findRunningServiceRecord(proxyServiceRecord.mServiceIntent);
        if (runningServiceRecord == null) {
            return null;
        }

        UnbindRecord record = new UnbindRecord();
        record.setComponentName(component);
        record.setBindCount(runningServiceRecord.mBindCount.get());
        record.setStartId(runningServiceRecord.mStartId.get());
        return record;
    }

    /**
     * Creates a stub service intent that points to a proxy service in the host process.
     *
     * @param targetIntent          the original service intent
     * @param serviceInfo           the resolved service info
     * @param processRecord         the process record for the target app
     * @param runningServiceRecord  the running service record tracking this service
     * @return a stub intent configured to launch the proxy service
     */
    private Intent createStubServiceIntent(Intent targetIntent, ServiceInfo serviceInfo, ProcessRecord processRecord, RunningServiceRecord runningServiceRecord) {
        Intent stub = new Intent();
        ComponentName stubComp = new ComponentName(BlackBoxCore.getHostPkg(), ProxyManifest.getProxyService(processRecord.bPID));
        stub.setComponent(stubComp);
        stub.setAction(UUID.randomUUID().toString());
        ProxyServiceRecord.saveStub(stub, targetIntent, serviceInfo, runningServiceRecord, processRecord.userId, runningServiceRecord.mStartId.get());
        return stub;
    }

    /**
     * Retrieves or creates a running service record for the given intent.
     *
     * @param intent the service intent to look up
     * @return the existing or newly created {@link RunningServiceRecord}
     */
    private RunningServiceRecord getOrCreateRunningServiceRecord(Intent intent) {
        RunningServiceRecord runningServiceRecord = findRunningServiceRecord(intent);
        if (runningServiceRecord == null) {
            runningServiceRecord = new RunningServiceRecord();
            runningServiceRecord.mIntent = intent;
            mRunningServiceRecords.put(new Intent.FilterComparison(intent), runningServiceRecord);
            mRunningTokens.put(runningServiceRecord, runningServiceRecord);
        }
        return runningServiceRecord;
    }

    /**
     * Finds a running service record by its intent using filter comparison.
     *
     * @param intent the service intent to search for
     * @return the matching {@link RunningServiceRecord}, or null if not found
     */
    private RunningServiceRecord findRunningServiceRecord(Intent intent) {
        return mRunningServiceRecords.get(new Intent.FilterComparison(intent));
    }

    /**
     * Finds a running service record by its binder token.
     *
     * @param token the IBinder token to search for
     * @return the matching {@link RunningServiceRecord}, or null if not found
     */
    private RunningServiceRecord findRunningServiceByToken(IBinder token) {
        return mRunningTokens.get(token);
    }

    /**
     * Retrieves information about running services for a given caller package.
     *
     * <p>Queries the system's ActivityManager for all running services and matches
     * them against tracked virtual services to build a composite running service info.</p>
     *
     * @param callerPackage  the package name of the calling application
     * @param userId         the virtual user ID
     * @return a {@link RunningServiceInfo} containing the list of matching running services
     */
    public RunningServiceInfo getRunningServiceInfo(String callerPackage, int userId) {
        ActivityManager manager = (ActivityManager) BlackBoxCore.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = manager.getRunningServices(Integer.MAX_VALUE);
        Map<Integer, ActivityManager.RunningServiceInfo> serviceInfoMap = new HashMap<>();
        for (ActivityManager.RunningServiceInfo runningService : runningServices) {
            serviceInfoMap.put(runningService.pid, runningService);
        }

        RunningServiceInfo info = new RunningServiceInfo();
        for (RunningServiceRecord value : mRunningServiceRecords.values()) {
            ServiceInfo serviceInfo = value.mServiceInfo;
            ProcessRecord processRecord = BProcessManagerService.get().findProcessRecord(callerPackage, serviceInfo.processName, userId);
            if (processRecord == null) {
                continue;
            }

            ActivityManager.RunningServiceInfo runningServiceInfo = serviceInfoMap.get(processRecord.pid);
            if (runningServiceInfo != null) {
                runningServiceInfo.process = processRecord.processName;
                runningServiceInfo.service = new ComponentName(serviceInfo.packageName, serviceInfo.name);
                info.mRunningServiceInfoList.add(runningServiceInfo);
            }
        }
        return info;
    }

    /**
     * Peeks at the IBinder of an existing service without starting it.
     *
     * @param intent        the intent describing the service
     * @param resolvedType  the resolved MIME type of the intent
     * @param userId        the virtual user ID
     * @return the service's IBinder, or null if the service is not running or an error occurs
     */
    public IBinder peekService(Intent intent, String resolvedType, int userId) {
        ResolveInfo resolveInfo = resolveService(intent, resolvedType, userId);
        if (resolveInfo == null) {
            return null;
        }

        ProcessRecord processRecord = BProcessManagerService.get().findProcessRecord(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.processName,
                userId);
        if (processRecord == null) {
            return null;
        }

        try {
            return processRecord.bActivityThread.peekService(intent);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Resolves a service intent through the virtual package manager.
     *
     * @param intent        the intent to resolve
     * @param resolvedType  the resolved MIME type
     * @param userId        the virtual user ID
     * @return the resolved {@link ResolveInfo}, or null if no matching service is found
     */
    private ResolveInfo resolveService(Intent intent, String resolvedType, int userId) {
        return BPackageManagerService.get().resolveService(intent, 0, resolvedType, userId);
    }

    /**
     * Represents a running service instance within the virtual environment.
     *
     * <p>Tracks the service info, intent, start ID (incremented each time the service
     * is started), and the number of active bind connections.</p>
     */
    public static class RunningServiceRecord extends IEmpty.Stub {
        private final AtomicInteger mStartId = new AtomicInteger(1);
        private final AtomicInteger mBindCount = new AtomicInteger(0);

        private ServiceInfo mServiceInfo;
        private Intent mIntent;

        /**
         * Increments the start ID and returns the previous value, mimicking
         * the Android system's startId behavior for onStartCommand.
         */
        public void getAndIncrementStartId() {
            mStartId.getAndIncrement();
        }

        /**
         * Increments the bind count to track a new client connection.
         */
        public void incrementBindCountAndGet() {
            mBindCount.incrementAndGet();
        }
    }

    /**
     * Represents a connection from a client to a service.
     * Tracks the intent of the bound service.
     */
    public static class ConnectedServiceRecord {
        private Intent mIntent;
    }
}
