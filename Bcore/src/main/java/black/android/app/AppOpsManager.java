package black.android.app;

import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.AppOpsManager} class.
 * Provides access to the IAppOpsService binder interface used for
 * application operations permission checking.
 */
public class AppOpsManager {
    public static final Reflector REF = Reflector.on("android.app.AppOpsManager");

    /** The IAppOpsService binder interface for app operations management. */
    public static Reflector.FieldWrapper<IInterface> mService = REF.field("mService");
}
