package black.android.app;

import black.Reflector;

/**
 * Reflection wrapper for hidden fields in {@code android.app.Notification} on Android O (Oreo, API 26+).
 * Provides access to the channel ID and group key fields introduced with notification channels.
 */
public class NotificationO {
    public static final Reflector REF = Reflector.on("android.app.Notification");

    /** The notification channel ID this notification was posted to. */
    public static Reflector.FieldWrapper<String> mChannelId = REF.field("mChannelId");

    /** The group key for grouped notifications. */
    public static Reflector.FieldWrapper<String> mGroupKey = REF.field("mGroupKey");
}
