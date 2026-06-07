package black.oem.vivo;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code vivo.app.popupcamera.IPopupCameraManager} AIDL interface.
 * Provides access to Vivo's popup camera manager service which controls
 * the motorized popup front-facing camera mechanism on supported Vivo devices.
 */
public class IPopupCameraManager {
    public static final Reflector TYPE = Reflector.on("vivo.app.popupcamera.IPopupCameraManager");

    /**
     * Reflection wrapper for {@code vivo.app.popupcamera.IPopupCameraManager$Stub}.
     */
    public static class Stub {
        public static final Reflector TYPE = Reflector.on("vivo.app.popupcamera.IPopupCameraManager$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the popup camera manager service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = TYPE.staticMethod("asInterface", IBinder.class);
    }
}
