package black.android.app;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.IApplicationThread$Stub} class.
 * Provides access to the asInterface factory method for obtaining an IApplicationThread
 * proxy, which is the application-side callback interface for the activity manager.
 */
public class IApplicationThread {
    /**
     * Reflection wrapper for {@code android.app.IApplicationThread$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.app.IApplicationThread$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the application thread.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
