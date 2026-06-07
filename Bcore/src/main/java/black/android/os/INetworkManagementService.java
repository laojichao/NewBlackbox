package black.android.os;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.os.INetworkManagementService$Stub} class.
 * Provides access to the network management service for low-level network
 * configuration such as firewall rules, interface management, and traffic stats.
 */
public class INetworkManagementService {
    /**
     * Reflection wrapper for {@code android.os.INetworkManagementService$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.os.INetworkManagementService$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the network management service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
