package black.android.content;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.content.IContentService$Stub} class.
 * Provides access to the content service which manages content observer
 * registration and content change notification dispatching.
 */
public class IContentService {
    /**
     * Reflection wrapper for {@code android.content.IContentService$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.content.IContentService$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the content service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
