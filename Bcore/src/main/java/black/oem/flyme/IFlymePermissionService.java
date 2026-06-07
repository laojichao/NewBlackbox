package black.oem.flyme;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code meizu.security.IFlymePermissionService} AIDL interface.
 * Provides access to the Flyme (Meizu) custom permission service used for
 * managing device-specific permission controls on Meizu devices.
 */
public class IFlymePermissionService {
    public static final Reflector TYPE = Reflector.on("meizu.security.IFlymePermissionService");

    /**
     * Reflection wrapper for {@code meizu.security.IFlymePermissionService$Stub}.
     */
    public static class Stub {
        public static final Reflector TYPE = Reflector.on("meizu.security.IFlymePermissionService$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the Flyme permission service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = TYPE.staticMethod("asInterface", IBinder.class);
    }
}
