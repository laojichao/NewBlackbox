package black.android.content.pm;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.content.pm.ILauncherApps$Stub} class.
 * Provides access to the launcher apps service used for querying and managing
 * activities and shortcuts visible in the device launcher.
 */
public class ILauncherApps {
    /**
     * Reflection wrapper for {@code android.content.pm.ILauncherApps$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.content.pm.ILauncherApps$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the launcher apps service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
