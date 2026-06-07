package black.android.view;

import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.view.WindowManagerGlobal} class.
 * WindowManagerGlobal is the global singleton that manages all window manager
 * interactions from the client side, holding the IWindowManager binder reference.
 */
public class WindowManagerGlobal {
    public static final Reflector REF = Reflector.on("android.view.WindowManagerGlobal");

    /** The IWindowManager binder interface (window manager system service). */
    public static Reflector.FieldWrapper<IInterface> sWindowManagerService = REF.field("sWindowManagerService");
}
