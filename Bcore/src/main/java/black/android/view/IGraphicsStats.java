package black.android.view;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.view.IGraphicsStats$Stub} class.
 * Provides access to the graphics stats service for retrieving GPU rendering
 * statistics and pipeline information.
 */
public class IGraphicsStats {
    /**
     * Reflection wrapper for {@code android.view.IGraphicsStats$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.view.IGraphicsStats$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the graphics stats service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
