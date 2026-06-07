package black.android.content.pm;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.content.pm.IShortcutService$Stub} class.
 * Provides access to the shortcut manager service for managing application shortcuts
 * that appear in the device launcher.
 */
public class IShortcutService {
    /**
     * Reflection wrapper for {@code android.content.pm.IShortcutService$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.content.pm.IShortcutService$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the shortcut service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
