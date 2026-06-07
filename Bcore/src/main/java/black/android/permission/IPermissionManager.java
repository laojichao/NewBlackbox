package black.android.permission;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.permission.IPermissionManager$Stub} class.
 * Provides access to the permission manager system service for querying and managing
 * runtime permissions, permission flags, and permission policies.
 */
public class IPermissionManager {
    /**
     * Reflection wrapper for {@code android.permission.IPermissionManager$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.permission.IPermissionManager$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the permission manager service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
