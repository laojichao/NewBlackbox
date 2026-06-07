package black.android.net.wifi;

import black.Reflector;

/**
 * Reflection wrapper for hidden fields in {@code android.net.wifi.WifiInfo}.
 * Provides access to the BSSID, MAC address, and WifiSsid fields which are
 * restricted in newer Android versions for privacy reasons.
 */
public class WifiInfo {
    public static final Reflector REF = Reflector.on("android.net.wifi.WifiInfo");

    /** The BSSID (MAC address) of the connected access point. */
    public static Reflector.FieldWrapper<String> mBSSID = REF.field("mBSSID");

    /** The MAC address of this device's Wi-Fi interface. */
    public static Reflector.FieldWrapper<String> mMacAddress = REF.field("mMacAddress");

    /** The WifiSsid object representing the connected network's SSID. */
    public static Reflector.FieldWrapper<Object> mWifiSsid = REF.field("mWifiSsid");
}
