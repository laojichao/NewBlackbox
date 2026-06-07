package black.oem.vivo;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code vivo.app.security.IVivoPermissionService} AIDL interface.
 * Provides access to Vivo's custom permission service used for managing
 * device-specific permission controls and security features on Vivo devices.
 */
public class IVivoPermissonService {
    public static final Reflector TYPE = Reflector.on("vivo.app.security.IVivoPermissionService");

    /**
     * Reflection wrapper for {@code vivo.app.security.IVivoPermissionService$Stub}.
     */
    public static class Stub {
        public static final Reflector TYPE = Reflector.on("vivo.app.security.IVivoPermissionService$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the Vivo permission service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = TYPE.staticMethod("asInterface", IBinder.class);
    }
}
