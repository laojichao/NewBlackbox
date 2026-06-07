package black.android.bluetooth;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.bluetooth.IBluetoothManager$Stub} class.
 * Provides access to the Bluetooth manager system service for controlling
 * Bluetooth adapter state and discovering paired devices.
 */
public class IBluetoothManager {
    /**
     * Reflection wrapper for {@code android.bluetooth.IBluetoothManager$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.bluetooth.IBluetoothManager$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the Bluetooth manager service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
