package black.android.app;

import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.NotificationManager} class.
 * Provides access to the INotificationManager binder interface used for
 * posting, canceling, and managing notifications.
 */
public class NotificationManager {
    public static final Reflector REF = Reflector.on("android.app.NotificationManager");

    /** Static reference to the INotificationManager binder interface. */
    public static Reflector.FieldWrapper<IInterface> sService = REF.field("sService");

    /**
     * Returns the INotificationManager service binder.
     *
     * @return the INotificationManager IInterface
     */
    public static Reflector.StaticMethodWrapper<IInterface> getService = REF.staticMethod("getService");
}
