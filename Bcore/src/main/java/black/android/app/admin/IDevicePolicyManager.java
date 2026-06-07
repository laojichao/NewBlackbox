package black.android.app.admin;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.admin.IDevicePolicyManager$Stub} class.
 * Provides access to the device policy manager system service used for enterprise
 * device management and policy enforcement.
 */
public class IDevicePolicyManager {
    /**
     * Reflection wrapper for {@code android.app.admin.IDevicePolicyManager$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.app.admin.IDevicePolicyManager$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the device policy manager service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
