package com.vcore.fake.delegate;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.view.KeyEvent;
import android.view.MotionEvent;

import androidx.annotation.RequiresApi;

import com.vcore.BlackBoxCore;
import com.vcore.app.configuration.AppLifecycleCallback;

/**
 * Delegate base class that wraps an existing {@link Instrumentation} and forwards all calls to it.
 *
 * <p>This class implements the decorator pattern for {@link Instrumentation}. All method calls
 * are delegated to {@link #mBaseInstrumentation}, the original instrumentation instance.
 * Subclasses (such as {@link AppInstrumentation}) can override specific methods to inject
 * custom behavior while preserving the delegation chain.</p>
 *
 * <p>Activity lifecycle callbacks (create, start, resume, pause, stop, destroy, saveInstanceState)
 * additionally notify registered {@link AppLifecycleCallback} listeners.</p>
 *
 * @see AppInstrumentation
 * @see AppLifecycleCallback
 */
public class BaseInstrumentationDelegate extends Instrumentation {

    /** The original {@link Instrumentation} instance being delegated to. */
    protected Instrumentation mBaseInstrumentation;

    /**
     * Delegates to the base instrumentation's onCreate.
     *
     * @param arguments the arguments passed during creation
     */
    @Override
    public void onCreate(Bundle arguments) {
        mBaseInstrumentation.onCreate(arguments);
    }

    /**
     * Delegates to the base instrumentation's start.
     */
    @Override
    public void start() {
        mBaseInstrumentation.start();
    }

    /**
     * Delegates to the base instrumentation's onStart.
     */
    @Override
    public void onStart() {
        mBaseInstrumentation.onStart();
    }

    /**
     * Delegates to the base instrumentation's onException.
     *
     * @param obj the object that caused the exception
     * @param e   the exception that was thrown
     * @return {@code true} if the exception was handled
     */
    @Override
    public boolean onException(Object obj, Throwable e) {
        return mBaseInstrumentation.onException(obj, e);
    }

    /**
     * Delegates to the base instrumentation's sendStatus.
     *
     * @param resultCode the result code to send
     * @param results    the results bundle
     */
    @Override
    public void sendStatus(int resultCode, Bundle results) {
        mBaseInstrumentation.sendStatus(resultCode, results);
    }

    /**
     * Delegates to the base instrumentation's addResults.
     *
     * @param results the results bundle to add
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void addResults(Bundle results) {
        mBaseInstrumentation.addResults(results);
    }

    /**
     * Delegates to the base instrumentation's finish.
     *
     * @param resultCode the result code
     * @param results    the results bundle
     */
    @Override
    public void finish(int resultCode, Bundle results) {
        mBaseInstrumentation.finish(resultCode, results);
    }

    /**
     * Delegates to the base instrumentation's setAutomaticPerformanceSnapshots.
     */
    @Override
    public void setAutomaticPerformanceSnapshots() {
        mBaseInstrumentation.setAutomaticPerformanceSnapshots();
    }

    /**
     * Delegates to the base instrumentation's startPerformanceSnapshot.
     */
    @Override
    public void startPerformanceSnapshot() {
        mBaseInstrumentation.startPerformanceSnapshot();
    }

    /**
     * Delegates to the base instrumentation's endPerformanceSnapshot.
     */
    @Override
    public void endPerformanceSnapshot() {
        mBaseInstrumentation.endPerformanceSnapshot();
    }

    /**
     * Delegates to the base instrumentation's onDestroy.
     */
    @Override
    public void onDestroy() {
        mBaseInstrumentation.onDestroy();
    }

    /**
     * Delegates to the base instrumentation's getContext.
     *
     * @return the instrumentation context
     */
    @Override
    public Context getContext() {
        return mBaseInstrumentation.getContext();
    }

    /**
     * Delegates to the base instrumentation's getComponentName.
     *
     * @return the component name
     */
    @Override
    public ComponentName getComponentName() {
        return mBaseInstrumentation.getComponentName();
    }

    /**
     * Delegates to the base instrumentation's getTargetContext.
     *
     * @return the target context
     */
    @Override
    public Context getTargetContext() {
        return mBaseInstrumentation.getTargetContext();
    }

    /**
     * Delegates to the base instrumentation's isProfiling.
     *
     * @return {@code true} if profiling is active
     */
    @Override
    public boolean isProfiling() {
        return mBaseInstrumentation.isProfiling();
    }

    /**
     * Delegates to the base instrumentation's startProfiling.
     */
    @Override
    public void startProfiling() {
        mBaseInstrumentation.startProfiling();
    }

    /**
     * Delegates to the base instrumentation's stopProfiling.
     */
    @Override
    public void stopProfiling() {
        mBaseInstrumentation.stopProfiling();
    }

    /**
     * Delegates to the base instrumentation's setInTouchMode.
     *
     * @param inTouch {@code true} to enable touch mode
     */
    @Override
    public void setInTouchMode(boolean inTouch) {
        mBaseInstrumentation.setInTouchMode(inTouch);
    }

    /**
     * Delegates to the base instrumentation's waitForIdle.
     *
     * @param recipient the callback to run when idle
     */
    @Override
    public void waitForIdle(Runnable recipient) {
        mBaseInstrumentation.waitForIdle(recipient);
    }

    /**
     * Delegates to the base instrumentation's waitForIdleSync.
     */
    @Override
    public void waitForIdleSync() {
        mBaseInstrumentation.waitForIdleSync();
    }

    /**
     * Delegates to the base instrumentation's runOnMainSync.
     *
     * @param runner the runnable to execute on the main thread
     */
    @Override
    public void runOnMainSync(Runnable runner) {
        mBaseInstrumentation.runOnMainSync(runner);
    }

    /**
     * Delegates to the base instrumentation's startActivitySync.
     *
     * @param intent the intent to start the activity with
     * @return the started Activity
     */
    @Override
    public Activity startActivitySync(Intent intent) {
        return mBaseInstrumentation.startActivitySync(intent);
    }

    /**
     * Delegates to the base instrumentation's addMonitor.
     *
     * @param monitor the ActivityMonitor to add
     */
    @Override
    public void addMonitor(ActivityMonitor monitor) {
        mBaseInstrumentation.addMonitor(monitor);
    }

    /**
     * Delegates to the base instrumentation's addMonitor with IntentFilter.
     *
     * @param filter  the IntentFilter to match against
     * @param result  the result to return when a match is found
     * @param block   whether to block until an activity matches
     * @return the ActivityMonitor
     */
    @Override
    public ActivityMonitor addMonitor(IntentFilter filter, ActivityResult result, boolean block) {
        return mBaseInstrumentation.addMonitor(filter, result, block);
    }

    /**
     * Delegates to the base instrumentation's addMonitor with class name.
     *
     * @param cls    the activity class name to monitor
     * @param result the result to return when matched
     * @param block  whether to block until an activity matches
     * @return the ActivityMonitor
     */
    @Override
    public ActivityMonitor addMonitor(String cls, ActivityResult result, boolean block) {
        return mBaseInstrumentation.addMonitor(cls, result, block);
    }

    /**
     * Delegates to the base instrumentation's checkMonitorHit.
     *
     * @param monitor the monitor to check
     * @param minHits the minimum number of hits required
     * @return {@code true} if the monitor was hit at least minHits times
     */
    @Override
    public boolean checkMonitorHit(ActivityMonitor monitor, int minHits) {
        return mBaseInstrumentation.checkMonitorHit(monitor, minHits);
    }

    /**
     * Delegates to the base instrumentation's waitForMonitor.
     *
     * @param monitor the monitor to wait for
     * @return the Activity that hit the monitor
     */
    @Override
    public Activity waitForMonitor(ActivityMonitor monitor) {
        return mBaseInstrumentation.waitForMonitor(monitor);
    }

    /**
     * Delegates to the base instrumentation's waitForMonitorWithTimeout.
     *
     * @param monitor  the monitor to wait for
     * @param timeOut  the timeout in milliseconds
     * @return the Activity that hit the monitor, or {@code null} on timeout
     */
    @Override
    public Activity waitForMonitorWithTimeout(ActivityMonitor monitor, long timeOut) {
        return mBaseInstrumentation.waitForMonitorWithTimeout(monitor, timeOut);
    }

    /**
     * Delegates to the base instrumentation's removeMonitor.
     *
     * @param monitor the monitor to remove
     */
    @Override
    public void removeMonitor(ActivityMonitor monitor) {
        mBaseInstrumentation.removeMonitor(monitor);
    }

    /**
     * Delegates to the base instrumentation's invokeMenuActionSync.
     *
     * @param targetActivity the activity containing the menu
     * @param id             the menu item ID
     * @param flag           additional flags
     * @return {@code true} if the action was invoked successfully
     */
    @Override
    public boolean invokeMenuActionSync(Activity targetActivity, int id, int flag) {
        return mBaseInstrumentation.invokeMenuActionSync(targetActivity, id, flag);
    }

    /**
     * Delegates to the base instrumentation's invokeContextMenuAction.
     *
     * @param targetActivity the activity containing the context menu
     * @param id             the menu item ID
     * @param flag           additional flags
     * @return {@code true} if the action was invoked successfully
     */
    @Override
    public boolean invokeContextMenuAction(Activity targetActivity, int id, int flag) {
        return mBaseInstrumentation.invokeContextMenuAction(targetActivity, id, flag);
    }

    /**
     * Delegates to the base instrumentation's sendStringSync.
     *
     * @param text the string to send
     */
    @Override
    public void sendStringSync(String text) {
        mBaseInstrumentation.sendStringSync(text);
    }

    /**
     * Delegates to the base instrumentation's sendKeySync.
     *
     * @param event the key event to send
     */
    @Override
    public void sendKeySync(KeyEvent event) {
        mBaseInstrumentation.sendKeySync(event);
    }

    /**
     * Delegates to the base instrumentation's sendKeyDownUpSync.
     *
     * @param key the key code to send
     */
    @Override
    public void sendKeyDownUpSync(int key) {
        mBaseInstrumentation.sendKeyDownUpSync(key);
    }

    /**
     * Delegates to the base instrumentation's sendCharacterSync.
     *
     * @param keyCode the character key code to send
     */
    @Override
    public void sendCharacterSync(int keyCode) {
        mBaseInstrumentation.sendCharacterSync(keyCode);
    }

    /**
     * Delegates to the base instrumentation's sendPointerSync.
     *
     * @param event the pointer motion event to send
     */
    @Override
    public void sendPointerSync(MotionEvent event) {
        mBaseInstrumentation.sendPointerSync(event);
    }

    /**
     * Delegates to the base instrumentation's sendTrackballEventSync.
     *
     * @param event the trackball motion event to send
     */
    @Override
    public void sendTrackballEventSync(MotionEvent event) {
        mBaseInstrumentation.sendTrackballEventSync(event);
    }

    /**
     * Delegates to the base instrumentation's newApplication.
     *
     * @param cl        the ClassLoader to use
     * @param className the fully-qualified class name of the Application
     * @param context   the base context
     * @return the newly created Application instance
     * @throws ClassNotFoundException if the class cannot be found
     * @throws IllegalAccessException if the class or its constructor is not accessible
     * @throws InstantiationException if the class cannot be instantiated
     */
    @Override
    public Application newApplication(ClassLoader cl, String className, Context context) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        return mBaseInstrumentation.newApplication(cl, className, context);
    }

    /**
     * Delegates to the base instrumentation's callApplicationOnCreate.
     *
     * @param app the application being created
     */
    @Override
    public void callApplicationOnCreate(Application app) {
        mBaseInstrumentation.callApplicationOnCreate(app);
    }

    /**
     * Delegates to the base instrumentation's newActivity with full parameters.
     *
     * @param clazz       the Activity class
     * @param context     the context
     * @param token       the activity token
     * @param application the parent Application
     * @param intent      the launch Intent
     * @param info        the ActivityInfo
     * @param title       the activity title
     * @param parent      the parent Activity
     * @param id          the activity ID
     * @param lastNonConfigurationInstance the last non-configuration instance
     * @return the newly created Activity
     * @throws IllegalAccessException if the class or its constructor is not accessible
     * @throws InstantiationException if the class cannot be instantiated
     */
    @Override
    public Activity newActivity(Class<?> clazz, Context context, IBinder token, Application application, Intent intent, ActivityInfo info, CharSequence title, Activity parent, String id, Object lastNonConfigurationInstance) throws IllegalAccessException, InstantiationException {
        return mBaseInstrumentation.newActivity(clazz, context, token, application, intent, info, title, parent, id, lastNonConfigurationInstance);
    }

    /**
     * Delegates to the base instrumentation's newActivity with ClassLoader.
     *
     * @param cl        the ClassLoader to use
     * @param className the fully-qualified class name of the Activity
     * @param intent    the launch Intent
     * @return the newly created Activity
     * @throws ClassNotFoundException if the class cannot be found
     * @throws IllegalAccessException if the class or its constructor is not accessible
     * @throws InstantiationException if the class cannot be instantiated
     */
    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        return mBaseInstrumentation.newActivity(cl, className, intent);
    }

    /**
     * Called when an activity is created. Delegates to the base instrumentation and then
     * notifies all registered {@link AppLifecycleCallback} listeners.
     *
     * @param activity the activity being created
     * @param icicle   the saved instance state bundle
     */
    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        mBaseInstrumentation.callActivityOnCreate(activity, icicle);
        for (AppLifecycleCallback appLifecycleCallback : BlackBoxCore.get().getAppLifecycleCallbacks()) {
            appLifecycleCallback.onActivityCreated(activity, icicle);
        }
    }

    /**
     * Called when an activity is created with persistent state. Delegates to the base
     * instrumentation and then notifies all registered {@link AppLifecycleCallback} listeners.
     *
     * @param activity        the activity being created
     * @param icicle          the saved instance state bundle
     * @param persistentState the persistent state bundle
     */
    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle, PersistableBundle persistentState) {
        mBaseInstrumentation.callActivityOnCreate(activity, icicle, persistentState);
        for (AppLifecycleCallback appLifecycleCallback : BlackBoxCore.get().getAppLifecycleCallbacks()) {
            appLifecycleCallback.onActivityCreated(activity, icicle);
        }
    }

    /**
     * Called when an activity is destroyed. Delegates to the base instrumentation and then
     * notifies all registered {@link AppLifecycleCallback} listeners.
     *
     * @param activity the activity being destroyed
     */
    @Override
    public void callActivityOnDestroy(Activity activity) {
        mBaseInstrumentation.callActivityOnDestroy(activity);
        for (AppLifecycleCallback appLifecycleCallback : BlackBoxCore.get().getAppLifecycleCallbacks()) {
            appLifecycleCallback.onActivityDestroyed(activity);
        }
    }

    /**
     * Delegates to the base instrumentation's callActivityOnRestoreInstanceState.
     *
     * @param activity         the activity being restored
     * @param savedInstanceState the saved instance state bundle
     */
    @Override
    public void callActivityOnRestoreInstanceState(Activity activity, Bundle savedInstanceState) {
        mBaseInstrumentation.callActivityOnRestoreInstanceState(activity, savedInstanceState);
    }

    /**
     * Delegates to the base instrumentation's callActivityOnRestoreInstanceState with persistent state.
     *
     * @param activity           the activity being restored
     * @param savedInstanceState the saved instance state bundle
     * @param persistentState    the persistent state bundle
     */
    @Override
    public void callActivityOnRestoreInstanceState(Activity activity, Bundle savedInstanceState, PersistableBundle persistentState) {
        mBaseInstrumentation.callActivityOnRestoreInstanceState(activity, savedInstanceState, persistentState);
    }

    /**
     * Delegates to the base instrumentation's callActivityOnPostCreate.
     *
     * @param activity the activity that was created
     * @param icicle   the saved instance state bundle
     */
    @Override
    public void callActivityOnPostCreate(Activity activity, Bundle icicle) {
        mBaseInstrumentation.callActivityOnPostCreate(activity, icicle);
    }

    /**
     * Delegates to the base instrumentation's callActivityOnPostCreate with persistent state.
     *
     * @param activity        the activity that was created
     * @param icicle          the saved instance state bundle
     * @param persistentState the persistent state bundle
     */
    @Override
    public void callActivityOnPostCreate(Activity activity, Bundle icicle, PersistableBundle persistentState) {
        mBaseInstrumentation.callActivityOnPostCreate(activity, icicle, persistentState);
    }

    /**
     * Delegates to the base instrumentation's callActivityOnNewIntent.
     *
     * @param activity the activity receiving the new intent
     * @param intent   the new intent
     */
    @Override
    public void callActivityOnNewIntent(Activity activity, Intent intent) {
        mBaseInstrumentation.callActivityOnNewIntent(activity, intent);
    }

    /**
     * Called when an activity is started. Delegates to the base instrumentation and then
     * notifies all registered {@link AppLifecycleCallback} listeners.
     *
     * @param activity the activity being started
     */
    @Override
    public void callActivityOnStart(Activity activity) {
        mBaseInstrumentation.callActivityOnStart(activity);
        for (AppLifecycleCallback appLifecycleCallback : BlackBoxCore.get().getAppLifecycleCallbacks()) {
            appLifecycleCallback.onActivityStarted(activity);
        }
    }

    /**
     * Delegates to the base instrumentation's callActivityOnRestart.
     *
     * @param activity the activity being restarted
     */
    @Override
    public void callActivityOnRestart(Activity activity) {
        mBaseInstrumentation.callActivityOnRestart(activity);
    }

    /**
     * Called when an activity is resumed. Delegates to the base instrumentation and then
     * notifies all registered {@link AppLifecycleCallback} listeners.
     *
     * @param activity the activity being resumed
     */
    @Override
    public void callActivityOnResume(Activity activity) {
        mBaseInstrumentation.callActivityOnResume(activity);
        for (AppLifecycleCallback appLifecycleCallback : BlackBoxCore.get().getAppLifecycleCallbacks()) {
            appLifecycleCallback.onActivityResumed(activity);
        }
    }

    /**
     * Called when an activity is stopped. Delegates to the base instrumentation and then
     * notifies all registered {@link AppLifecycleCallback} listeners.
     *
     * @param activity the activity being stopped
     */
    @Override
    public void callActivityOnStop(Activity activity) {
        mBaseInstrumentation.callActivityOnStop(activity);
        for (AppLifecycleCallback appLifecycleCallback : BlackBoxCore.get().getAppLifecycleCallbacks()) {
            appLifecycleCallback.onActivityStopped(activity);
        }
    }

    /**
     * Delegates to the base instrumentation's callActivityOnSaveInstanceState.
     *
     * @param activity  the activity saving its state
     * @param outState  the bundle to save state into
     */
    @Override
    public void callActivityOnSaveInstanceState(Activity activity, Bundle outState) {
        mBaseInstrumentation.callActivityOnSaveInstanceState(activity, outState);
    }

    /**
     * Called when an activity saves its instance state with persistent state. Delegates to the
     * base instrumentation and then notifies all registered {@link AppLifecycleCallback} listeners.
     *
     * @param activity            the activity saving its state
     * @param outState            the bundle to save state into
     * @param outPersistentState  the persistent state bundle
     */
    @Override
    public void callActivityOnSaveInstanceState(Activity activity, Bundle outState, PersistableBundle outPersistentState) {
        mBaseInstrumentation.callActivityOnSaveInstanceState(activity, outState, outPersistentState);
        for (AppLifecycleCallback appLifecycleCallback : BlackBoxCore.get().getAppLifecycleCallbacks()) {
            appLifecycleCallback.onActivitySaveInstanceState(activity, outState);
        }
    }

    /**
     * Called when an activity is paused. Delegates to the base instrumentation and then
     * notifies all registered {@link AppLifecycleCallback} listeners.
     *
     * @param activity the activity being paused
     */
    @Override
    public void callActivityOnPause(Activity activity) {
        mBaseInstrumentation.callActivityOnPause(activity);
        for (AppLifecycleCallback appLifecycleCallback : BlackBoxCore.get().getAppLifecycleCallbacks()) {
            appLifecycleCallback.onActivityPaused(activity);
        }
    }

    /**
     * Delegates to the base instrumentation's callActivityOnUserLeaving.
     *
     * @param activity the activity the user is leaving
     */
    @Override
    public void callActivityOnUserLeaving(Activity activity) {
        mBaseInstrumentation.callActivityOnUserLeaving(activity);
    }

    /**
     * Delegates to the base instrumentation's startAllocCounting.
     */
    @Override
    public void startAllocCounting() {
        mBaseInstrumentation.startAllocCounting();
    }

    /**
     * Delegates to the base instrumentation's stopAllocCounting.
     */
    @Override
    public void stopAllocCounting() {
        mBaseInstrumentation.stopAllocCounting();
    }

    /**
     * Delegates to the base instrumentation's getAllocCounts.
     *
     * @return a Bundle containing allocation counts
     */
    @Override
    public Bundle getAllocCounts() {
        return mBaseInstrumentation.getAllocCounts();
    }

    /**
     * Delegates to the base instrumentation's getBinderCounts.
     *
     * @return a Bundle containing binder counts
     */
    @Override
    public Bundle getBinderCounts() {
        return mBaseInstrumentation.getBinderCounts();
    }

    /**
     * Delegates to the base instrumentation's getUiAutomation.
     *
     * @return the UiAutomation instance
     */
    @Override
    public UiAutomation getUiAutomation() {
        return mBaseInstrumentation.getUiAutomation();
    }
}
