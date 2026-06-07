package black.android.os;

import black.Reflector;

/**
 * Reflection wrapper for hidden fields and methods in {@code android.os.StrictMode}.
 * Provides access to file URI exposure detection configuration, allowing
 * bypass of the StrictMode death penalty for file URI sharing between apps.
 */
public class StrictMode {
    public static final Reflector REF = Reflector.on("android.os.StrictMode");

    /** The VM policy bitmask flag for detecting file URI exposure. */
    public static Reflector.FieldWrapper<Integer> DETECT_VM_FILE_URI_EXPOSURE = REF.field("DETECT_VM_FILE_URI_EXPOSURE");

    /** The penalty flag that causes death on file URI exposure. */
    public static Reflector.FieldWrapper<Integer> PENALTY_DEATH_ON_FILE_URI_EXPOSURE = REF.field("PENALTY_DEATH_ON_FILE_URI_EXPOSURE");

    /** The current VM policy mask that controls which StrictMode checks are active. */
    public static Reflector.FieldWrapper<Integer> sVmPolicyMask = REF.field("sVmPolicyMask");

    /**
     * Disables the death penalty for file URI exposure violations.
     * This prevents crashes when sharing file:// URIs between apps on Android N+.
     */
    public static Reflector.StaticMethodWrapper<Void> disableDeathOnFileUriExposure = REF.staticMethod("disableDeathOnFileUriExposure");
}
