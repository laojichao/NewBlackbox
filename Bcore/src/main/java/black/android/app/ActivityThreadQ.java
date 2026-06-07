package black.android.app;

import android.os.IBinder;

import java.util.List;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.ActivityThread#handleNewIntent} method
 * variant on Android Q (API 29+). This version handles new intent delivery differently
 * from the earlier performNewIntents approach.
 */
public class ActivityThreadQ {
    public static final Reflector REF = Reflector.on("android.app.ActivityThread");

    /**
     * Handles delivery of new intents to an activity (Q+ variant).
     *
     * @param token   the IBinder token of the target activity
     * @param intents the list of new Intent objects
     */
    public static Reflector.MethodWrapper<Void> handleNewIntent = REF.method("handleNewIntent", IBinder.class, List.class);
}
