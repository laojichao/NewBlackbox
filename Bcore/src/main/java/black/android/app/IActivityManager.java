package black.android.app;

import android.content.Intent;
import android.content.pm.ProviderInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.IActivityManager} AIDL interface.
 * Provides access to activity management methods such as starting activities, finishing
 * activities, and querying task information. This is the older (pre-Q) API surface.
 */
public class IActivityManager {
    public static final Reflector REF = Reflector.on("android.app.IActivityManager");

    /**
     * Returns the task ID for the activity associated with the given token.
     *
     * @param token    the IBinder token of the activity
     * @param onlyRoot whether to only return the task if the activity is the root
     * @return the task ID, or -1 if not found
     */
    public static Reflector.MethodWrapper<Integer> getTaskForActivity = REF.method("getTaskForActivity", IBinder.class, boolean.class);

    /**
     * Sets the requested orientation for the activity identified by the given token.
     *
     * @param token       the IBinder token of the activity
     * @param orientation the requested screen orientation constant
     */
    public static Reflector.MethodWrapper<Void> setRequestedOrientation = REF.method("setRequestedOrientation", IBinder.class, int.class);

    /**
     * Starts an activity through the activity manager.
     *
     * @param caller       the IApplicationThread of the calling application
     * @param callingPackage the package name of the caller
     * @param intent       the Intent describing the activity to start
     * @param resolvedType the MIME type of the intent
     * @param resultTo     the IBinder token of the activity that should receive the result
     * @param resultWho    the result target identifier
     * @param requestCode  the request code for the result
     * @param flags        start flags
     * @param profilerInfo profiler information, or null
     * @param options      additional options Bundle
     * @return the result code from the activity manager
     */
    public static Reflector.MethodWrapper<Integer> startActivity = REF.method("startActivity", Reflector.findClass("android.app.IApplicationThread"), String.class, Intent.class, String.class, IBinder.class, String.class, int.class, int.class, Reflector.findClass("android.app.ProfilerInfo"), Bundle.class);

    /**
     * Reflection wrapper for {@code android.app.IActivityManager$ContentProviderHolder}.
     * Holds a reference to a content provider and its metadata.
     */
    public static class ContentProviderHolder {
        public static final Reflector REF = Reflector.on("android.app.IActivityManager$ContentProviderHolder");

        /** The ProviderInfo describing this content provider. */
        public static Reflector.FieldWrapper<ProviderInfo> info = REF.field("info");

        /** The IInterface proxy for the content provider. */
        public static Reflector.FieldWrapper<IInterface> provider = REF.field("provider");
    }
}
