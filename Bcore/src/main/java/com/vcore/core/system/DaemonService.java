package com.vcore.core.system;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.vcore.BlackBoxCore;
import com.vcore.utils.compat.BuildCompat;

/**
 * A foreground daemon service designed to keep the BlackBox host process alive.
 * <p>
 * On Android Oreo and above, this service shows a persistent foreground notification
 * to prevent the system from killing the process. It also starts an inner
 * {@link DaemonInnerService} to immediately cancel the notification after the
 * foreground state is established, achieving a "silent" foreground service effect.
 */
public class DaemonService extends Service {
    public static final String TAG = "DaemonService";

    /** Notification ID derived from the host package name hash. */
    private static final int NOTIFY_ID = BlackBoxCore.getHostPkg().hashCode();

    /**
     * Called when a client binds to this service. Returns null as binding is not supported.
     *
     * @param intent the binding intent
     * @return always null
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Handles service start commands. Starts the inner daemon service and promotes
     * to foreground on Android Oreo+.
     *
     * @param intent  the start intent
     * @param flags   additional data about the start request
     * @param startId a unique start request identifier
     * @return {@link #START_STICKY} to ensure the service is restarted if killed
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent innerIntent = new Intent(this, DaemonInnerService.class);
        startService(innerIntent);
        if (BuildCompat.isOreo()) {
            showNotification();
        }
        return START_STICKY;
    }

    /**
     * Called when the service is being destroyed.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    /**
     * Displays a minimal foreground notification to keep the service alive
     * on Android Oreo and above.
     */
    private void showNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), getPackageName() + ".blackbox_core")
                .setPriority(NotificationCompat.PRIORITY_MAX);
        startForeground(NOTIFY_ID, builder.build());
    }

    /**
     * An inner service that immediately cancels the foreground notification
     * and stops itself, effectively hiding the notification from the user
     * while the parent DaemonService remains in the foreground.
     */
    public static class DaemonInnerService extends Service {
        /**
         * Called when the inner service is created.
         */
        @Override
        public void onCreate() {
            Log.i(TAG, "DaemonInnerService -> onCreate");
            super.onCreate();
        }

        /**
         * Cancels the foreground notification and stops the service immediately.
         *
         * @param intent  the start intent
         * @param flags   additional data about the start request
         * @param startId a unique start request identifier
         * @return the result from the parent implementation
         */
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            Log.i(TAG, "DaemonInnerService -> onStartCommand");
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(NOTIFY_ID);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        /**
         * Called when a client binds to this service. Returns null as binding is not supported.
         *
         * @param intent the binding intent
         * @return always null
         */
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        /**
         * Called when the inner service is being destroyed.
         */
        @Override
        public void onDestroy() {
            Log.i(TAG, "DaemonInnerService -> onDestroy");
            super.onDestroy();
        }
    }
}
