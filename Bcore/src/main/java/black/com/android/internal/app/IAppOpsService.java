package black.com.android.internal.app;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code com.android.internal.app.IAppOpsService$Stub} class.
 * Provides access to the AppOps (Application Operations) service for monitoring
 * and controlling application permissions at a fine-grained level.
 */
public class IAppOpsService {
    /**
     * Reflection wrapper for {@code com.android.internal.app.IAppOpsService$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("com.android.internal.app.IAppOpsService$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the AppOps service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
