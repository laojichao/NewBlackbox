package black.android.content;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.content.IRestrictionsManager$Stub} class.
 * Provides access to the restrictions manager service used for managing
 * application restrictions set by device administrators.
 */
public class IRestrictionsManager {
    /**
     * Reflection wrapper for {@code android.content.IRestrictionsManager$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.content.IRestrictionsManager$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the restrictions manager service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
