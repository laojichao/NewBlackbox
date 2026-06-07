package black.android.content;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.content.ContentResolver} class.
 * Provides access to the package name field used by content resolvers for
 * caller identification and attribution.
 */
public class ContentResolver {
    public static final Reflector REF = Reflector.on("android.content.ContentResolver");

    /** The package name associated with this content resolver. */
    public static Reflector.FieldWrapper<String> mPackageName = REF.field("mPackageName");
}
