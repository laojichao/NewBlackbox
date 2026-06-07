package black.android.app;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.NotificationChannel} class (Android O+).
 * Provides access to the private channel ID field used for notification categorization.
 */
public class NotificationChannel {
    public static final Reflector REF = Reflector.on("android.app.NotificationChannel");

    /** The unique channel ID string. */
    public static Reflector.FieldWrapper<String> mId = REF.field("mId");
}
