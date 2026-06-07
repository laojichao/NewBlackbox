package black.android.role;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.role.IRoleManager} AIDL interface.
 * Provides access to the role manager service which manages the assignment of
 * special roles (e.g., default browser, default SMS app) to applications.
 */
public class IRoleManager {
    public static final Reflector TYPE = Reflector.on("android.app.role.IRoleManager");

    /**
     * Reflection wrapper for {@code android.app.role.IRoleManager$Stub}.
     */
    public static class Stub {
        public static final Reflector TYPE = Reflector.on("android.app.role.IRoleManager$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the role manager service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = TYPE.staticMethod("asInterface", IBinder.class);
    }
}
