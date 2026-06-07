package black.com.android.internal.telephony;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code com.android.internal.telephony.ITelephony$Stub} class.
 * Provides access to the telephony system service for controlling phone calls,
 * querying network state, and managing telephony features.
 */
public class ITelephony {
    /**
     * Reflection wrapper for {@code com.android.internal.telephony.ITelephony$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("com.android.internal.telephony.ITelephony$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the telephony service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
