package black.android.view;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.view.IWindowManager$Stub} class.
 * Provides access to the window manager system service for managing windows,
 * display properties, rotation, and input focus.
 */
public class IWindowManager {
    /**
     * Reflection wrapper for {@code android.view.IWindowManager$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.view.IWindowManager$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the window manager service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
