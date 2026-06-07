package com.vcore.app.dispatcher;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.IBinder;

import java.util.HashMap;
import java.util.Map;

import com.vcore.BlackBoxCore;
import com.vcore.app.BActivityThread;
import com.vcore.entity.ServiceRecord;
import com.vcore.entity.UnbindRecord;
import com.vcore.proxy.record.ProxyServiceRecord;

/**
 * Dispatcher that manages the lifecycle of virtual application {@link Service} instances
 * within the BlackBox environment.
 * <p>
 * This class acts as a proxy between proxy service stubs (declared in the host app manifest)
 * and the actual virtual services running inside the virtual app process. It handles binding,
 * unbinding, starting, stopping, and destroying virtual services. Active service records are
 * cached by their original {@link Intent} using {@link Intent.FilterComparison}.
 */
public class AppServiceDispatcher {
    /** Singleton instance. */
    private static final AppServiceDispatcher sServiceDispatcher = new AppServiceDispatcher();
    /** Map of active service records keyed by their original intent. */
    private final Map<Intent.FilterComparison, ServiceRecord> mService = new HashMap<>();
    /** Handler bound to the main looper for posting work. */
    private final Handler mHandler = BlackBoxCore.get().getHandler();

    /**
     * Returns the singleton instance of {@link AppServiceDispatcher}.
     *
     * @return the dispatcher instance
     */
    public static AppServiceDispatcher get() {
        return sServiceDispatcher;
    }

    /**
     * Handles a bind request from the proxy service stub.
     * <p>
     * Creates or retrieves the virtual service for the given proxy intent, then delegates
     * the {@code onBind} call to it. Manages bind counts and handles rebind scenarios
     * per the Android service lifecycle contract.
     *
     * @param proxyIntent the intent from the proxy service stub containing the real service info
     * @return the {@link IBinder} returned by the virtual service's {@code onBind()}, or {@code null}
     *         if the service could not be created or binding failed
     */
    public IBinder onBind(Intent proxyIntent) {
        ProxyServiceRecord serviceRecord = ProxyServiceRecord.create(proxyIntent);
        Intent intent = serviceRecord.mServiceIntent;
        ServiceInfo serviceInfo = serviceRecord.mServiceInfo;

        if (intent == null || serviceInfo == null) {
            return null;
        }

        Service service = getOrCreateService(serviceRecord);
        if (service == null) {
            return null;
        }
        intent.setExtrasClassLoader(service.getClassLoader());

        ServiceRecord record = findRecord(intent);
        record.incrementAndGetBindCount(intent);

        if (record.hasBinder(intent)) {
            if (record.isRebind()) {
                service.onRebind(intent);
                record.setRebind(false);
            }
            return record.getBinder(intent);
        }

        try {
            IBinder iBinder = service.onBind(intent);
            record.addBinder(intent, iBinder);
            return iBinder;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Handles a start command from the proxy service stub.
     * <p>
     * Creates or retrieves the virtual service and updates its start ID to track the
     * most recent start request.
     *
     * @param proxyIntent the intent from the proxy service stub containing the real service info
     */
    public void onStartCommand(Intent proxyIntent) {
        ProxyServiceRecord stubRecord = ProxyServiceRecord.create(proxyIntent);
        if (stubRecord.mServiceIntent == null || stubRecord.mServiceInfo == null) {
            return;
        }

        Service service = getOrCreateService(stubRecord);
        if (service == null) {
            return;
        }
        stubRecord.mServiceIntent.setExtrasClassLoader(service.getClassLoader());

        ServiceRecord record = findRecord(stubRecord.mServiceIntent);
        record.setStartId(stubRecord.mStartId);
    }

    /**
     * Destroys all active virtual services and clears the service cache.
     * Called when the host process is shutting down.
     */
    public void onDestroy() {
        if (mService.size() > 0) {
            for (ServiceRecord record : mService.values()) {
                try {
                    record.getService().onDestroy();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        mService.clear();
    }

    /**
     * Forwards a configuration change event to all active virtual services.
     *
     * @param newConfig the new device configuration
     */
    public void onConfigurationChanged(Configuration newConfig) {
        if (mService.size() > 0) {
            for (ServiceRecord record : mService.values()) {
                try {
                    record.getService().onConfigurationChanged(newConfig);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Forwards a low-memory event to all active virtual services.
     */
    public void onLowMemory() {
        if (mService.size() > 0) {
            for (ServiceRecord record : mService.values()) {
                try {
                    record.getService().onLowMemory();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Forwards a trim-memory event to all active virtual services.
     *
     * @param level the memory trim level from the system
     */
    public void onTrimMemory(int level) {
        if (mService.size() > 0) {
            for (ServiceRecord record : mService.values()) {
                try {
                    record.getService().onTrimMemory(level);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Handles an unbind request from the proxy service stub.
     * <p>
     * Decrements the connection count for the virtual service. If the connection count reaches
     * zero and there is no pending start, the service is destroyed and removed from the cache.
     * Otherwise, the service is marked for rebind on the next bind call.
     *
     * @param proxyIntent the intent from the proxy service stub containing the real service info
     */
    public void onUnbind(Intent proxyIntent) {
        ProxyServiceRecord stubRecord = ProxyServiceRecord.create(proxyIntent);
        if (stubRecord.mServiceIntent == null || stubRecord.mServiceInfo == null) {
            return;
        }

        Intent intent = stubRecord.mServiceIntent;
        try {
            UnbindRecord unbindRecord = BlackBoxCore.getBActivityManager().onServiceUnbind(proxyIntent, BActivityThread.getUserId());
            if (unbindRecord == null) {
                return;
            }

            Service service = getOrCreateService(stubRecord);
            if (service == null) {
                return;
            }
            stubRecord.mServiceIntent.setExtrasClassLoader(service.getClassLoader());

            ServiceRecord record = findRecord(intent);
            boolean destroy = unbindRecord.getStartId() == 0;

            if (destroy || record.decreaseConnectionCount(intent)) {
                if (destroy) {
                    service.onDestroy();

                    BlackBoxCore.getBActivityManager().onServiceDestroy(proxyIntent, BActivityThread.getUserId());
                    mService.remove(new Intent.FilterComparison(intent));
                }
                record.setRebind(true);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the existing binder for a bound virtual service without incrementing the bind count.
     *
     * @param intent the original intent identifying the virtual service
     * @return the service's {@link IBinder}, or {@code null} if not bound
     */
    public IBinder peekService(Intent intent) {
        ServiceRecord record = findRecord(intent);
        if (record == null) {
            return null;
        }
        return record.getBinder(intent);
    }

    /**
     * Stops the virtual service identified by the given intent.
     * <p>
     * If the service has been started (start ID > 0), the service is destroyed on the main thread,
     * its record is removed from the cache, and the virtual activity manager is notified.
     *
     * @param intent the intent identifying the virtual service to stop
     */
    public void stopService(Intent intent) {
        if (intent == null) {
            return;
        }

        ServiceRecord record = findRecord(intent);
        if (record == null) {
            return;
        }

        if (record.getService() != null) {
            boolean destroy = record.getStartId() > 0;
            try {
                if (destroy) {
                    mHandler.post(() -> record.getService().onDestroy());
                    BlackBoxCore.getBActivityManager().onServiceDestroy(intent, BActivityThread.getUserId());
                    mService.remove(new Intent.FilterComparison(intent));
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Looks up the service record for the given intent.
     *
     * @param intent the intent identifying the virtual service
     * @return the {@link ServiceRecord}, or {@code null} if not found
     */
    private ServiceRecord findRecord(Intent intent) {
        return mService.get(new Intent.FilterComparison(intent));
    }

    /**
     * Returns an existing virtual service for the given proxy record, or creates a new one.
     * <p>
     * If the service already exists in the cache, it is returned immediately. Otherwise,
     * a new service instance is created via {@link BActivityThread#createService} and cached.
     *
     * @param proxyServiceRecord the proxy record containing the service intent, info, and token
     * @return the virtual {@link Service}, or {@code null} if creation failed
     */
    private Service getOrCreateService(ProxyServiceRecord proxyServiceRecord) {
        Intent intent = proxyServiceRecord.mServiceIntent;
        ServiceInfo serviceInfo = proxyServiceRecord.mServiceInfo;
        IBinder token = proxyServiceRecord.mToken;

        ServiceRecord record = findRecord(intent);
        if (record != null && record.getService() != null) {
            return record.getService();
        }

        Service service = BActivityThread.currentActivityThread().createService(serviceInfo, token);
        if (service == null) {
            return null;
        }

        record = new ServiceRecord();
        record.setService(service);
        mService.put(new Intent.FilterComparison(intent), record);
        return service;
    }
}
