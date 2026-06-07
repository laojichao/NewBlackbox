package black.android.graphics;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.graphics.Compatibility} class.
 * Provides access to the setTargetSdkVersion method which configures graphics
 * rendering compatibility behavior based on the target SDK version.
 */
public class Compatibility {
    public static final Reflector REF = Reflector.on("android.graphics.Compatibility");

    /**
     * Sets the target SDK version for graphics compatibility checks.
     *
     * @param targetSdkVersion the target SDK version number
     */
    public static Reflector.StaticMethodWrapper<Void> setTargetSdkVersion = REF.staticMethod("setTargetSdkVersion", int.class);
}
