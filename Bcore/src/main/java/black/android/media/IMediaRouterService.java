package black.android.media;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.media.IMediaRouterService$Stub} class.
 * Provides access to the media router service for discovering and selecting
 * media output routes (e.g., Bluetooth, Cast, wired).
 */
public class IMediaRouterService {
    /**
     * Reflection wrapper for {@code android.media.IMediaRouterService$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.media.IMediaRouterService$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the media router service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
