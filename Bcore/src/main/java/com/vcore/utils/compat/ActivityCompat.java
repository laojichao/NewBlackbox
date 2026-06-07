package com.vcore.utils.compat;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.WindowManager;

import com.vcore.app.BActivityThread;
import com.vcore.utils.ArrayUtils;
import com.vcore.utils.DrawableUtils;

/**
 * Compatibility utility for fixing Android {@link Activity} properties within the virtual
 * framework. Handles wallpaper background restoration, fullscreen flag application, and
 * task description patching so that virtual activities display correct metadata in the
 * recent tasks screen.
 */
public class ActivityCompat {
    /**
     * Applies necessary compatibility fixes to an Activity after creation. This includes:
     * <ul>
     *   <li>Restoring the wallpaper background if the window theme requests it</li>
     *   <li>Setting the fullscreen flag if the window theme requests it</li>
     *   <li>Updating the task description with the virtual app's label and icon (API 21+)</li>
     * </ul>
     *
     * @param activity the Activity instance to fix
     */
    public static void fix(Activity activity) {
        Context baseContext = activity.getBaseContext();
        try {
            TypedArray typedArray = activity.obtainStyledAttributes(ArrayUtils.toInt(black.com.android.internal.R.styleable.Window.get()));
            if (typedArray != null) {
                boolean isShowWallpaper = typedArray.getBoolean(black.com.android.internal.R.styleable.Window_windowShowWallpaper.get(), false);
                if (isShowWallpaper) {
                    activity.getWindow().setBackgroundDrawable(WallpaperManager.getInstance(activity).getDrawable());
                }

                boolean isFullscreen = typedArray.getBoolean(black.com.android.internal.R.styleable.Window_windowFullscreen.get(), false);
                if (isFullscreen) {
                    activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                }
                typedArray.recycle();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (BuildCompat.isL()) {
            Intent intent = activity.getIntent();
            ApplicationInfo applicationInfo = baseContext.getApplicationInfo();
            PackageManager packageManager = activity.getPackageManager();

            if (intent != null && activity.isTaskRoot()) {
                try {
                    String taskDescriptionLabel = TaskDescriptionCompat.getTaskDescriptionLabel(BActivityThread.getUserId(), applicationInfo.loadLabel(packageManager));

                    Bitmap icon = null;
                    Drawable activityIcon = getActivityIcon(activity);
                    if (activityIcon != null) {
                        ActivityManager activityManager = (ActivityManager) baseContext.getSystemService(Context.ACTIVITY_SERVICE);

                        int iconSize = activityManager.getLauncherLargeIconSize();
                        icon = DrawableUtils.drawableToBitmap(activityIcon, iconSize, iconSize);
                    }
                    activity.setTaskDescription(new ActivityManager.TaskDescription(taskDescriptionLabel, icon));
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Retrieves the icon drawable for the given activity. First attempts to load the
     * activity-specific icon, then falls back to the application icon.
     *
     * @param activity the activity to retrieve the icon for
     * @return the activity or application icon drawable, or {@code null} if neither can be loaded
     */
    private static Drawable getActivityIcon(Activity activity) {
        PackageManager packageManager = activity.getPackageManager();
        try {
            Drawable icon = packageManager.getActivityIcon(activity.getComponentName());
            if (icon != null) {
                return icon;
            }
        } catch (PackageManager.NameNotFoundException ignore) { }

        ApplicationInfo applicationInfo = activity.getApplicationInfo();
        return applicationInfo.loadIcon(packageManager);
    }
}