package black.android.os.storage;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.os.storage.IStorageManager$Stub} class.
 * This is the modern (Oreo+) storage manager service for managing storage volumes,
 * disk quotas, and storage-related operations.
 */
public class IStorageManager {
    /**
     * Reflection wrapper for {@code android.os.storage.IStorageManager$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.os.storage.IStorageManager$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the storage manager service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
