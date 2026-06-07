package black.android.net;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.net.IVpnManager$Stub} class.
 * Provides access to the VPN manager system service for establishing and
 * managing VPN connections.
 */
public class IVpnManager {
    /**
     * Reflection wrapper for {@code android.net.IVpnManager$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.net.IVpnManager$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the VPN manager service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
