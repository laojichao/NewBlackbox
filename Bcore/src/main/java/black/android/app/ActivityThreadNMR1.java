package black.android.app;

import android.os.IBinder;

import java.util.List;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.ActivityThread#performNewIntents} method
 * variant on Android N MR1 (API 25+). This version includes an additional boolean parameter
 * for indicating whether the activity was already in a resumed state.
 */
public class ActivityThreadNMR1 {
    public static final Reflector REF = Reflector.on("android.app.ActivityThread");

    /**
     * Delivers new intents to an activity (N MR1 variant with resumed flag).
     *
     * @param token      the IBinder token of the target activity
     * @param intents    the list of new Intent objects
     * @param andResume  whether the activity should be resumed after receiving intents
     */
    public static Reflector.MethodWrapper<Void> performNewIntents = REF.method("performNewIntents", IBinder.class, List.class, boolean.class);
}
