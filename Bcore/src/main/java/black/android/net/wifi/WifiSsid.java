package black.android.net.wifi;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.net.wifi.WifiSsid} class.
 * Provides access to the static factory method for creating a WifiSsid from
 * an ASCII-encoded SSID string.
 */
public class WifiSsid {
    public static final Reflector REF = Reflector.on("android.net.wifi.WifiSsid");

    /**
     * Creates a WifiSsid from an ASCII-encoded SSID string.
     *
     * @param asciiEncoded the ASCII-encoded SSID string
     * @return the WifiSsid object
     */
    public static Reflector.StaticMethodWrapper<Object> createFromAsciiEncoded = REF.staticMethod("createFromAsciiEncoded", String.class);
}
