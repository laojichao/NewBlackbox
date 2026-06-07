package black.android.service.persistentdata;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.service.persistentdata.IPersistentDataBlockService$Stub} class.
 * Provides access to the persistent data block service which stores data that
 * persists across factory resets (e.g., for OEM unlock state).
 */
public class IPersistentDataBlockService {
    /**
     * Reflection wrapper for {@code android.service.persistentdata.IPersistentDataBlockService$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.service.persistentdata.IPersistentDataBlockService$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the persistent data block service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
