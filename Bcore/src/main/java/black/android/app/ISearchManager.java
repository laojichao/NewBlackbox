package black.android.app;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.ISearchManager} AIDL interface.
 * Provides access to the search manager system service used for global and in-app search.
 */
public class ISearchManager {
    public static final Reflector REF = Reflector.on("android.app.ISearchManager");

    /**
     * Reflection wrapper for {@code android.app.ISearchManager$Stub}.
     * Provides access to the static asInterface factory method.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.app.ISearchManager$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the search manager service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
