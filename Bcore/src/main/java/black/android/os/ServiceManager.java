package black.android.os;

import android.os.IBinder;

import java.util.Map;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.os.ServiceManager} class.
 * ServiceManager is the central registry for system services in Android.
 * Provides access to the service cache and the getService method for
 * obtaining system service binder interfaces.
 */
public class ServiceManager {
    public static final Reflector REF = Reflector.on("android.os.ServiceManager");

    /** The cached map of service names to their IBinder interfaces. */
    public static Reflector.FieldWrapper<Map<String, IBinder>> sCache = REF.field("sCache");

    /**
     * Returns the IBinder for a named system service.
     *
     * @param name the service name (e.g., "activity", "package")
     * @return the IBinder for the service, or null if not found
     */
    public static Reflector.StaticMethodWrapper<IBinder> getService = REF.staticMethod("getService", String.class);
}
