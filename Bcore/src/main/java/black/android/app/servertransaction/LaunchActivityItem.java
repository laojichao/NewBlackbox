package black.android.app.servertransaction;

import android.content.Intent;
import android.content.pm.ActivityInfo;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.servertransaction.LaunchActivityItem} class.
 * This transaction item triggers activity launch on the client side and contains
 * the Intent and ActivityInfo needed to create the activity.
 */
public class LaunchActivityItem {
    public static final Reflector REF = Reflector.on("android.app.servertransaction.LaunchActivityItem");

    /** The ActivityInfo describing the activity to launch. */
    public static Reflector.FieldWrapper<ActivityInfo> mInfo = REF.field("mInfo");

    /** The Intent used to launch the activity. */
    public static Reflector.FieldWrapper<Intent> mIntent = REF.field("mIntent");
}
