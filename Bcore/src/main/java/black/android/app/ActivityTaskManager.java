package black.android.app;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.ActivityTaskManager} class (Android Q+).
 * Provides access to the IActivityTaskManagerSingleton which replaced IActivityManager
 * for activity lifecycle management on Android 10+.
 */
public class ActivityTaskManager {
    public static final Reflector REF = Reflector.on("android.app.ActivityTaskManager");

    /** The Singleton holding the IActivityTaskManager binder interface (Android Q+). */
    public static Reflector.FieldWrapper<Object> IActivityTaskManagerSingleton = REF.field("IActivityTaskManagerSingleton");
}
