package black.com.android.internal.content;

import android.content.Intent;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code com.android.internal.content.ReferrerIntent} class.
 * ReferrerIntent extends Intent to include the referring package name, used by the system
 * to track which application initiated a broadcast or activity start.
 */
public class ReferrerIntent {
    public static final Reflector REF = Reflector.on("com.android.internal.content.ReferrerIntent");

    /**
     * Creates a new ReferrerIntent from an existing Intent and a referring package name.
     *
     * @param intent      the base Intent
     * @param referrer    the package name of the referring application
     */
    public static Reflector.ConstructorWrapper<Intent> _new = REF.constructor(Intent.class, String.class);
}
