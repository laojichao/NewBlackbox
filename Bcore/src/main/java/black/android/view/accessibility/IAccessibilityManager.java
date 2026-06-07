package black.android.view.accessibility;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.view.accessibility.IAccessibilityManager$Stub} class.
 * Provides access to the accessibility manager system service for managing
 * accessibility services, accessibility events, and accessibility features.
 */
public class IAccessibilityManager {
    /**
     * Reflection wrapper for {@code android.view.accessibility.IAccessibilityManager$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.view.accessibility.IAccessibilityManager$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the accessibility manager service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
