package black.android.view;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.view.autofill.IAutoFillManager$Stub} class.
 * Provides access to the autofill manager system service for managing
 * automatic form filling functionality.
 */
public class IAutoFillManager {
    /**
     * Reflection wrapper for {@code android.view.autofill.IAutoFillManager$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.view.autofill.IAutoFillManager$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the autofill manager service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
