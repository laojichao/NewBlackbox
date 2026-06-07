package com.vcore.utils.compat;

import android.os.IBinder;
import android.os.IInterface;

import black.android.app.ApplicationThreadNative;
import black.android.app.IApplicationThread;

/**
 * Compatibility wrapper for obtaining an {@link IInterface} proxy for the application thread
 * binder. Handles the API change introduced in Android Oreo (API 26) where
 * {@code ApplicationThreadNative.asInterface} was replaced by
 * {@code IApplicationThread.Stub.asInterface}.
 */
public class ApplicationThreadCompat {
    /**
     * Converts a raw {@link IBinder} to an {@link IInterface} application thread proxy,
     * using the appropriate API method based on the current Android version.
     *
     * @param binder the raw binder representing the application thread
     * @return an {@link IInterface} proxy for the application thread
     */
    public static IInterface asInterface(IBinder binder) {
        if (BuildCompat.isOreo()) {
            return IApplicationThread.Stub.asInterface.call(binder);
        }
        return ApplicationThreadNative.asInterface.call(binder);
    }
}
