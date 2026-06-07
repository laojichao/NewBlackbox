package black.android.app;

import android.app.ActivityThread;
import android.app.Application;
import android.content.Context;
import android.os.IBinder;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.Service#attach} method.
 * Provides access to the internal attach method called during service creation
 * to bind the service to its context and activity thread.
 */
public class Service {
    public static final Reflector REF = Reflector.on("android.app.Service");

    /**
     * Attaches the service to its context and activity thread during creation.
     *
     * @param context        the Context for this service
     * @param activityThread the ActivityThread managing this service
     * @param className      the class name of the service
     * @param token          the IBinder token identifying this service
     * @param application    the Application instance
     * @param activityManager the IActivityManager binder
     */
    public static Reflector.MethodWrapper<Void> attach = REF.method("attach", Context.class, ActivityThread.class, String.class, IBinder.class, Application.class, Object.class);
}
