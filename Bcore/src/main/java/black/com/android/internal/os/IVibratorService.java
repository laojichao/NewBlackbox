package black.com.android.internal.os;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.os.IVibratorService$Stub} class.
 * Provides access to the vibrator service for controlling device vibration
 * including one-shot, pattern-based, and waveform vibration effects.
 */
public class IVibratorService {
    /**
     * Reflection wrapper for {@code android.os.IVibratorService$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.os.IVibratorService$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the vibrator service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
