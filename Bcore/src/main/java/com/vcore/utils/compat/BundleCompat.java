package com.vcore.utils.compat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

/**
 * Compatibility wrapper for {@link Bundle} and {@link Intent} IBinder operations.
 * Provides version-safe access to the hidden {@code getBinder}/{@code putBinder} methods
 * that were added to the public API in Android Jelly Bean MR2 (API 18). For earlier versions,
 * falls back to the hidden {@code Bundle.getIBinder}/{@code putIBinder} methods.
 * <p>
 * Also provides convenience methods for embedding an {@link IBinder} inside a {@link Bundle}
 * within an {@link Intent} extra, since direct IBinder extras are not supported.
 * </p>
 */
public class BundleCompat {
    /**
     * Retrieves an {@link IBinder} from a Bundle by key, using the appropriate API
     * method based on the current Android version.
     *
     * @param bundle the bundle to retrieve the binder from
     * @param key    the key associated with the binder value
     * @return the {@link IBinder} stored under the given key
     */
    public static IBinder getBinder(Bundle bundle, String key) {
        if (Build.VERSION.SDK_INT >= 18) {
            return bundle.getBinder(key);
        }
        return black.android.os.Bundle.getIBinder.call(bundle, key);
    }

    /**
     * Stores an {@link IBinder} into a Bundle by key, using the appropriate API
     * method based on the current Android version.
     *
     * @param bundle the bundle to store the binder in
     * @param key    the key under which to store the binder
     * @param value  the {@link IBinder} value to store
     */
    public static void putBinder(Bundle bundle, String key, IBinder value) {
        if (Build.VERSION.SDK_INT >= 18) {
            bundle.putBinder(key, value);
        }
        black.android.os.Bundle.putIBinder.call(bundle, key, value);
    }

    /**
     * Stores an {@link IBinder} inside a {@link Bundle} within an {@link Intent} extra.
     * This is a workaround since intents do not directly support IBinder extras; the binder
     * is wrapped in a bundle under the key {@code "binder"}.
     *
     * @param intent the intent to add the binder extra to
     * @param key    the intent extra key for the wrapping bundle
     * @param value  the {@link IBinder} value to store
     */
    public static void putBinder(Intent intent, String key, IBinder value) {
        Bundle bundle = new Bundle();
        putBinder(bundle, "binder", value);
        intent.putExtra(key, bundle);
    }

    /**
     * Retrieves an {@link IBinder} from a {@link Bundle} embedded in an {@link Intent} extra.
     * The binder is expected to be stored under the key {@code "binder"} within the bundle.
     *
     * @param intent the intent containing the bundle extra
     * @param key    the intent extra key where the bundle is stored
     * @return the {@link IBinder} retrieved from the embedded bundle, or {@code null} if
     *         no bundle exists under the given key
     */
    public static IBinder getBinder(Intent intent, String key) {
        Bundle bundle = intent.getBundleExtra(key);
        if (bundle != null) {
            return getBinder(bundle, "binder");
        }
        return null;
    }
}
