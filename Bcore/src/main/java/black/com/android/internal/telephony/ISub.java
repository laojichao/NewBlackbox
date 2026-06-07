package black.com.android.internal.telephony;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code com.android.internal.telephony.ISub$Stub} class.
 * Provides access to the telephony subscription service for managing SIM
 * subscriptions, carrier configurations, and multi-SIM operations.
 */
public class ISub {
    /**
     * Reflection wrapper for {@code com.android.internal.telephony.ISub$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("com.android.internal.telephony.ISub$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the subscription service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
