package com.vcore.utils.compat;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import java.util.Locale;

import com.vcore.BlackBoxCore;
import com.vcore.app.BActivityThread;
import com.vcore.utils.DrawableUtils;

/**
 * Compatibility utility for fixing Android {@link ActivityManager.TaskDescription} objects
 * within the virtual framework. The task description is what appears in the recent apps
 * screen, showing the app's label and icon. This class ensures that virtual apps display
 * their own label and icon (prefixed with the user ID) rather than the host app's.
 */
public class TaskDescriptionCompat {
    /**
     * Fixes a {@link ActivityManager.TaskDescription} by replacing missing label and icon
     * with the virtual application's own label and icon. If both label and icon are already
     * set, the original task description is returned unchanged.
     *
     * @param td the task description to fix; must not be {@code null}
     * @return the fixed {@link ActivityManager.TaskDescription} with the virtual app's
     *         label and icon, or the original if both were already present
     */
    public static ActivityManager.TaskDescription fix(ActivityManager.TaskDescription td) {
        String label = td.getLabel();
        Bitmap icon = td.getIcon();

        if (label != null && icon != null) {
            return td;
        }

        label = getTaskDescriptionLabel(BActivityThread.getUserId(), getApplicationLabel());
        Drawable drawable = getApplicationIcon();
        if (drawable == null) {
            return td;
        }

        ActivityManager am = (ActivityManager) BlackBoxCore.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        int iconSize = am.getLauncherLargeIconSize();

        icon = DrawableUtils.drawableToBitmap(drawable, iconSize, iconSize);
        td = new ActivityManager.TaskDescription(label, icon, td.getPrimaryColor());
        return td;
    }

    /**
     * Generates a task description label prefixed with the virtual user ID in brackets.
     * This allows users to distinguish between multiple instances of the same app
     * running under different virtual users in the recent apps screen.
     *
     * @param userId the virtual user ID
     * @param label  the application label
     * @return a formatted string in the pattern {@code "[B<userId>]<label>"} using
     *         {@link Locale#CHINA} formatting
     */
    public static String getTaskDescriptionLabel(int userId, CharSequence label) {
        return String.format(Locale.CHINA, "[B%d]%s", userId, label);
    }

    /**
     * Retrieves the display label of the currently virtualized application.
     *
     * @return the application label, or {@code null} if the application info cannot be found
     */
    private static CharSequence getApplicationLabel() {
        try {
            PackageManager pm = BlackBoxCore.getPackageManager();
            return pm.getApplicationLabel(pm.getApplicationInfo(BActivityThread.getAppPackageName(), 0));
        } catch (PackageManager.NameNotFoundException ignore) {
            return null;
        }
    }

    /**
     * Retrieves the icon drawable of the currently virtualized application.
     *
     * @return the application icon drawable, or {@code null} if the application info cannot be found
     */
    private static Drawable getApplicationIcon() {
        try {
            return BlackBoxCore.getPackageManager().getApplicationIcon(BActivityThread.getAppPackageName());
        } catch (PackageManager.NameNotFoundException ignore) {
            return null;
        }
    }
}
