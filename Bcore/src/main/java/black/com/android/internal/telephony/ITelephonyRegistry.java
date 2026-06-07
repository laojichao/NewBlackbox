package black.com.android.internal.telephony;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code com.android.internal.telephony.ITelephonyRegistry$Stub} class.
 * Provides access to the telephony registry service which broadcasts telephony
 * state changes (e.g., signal strength, call state, data connection) to registered listeners.
 */
public class ITelephonyRegistry {
    /**
     * Reflection wrapper for {@code com.android.internal.telephony.ITelephonyRegistry$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("com.android.internal.telephony.ITelephonyRegistry$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the telephony registry service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
