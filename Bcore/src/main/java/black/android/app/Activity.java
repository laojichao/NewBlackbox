package black.android.app;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.IBinder;

import black.Reflector;

/**
 * Reflection wrapper for hidden fields in {@code android.app.Activity}.
 * Provides access to private/internal fields of the Activity class that are not
 * part of the public SDK, such as activity info, result data, and window token.
 */
public class Activity {
    public static final Reflector REF = Reflector.on("android.app.Activity");

    /** The {@link ActivityInfo} associated with this activity. */
    public static Reflector.FieldWrapper<ActivityInfo> mActivityInfo = REF.field("mActivityInfo");

    /** Whether this activity has finished. */
    public static Reflector.FieldWrapper<Boolean> mFinished = REF.field("mFinished");

    /** The parent activity of this activity, if embedded. */
    public static Reflector.FieldWrapper<android.app.Activity> mParent = REF.field("mParent");

    /** The result code set by this activity (e.g., {@code RESULT_OK}). */
    public static Reflector.FieldWrapper<Integer> mResultCode = REF.field("mResultCode");

    /** The result data {@link Intent} returned by this activity. */
    public static Reflector.FieldWrapper<Intent> mResultData = REF.field("mResultData");

    /** The IBinder token identifying this activity in the activity manager. */
    public static Reflector.FieldWrapper<IBinder> mToken = REF.field("mToken");
}
