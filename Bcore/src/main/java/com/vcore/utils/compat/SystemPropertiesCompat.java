package com.vcore.utils.compat;

import android.text.TextUtils;

import black.android.os.SystemProperties;

/**
 * Compatibility wrapper for Android's hidden {@code android.os.SystemProperties} class.
 * Provides safe access to system property reads with exception handling, since the
 * {@code SystemProperties} API is not part of the public Android SDK and must be
 * accessed via reflection.
 */
public class SystemPropertiesCompat {
    /**
     * Gets the value of a system property, returning the given default if the property
     * does not exist or cannot be read.
     *
     * @param key the system property key to look up
     * @param def the default value to return if the property is not found
     * @return the property value, or {@code def} if not found or on error
     */
    public static String get(String key, String def) {
        try {
            return SystemProperties.get0.call(key, def);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return def;
    }

    /**
     * Gets the value of a system property, returning {@code null} if the property
     * does not exist or cannot be read.
     *
     * @param key the system property key to look up
     * @return the property value, or {@code null} if not found or on error
     */
    public static String get(String key) {
        try {
            return SystemProperties.get1.call(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets the integer value of a system property, returning the given default if the
     * property does not exist, cannot be read, or is not a valid integer.
     *
     * @param key the system property key to look up
     * @param def the default integer value to return if the property is not found
     * @return the property value as an integer, or {@code def} if not found or on error
     */
    public static int getInt(String key, int def) {
        try {
            return SystemProperties.getInt.call(key, def);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return def;
    }

    /**
     * Checks whether a system property exists (i.e., has a non-empty value).
     *
     * @param key the system property key to check
     * @return {@code true} if the property exists and has a non-empty value
     */
    public static boolean isExist(String key) {
        return !TextUtils.isEmpty(get(key));
    }
}
