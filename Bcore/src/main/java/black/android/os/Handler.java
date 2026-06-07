package black.android.os;

import android.os.Handler.Callback;

import black.Reflector;

/**
 * Reflection wrapper for hidden fields in {@code android.os.Handler}.
 * Provides access to the mCallback field which holds the optional Callback
 * that intercepts messages before they are handled.
 */
public class Handler {
    public static final Reflector REF = Reflector.on("android.os.Handler");

    /** The Callback that intercepts messages before default handling. */
    public static Reflector.FieldWrapper<Callback> mCallback = REF.field("mCallback");
}
