package com.vcore.utils.compat;

import android.app.Activity;
import android.content.Intent;
import android.os.IBinder;

import black.android.app.ActivityManagerNative;
import black.android.app.IActivityManager;
import black.android.app.IActivityManagerL;
import black.android.app.IActivityManagerN;

/**
 * Compatibility wrapper for Android's internal {@code IActivityManager} API. Provides
 * version-aware implementations of activity management operations that differ across
 * Android API levels, including finishing activities and setting screen orientation.
 */
public class ActivityManagerCompat {
	/**
	 * Type for IActivityManager.getIntentSender: this PendingIntent is
	 * for a startActivity operation.
	 */
	public static final int INTENT_SENDER_ACTIVITY = 2;

	/** Start flag indicating the activity should be launched in debug mode. */
	public static final int START_FLAG_DEBUG = 1<<1;
	/** Start flag indicating native memory tracking should be enabled for the activity. */
	public static final int START_FLAG_TRACK_ALLOCATION = 1<<2;
	/** Start flag indicating native debugging should be enabled for the activity. */
	public static final int START_FLAG_NATIVE_DEBUGGING = 1<<3;

	/**
	 * Finishes an activity by calling the appropriate version-specific {@code IActivityManager}
	 * method. Dispatches to the Nougat+ or Lollipop+ implementation based on the current API level.
	 *
	 * @param token the activity's {@link IBinder} token
	 * @param code  the result code to return to the launching activity
	 * @param data  the result intent data to return to the launching activity; may be {@code null}
	 */
	public static void finishActivity(IBinder token, int code, Intent data) {
		if (BuildCompat.isN()) {
			IActivityManagerN.finishActivity.call(ActivityManagerNative.getDefault.call(), token, code, data, 0);
		} else if (BuildCompat.isL()) {
			IActivityManagerL.finishActivity.call(ActivityManagerNative.getDefault.call(), token, code, data, false);
		}
	}

    /**
     * Sets the requested screen orientation for an activity. First attempts the standard
     * {@link Activity#setRequestedOrientation(int)} method. If that fails (e.g., on Samsung
     * devices that override {@code WindowManager.setRequestedOrientation}), falls back to
     * calling {@code IActivityManager.setRequestedOrientation} directly using the activity's
     * root parent token.
     *
     * @param activity    the activity whose orientation to change
     * @param orientation the desired orientation constant (e.g.,
     *                    {@link android.content.pm.ActivityInfo#SCREEN_ORIENTATION_PORTRAIT})
     */
    public static void setActivityOrientation(Activity activity, int orientation) {
        try {
            activity.setRequestedOrientation(orientation);
        } catch (Throwable e) {
            e.printStackTrace();
            // Samsung is WindowManager.setRequestedOrientation
            Activity parent = black.android.app.Activity.mParent.get(activity);
            while (true) {
				Activity tmp = black.android.app.Activity.mParent.get(parent);
				if (tmp != null) {
					parent = tmp;
				} else {
					break;
				}
			}

            IBinder token = black.android.app.Activity.mToken.get(parent);
            try {
				IActivityManager.setRequestedOrientation.call(ActivityManagerNative.getDefault.call(), token, orientation);
			} catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }
}
