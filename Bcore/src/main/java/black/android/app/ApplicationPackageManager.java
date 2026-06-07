package black.android.app;

import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.ApplicationPackageManager} class.
 * Provides access to the IPackageManager and IPermissionManager binder interfaces
 * used internally by the PackageManager implementation.
 */
public class ApplicationPackageManager {
    public static final Reflector REF = Reflector.on("android.app.ApplicationPackageManager");

    /** The IPackageManager binder interface (package management service). */
    public static Reflector.FieldWrapper<IInterface> mPM = REF.field("mPM");

    /** The IPermissionManager binder interface (permission management service). */
    public static Reflector.FieldWrapper<Object> mPermissionManager = REF.field("mPermissionManager");
}
