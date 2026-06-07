package black.android.os;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.os.IUserManager$Stub} class.
 * Provides access to the user manager system service for managing device users,
 * user profiles, and user restrictions.
 */
public class IUserManager {
    /**
     * Reflection wrapper for {@code android.os.IUserManager$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.os.IUserManager$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the user manager service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
