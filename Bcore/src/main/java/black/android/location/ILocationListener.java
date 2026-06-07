package black.android.location;

import android.location.Location;
import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.location.ILocationListener} AIDL interface.
 * This is the binder callback interface used by the location manager to deliver
 * location updates to registered listeners in application processes.
 */
public class ILocationListener {
    public static final Reflector REF = Reflector.on("android.location.ILocationListener");

    /**
     * Called when a new location is available.
     *
     * @param location the new Location object
     */
    public static Reflector.MethodWrapper<Void> onLocationChanged = REF.method("onLocationChanged", Location.class);

    /**
     * Reflection wrapper for {@code android.location.ILocationListener$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.location.ILocationListener$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the location listener.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
