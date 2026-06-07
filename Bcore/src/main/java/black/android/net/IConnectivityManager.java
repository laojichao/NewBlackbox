package black.android.net;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.net.IConnectivityManager$Stub} class.
 * Provides access to the connectivity manager system service for managing network
 * connections, registering network callbacks, and querying network state.
 */
public class IConnectivityManager {
    /**
     * Reflection wrapper for {@code android.net.IConnectivityManager$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.net.IConnectivityManager$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the connectivity manager service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
