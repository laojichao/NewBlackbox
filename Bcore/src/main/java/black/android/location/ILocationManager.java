package black.android.location;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.location.ILocationManager$Stub} class.
 * Provides access to the location manager system service for requesting location
 * updates, managing geofences, and accessing GNSS status.
 */
public class ILocationManager {
    /**
     * Reflection wrapper for {@code android.location.ILocationManager$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.location.ILocationManager$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the location manager service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
