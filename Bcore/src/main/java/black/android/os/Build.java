package black.android.os;

import black.Reflector;

/**
 * Reflection wrapper for hidden fields in {@code android.os.Build}.
 * Provides access to device identification fields that can be read/modified
 * via reflection to spoof device properties.
 */
public class Build {
    public static final Reflector REF = Reflector.on("android.os.Build");

    /** The name of the board (e.g., "goldfish", "ranchu"). */
    public static Reflector.FieldWrapper<String> BOARD = REF.field("BOARD");

    /** The consumer-visible brand (e.g., "google", "samsung"). */
    public static Reflector.FieldWrapper<String> BRAND = REF.field("BRAND");

    /** The name of the industrial design (e.g., "marlin", "angler"). */
    public static Reflector.FieldWrapper<String> DEVICE = REF.field("DEVICE");

    /** A build ID string meant for display to the user. */
    public static Reflector.FieldWrapper<String> DISPLAY = REF.field("DISPLAY");

    /** The hostname of the build system. */
    public static Reflector.FieldWrapper<String> HOST = REF.field("HOST");

    /** Either a changelist number or a label. */
    public static Reflector.FieldWrapper<String> ID = REF.field("ID");

    /** The manufacturer of the product/hardware. */
    public static Reflector.FieldWrapper<String> MANUFACTURER = REF.field("MANUFACTURER");

    /** The end-user-visible name for the end product. */
    public static Reflector.FieldWrapper<String> MODEL = REF.field("MODEL");

    /** The name of the overall product. */
    public static Reflector.FieldWrapper<String> PRODUCT = REF.field("PRODUCT");

    /** Comma-separated tags describing the build (e.g., "release-keys"). */
    public static Reflector.FieldWrapper<String> TAGS = REF.field("TAGS");

    /** The type of build (e.g., "user", "userdebug", "eng"). */
    public static Reflector.FieldWrapper<String> TYPE = REF.field("TYPE");

    /** The name of the user that performed the build. */
    public static Reflector.FieldWrapper<String> USER = REF.field("USER");
}
