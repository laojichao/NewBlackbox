package black.android.os;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.os.SystemProperties} class.
 * Provides access to Android system properties (build.prop, etc.) which store
 * device configuration values like build version, hardware specs, and feature flags.
 */
public class SystemProperties {
    public static final Reflector REF = Reflector.on("android.os.SystemProperties");

    /**
     * Gets a system property value with a default fallback.
     *
     * @param key          the property key
     * @param defaultValue the default value if the property is not set
     * @return the property value, or the default value
     */
    public static Reflector.StaticMethodWrapper<String> get0 = REF.staticMethod("get", String.class, String.class);

    /**
     * Gets a system property value.
     *
     * @param key the property key
     * @return the property value, or empty string if not set
     */
    public static Reflector.StaticMethodWrapper<String> get1 = REF.staticMethod("get", String.class);

    /**
     * Gets a system property value as an integer.
     *
     * @param key          the property key
     * @param defaultValue the default value if the property is not set or not a valid integer
     * @return the property value as an integer
     */
    public static Reflector.StaticMethodWrapper<Integer> getInt = REF.staticMethod("getInt", String.class, int.class);
}
