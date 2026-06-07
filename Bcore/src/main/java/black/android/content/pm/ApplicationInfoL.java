package black.android.content.pm;

import black.Reflector;

/**
 * Reflection wrapper for hidden fields in {@code android.content.pm.ApplicationInfo}
 * on Android L (Lollipop, API 21+). Provides access to CPU ABI and source directory
 * fields introduced in Lollipop.
 */
public class ApplicationInfoL {
    public static final Reflector REF = Reflector.on("android.content.pm.ApplicationInfo");

    /** The primary CPU ABI for this application (e.g., "arm64-v8a"). */
    public static Reflector.FieldWrapper<String> primaryCpuAbi = REF.field("primaryCpuAbi");

    /** The public source directory path for split APKs. */
    public static Reflector.FieldWrapper<String> scanPublicSourceDir = REF.field("scanPublicSourceDir");

    /** The source directory path for split APKs. */
    public static Reflector.FieldWrapper<String> scanSourceDir = REF.field("scanSourceDir");
}
