package black.android.app;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.ApplicationThreadNative} class.
 * Provides access to the static {@code asInterface} method for obtaining an
 * IApplicationThread proxy from an IBinder.
 */
public class ApplicationThreadNative {
    public static final Reflector REF = Reflector.on("android.app.ApplicationThreadNative");

    /**
     * Converts an {@link IBinder} into an {@link IInterface} proxy for the application thread.
     */
    public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
}
