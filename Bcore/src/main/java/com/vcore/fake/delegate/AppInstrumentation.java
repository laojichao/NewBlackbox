package com.vcore.fake.delegate;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import java.lang.reflect.Field;

import black.Reflector;
import black.android.app.ActivityThread;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import com.vcore.BlackBoxCore;
import com.vcore.app.BActivityThread;
import com.vcore.fake.hook.HookManager;
import com.vcore.fake.hook.IInjectHook;
import com.vcore.fake.service.HCallbackProxy;
import com.vcore.fake.service.IActivityClientProxy;
import com.vcore.utils.compat.ActivityCompat;
import com.vcore.utils.compat.ActivityManagerCompat;
import com.vcore.utils.compat.BuildCompat;
import com.vcore.utils.compat.ContextCompat;

/**
 * Custom {@link Instrumentation} implementation that intercepts Activity and Application
 * lifecycle events within the virtual environment.
 *
 * <p>This class replaces the system's default {@link Instrumentation} in the {@link ActivityThread}
 * to hook into application creation, activity creation, and other lifecycle callbacks. It ensures
 * that virtual environment context fixes (theme, orientation, etc.) are applied before activities
 * are displayed, and that the environment's hook state is verified before proceeding.</p>
 *
 * <p>Uses the singleton holder pattern for thread-safe lazy initialization.</p>
 *
 * @see BaseInstrumentationDelegate
 * @see IInjectHook
 */
public final class AppInstrumentation extends BaseInstrumentationDelegate implements IInjectHook {
    private static final String TAG = AppInstrumentation.class.getSimpleName();

    /** Thread-safe lazy singleton holder. */
    private static final class SAppInstrumentationHolder {
        static final AppInstrumentation sAppInstrumentation = new AppInstrumentation();
    }

    /**
     * Returns the singleton instance of {@link AppInstrumentation}.
     *
     * @return the global AppInstrumentation instance
     */
    public static AppInstrumentation get() {
        return SAppInstrumentationHolder.sAppInstrumentation;
    }

    /**
     * Default constructor.
     */
    public AppInstrumentation() { }

    /**
     * Injects this instrumentation into the current {@link ActivityThread} by replacing
     * the system's default {@link Instrumentation} instance. Skips injection if already
     * injected or if another instrumentation has already been properly wrapped.
     */
    @Override
    public void injectHook() {
        try {
            Instrumentation mInstrumentation = getCurrInstrumentation();
            if (mInstrumentation == this || checkInstrumentation(mInstrumentation)) {
                return;
            }
            mBaseInstrumentation = mInstrumentation;
            ActivityThread.mInstrumentation.set(BlackBoxCore.mainThread(), this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the current {@link Instrumentation} instance from the active {@link ActivityThread}.
     *
     * @return the current Instrumentation, or {@code null} if unavailable
     */
    private Instrumentation getCurrInstrumentation() {
        Object currentActivityThread = BlackBoxCore.mainThread();
        return ActivityThread.mInstrumentation.get(currentActivityThread);
    }

    /**
     * Checks whether the hook environment is invalid by verifying the current instrumentation
     * is properly wrapped.
     *
     * @return {@code true} if re-injection is needed
     */
    @Override
    public boolean isBadEnv() {
        return !checkInstrumentation(getCurrInstrumentation());
    }

    /**
     * Verifies that the given instrumentation has this {@link AppInstrumentation} instance
     * properly embedded, either directly or within a field of a wrapper class.
     *
     * @param instrumentation the instrumentation to check
     * @return {@code true} if the instrumentation is properly configured
     */
    private boolean checkInstrumentation(Instrumentation instrumentation) {
        if (instrumentation instanceof AppInstrumentation) {
            return true;
        }

        Class<?> clazz = instrumentation.getClass();
        if (Instrumentation.class.equals(clazz)) {
            return false;
        }

        do {
            assert clazz != null;
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (Instrumentation.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    try {
                        Object obj = field.get(instrumentation);
                        if ((obj instanceof AppInstrumentation)) {
                            return true;
                        }
                    } catch (Exception e) {
                        return false;
                    }
                }
            }
            clazz = clazz.getSuperclass();
        } while (!Instrumentation.class.equals(clazz));
        return false;
    }

    /**
     * Ensures the HCallback hook environment is valid.
     */
    private void checkHCallback() {
        HookManager.get().checkEnv(HCallbackProxy.class);
    }

    /**
     * Performs pre-activity creation checks including HCallback verification, context fixes,
     * and theme/orientation application based on the activity's {@link ActivityInfo}.
     *
     * @param activity the activity being created
     */
    private void checkActivity(Activity activity) {
        Log.d(TAG, "callActivityOnCreate: " + activity.getClass().getName());
        checkHCallback();
        HookManager.get().checkEnv(IActivityClientProxy.class);

        ActivityInfo info = black.android.app.Activity.mActivityInfo.get(activity);
        ContextCompat.fix(activity);
        ActivityCompat.fix(activity);
        if (info.theme != 0) {
            activity.getTheme().applyStyle(info.theme, true);
        }
        ActivityManagerCompat.setActivityOrientation(activity, info.screenOrientation);
    }

    /**
     * Creates a new {@link Application} instance with virtual environment context fixes applied.
     *
     * @param cl        the ClassLoader to use
     * @param className the fully-qualified class name of the Application
     * @param context   the base context
     * @return the newly created Application instance
     * @throws InstantiationException if the class cannot be instantiated
     * @throws IllegalAccessException if the class or its constructor is not accessible
     * @throws ClassNotFoundException if the class cannot be found
     */
    @Override
    public Application newApplication(ClassLoader cl, String className, Context context) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        ContextCompat.fix(context);
        //BActivityThread.currentActivityThread().loadXposed(context);
        return super.newApplication(cl, className, context);
    }

    /**
     * Called when an activity is created with a persistent state bundle.
     * Performs environment checks before delegating to the base implementation.
     *
     * @param activity        the activity being created
     * @param icicle          the saved instance state bundle
     * @param persistentState the persistent state bundle
     */
    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle, PersistableBundle persistentState) {
        checkActivity(activity);
        super.callActivityOnCreate(activity, icicle, persistentState);
    }

    /**
     * Called when an activity is created. Performs environment checks before
     * delegating to the base implementation.
     *
     * @param activity the activity being created
     * @param icicle   the saved instance state bundle
     */
    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        checkActivity(activity);
        super.callActivityOnCreate(activity, icicle);
    }

    /**
     * Called when the application is created. Ensures HCallback environment is valid
     * before delegating to the base implementation.
     *
     * @param app the application being created
     */
    @Override
    public void callApplicationOnCreate(Application app) {
        checkHCallback();
        super.callApplicationOnCreate(app);
    }

    /**
     * Creates a new {@link Activity} instance. Falls back to the base instrumentation
     * if the class is not found through the proxied class loader.
     *
     * @param cl        the ClassLoader to use
     * @param className the fully-qualified class name of the Activity
     * @param intent    the Intent that started the activity
     * @return the newly created Activity instance
     * @throws InstantiationException if the class cannot be instantiated
     * @throws IllegalAccessException if the class or its constructor is not accessible
     * @throws ClassNotFoundException if the class cannot be found
     */
    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        try {
            return super.newActivity(cl, className, intent);
        } catch (ClassNotFoundException e) {
            return mBaseInstrumentation.newActivity(cl, className, intent);
        }
    }
}
