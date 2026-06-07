package black.android.hardware.display;

import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.hardware.display.DisplayManagerGlobal} class.
 * This is the global singleton that manages display connections and provides access
 * to the IDisplayManager binder interface.
 */
public class DisplayManagerGlobal {
    public static final Reflector REF = Reflector.on("android.hardware.display.DisplayManagerGlobal");

    /** The IDisplayManager binder interface. */
    public static Reflector.FieldWrapper<IInterface> mDm = REF.field("mDm");

    /**
     * Returns the global DisplayManagerGlobal singleton instance.
     *
     * @return the DisplayManagerGlobal instance
     */
    public static Reflector.StaticMethodWrapper<Object> getInstance = REF.staticMethod("getInstance");
}
