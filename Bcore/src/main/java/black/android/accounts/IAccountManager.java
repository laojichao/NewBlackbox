package black.android.accounts;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.accounts.IAccountManager} AIDL interface.
 * Provides access to the account management system service via reflection.
 */
public class IAccountManager {
    /**
     * Reflection wrapper for {@code android.accounts.IAccountManager$Stub}.
     * Provides access to the static {@code asInterface} factory method for obtaining
     * an IAccountManager proxy from an IBinder.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.accounts.IAccountManager$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the account manager service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
