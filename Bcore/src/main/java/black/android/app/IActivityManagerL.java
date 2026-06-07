package black.android.app;

import android.content.Intent;
import android.os.IBinder;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.IActivityManager#finishActivity} method
 * variant on Android L (Lollipop, API 21+). Uses a boolean parameter to indicate
 * whether the result should be forwarded.
 */
public class IActivityManagerL {
    public static final Reflector REF = Reflector.on("android.app.IActivityManager");

    /**
     * Finishes an activity (Lollipop variant).
     *
     * @param token       the IBinder token of the activity to finish
     * @param resultCode  the result code to return
     * @param resultData  the result Intent data
     * @param finishTask  whether to finish the entire task
     * @return true if the activity was finished successfully
     */
    public static Reflector.MethodWrapper<Boolean> finishActivity = REF.method("finishActivity", IBinder.class, int.class, Intent.class, boolean.class);
}
