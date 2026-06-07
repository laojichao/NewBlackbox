package black.oem.vivo;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code vivo.app.systemdefence.ISystemDefenceManager} AIDL interface.
 * Provides access to Vivo's system defence manager service which manages
 * system-level security protections and anti-tampering mechanisms on Vivo devices.
 */
public class ISystemDefenceManager {
    public static final Reflector TYPE = Reflector.on("vivo.app.systemdefence.ISystemDefenceManager");

    /**
     * Reflection wrapper for {@code vivo.app.systemdefence.ISystemDefenceManager$Stub}.
     */
    public static class Stub {
        public static final Reflector TYPE = Reflector.on("vivo.app.systemdefence.ISystemDefenceManager$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the system defence manager service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = TYPE.staticMethod("asInterface", IBinder.class);
    }
}
