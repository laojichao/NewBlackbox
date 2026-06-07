package black.android.app;

import android.content.Intent;
import android.os.IBinder;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.IActivityManager#finishActivity} method
 * variant on Android N (Nougat, API 24+). Uses an int flags parameter instead of a boolean.
 */
public class IActivityManagerN {
    public static final Reflector REF = Reflector.on("android.app.IActivityManager");

    /**
     * Finishes an activity (Nougat variant).
     *
     * @param token       the IBinder token of the activity to finish
     * @param resultCode  the result code to return
     * @param resultData  the result Intent data
     * @param flags       finish flags (e.g., finishing-task indicator)
     * @return true if the activity was finished successfully
     */
    public static Reflector.MethodWrapper<Boolean> finishActivity = REF.method("finishActivity", IBinder.class, int.class, Intent.class, int.class);
}
