package black.android.os;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.os.IPowerManager$Stub} class.
 * Provides access to the power manager system service for controlling device power
 * state, wake locks, and display brightness.
 */
public class IPowerManager {
    /**
     * Reflection wrapper for {@code android.os.IPowerManager$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.os.IPowerManager$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the power manager service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
