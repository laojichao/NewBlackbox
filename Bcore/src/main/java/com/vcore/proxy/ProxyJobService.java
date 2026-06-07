package com.vcore.proxy;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.res.Configuration;

import com.vcore.app.dispatcher.AppJobServiceDispatcher;

/**
 * Proxy job service that acts as a stub entry point for scheduled jobs
 * in the host application manifest.
 * <p>
 * Delegates all job lifecycle callbacks ({@link #onStartJob}, {@link #onStopJob},
 * {@link #onDestroy}, etc.) to the {@link AppJobServiceDispatcher}, which routes them
 * to the appropriate guest application's job service implementation.
 * </p>
 */
public class ProxyJobService extends JobService {
    /** Tag for logging. */
    public static final String TAG = "StubJobService";

    /**
     * Called when the system determines that the job should start executing.
     * Delegates to {@link AppJobServiceDispatcher}.
     *
     * @param params the parameters specifying the job to be started
     * @return {@code true} if the job is still running and the system should hold a
     *         partial wake lock; {@code false} if the job is finished
     */
    @Override
    public boolean onStartJob(JobParameters params) {
        return AppJobServiceDispatcher.get().onStartJob(params);
    }

    /**
     * Called when the system determines that the job should stop executing.
     * Delegates to {@link AppJobServiceDispatcher}.
     *
     * @param params the parameters specifying the job that is being stopped
     * @return {@code true} if the job should be rescheduled; {@code false} otherwise
     */
    @Override
    public boolean onStopJob(JobParameters params) {
        return AppJobServiceDispatcher.get().onStopJob(params);
    }

    /**
     * Called when the service receives a start command. Returns {@link #START_NOT_STICKY}
     * to indicate the service should not be restarted if the system kills it.
     *
     * @param intent  the intent that was used to start the service
     * @param flags   additional data about the start request
     * @param startId a unique integer representing this specific start request
     * @return {@link #START_NOT_STICKY}
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    }

    /**
     * Called when the service is being destroyed. Delegates cleanup to
     * {@link AppJobServiceDispatcher}.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        AppJobServiceDispatcher.get().onDestroy();
    }

    /**
     * Called when the device configuration changes while the service is running.
     * Delegates to {@link AppJobServiceDispatcher}.
     *
     * @param newConfig the new device configuration
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        AppJobServiceDispatcher.get().onConfigurationChanged(newConfig);
    }

    /**
     * Called when the system is running low on memory. Delegates to
     * {@link AppJobServiceDispatcher}.
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        AppJobServiceDispatcher.get().onLowMemory();
    }

    /**
     * Called when the operating system determines that it is a good time for the
     * process to trim unneeded memory. Delegates to {@link AppJobServiceDispatcher}.
     *
     * @param level the context hint for the degree of trimming being requested
     */
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        AppJobServiceDispatcher.get().onTrimMemory(level);
    }

    /** Proxy stub subclass P0 registered in the host manifest. */
    public static class P0 extends ProxyJobService { }

    /** Proxy stub subclass P1 registered in the host manifest. */
    public static class P1 extends ProxyJobService { }

    /** Proxy stub subclass P2 registered in the host manifest. */
    public static class P2 extends ProxyJobService { }

    /** Proxy stub subclass P3 registered in the host manifest. */
    public static class P3 extends ProxyJobService { }

    /** Proxy stub subclass P4 registered in the host manifest. */
    public static class P4 extends ProxyJobService { }

    /** Proxy stub subclass P5 registered in the host manifest. */
    public static class P5 extends ProxyJobService { }

    /** Proxy stub subclass P6 registered in the host manifest. */
    public static class P6 extends ProxyJobService { }

    /** Proxy stub subclass P7 registered in the host manifest. */
    public static class P7 extends ProxyJobService { }

    /** Proxy stub subclass P8 registered in the host manifest. */
    public static class P8 extends ProxyJobService { }

    /** Proxy stub subclass P9 registered in the host manifest. */
    public static class P9 extends ProxyJobService { }

    /** Proxy stub subclass P10 registered in the host manifest. */
    public static class P10 extends ProxyJobService { }

    /** Proxy stub subclass P11 registered in the host manifest. */
    public static class P11 extends ProxyJobService { }

    /** Proxy stub subclass P12 registered in the host manifest. */
    public static class P12 extends ProxyJobService { }

    /** Proxy stub subclass P13 registered in the host manifest. */
    public static class P13 extends ProxyJobService { }

    /** Proxy stub subclass P14 registered in the host manifest. */
    public static class P14 extends ProxyJobService { }

    /** Proxy stub subclass P15 registered in the host manifest. */
    public static class P15 extends ProxyJobService { }

    /** Proxy stub subclass P16 registered in the host manifest. */
    public static class P16 extends ProxyJobService { }

    /** Proxy stub subclass P17 registered in the host manifest. */
    public static class P17 extends ProxyJobService { }

    /** Proxy stub subclass P18 registered in the host manifest. */
    public static class P18 extends ProxyJobService { }

    /** Proxy stub subclass P19 registered in the host manifest. */
    public static class P19 extends ProxyJobService { }

    /** Proxy stub subclass P20 registered in the host manifest. */
    public static class P20 extends ProxyJobService { }

    /** Proxy stub subclass P21 registered in the host manifest. */
    public static class P21 extends ProxyJobService { }

    /** Proxy stub subclass P22 registered in the host manifest. */
    public static class P22 extends ProxyJobService { }

    /** Proxy stub subclass P23 registered in the host manifest. */
    public static class P23 extends ProxyJobService { }

    /** Proxy stub subclass P24 registered in the host manifest. */
    public static class P24 extends ProxyJobService { }

    /** Proxy stub subclass P25 registered in the host manifest. */
    public static class P25 extends ProxyJobService { }

    /** Proxy stub subclass P26 registered in the host manifest. */
    public static class P26 extends ProxyJobService { }

    /** Proxy stub subclass P27 registered in the host manifest. */
    public static class P27 extends ProxyJobService { }

    /** Proxy stub subclass P28 registered in the host manifest. */
    public static class P28 extends ProxyJobService { }

    /** Proxy stub subclass P29 registered in the host manifest. */
    public static class P29 extends ProxyJobService { }

    /** Proxy stub subclass P30 registered in the host manifest. */
    public static class P30 extends ProxyJobService { }

    /** Proxy stub subclass P31 registered in the host manifest. */
    public static class P31 extends ProxyJobService { }

    /** Proxy stub subclass P32 registered in the host manifest. */
    public static class P32 extends ProxyJobService { }

    /** Proxy stub subclass P33 registered in the host manifest. */
    public static class P33 extends ProxyJobService { }

    /** Proxy stub subclass P34 registered in the host manifest. */
    public static class P34 extends ProxyJobService { }

    /** Proxy stub subclass P35 registered in the host manifest. */
    public static class P35 extends ProxyJobService { }

    /** Proxy stub subclass P36 registered in the host manifest. */
    public static class P36 extends ProxyJobService { }

    /** Proxy stub subclass P37 registered in the host manifest. */
    public static class P37 extends ProxyJobService { }

    /** Proxy stub subclass P38 registered in the host manifest. */
    public static class P38 extends ProxyJobService { }

    /** Proxy stub subclass P39 registered in the host manifest. */
    public static class P39 extends ProxyJobService { }

    /** Proxy stub subclass P40 registered in the host manifest. */
    public static class P40 extends ProxyJobService { }

    /** Proxy stub subclass P41 registered in the host manifest. */
    public static class P41 extends ProxyJobService { }

    /** Proxy stub subclass P42 registered in the host manifest. */
    public static class P42 extends ProxyJobService { }

    /** Proxy stub subclass P43 registered in the host manifest. */
    public static class P43 extends ProxyJobService { }

    /** Proxy stub subclass P44 registered in the host manifest. */
    public static class P44 extends ProxyJobService { }

    /** Proxy stub subclass P45 registered in the host manifest. */
    public static class P45 extends ProxyJobService { }

    /** Proxy stub subclass P46 registered in the host manifest. */
    public static class P46 extends ProxyJobService { }

    /** Proxy stub subclass P47 registered in the host manifest. */
    public static class P47 extends ProxyJobService { }

    /** Proxy stub subclass P48 registered in the host manifest. */
    public static class P48 extends ProxyJobService { }

    /** Proxy stub subclass P49 registered in the host manifest. */
    public static class P49 extends ProxyJobService { }
}
