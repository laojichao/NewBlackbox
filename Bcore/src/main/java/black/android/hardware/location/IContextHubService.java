package black.android.hardware.location;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.hardware.location.IContextHubService$Stub} class.
 * Provides access to the Context Hub service for communicating with low-power
 * companion processors (e.g., for sensor hub or nano-app management).
 */
public class IContextHubService {
    /**
     * Reflection wrapper for {@code android.hardware.location.IContextHubService$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.hardware.location.IContextHubService$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the Context Hub service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
