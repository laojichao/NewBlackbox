package black.android.app;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.ContextImpl} class on Kitkat (API 19).
 * Provides access to the {@code mOpPackageName} field which stores the package name
 * used for application operations (AppOps) checks.
 */
public class ContextImplKitkat {
    public static final Reflector REF = Reflector.on("android.app.ContextImpl");

    /** The package name used for AppOps operations (Kitkat-specific field). */
    public static Reflector.FieldWrapper<String> mOpPackageName = REF.field("mOpPackageName");
}
