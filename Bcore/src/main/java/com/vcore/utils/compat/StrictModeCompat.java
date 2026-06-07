package com.vcore.utils.compat;

import black.android.os.StrictMode;

/**
 * Compatibility wrapper for disabling Android's {@code StrictMode} file URI exposure
 * death penalty. On Android Nougat (API 24+), passing a {@code file://} URI between
 * apps triggers a {@code FileUriExposedException} by default. This class provides a
 * safe way to suppress that behavior by either calling the official API or directly
 * clearing the relevant policy flags in the internal {@code sVmPolicyMask} field.
 */
public class StrictModeCompat {
    /**
     * The {@code DETECT_VM_FILE_URI_EXPOSURE} policy flag value. Falls back to the known
     * constant {@code 0x20 << 8} if the reflection lookup fails.
     */
    public static final int DETECT_VM_FILE_URI_EXPOSURE = StrictMode.DETECT_VM_FILE_URI_EXPOSURE.get() == null ?
            (0x20 << 8) : StrictMode.DETECT_VM_FILE_URI_EXPOSURE.get();

    /**
     * The {@code PENALTY_DEATH_ON_FILE_URI_EXPOSURE} penalty flag value. Falls back to the
     * known constant {@code 0x04 << 24} if the reflection lookup fails.
     */
    public static final int PENALTY_DEATH_ON_FILE_URI_EXPOSURE = StrictMode.PENALTY_DEATH_ON_FILE_URI_EXPOSURE.get() == null ?
            (0x04 << 24) : StrictMode.PENALTY_DEATH_ON_FILE_URI_EXPOSURE.get();

    /**
     * Disables the StrictMode death penalty for file URI exposure. First attempts to call
     * the official {@code StrictMode.disableDeathOnFileUriExposure()} API. If that fails
     * (e.g., due to the method not existing on older platforms), falls back to directly
     * clearing the {@code DETECT_VM_FILE_URI_EXPOSURE} and
     * {@code PENALTY_DEATH_ON_FILE_URI_EXPOSURE} bits from the internal policy mask.
     */
    public static void disableDeathOnFileUriExposure() {
        try {
            StrictMode.disableDeathOnFileUriExposure.call();
        } catch (Throwable e) {
            try {
                int sVmPolicyMask = StrictMode.sVmPolicyMask.get();
                sVmPolicyMask &= ~(DETECT_VM_FILE_URI_EXPOSURE | PENALTY_DEATH_ON_FILE_URI_EXPOSURE);
                StrictMode.sVmPolicyMask.set(sVmPolicyMask);
            } catch (Throwable e2) {
                e2.printStackTrace();
            }
        }
    }
}
