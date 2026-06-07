package black.android.app.usage;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.usage.IStorageStatsManager$Stub} class.
 * Provides access to the storage stats manager service for querying storage
 * usage statistics per package, user, or UUID.
 */
public class IStorageStatsManager {
    /**
     * Reflection wrapper for {@code android.app.usage.IStorageStatsManager$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.app.usage.IStorageStatsManager$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the storage stats manager service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
