package black.android.app;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.IActivityTaskManager$Stub} class.
 * Provides access to the asInterface factory method for obtaining an IActivityTaskManager
 * proxy from an IBinder. This is the Android Q+ replacement for IActivityManager's
 * activity lifecycle management.
 */
public class IActivityTaskManager {
    /**
     * Reflection wrapper for {@code android.app.IActivityTaskManager$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.app.IActivityTaskManager$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the activity task manager service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
