package black.android.os.mount;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.os.storage.IMountService$Stub} class.
 * This is the legacy mount service (pre-Oreo) for managing storage volumes,
 * including mounting, unmounting, and formatting storage devices.
 */
public class IMountService {
    /**
     * Reflection wrapper for {@code android.os.storage.IMountService$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.os.storage.IMountService$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the mount service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
