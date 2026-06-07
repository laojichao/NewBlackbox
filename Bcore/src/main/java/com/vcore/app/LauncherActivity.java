package com.vcore.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.List;

import com.vcore.BlackBoxCore;
import com.vcore.R;
import com.vcore.core.system.user.BUserHandle;
import com.vcore.fake.frameworks.BActivityManager;
import com.vcore.utils.Slog;

/**
 * Transparent launcher activity that displays a splash screen while a virtual application starts.
 * <p>
 * This activity acts as a proxy entry point for launching virtual apps. It displays the app icon
 * on a launcher layout, starts the actual virtual activity on a background thread, and automatically
 * finishes itself once the virtual activity has resumed. The lifecycle flow is:
 * <ol>
 *   <li>{@link #onCreate} extracts the target intent, package name, and user ID from extras</li>
 *   <li>Displays the app icon and launches the virtual activity in the background</li>
 *   <li>{@link #onPause} marks that the activity has been paused (the virtual activity appeared)</li>
 *   <li>{@link #onResume} finishes this activity if it was previously paused</li>
 * </ol>
 */
public class LauncherActivity extends Activity {
    /** Logging tag for this class. */
    public static final String TAG = "SplashScreen";

    /** Extra key for the target launch {@link Intent}. */
    public static final String KEY_INTENT = "launch_intent";
    /** Extra key for the target package name. */
    public static final String KEY_PKG = "launch_pkg";
    /** Extra key for the virtual user ID. */
    public static final String KEY_USER_ID = "launch_user_id";
    /** Flag indicating this activity has been paused at least once. */
    private boolean isRunning = false;
    /** Flag indicating the activity was not running (unused, reserved). */
    private boolean UnRunning = false;

    /**
     * Convenience method to launch the {@link LauncherActivity} for a given virtual app intent.
     *
     * @param intent the target intent describing the virtual activity to launch
     * @param userId the virtual user ID to launch the activity for
     */
    public static void launch(Intent intent, int userId) {
        Intent splash = new Intent();
        splash.setClass(BlackBoxCore.getContext(), LauncherActivity.class);
        splash.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        splash.putExtra(LauncherActivity.KEY_INTENT, intent);
        splash.putExtra(LauncherActivity.KEY_PKG, intent.getPackage());
        splash.putExtra(LauncherActivity.KEY_USER_ID, userId);
        BlackBoxCore.getContext().startActivity(splash);
    }

    /**
     * Called when the activity is first created. Extracts the launch intent, package name, and user ID
     * from the incoming intent extras. Loads the app icon and starts the virtual activity on a background thread.
     * Finishes immediately if the intent is missing or the package is not installed.
     *
     * @param savedInstanceState the saved instance state bundle, or {@code null} if not restoring
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //这里解决多进程问题
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        Intent launchIntent = intent.getParcelableExtra(KEY_INTENT);
        String packageName = intent.getStringExtra(KEY_PKG);
        int userId = intent.getIntExtra(KEY_USER_ID, 0);

        PackageInfo packageInfo = BlackBoxCore.getBPackageManager().getPackageInfo(packageName, 0, userId);
        if (packageInfo == null) {
            Slog.e(TAG, packageName + " not installed!");
            finish();
            return;
        }
        Drawable drawable = packageInfo.applicationInfo.loadIcon(BlackBoxCore.getPackageManager());
        setContentView(R.layout.activity_launcher);

        findViewById(R.id.iv_icon)
                .setBackground(drawable);
        new Thread(() -> BlackBoxCore.getBActivityManager().startActivity(launchIntent, userId)).start();
    }

    /**
     * Called when the activity is paused. Sets a flag indicating this activity has moved to the background,
     * which signals that the virtual activity is likely now in the foreground.
     */
    @Override
    protected void onPause() {
        super.onPause();
        isRunning = true;
    }

    /**
     * Called when the activity resumes. If this activity was previously paused (meaning the virtual
     * activity has appeared), this activity finishes itself automatically.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (isRunning) {
            finish();
        }
    }
}
