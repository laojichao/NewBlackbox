package black.oem.vivo;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code vivo.app.physicalfling.IPhysicalFlingManager} AIDL interface.
 * Provides access to Vivo's proprietary physical fling manager service which
 * controls physics-based fling scrolling behavior on Vivo devices.
 */
public class IPhysicalFlingManager {
    public static final Reflector TYPE = Reflector.on("vivo.app.physicalfling.IPhysicalFlingManager");

    /**
     * Reflection wrapper for {@code vivo.app.physicalfling.IPhysicalFlingManager$Stub}.
     */
    public static class Stub {
        public static final Reflector TYPE = Reflector.on("vivo.app.physicalfling.IPhysicalFlingManager$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the physical fling manager service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = TYPE.staticMethod("asInterface", IBinder.class);
    }
}
