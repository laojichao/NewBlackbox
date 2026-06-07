package black.android.app;

import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.ActivityManagerNative} class.
 * This was the pre-Oreo mechanism for obtaining the IActivityManager binder interface.
 * On Android O+, {@link ActivityManagerOreo} should be used instead.
 */
public class ActivityManagerNative {
    public static final Reflector REF = Reflector.on("android.app.ActivityManagerNative");

    /** The static gDefault singleton field holding the IActivityManager. */
    public static Reflector.FieldWrapper<Object> gDefault = REF.field("gDefault");

    /**
     * Returns the default IActivityManager interface instance.
     */
    public static Reflector.StaticMethodWrapper<IInterface> getDefault = REF.staticMethod("getDefault");
}
