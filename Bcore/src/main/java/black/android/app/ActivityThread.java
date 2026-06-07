package black.android.app;

import android.app.Activity;
import android.app.Application;
import android.app.ContentProviderHolder;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;

import java.util.List;
import java.util.Map;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.ActivityThread} class.
 * ActivityThread is the main thread entry point for an Android application process.
 * This wrapper provides access to private fields and methods used for hooking into
 * the application lifecycle, content provider installation, and activity management.
 */
public class ActivityThread {
    public static final Reflector REF = Reflector.on("android.app.ActivityThread");

    /** Static reference to the IPackageManager binder interface. */
    public static Reflector.FieldWrapper<IInterface> sPackageManager = REF.field("sPackageManager");

    /** Static reference to the IPermissionManager binder interface. */
    public static Reflector.FieldWrapper<IInterface> sPermissionManager = REF.field("sPermissionManager");

    /** Map of activity tokens to ActivityClientRecord objects. */
    public static Reflector.FieldWrapper<Map<IBinder, Object>> mActivities = REF.field("mActivities");

    /** The AppBindData bound to this application thread. */
    public static Reflector.FieldWrapper<Object> mBoundApplication = REF.field("mBoundApplication");

    /** The main Handler for this activity thread. */
    public static Reflector.FieldWrapper<Handler> mH = REF.field("mH");

    /** The initial Application instance. */
    public static Reflector.FieldWrapper<Application> mInitialApplication = REF.field("mInitialApplication");

    /** The Instrumentation instance for this activity thread. */
    public static Reflector.FieldWrapper<Instrumentation> mInstrumentation = REF.field("mInstrumentation");

    /** Map of authority strings to ProviderClientRecord objects for content providers. */
    public static Reflector.FieldWrapper<Map<?, ?>> mProviderMap = REF.field("mProviderMap");

    /**
     * Returns the current ActivityThread instance for this process.
     */
    public static Reflector.StaticMethodWrapper<Object> currentActivityThread = REF.staticMethod("currentActivityThread");

    /**
     * Returns the IApplicationThread binder for this activity thread.
     *
     * @return the IBinder representing the application thread
     */
    public static Reflector.MethodWrapper<IBinder> getApplicationThread = REF.method("getApplicationThread");

    /**
     * Returns the system-level Context for this application.
     *
     * @return the system Context object
     */
    public static Reflector.MethodWrapper<Object> getSystemContext = REF.method("getSystemContext");

    /**
     * Returns the ActivityClientRecord for the activity being launched with the given token.
     *
     * @param token the IBinder token of the launching activity
     * @return the launching ActivityClientRecord
     */
    public static Reflector.MethodWrapper<Object> getLaunchingActivity = REF.method("getLaunchingActivity", IBinder.class);

    /**
     * Delivers new intents to the activity identified by the given token.
     *
     * @param token      the IBinder token of the target activity
     * @param intents    the list of new Intent objects
     */
    public static Reflector.MethodWrapper<Void> performNewIntents = REF.method("performNewIntents", IBinder.class, List.class);

    /**
     * Installs a content provider into this activity thread.
     *
     * @param context      the Context
     * @param holder       the ContentProviderHolder
     * @param info         the ProviderInfo
     * @param noisy        whether to log noisy warnings
     * @param noRelease    whether to skip release
     * @param stable       whether this is a stable provider
     */
    public static Reflector.MethodWrapper<Void> installProvider = REF.method("installProvider", Context.class, ContentProviderHolder.class, ProviderInfo.class, boolean.class, boolean.class, boolean.class);

    /**
     * Reflection wrapper for {@code android.app.ActivityThread$CreateServiceData}.
     * Contains data passed when creating a new service.
     */
    public static class CreateServiceData {
        public static final Reflector REF = Reflector.on("android.app.ActivityThread$CreateServiceData");

        /** The ServiceInfo for the service being created. */
        public static Reflector.FieldWrapper<ServiceInfo> info = REF.field("info");
    }

    /**
     * Reflection wrapper for {@code android.app.ActivityThread$H} message constants.
     * The H class is the main Handler that processes lifecycle messages.
     */
    public static class H {
        public static final Reflector REF = Reflector.on("android.app.ActivityThread$H");

        /** Message code for creating a service. */
        public static Reflector.FieldWrapper<Integer> CREATE_SERVICE = REF.field("CREATE_SERVICE");

        /** Message code for executing a ClientTransaction. */
        public static Reflector.FieldWrapper<Integer> EXECUTE_TRANSACTION = REF.field("EXECUTE_TRANSACTION");

        /** Message code for launching an activity. */
        public static Reflector.FieldWrapper<Integer> LAUNCH_ACTIVITY = REF.field("LAUNCH_ACTIVITY");
    }

    /**
     * Reflection wrapper for {@code android.app.ActivityThread$AppBindData}.
     * Contains data about the application binding configuration.
     */
    public static class AppBindData {
        public static final Reflector REF = Reflector.on("android.app.ActivityThread$AppBindData");

        /** The ApplicationInfo for the bound application. */
        public static Reflector.FieldWrapper<ApplicationInfo> appInfo = REF.field("appInfo");

        /** The LoadedApk info object. */
        public static Reflector.FieldWrapper<Object> info = REF.field("info");

        /** The ComponentName of the instrumentation, if any. */
        public static Reflector.FieldWrapper<ComponentName> instrumentationName = REF.field("instrumentationName");

        /** The process name for this application. */
        public static Reflector.FieldWrapper<String> processName = REF.field("processName");

        /** The list of content provider ProviderInfo objects to install. */
        public static Reflector.FieldWrapper<List<ProviderInfo>> providers = REF.field("providers");
    }

    /**
     * Reflection wrapper for {@code android.app.ActivityThread$ProviderClientRecord}.
     * Holds the client-side reference to an installed content provider.
     */
    public static class ProviderClientRecordP {
        public static final Reflector REF = Reflector.on("android.app.ActivityThread$ProviderClientRecord");

        /** The authority names registered for this provider. */
        public static Reflector.FieldWrapper<String[]> mNames = REF.field("mNames");

        /** The IInterface proxy for the content provider. */
        public static Reflector.FieldWrapper<IInterface> mProvider = REF.field("mProvider");
    }

    /**
     * Reflection wrapper for {@code android.app.ActivityThread$ActivityClientRecord}.
     * Contains per-activity state tracked by the client side of the activity lifecycle.
     */
    public static class ActivityClientRecord {
        public static final Reflector REF = Reflector.on("android.app.ActivityThread$ActivityClientRecord");

        /** The Activity instance. */
        public static Reflector.FieldWrapper<Activity> activity = REF.field("activity");

        /** The ActivityInfo for this activity. */
        public static Reflector.FieldWrapper<ActivityInfo> activityInfo = REF.field("activityInfo");

        /** The Intent that launched this activity. */
        public static Reflector.FieldWrapper<Intent> intent = REF.field("intent");

        /** The IBinder token for this activity. */
        public static Reflector.FieldWrapper<IBinder> token = REF.field("token");

        /** The LoadedApk (package info) for this activity. */
        public static Reflector.FieldWrapper<Object> packageInfo = REF.field("packageInfo");
    }

    /**
     * Reflection wrapper for {@code android.app.ActivityThread$AndroidOs}.
     * An inner class within ActivityThread related to OS-level operations.
     */
    public static class AndroidOs {
        public static final Reflector REF = Reflector.on("android.app.ActivityThread$AndroidOs");
    }
}
