package black.android.app;

import android.app.Application;
import android.app.Instrumentation;
//import android.content.IIntentReceiver;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;

import java.lang.ref.WeakReference;

import black.Reflector;
import black.android.content.IIntentReceiver;

/**
 * Reflection wrapper for the hidden {@code android.app.LoadedApk} class.
 * LoadedApk represents a loaded APK and holds per-package data including
 * the class loader, application info, and service/receiver dispatchers.
 */
public class LoadedApk {

    public static final Reflector REF = Reflector.on("android.app.LoadedApk");

    /** The ApplicationInfo for this loaded APK. */
    public static Reflector.FieldWrapper<ApplicationInfo> mApplicationInfo = REF.field("mApplicationInfo");

    /** Whether a security violation was detected for this APK. */
    public static Reflector.FieldWrapper<Boolean> mSecurityViolation = REF.field("mSecurityViolation");

    /**
     * Returns the ClassLoader for this loaded APK.
     *
     * @return the ClassLoader instance
     */
    public static Reflector.MethodWrapper<ClassLoader> getClassLoader = REF.method("getClassLoader");

    /**
     * Creates and returns the Application instance for this loaded APK.
     *
     * @param reportFully whether to report fully drawn
     * @param instrumentation the Instrumentation instance, or null
     * @return the Application instance
     */
    public static Reflector.MethodWrapper<Application> makeApplication = REF.method("makeApplication", boolean.class, Instrumentation.class);

    /**
     * Reflection wrapper for {@code android.app.LoadedApk$ServiceDispatcher}.
     * Dispatches service connection callbacks to the registered ServiceConnection.
     */
    public static class ServiceDispatcher {
        public static final Reflector REF = Reflector.on("android.app.LoadedApk$ServiceDispatcher");

        /** The ServiceConnection registered with this dispatcher. */
        public static Reflector.FieldWrapper<ServiceConnection> mConnection = REF.field("mConnection");

        /**
         * Reflection wrapper for {@code android.app.LoadedApk$ServiceDispatcher$InnerConnection}.
         * The inner Binder stub that receives connection callbacks from the system.
         */
        public static class InnerConnection {
            public static final Reflector REF = Reflector.on("android.app.LoadedApk$ServiceDispatcher$InnerConnection");

            /** WeakReference back to the owning ServiceDispatcher. */
            public static Reflector.FieldWrapper<WeakReference<?>> mDispatcher = REF.field("mDispatcher");
        }
    }

    /**
     * Reflection wrapper for {@code android.app.LoadedApk$ReceiverDispatcher}.
     * Dispatches broadcast intents to the registered BroadcastReceiver.
     */
    public static class ReceiverDispatcher {
        public static final Reflector REF = Reflector.on("android.app.LoadedApk$ReceiverDispatcher");

      /** The IIntentReceiver binder stub for receiving broadcasts. */
      public static Reflector.FieldWrapper<IIntentReceiver> mIIntentReceiver = REF.field("mIIntentReceiver");

        /**
         * Reflection wrapper for {@code android.app.LoadedApk$ReceiverDispatcher$InnerReceiver}.
         * The inner Binder stub that receives broadcast intents from the system.
         */
        public static class InnerReceiver {
            public static final Reflector REF = Reflector.on("android.app.LoadedApk$ReceiverDispatcher$InnerReceiver");

            /** WeakReference back to the owning ReceiverDispatcher. */
            public static Reflector.FieldWrapper<WeakReference<?>> mDispatcher = REF.field("mDispatcher");
        }
    }
}
