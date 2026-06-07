package black.oem.vivo;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code vivo.app.superresolution.ISuperResolutionManager} AIDL interface.
 * Provides access to Vivo's super resolution manager service which enhances
 * image and video resolution using AI-based upscaling on supported Vivo devices.
 */
public class ISuperResolutionManager {
    public static final Reflector TYPE = Reflector.on("vivo.app.superresolution.ISuperResolutionManager");

    /**
     * Reflection wrapper for {@code vivo.app.superresolution.ISuperResolutionManager$Stub}.
     */
    public static class Stub {
        public static final Reflector TYPE = Reflector.on("vivo.app.superresolution.ISuperResolutionManager$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the super resolution manager service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = TYPE.staticMethod("asInterface", IBinder.class);
    }
}
