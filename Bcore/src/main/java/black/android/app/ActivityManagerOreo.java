package black.android.app;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.ActivityManager} class on Android O (Oreo, API 26+).
 * Provides access to the IActivityManagerSingleton field which replaced the deprecated
 * {@link ActivityManagerNative#getDefault()} pattern.
 */
public class ActivityManagerOreo {
    public static final Reflector REF = Reflector.on("android.app.ActivityManager");

    /** The Singleton holding the IActivityManager binder interface (Android O+). */
    public static Reflector.FieldWrapper<Object> IActivityManagerSingleton = REF.field("IActivityManagerSingleton");
}
