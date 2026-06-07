package black.android.content;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.content.AttributionSourceState} class.
 * This is the parcelable state backing an AttributionSource, containing
 * the package name and UID of the attributing entity.
 */
public class AttributionSourceState {
    public static final Reflector REF = Reflector.on("android.content.AttributionSourceState");

    /** The package name of the attributing application. */
    public static Reflector.FieldWrapper<String> packageName = REF.field("packageName");

    /** The UID of the attributing application. */
    public static Reflector.FieldWrapper<Integer> uid = REF.field("uid");
}
