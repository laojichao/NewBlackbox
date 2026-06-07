package com.vcore.core.system;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vcore.utils.Slog;
import com.vcore.utils.compat.BundleCompat;

/**
 * ContentProvider that serves as the entry point for the BlackBox system.
 * <p>
 * On creation, it triggers the entire BlackBox system startup via
 * {@link BlackBoxSystem#startup()}. It also acts as an IPC endpoint for
 * retrieving system service binders: callers invoke {@link #call(String, String, Bundle)}
 * with method "VM" and a service name in the extras bundle to obtain the
 * corresponding service binder.
 */
public class SystemCallProvider extends ContentProvider {
    public static final String TAG = "SystemCallProvider";

    /**
     * Called when the provider is created. Initializes the BlackBox system.
     *
     * @return true if the system was successfully initialized
     */
    @Override
    public boolean onCreate() {
        return initSystem();
    }

    /**
     * Initializes the BlackBox system by calling {@link BlackBoxSystem#startup()}.
     *
     * @return always returns true
     */
    private boolean initSystem() {
        BlackBoxSystem.getSystem().startup();
        return true;
    }

    /**
     * Handles IPC calls to retrieve system service binders.
     * <p>
     * When the method is "VM", reads the service name from extras key
     * "_B_|_server_name_" and returns the corresponding service binder
     * under the key "_B_|_server_".
     *
     * @param method the method name to call
     * @param arg    optional argument
     * @param extras optional Bundle containing the service name
     * @return a Bundle containing the requested service binder, or the result
     *         of the parent implementation for unrecognized methods
     */
    @Nullable
    @Override
    public Bundle call(@NonNull String method, @Nullable String arg, @Nullable Bundle extras) {
        Slog.d(TAG, "call: " + method + ", " + extras);
        if ("VM".equals(method)) {
            Bundle bundle = new Bundle();
            if (extras != null) {
                String name = extras.getString("_B_|_server_name_");
                BundleCompat.putBinder(bundle, "_B_|_server_", ServiceManager.getService(name));
            }
            return bundle;
        }
        return super.call(method, arg, extras);
    }

    /** Not supported; returns null. */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    /** Not supported; returns null. */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    /** Not supported; returns null. */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    /** Not supported; returns 0. */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    /** Not supported; returns 0. */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
