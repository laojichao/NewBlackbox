package black.android.media.session;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.media.session.ISessionManager$Stub} class.
 * Provides access to the media session manager service for managing media playback
 * sessions and dispatching media button events.
 */
public class ISessionManager {
    /**
     * Reflection wrapper for {@code android.media.session.ISessionManager$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.media.session.ISessionManager$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the session manager service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
