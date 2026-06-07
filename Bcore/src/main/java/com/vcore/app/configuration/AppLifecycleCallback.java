package com.vcore.app.configuration;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import com.vcore.BlackBoxCore;

/**
 * Callback interface for observing virtual application lifecycle events in the BlackBox environment.
 * <p>
 * Host applications can register implementations of this class via
 * {@link BlackBoxCore#addAppLifecycleCallback(AppLifecycleCallback)} to receive notifications at
 * key points during a virtual app's lifecycle, including application creation and standard
 * Android activity lifecycle events.
 * <p>
 * This class extends {@link Application.ActivityLifecycleCallbacks} and adds three additional hooks:
 * <ul>
 *   <li>{@link #beforeCreateApplication} - called before the Application instance is created</li>
 *   <li>{@link #beforeApplicationOnCreate} - called after the Application is created but before {@code onCreate()}</li>
 *   <li>{@link #afterApplicationOnCreate} - called after the Application's {@code onCreate()} completes</li>
 * </ul>
 * All methods have empty default implementations so that subclasses need only override the events they care about.
 */
public class AppLifecycleCallback implements Application.ActivityLifecycleCallbacks {
    /**
     * Called before a virtual application's {@link Application} instance is created.
     *
     * @param packageName the package name of the virtual application
     * @param processName the process name the virtual application is running in
     * @param context     the package context for the virtual application
     * @param userId      the virtual user ID under which the app is running
     */
    public void beforeCreateApplication(String packageName, String processName, Context context, int userId) { }

    /**
     * Called after the {@link Application} instance is created but before its {@code onCreate()} method is invoked.
     *
     * @param packageName the package name of the virtual application
     * @param processName the process name the virtual application is running in
     * @param application the newly created Application instance
     * @param userId      the virtual user ID under which the app is running
     */
    public void beforeApplicationOnCreate(String packageName, String processName, Application application, int userId) {

    }

    /**
     * Called after the virtual application's {@code onCreate()} method has completed.
     *
     * @param packageName the package name of the virtual application
     * @param processName the process name the virtual application is running in
     * @param application the Application instance whose onCreate has completed
     * @param userId      the virtual user ID under which the app is running
     */
    public void afterApplicationOnCreate(String packageName, String processName, Application application, int userId) { }

    /**
     * {@inheritDoc}
     * Called when a virtual activity is created.
     */
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) { }

    /**
     * {@inheritDoc}
     * Called when a virtual activity becomes visible.
     */
    @Override
    public void onActivityStarted(Activity activity) { }

    /**
     * {@inheritDoc}
     * Called when a virtual activity gains focus.
     */
    @Override
    public void onActivityResumed(Activity activity) { }

    /**
     * {@inheritDoc}
     * Called when a virtual activity loses focus.
     */
    @Override
    public void onActivityPaused(Activity activity) { }

    /**
     * {@inheritDoc}
     * Called when a virtual activity is no longer visible.
     */
    @Override
    public void onActivityStopped(Activity activity) { }

    /**
     * {@inheritDoc}
     * Called when a virtual activity saves its instance state.
     */
    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }

    /**
     * {@inheritDoc}
     * Called when a virtual activity is destroyed.
     */
    @Override
    public void onActivityDestroyed(Activity activity) { }
}
