package com.vcore.utils.compat;

import android.os.Build;

/**
 * Utility class for detecting the current Android API level and identifying specific
 * Android OEM ROM variants. Provides version-safe checks that correctly handle
 * developer preview builds by inspecting both {@link Build.VERSION#SDK_INT} and
 * {@link Build.VERSION#PREVIEW_SDK_INT}.
 * <p>
 * Also detects custom Android ROMs including EMUI (Huawei), MIUI (Xiaomi), Flyme (Meizu),
 * ColorOS (OPPO), 360UI, Letv, Vivo, and Samsung.
 * </p>
 */
public class BuildCompat {

    /**
     * Checks whether the device is running Android 14 (API 34) or higher.
     *
     * @return {@code true} if SDK_INT >= 34, or SDK_INT == 33 with preview SDK 1
     */
    public static boolean isU() {
        return Build.VERSION.SDK_INT >= 34 || (Build.VERSION.SDK_INT >= 33 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    /**
     * Checks whether the device is running Android 13 Tiramisu (API 33) or higher.
     *
     * @return {@code true} if SDK_INT >= TIRAMISU, or SDK_INT >= S with preview SDK 1
     */
    public static boolean isT() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    /**
     * Checks whether the device is running Android 12 S (API 31) or higher.
     *
     * @return {@code true} if SDK_INT >= S, or SDK_INT >= R with preview SDK 1
     */
    public static boolean isS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    /**
     * Checks whether the device is running Android 11 R (API 30) or higher.
     *
     * @return {@code true} if SDK_INT >= R, or SDK_INT >= Q with preview SDK 1
     */
    public static boolean isR() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    /**
     * Checks whether the device is running Android 10 Q (API 29) or higher.
     *
     * @return {@code true} if SDK_INT >= Q, or SDK_INT >= P with preview SDK 1
     */
    public static boolean isQ() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    /**
     * Checks whether the device is running Android 9 Pie (API 28) or higher.
     *
     * @return {@code true} if SDK_INT >= P, or SDK_INT >= O with preview SDK 1
     */
    public static boolean isPie() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    /**
     * Checks whether the device is running Android 8 Oreo (API 26) or higher.
     *
     * @return {@code true} if SDK_INT >= O, or SDK_INT >= N with preview SDK 1
     */
    public static boolean isOreo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    /**
     * Checks whether the device is running Android 7 Nougat (API 24) or higher.
     *
     * @return {@code true} if SDK_INT >= N, or SDK_INT >= M with preview SDK 1
     */
    public static boolean isN() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    /**
     * Checks whether the device is running Android 6 Marshmallow (API 23) or higher.
     *
     * @return {@code true} if SDK_INT >= M
     */
    public static boolean isM() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * Checks whether the device is running Android 5 Lollipop (API 21) or higher.
     *
     * @return {@code true} if SDK_INT >= LOLLIPOP
     */
    public static boolean isL() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * Checks whether the device is manufactured by Samsung.
     *
     * @return {@code true} if {@link Build#BRAND} or {@link Build#MANUFACTURER} is "samsung"
     */
    public static boolean isSamsung() {
        return "samsung".equalsIgnoreCase(Build.BRAND) || "samsung".equalsIgnoreCase(Build.MANUFACTURER);
    }

    /**
     * Checks whether the device is running Huawei's EMUI ROM.
     *
     * @return {@code true} if the build display starts with "EMUI" or the
     *         {@code ro.build.version.emui} property contains "EmotionUI"
     */
    public static boolean isEMUI() {
        if (Build.DISPLAY.toUpperCase().startsWith("EMUI")) {
            return true;
        }

        String property = SystemPropertiesCompat.get("ro.build.version.emui");
        return property != null && property.contains("EmotionUI");
    }

    /**
     * Checks whether the device is running Xiaomi's MIUI ROM.
     *
     * @return {@code true} if the {@code ro.miui.ui.version.code} system property exists and is > 0
     */
    public static boolean isMIUI() {
        return SystemPropertiesCompat.getInt("ro.miui.ui.version.code", 0) > 0;
    }

    /**
     * Checks whether the device is running Meizu's Flyme ROM.
     *
     * @return {@code true} if {@link Build#DISPLAY} contains "flyme" (case-insensitive)
     */
    public static boolean isFlyme() {
        return Build.DISPLAY.toLowerCase().contains("flyme");
    }

    /**
     * Checks whether the device is running OPPO's ColorOS ROM.
     *
     * @return {@code true} if either the {@code ro.build.version.opporom} or
     *         {@code ro.rom.different.version} system property exists
     */
    public static boolean isColorOS() {
        return SystemPropertiesCompat.isExist("ro.build.version.opporom") || SystemPropertiesCompat.isExist("ro.rom.different.version");
    }

    /**
     * Checks whether the device is running 360's custom UI.
     *
     * @return {@code true} if the {@code ro.build.uiversion} property contains "360UI"
     */
    public static boolean is360UI() {
        String property = SystemPropertiesCompat.get("ro.build.uiversion");
        return property != null && property.toUpperCase().contains("360UI");
    }

    /**
     * Checks whether the device is manufactured by Letv (LeEco).
     *
     * @return {@code true} if {@link Build#MANUFACTURER} is "Letv" (case-insensitive)
     */
    public static boolean isLetv() {
        return Build.MANUFACTURER.equalsIgnoreCase("Letv");
    }

    /**
     * Checks whether the device is running Vivo's custom ROM.
     *
     * @return {@code true} if the {@code ro.vivo.os.build.display.id} system property exists
     */
    public static boolean isVivo() {
        return SystemPropertiesCompat.isExist("ro.vivo.os.build.display.id");
    }

    /** Cached ROM type to avoid repeated detection. */
    private static ROMType sRomType;

    /**
     * Detects and returns the current device's ROM type. The result is cached after the
     * first invocation. Detection priority: EMUI, MIUI, Flyme, ColorOS, 360UI, Letv, Vivo,
     * Samsung, then OTHER.
     *
     * @return the detected {@link ROMType} for this device
     */
    public static ROMType getROMType() {
        if (sRomType == null) {
            if (isEMUI()) {
                sRomType = ROMType.EMUI;
            } else if (isMIUI()) {
                sRomType = ROMType.MIUI;
            } else if (isFlyme()) {
                sRomType = ROMType.FLYME;
            } else if (isColorOS()) {
                sRomType = ROMType.COLOR_OS;
            } else if (is360UI()) {
                sRomType = ROMType._360;
            } else if (isLetv()) {
                sRomType = ROMType.LETV;
            } else if (isVivo()) {
                sRomType = ROMType.VIVO;
            } else if (isSamsung()) {
                sRomType = ROMType.SAMSUNG;
            } else {
                sRomType = ROMType.OTHER;
            }
        }
        return sRomType;
    }

    /**
     * Enumeration of known Android OEM ROM types for device-specific compatibility handling.
     */
    public enum ROMType {
        /** Huawei EMUI */
        EMUI,
        /** Xiaomi MIUI */
        MIUI,
        /** Meizu Flyme */
        FLYME,
        /** OPPO ColorOS */
        COLOR_OS,
        /** LeEco (Letv) */
        LETV,
        /** Vivo */
        VIVO,
        /** 360 Security UI */
        _360,
        /** Samsung One UI / TouchWiz */
        SAMSUNG,
        /** Unrecognized or stock Android ROM */
        OTHER
    }
}