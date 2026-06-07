package black.android.net.wifi;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.net.wifi.IWifiManager$Stub} class.
 * Provides access to the Wi-Fi manager system service for scanning, connecting,
 * and managing Wi-Fi network configurations.
 */
public class IWifiManager {
    /**
     * Reflection wrapper for {@code android.net.wifi.IWifiManager$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.net.wifi.IWifiManager$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the Wi-Fi manager service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
