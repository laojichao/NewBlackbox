package black.android.os;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.os.IDeviceIdentifiersPolicyService$Stub} class.
 * Provides access to the device identifiers policy service which controls access
 * to sensitive device identifiers like IMEI, MEID, and serial number.
 */
public class IDeviceIdentifiersPolicyService {
    /**
     * Reflection wrapper for {@code android.os.IDeviceIdentifiersPolicyService$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.os.IDeviceIdentifiersPolicyService$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the device identifiers policy service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
