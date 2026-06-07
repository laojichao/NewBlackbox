package black.android.app;

import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.ActivityClient} class.
 * Provides access to the activity client singleton and its controller interface,
 * which is the client-side counterpart for communicating with the ActivityTaskManager service.
 */
public class ActivityClient {
    public static final Reflector REF = Reflector.on("android.app.ActivityClient");

    /** The singleton holder for the IActivityClientController interface. */
    public static Reflector.FieldWrapper<Object> INTERFACE_SINGLETON = REF.field("INTERFACE_SINGLETON");

    /**
     * Returns the singleton ActivityClient instance.
     */
    public static Reflector.StaticMethodWrapper<Object> getInstance = REF.staticMethod("getInstance");

    /**
     * Returns the IActivityClientController binder interface.
     */
    public static Reflector.StaticMethodWrapper<Object> getActivityClientController = REF.staticMethod("getActivityClientController");

    /**
     * Reflection wrapper for the inner {@code ActivityClientControllerSingleton} class.
     * Holds a cached reference to the IActivityClientController interface instance.
     */
    public static class ActivityClientControllerSingleton {
        public static final Reflector REF = Reflector.on("android.app.ActivityClient$ActivityClientControllerSingleton");

        /** The cached IActivityClientController instance. */
        public static Reflector.FieldWrapper<IInterface> mKnownInstance = REF.field("mKnownInstance");
    }
}
