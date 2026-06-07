package black.android.os;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.os.IVibratorManagerService$Stub} class.
 * Provides access to the vibrator manager service for controlling device vibration
 * motors including haptic feedback and pattern-based vibration.
 */
public class IVibratorManagerService {
    /**
     * Reflection wrapper for {@code android.os.IVibratorManagerService$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.os.IVibratorManagerService$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the vibrator manager service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
