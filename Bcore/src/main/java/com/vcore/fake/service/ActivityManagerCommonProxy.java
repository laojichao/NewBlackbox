package com.vcore.fake.service;

import static android.content.pm.PackageManager.GET_META_DATA;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;

import java.io.File;
import java.lang.reflect.Method;

import com.vcore.BlackBoxCore;
import com.vcore.app.BActivityThread;
import com.vcore.fake.hook.MethodHook;
import com.vcore.fake.hook.ProxyMethod;
import com.vcore.fake.provider.FileProviderHandler;
import com.vcore.utils.ComponentUtils;
import com.vcore.utils.MethodParameterUtils;
import com.vcore.utils.Slog;
import com.vcore.utils.compat.BuildCompat;

/**
 * Proxy container for common IActivityManager methods that intercept activity lifecycle operations.
 * Provides hook implementations for startActivity, startActivities, activityResumed,
 * activityDestroyed, finishActivity, getAppTasks, getCallingPackage, and getCallingActivity
 * within the virtual environment.
 */
public class ActivityManagerCommonProxy {
    public static final String TAG = "ActivityManagerCommonProxy";

    /**
     * Intercepts the {@code startActivity} call to redirect activity launches through
     * the virtual environment's activity manager, handling package resolution and
     * intent rewriting for the virtual space.
     */
    @ProxyMethod("startActivity")
    public static class StartActivity extends MethodHook {

        /**
         * Hooks the startActivity call to resolve the target activity within the virtual
         * environment, handling package install requests and redirecting to the virtual activity manager.
         *
         * @param who    the original object being hooked
         * @param method the original method being intercepted
         * @param args   the method arguments containing callingPackage, Intent, etc.
         * @return the result of the intercepted method call
         * @throws Throwable if the underlying method invocation fails
         */
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            Intent intent = getIntent(args);

            Slog.d(TAG, "Hook in : " + intent);
            assert intent != null;
            if (intent.getParcelableExtra("_B_|_target_") != null) {
                return method.invoke(who, args);
            }

            if (ComponentUtils.isRequestInstall(intent)) {
                File file = FileProviderHandler.convertFile(BActivityThread.getApplication(), intent.getData());
                if (BlackBoxCore.get().requestInstallPackage(file, BActivityThread.getUserId())) {
                    return 0;
                }

                intent.setData(FileProviderHandler.convertFileUri(BActivityThread.getApplication(), intent.getData()));
                return method.invoke(who, args);
            }

            String dataString = intent.getDataString();
            if (dataString != null && dataString.equals("package:" + BActivityThread.getAppPackageName())) {
                intent.setData(Uri.parse("package:" + BlackBoxCore.getHostPkg()));
            }

            ResolveInfo resolveInfo = BlackBoxCore.getBPackageManager().resolveActivity(intent, GET_META_DATA, getResolvedType(args),
                    BActivityThread.getUserId());
            if (resolveInfo == null) {
                String origPackage = intent.getPackage();
                if (intent.getPackage() == null && intent.getComponent() == null) {
                    intent.setPackage(BActivityThread.getAppPackageName());
                } else {
                    origPackage = intent.getPackage();
                }

                resolveInfo = BlackBoxCore.getBPackageManager().resolveActivity(intent, GET_META_DATA, getResolvedType(args),
                        BActivityThread.getUserId());
                if (resolveInfo == null) {
                    intent.setPackage(origPackage);
                    return method.invoke(who, args);
                }
            }

            intent.setExtrasClassLoader(who.getClass().getClassLoader());
            intent.setComponent(new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name));
            BlackBoxCore.getBActivityManager().startActivityAms(BActivityThread.getUserId(), getIntent(args),
                    getResolvedType(args), getResultTo(args), getResultWho(args),
                    getRequestCode(args), getFlags(args), getOptions(args));
            return 0;
        }

        /**
         * Extracts the Intent from the method arguments, accounting for Android version differences.
         *
         * @param args the method arguments
         * @return the Intent found in the arguments, or null if not found
         */
        private Intent getIntent(Object[] args) {
            int index;
            if (BuildCompat.isR()) {
                index = 3;
            } else {
                index = 2;
            }

            if (args[index] instanceof Intent) {
                return (Intent) args[index];
            }

            for (Object arg : args) {
                if (arg instanceof Intent) {
                    return (Intent) arg;
                }
            }
            return null;
        }

        /**
         * Extracts the resolved MIME type string from the method arguments.
         *
         * @param args the method arguments
         * @return the resolved type string, or null if not found
         */
        private String getResolvedType(Object[] args) {
            int index;
            if (BuildCompat.isR()) {
                index = 4;
            } else {
                index = 3;
            }

            if (args[index] instanceof String) {
                return (String) args[index];
            }

            for (Object arg : args) {
                if (arg instanceof String) {
                    return (String) arg;
                }
            }
            return null;
        }

        /**
         * Extracts the resultTo IBinder token from the method arguments.
         *
         * @param args the method arguments
         * @return the IBinder token, or null if not found
         */
        private IBinder getResultTo(Object[] args) {
            int index;
            if (BuildCompat.isR()) {
                index = 5;
            } else {
                index = 4;
            }

            if (args[index] instanceof IBinder) {
                return (IBinder) args[index];
            }

            for (Object arg : args) {
                if (arg instanceof IBinder) {
                    return (IBinder) arg;
                }
            }
            return null;
        }

        /**
         * Extracts the resultWho string from the method arguments.
         *
         * @param args the method arguments
         * @return the resultWho string, or null if not found
         */
        private String getResultWho(Object[] args) {
            int index;
            if (BuildCompat.isR()) {
                index = 6;
            } else {
                index = 5;
            }

            if (args[index] instanceof String) {
                return (String) args[index];
            }

            for (Object arg : args) {
                if (arg instanceof String) {
                    return (String) arg;
                }
            }
            return null;
        }

        /**
         * Extracts the request code from the method arguments.
         *
         * @param args the method arguments
         * @return the request code, or 0 if not found
         */
        private int getRequestCode(Object[] args) {
            int index;
            if (BuildCompat.isR()) {
                index = 7;
            } else {
                index = 6;
            }

            if (args[index] instanceof Integer) {
                return (Integer) args[index];
            }

            for (Object arg : args) {
                if (arg instanceof Integer) {
                    return (Integer) arg;
                }
            }
            return 0;
        }

        /**
         * Extracts the intent flags from the method arguments.
         *
         * @param args the method arguments
         * @return the flags value, or 0 if not found
         */
        private int getFlags(Object[] args) {
            int index;
            if (BuildCompat.isR()) {
                index = 8;
            } else {
                index = 7;
            }

            if (args[index] instanceof Integer) {
                return (Integer) args[index];
            }

            for (Object arg : args) {
                if (arg instanceof Integer) {
                    return (Integer) arg;
                }
            }
            return 0;
        }

        /**
         * Extracts the activity launch options Bundle from the method arguments.
         *
         * @param args the method arguments
         * @return the options Bundle, or null if not found
         */
        private Bundle getOptions(Object[] args) {
            int index;
            if (BuildCompat.isR()) {
                index = 9;
            } else {
                index = 8;
            }

            if (args[index] instanceof Bundle) {
                return (Bundle) args[index];
            }

            for (Object arg : args) {
                if (arg instanceof Bundle) {
                    return (Bundle) arg;
                }
            }
            return null;
        }
    }

    /**
     * Intercepts the {@code startActivities} call to redirect batch activity launches
     * through the virtual environment's activity manager.
     */
    @ProxyMethod("startActivities")
    public static class StartActivities extends MethodHook {

        /**
         * Hooks the startActivities call, forwarding the intents through the virtual
         * activity manager if they target self packages.
         *
         * @param who    the original object being hooked
         * @param method the original method being intercepted
         * @param args   the method arguments containing intents, resolvedTypes, IBinder, and Bundle
         * @return the result of the intercepted method call
         * @throws Throwable if the underlying method invocation fails
         */
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int index = getIntents();
            Intent[] intents = (Intent[]) args[index++];
            String[] resolvedTypes = (String[]) args[index++];
            IBinder resultTo = (IBinder) args[index++];
            Bundle options = (Bundle) args[index];

            if (!ComponentUtils.isSelf(intents)) {
                return method.invoke(who, args);
            }

            for (Intent intent : intents) {
                intent.setExtrasClassLoader(who.getClass().getClassLoader());
            }
            return BlackBoxCore.getBActivityManager().startActivities(BActivityThread.getUserId(), intents, resolvedTypes, resultTo, options);
        }

        /**
         * Returns the argument index where the Intent array is located.
         *
         * @return the index of the intents argument
         */
        public int getIntents() {
            return 2;
        }
    }

    /**
     * Intercepts the {@code activityResumed} call to notify the virtual activity manager
     * when an activity resumes.
     */
    @ProxyMethod("activityResumed")
    public static class ActivityResumed extends MethodHook {

        /**
         * Hooks the activityResumed call to notify the virtual activity manager.
         *
         * @param who    the original object being hooked
         * @param method the original method being intercepted
         * @param args   the method arguments, where args[0] is the activity token IBinder
         * @return the result of the original method invocation
         * @throws Throwable if the underlying method invocation fails
         */
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            BlackBoxCore.getBActivityManager().onActivityResumed((IBinder) args[0]);
            return method.invoke(who, args);
        }
    }

    /**
     * Intercepts the {@code activityDestroyed} call to notify the virtual activity manager
     * when an activity is destroyed.
     */
    @ProxyMethod("activityDestroyed")
    public static class ActivityDestroyed extends MethodHook {

        /**
         * Hooks the activityDestroyed call to notify the virtual activity manager.
         *
         * @param who    the original object being hooked
         * @param method the original method being intercepted
         * @param args   the method arguments, where args[0] is the activity token IBinder
         * @return the result of the original method invocation
         * @throws Throwable if the underlying method invocation fails
         */
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            BlackBoxCore.getBActivityManager().onActivityDestroyed((IBinder) args[0]);
            return method.invoke(who, args);
        }
    }

    /**
     * Intercepts the {@code finishActivity} call to notify the virtual activity manager
     * when an activity finishes.
     */
    @ProxyMethod("finishActivity")
    public static class FinishActivity extends MethodHook {

        /**
         * Hooks the finishActivity call to notify the virtual activity manager.
         *
         * @param who    the original object being hooked
         * @param method the original method being intercepted
         * @param args   the method arguments, where args[0] is the activity token IBinder
         * @return the result of the original method invocation
         * @throws Throwable if the underlying method invocation fails
         */
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            BlackBoxCore.getBActivityManager().onFinishActivity((IBinder) args[0]);
            return method.invoke(who, args);
        }
    }

    /**
     * Intercepts the {@code getAppTasks} call to replace the calling package name
     * with the virtual app package name.
     */
    @ProxyMethod("getAppTasks")
    public static class GetAppTasks extends MethodHook {

        /**
         * Hooks the getAppTasks call, replacing the first package argument with the virtual app package.
         *
         * @param who    the original object being hooked
         * @param method the original method being intercepted
         * @param args   the method arguments
         * @return the result of the original method invocation
         * @throws Throwable if the underlying method invocation fails
         */
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }

    /**
     * Intercepts the {@code getCallingPackage} call to return the virtual app's
     * calling package name instead of the host package.
     */
    @ProxyMethod("getCallingPackage")
    public static class GetCallingPackage extends MethodHook {

        /**
         * Hooks the getCallingPackage call to return the virtual calling package.
         *
         * @param who    the original object being hooked
         * @param method the original method being intercepted
         * @param args   the method arguments, where args[0] is the activity token IBinder
         * @return the virtual calling package name
         * @throws Throwable if the underlying method invocation fails
         */
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return BlackBoxCore.getBActivityManager().getCallingPackage((IBinder) args[0], BActivityThread.getUserId());
        }
    }

    /**
     * Intercepts the {@code getCallingActivity} call to return the virtual app's
     * calling activity ComponentName instead of the host activity.
     */
    @ProxyMethod("getCallingActivity")
    public static class GetCallingActivity extends MethodHook {

        /**
         * Hooks the getCallingActivity call to return the virtual calling activity.
         *
         * @param who    the original object being hooked
         * @param method the original method being intercepted
         * @param args   the method arguments, where args[0] is the activity token IBinder
         * @return the virtual calling activity ComponentName
         * @throws Throwable if the underlying method invocation fails
         */
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return BlackBoxCore.getBActivityManager().getCallingActivity((IBinder) args[0], BActivityThread.getUserId());
        }
    }
}
