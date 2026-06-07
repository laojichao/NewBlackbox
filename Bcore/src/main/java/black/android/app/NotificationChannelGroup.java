package black.android.app;

import java.util.List;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.NotificationChannelGroup} class (Android O+).
 * Provides access to the group ID and list of channels belonging to the group.
 */
public class NotificationChannelGroup {
    public static final Reflector REF = Reflector.on("android.app.NotificationChannelGroup");

    /** The list of NotificationChannel objects belonging to this group. */
    public static Reflector.FieldWrapper<List<android.app.NotificationChannel>> mChannels = REF.field("mChannels");

    /** The unique group ID string. */
    public static Reflector.FieldWrapper<String> mId = REF.field("mId");
}
