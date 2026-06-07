package black.com.android.internal.appwidget;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code com.android.internal.appwidget.IAppWidgetService$Stub} class.
 * Provides access to the app widget service for managing home screen widgets,
 * including binding, updating, and configuring widget instances.
 */
public class IAppWidgetService {
    /**
     * Reflection wrapper for {@code com.android.internal.appwidget.IAppWidgetService$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("com.android.internal.appwidget.IAppWidgetService$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the app widget service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
