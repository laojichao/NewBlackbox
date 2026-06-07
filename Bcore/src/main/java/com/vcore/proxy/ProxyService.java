package com.vcore.proxy;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.vcore.app.dispatcher.AppServiceDispatcher;

/**
 * Proxy service that acts as a stub entry point in the host application manifest.
 * <p>
 * Delegates all service lifecycle callbacks ({@link #onBind}, {@link #onStartCommand},
 * {@link #onDestroy}, {@link #onUnbind}, etc.) to the {@link AppServiceDispatcher},
 * which routes them to the appropriate guest application's service implementation.
 * </p>
 */
public class ProxyService extends Service {
    /** Tag for logging. */
    public static final String TAG = "StubService";

    /**
     * Called when a client binds to this service. Delegates to {@link AppServiceDispatcher}
     * to return the appropriate binder for the guest application's service.
     *
     * @param intent the intent that was used to bind to this service
     * @return the {@link IBinder} object for the guest service, or {@code null} if unavailable
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return AppServiceDispatcher.get().onBind(intent);
    }

    /**
     * Called when the service receives a start command. Delegates to
     * {@link AppServiceDispatcher} and returns {@link #START_NOT_STICKY}.
     *
     * @param intent  the intent that was used to start the service
     * @param flags   additional data about the start request
     * @param startId a unique integer representing this specific start request
     * @return {@link #START_NOT_STICKY}
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AppServiceDispatcher.get().onStartCommand(intent);
        return START_NOT_STICKY;
    }

    /**
     * Called when the service is being destroyed. Delegates cleanup to
     * {@link AppServiceDispatcher}.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        AppServiceDispatcher.get().onDestroy();
    }

    /**
     * Called when the device configuration changes while the service is running.
     * Delegates to {@link AppServiceDispatcher}.
     *
     * @param newConfig the new device configuration
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        AppServiceDispatcher.get().onConfigurationChanged(newConfig);
    }

    /**
     * Called when the system is running low on memory. Delegates to
     * {@link AppServiceDispatcher}.
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        AppServiceDispatcher.get().onLowMemory();
    }

    /**
     * Called when the operating system determines that it is a good time for the
     * process to trim unneeded memory. Delegates to {@link AppServiceDispatcher}.
     *
     * @param level the context hint for the degree of trimming being requested
     */
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        AppServiceDispatcher.get().onTrimMemory(level);
    }

    /**
     * Called when all clients have unbound from the service. Delegates to
     * {@link AppServiceDispatcher}.
     *
     * @param intent the intent that was used to bind to this service
     * @return always {@code false} (does not support rebind)
     */
    @Override
    public boolean onUnbind(Intent intent) {
        AppServiceDispatcher.get().onUnbind(intent);
        return false;
    }

    /** Proxy stub subclass P0 registered in the host manifest. */
    public static class P0 extends ProxyService { }

    /** Proxy stub subclass P1 registered in the host manifest. */
    public static class P1 extends ProxyService { }

    /** Proxy stub subclass P2 registered in the host manifest. */
    public static class P2 extends ProxyService { }

    /** Proxy stub subclass P3 registered in the host manifest. */
    public static class P3 extends ProxyService { }

    /** Proxy stub subclass P4 registered in the host manifest. */
    public static class P4 extends ProxyService { }

    /** Proxy stub subclass P5 registered in the host manifest. */
    public static class P5 extends ProxyService { }

    /** Proxy stub subclass P6 registered in the host manifest. */
    public static class P6 extends ProxyService { }

    /** Proxy stub subclass P7 registered in the host manifest. */
    public static class P7 extends ProxyService { }

    /** Proxy stub subclass P8 registered in the host manifest. */
    public static class P8 extends ProxyService { }

    /** Proxy stub subclass P9 registered in the host manifest. */
    public static class P9 extends ProxyService { }

    /** Proxy stub subclass P10 registered in the host manifest. */
    public static class P10 extends ProxyService { }

    /** Proxy stub subclass P11 registered in the host manifest. */
    public static class P11 extends ProxyService { }

    /** Proxy stub subclass P12 registered in the host manifest. */
    public static class P12 extends ProxyService { }

    /** Proxy stub subclass P13 registered in the host manifest. */
    public static class P13 extends ProxyService { }

    /** Proxy stub subclass P14 registered in the host manifest. */
    public static class P14 extends ProxyService { }

    /** Proxy stub subclass P15 registered in the host manifest. */
    public static class P15 extends ProxyService { }

    /** Proxy stub subclass P16 registered in the host manifest. */
    public static class P16 extends ProxyService { }

    /** Proxy stub subclass P17 registered in the host manifest. */
    public static class P17 extends ProxyService { }

    /** Proxy stub subclass P18 registered in the host manifest. */
    public static class P18 extends ProxyService { }

    /** Proxy stub subclass P19 registered in the host manifest. */
    public static class P19 extends ProxyService { }

    /** Proxy stub subclass P20 registered in the host manifest. */
    public static class P20 extends ProxyService { }

    /** Proxy stub subclass P21 registered in the host manifest. */
    public static class P21 extends ProxyService { }

    /** Proxy stub subclass P22 registered in the host manifest. */
    public static class P22 extends ProxyService { }

    /** Proxy stub subclass P23 registered in the host manifest. */
    public static class P23 extends ProxyService { }

    /** Proxy stub subclass P24 registered in the host manifest. */
    public static class P24 extends ProxyService { }

    /** Proxy stub subclass P25 registered in the host manifest. */
    public static class P25 extends ProxyService { }

    /** Proxy stub subclass P26 registered in the host manifest. */
    public static class P26 extends ProxyService { }

    /** Proxy stub subclass P27 registered in the host manifest. */
    public static class P27 extends ProxyService { }

    /** Proxy stub subclass P28 registered in the host manifest. */
    public static class P28 extends ProxyService { }

    /** Proxy stub subclass P29 registered in the host manifest. */
    public static class P29 extends ProxyService { }

    /** Proxy stub subclass P30 registered in the host manifest. */
    public static class P30 extends ProxyService { }

    /** Proxy stub subclass P31 registered in the host manifest. */
    public static class P31 extends ProxyService { }

    /** Proxy stub subclass P32 registered in the host manifest. */
    public static class P32 extends ProxyService { }

    /** Proxy stub subclass P33 registered in the host manifest. */
    public static class P33 extends ProxyService { }

    /** Proxy stub subclass P34 registered in the host manifest. */
    public static class P34 extends ProxyService { }

    /** Proxy stub subclass P35 registered in the host manifest. */
    public static class P35 extends ProxyService { }

    /** Proxy stub subclass P36 registered in the host manifest. */
    public static class P36 extends ProxyService { }

    /** Proxy stub subclass P37 registered in the host manifest. */
    public static class P37 extends ProxyService { }

    /** Proxy stub subclass P38 registered in the host manifest. */
    public static class P38 extends ProxyService { }

    /** Proxy stub subclass P39 registered in the host manifest. */
    public static class P39 extends ProxyService { }

    /** Proxy stub subclass P40 registered in the host manifest. */
    public static class P40 extends ProxyService { }

    /** Proxy stub subclass P41 registered in the host manifest. */
    public static class P41 extends ProxyService { }

    /** Proxy stub subclass P42 registered in the host manifest. */
    public static class P42 extends ProxyService { }

    /** Proxy stub subclass P43 registered in the host manifest. */
    public static class P43 extends ProxyService { }

    /** Proxy stub subclass P44 registered in the host manifest. */
    public static class P44 extends ProxyService { }

    /** Proxy stub subclass P45 registered in the host manifest. */
    public static class P45 extends ProxyService { }

    /** Proxy stub subclass P46 registered in the host manifest. */
    public static class P46 extends ProxyService { }

    /** Proxy stub subclass P47 registered in the host manifest. */
    public static class P47 extends ProxyService { }

    /** Proxy stub subclass P48 registered in the host manifest. */
    public static class P48 extends ProxyService { }

    /** Proxy stub subclass P49 registered in the host manifest. */
    public static class P49 extends ProxyService { }
}
