package com.vcore;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import black.android.app.ActivityThread;
import black.android.os.UserHandle;
import com.vcore.app.LauncherActivity;
import com.vcore.app.configuration.AppLifecycleCallback;
import com.vcore.app.configuration.ClientConfiguration;
import com.vcore.core.GmsCore;
import com.vcore.core.env.BEnvironment;
import com.vcore.core.system.DaemonService;
import com.vcore.core.system.ServiceManager;
import com.vcore.core.system.user.BUserHandle;
import com.vcore.core.system.user.BUserInfo;
import com.vcore.entity.pm.InstallOption;
import com.vcore.entity.pm.InstallResult;
import com.vcore.entity.pm.InstalledModule;
import com.vcore.fake.delegate.ContentProviderDelegate;
import com.vcore.fake.frameworks.BActivityManager;
import com.vcore.fake.frameworks.BJobManager;
import com.vcore.fake.frameworks.BPackageManager;
import com.vcore.fake.frameworks.BStorageManager;
import com.vcore.fake.frameworks.BUserManager;
import com.vcore.fake.frameworks.BXposedManager;
import com.vcore.fake.hook.HookManager;
import com.vcore.proxy.ProxyManifest;
import com.vcore.utils.FileUtils;
import com.vcore.utils.ShellUtils;
import com.vcore.utils.Slog;
import com.vcore.utils.compat.BuildCompat;
import com.vcore.utils.compat.BundleCompat;
import com.vcore.utils.compat.XposedParserCompat;
import com.vcore.utils.provider.ProviderCall;

/**
 * Central singleton managing the BlackBox virtual environment.
 * <p>
 * This class serves as the main entry point for the virtual engine. It handles process type detection
 * (Main, Server, BAppClient), application lifecycle initialization, package management (install/uninstall),
 * Xposed module management, GMS (Google Mobile Services) support, user management, and service binding.
 * It extends {@link ClientConfiguration} to delegate configuration decisions to the host application.
 */
@SuppressLint({"StaticFieldLeak", "NewApi"})
public class BlackBoxCore extends ClientConfiguration {
    /** Logging tag for this class. */
    public static final String TAG = "BlackBoxCore";

    /** Singleton instance of BlackBoxCore. */
    private static final BlackBoxCore sBlackBoxCore = new BlackBoxCore();
    /** Application context, set during {@link #doAttachBaseContext}. */
    private static Context sContext;
    /** The current process type (Main, Server, or BAppClient). */
    private ProcessType mProcessType;
    /** Cache of bound service binders keyed by service name. */
    private final Map<String, IBinder> mServices = new HashMap<>();
    /** Custom uncaught exception handler, if set. */
    private Thread.UncaughtExceptionHandler mExceptionHandler;
    /** Client configuration provided by the host application. */
    private ClientConfiguration mClientConfiguration;
    /** Registered app lifecycle callbacks. */
    private final List<AppLifecycleCallback> mAppLifecycleCallbacks = new ArrayList<>();
    /** Main-thread handler for posting work. */
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    /** UID of the host (outer) application process. */
    private final int mHostUid = Process.myUid();
    /** User ID of the host device user. */
    private final int mHostUserId = UserHandle.myUserId.call();

    /**
     * Returns the singleton instance of {@link BlackBoxCore}.
     *
     * @return the global BlackBoxCore instance
     */
    public static BlackBoxCore get() {
        return sBlackBoxCore;
    }

    /**
     * Returns the main-thread {@link Handler} for posting work to the UI thread.
     *
     * @return the main looper handler
     */
    public Handler getHandler() {
        return mHandler;
    }

    /**
     * Returns the system {@link PackageManager} from the application context.
     *
     * @return the host application's PackageManager
     */
    public static PackageManager getPackageManager() {
        return sContext.getPackageManager();
    }

    /**
     * Returns the package name of the host (outer) application.
     *
     * @return the host package name configured via {@link ClientConfiguration}
     */
    public static String getHostPkg() {
        return get().getHostPackageName();
    }

    /**
     * Returns the UID of the host application process.
     *
     * @return the host UID
     */
    public static int getHostUid() {
        return get().mHostUid;
    }

    /**
     * Returns the Android user ID of the host device user.
     *
     * @return the host user ID
     */
    public static int getHostUserId() {
        return get().mHostUserId;
    }

    /**
     * Returns the global application {@link Context}.
     *
     * @return the application context
     */
    public static Context getContext() {
        return sContext;
    }

    /**
     * Returns the custom uncaught exception handler, if one has been set.
     *
     * @return the exception handler, or {@code null} if not set
     */
    public Thread.UncaughtExceptionHandler getExceptionHandler() {
        return mExceptionHandler;
    }

    /**
     * Sets a custom uncaught exception handler that will be invoked before the default handler.
     *
     * @param exceptionHandler the exception handler to set
     */
    public void setExceptionHandler(Thread.UncaughtExceptionHandler exceptionHandler) {
        mExceptionHandler = exceptionHandler;
    }

    /**
     * Initializes the BlackBox core during the host application's {@code attachBaseContext} phase.
     * <p>
     * This method determines the current process type, loads the virtual environment for BApp processes,
     * optionally starts the daemon service in the server process, initializes XCrash, and sets up the hook system.
     *
     * @param context             the host application context
     * @param clientConfiguration the client configuration provided by the host; must not be {@code null}
     * @throws IllegalArgumentException if {@code clientConfiguration} is {@code null}
     */
    public void doAttachBaseContext(Context context, ClientConfiguration clientConfiguration) {
        if (clientConfiguration == null) {
            throw new IllegalArgumentException("ClientConfiguration is null!");
        }

        if (BuildCompat.isPie()) {
            HiddenApiBypass.addHiddenApiExemptions("L");
        }

        sContext = context;
        mClientConfiguration = clientConfiguration;
        initNotificationManager();

        String processName = getProcessName(getContext());
        if (processName.equals(BlackBoxCore.getHostPkg())) {
            mProcessType = ProcessType.Main;
            startLogcat();
        } else if (processName.endsWith(getContext().getString(R.string.black_box_service_name))) {
            mProcessType = ProcessType.Server;
        } else {
            mProcessType = ProcessType.BAppClient;
        }

        if (BlackBoxCore.get().isBlackProcess()) {
            BEnvironment.load();
        }

        if (isServerProcess()) {
            if (clientConfiguration.isEnableDaemonService()) {
                Intent intent = new Intent();
                intent.setClass(getContext(), DaemonService.class);
                if (BuildCompat.isOreo()) {
                    getContext().startForegroundService(intent);
                } else {
                    getContext().startService(intent);
                }
            }
        }

        xcrash.XCrash.init(context);
        HookManager.get().init();
    }

    /**
     * Called during the host application's {@code onCreate} phase.
     * <p>
     * Initializes the content provider delegate for black (virtual) processes and
     * binds the BlackBox service manager for non-server processes.
     */
    public void doCreate() {
        if (isBlackProcess()) {
            ContentProviderDelegate.init();
        }
        if (!isServerProcess()) {
            ServiceManager.initBlackManager();
        }
    }

    /**
     * Returns the current {@code ActivityThread} instance of the host process via reflection.
     *
     * @return the host process's ActivityThread object
     */
    public static Object mainThread() {
        return ActivityThread.currentActivityThread.call();
    }

    /**
     * Starts an activity in the virtual environment for the specified user.
     * <p>
     * If the launcher activity feature is enabled in configuration, it launches via {@link LauncherActivity};
     * otherwise it delegates directly to {@link BActivityManager}.
     *
     * @param intent  the intent describing the activity to start
     * @param userId  the virtual user ID to start the activity for
     */
    public void startActivity(Intent intent, int userId) {
        if (mClientConfiguration.isEnableLauncherActivity()) {
            LauncherActivity.launch(intent, userId);
        } else {
            getBActivityManager().startActivity(intent, userId);
        }
    }

    /**
     * Returns the virtual {@link BJobManager} instance for managing job scheduling in the virtual environment.
     *
     * @return the BJobManager singleton
     */
    public static BJobManager getBJobManager() {
        return BJobManager.get();
    }

    /**
     * Returns the virtual {@link BPackageManager} instance for managing package operations in the virtual environment.
     *
     * @return the BPackageManager singleton
     */
    public static BPackageManager getBPackageManager() {
        return BPackageManager.get();
    }

    /**
     * Returns the virtual {@link BActivityManager} instance for managing activities in the virtual environment.
     *
     * @return the BActivityManager singleton
     */
    public static BActivityManager getBActivityManager() {
        return BActivityManager.get();
    }

    /**
     * Returns the virtual {@link BStorageManager} instance for managing storage in the virtual environment.
     *
     * @return the BStorageManager singleton
     */
    public static BStorageManager getBStorageManager() {
        return BStorageManager.get();
    }

    /**
     * Launches the main activity of the specified package in the virtual environment.
     *
     * @param packageName the package name of the application to launch
     * @param userId      the virtual user ID
     * @return {@code true} if the launch intent was found and the activity was started, {@code false} otherwise
     */
    public boolean launchApk(String packageName, int userId) {
        Intent launchIntentForPackage = getBPackageManager().getLaunchIntentForPackage(packageName, userId);
        if (launchIntentForPackage == null) {
            return false;
        }

        startActivity(launchIntentForPackage, userId);
        return true;
    }

    /**
     * Checks whether the specified package is installed in the virtual environment for the given user.
     *
     * @param packageName the package name to check
     * @param userId      the virtual user ID
     * @return {@code true} if the package is installed, {@code false} otherwise
     */
    public boolean isInstalled(String packageName, int userId) {
        return getBPackageManager().isInstalled(packageName, userId);
    }

    /**
     * Uninstalls a package from the virtual environment for the specified user.
     *
     * @param packageName the package name to uninstall
     * @param userId      the virtual user ID
     */
    public void uninstallPackageAsUser(String packageName, int userId) {
        getBPackageManager().uninstallPackageAsUser(packageName, userId);
    }

    /**
     * Uninstalls a package from the virtual environment for all users.
     *
     * @param packageName the package name to uninstall
     */
    public void uninstallPackage(String packageName) {
        getBPackageManager().uninstallPackage(packageName);
    }

    /**
     * Installs a system package (already installed on the host device) into the virtual environment
     * for the specified user.
     *
     * @param packageName the package name of the app installed on the host device
     * @param userId      the virtual user ID to install for
     * @return the {@link InstallResult} indicating success or failure
     */
    public InstallResult installPackageAsUser(String packageName, int userId) {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(packageName, 0);
            return getBPackageManager().installPackageAsUser(packageInfo.applicationInfo.sourceDir, InstallOption.installBySystem(), userId);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return new InstallResult().installError(e.getMessage());
        }
    }

    /**
     * Installs an APK file into the virtual environment for the specified user.
     *
     * @param apk    the APK file to install
     * @param userId the virtual user ID to install for
     * @return the {@link InstallResult} indicating success or failure
     */
    public InstallResult installPackageAsUser(File apk, int userId) {
        return getBPackageManager().installPackageAsUser(apk.getAbsolutePath(), InstallOption.installByStorage(), userId);
    }

    /**
     * Installs an APK from a content URI into the virtual environment for the specified user.
     *
     * @param apk    the content URI pointing to the APK file
     * @param userId the virtual user ID to install for
     * @return the {@link InstallResult} indicating success or failure
     */
    public InstallResult installPackageAsUser(Uri apk, int userId) {
        return getBPackageManager().installPackageAsUser(apk.toString(), InstallOption.installByStorage().makeUriFile(), userId);
    }

    /**
     * Installs an Xposed module from an APK file into the virtual Xposed environment.
     *
     * @param apk the APK file of the Xposed module to install
     * @return the {@link InstallResult} indicating success or failure
     */
    public InstallResult installXPModule(File apk) {
        return getBPackageManager().installPackageAsUser(apk.getAbsolutePath(), InstallOption.installByStorage().makeXposed(), BUserHandle.USER_XPOSED);
    }

    /**
     * Installs an Xposed module from a content URI into the virtual Xposed environment.
     *
     * @param apk the content URI pointing to the Xposed module APK
     * @return the {@link InstallResult} indicating success or failure
     */
    public InstallResult installXPModule(Uri apk) {
        return getBPackageManager().installPackageAsUser(apk.toString(), InstallOption.installByStorage()
                .makeXposed()
                .makeUriFile(), BUserHandle.USER_XPOSED);
    }

    /**
     * Installs an Xposed module by its package name (must already be installed on the host device).
     *
     * @param packageName the package name of the Xposed module on the host device
     * @return the {@link InstallResult} indicating success or failure
     */
    public InstallResult installXPModule(String packageName) {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(packageName, 0);
            String path = packageInfo.applicationInfo.sourceDir;
            return getBPackageManager().installPackageAsUser(path, InstallOption.installBySystem().makeXposed(), BUserHandle.USER_XPOSED);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return new InstallResult().installError(e.getMessage());
        }
    }

    /**
     * Uninstalls an Xposed module from the virtual environment.
     *
     * @param packageName the package name of the Xposed module to uninstall
     */
    public void uninstallXPModule(String packageName) {
        uninstallPackage(packageName);
    }

    /**
     * Checks whether the Xposed framework is enabled in the virtual environment.
     *
     * @return {@code true} if Xposed is enabled, {@code false} otherwise
     */
    public boolean isXPEnable() {
        return BXposedManager.get().isXPEnable();
    }

    /**
     * Enables or disables the Xposed framework in the virtual environment.
     *
     * @param enable {@code true} to enable Xposed, {@code false} to disable
     */
    public void setXPEnable(boolean enable) {
        BXposedManager.get().setXPEnable(enable);
    }

    /**
     * Checks whether the given file is a valid Xposed module APK.
     *
     * @param file the APK file to check
     * @return {@code true} if the file is an Xposed module, {@code false} otherwise
     */
    public boolean isXposedModule(File file) {
        return XposedParserCompat.isXPModule(file.getAbsolutePath());
    }

    /**
     * Checks whether the specified Xposed module is installed in the virtual Xposed environment.
     *
     * @param packageName the package name of the Xposed module
     * @return {@code true} if the module is installed, {@code false} otherwise
     */
    public boolean isInstalledXposedModule(String packageName) {
        return isInstalled(packageName, BUserHandle.USER_XPOSED);
    }

    /**
     * Checks whether the specified Xposed module is currently enabled.
     *
     * @param packageName the package name of the Xposed module
     * @return {@code true} if the module is enabled, {@code false} otherwise
     */
    public boolean isModuleEnable(String packageName) {
        return BXposedManager.get().isModuleEnable(packageName);
    }

    /**
     * Enables or disables the specified Xposed module.
     *
     * @param packageName the package name of the Xposed module
     * @param enable      {@code true} to enable, {@code false} to disable
     */
    public void setModuleEnable(String packageName, boolean enable) {
        BXposedManager.get().setModuleEnable(packageName, enable);
    }

    /**
     * Returns the list of all installed Xposed modules in the virtual environment.
     *
     * @return list of {@link InstalledModule} objects
     */
    public List<InstalledModule> getInstalledXPModules() {
        return BXposedManager.get().getInstalledModules();
    }

    /**
     * Returns the list of installed applications in the virtual environment for the specified user.
     *
     * @param flags  additional flags to pass to the package manager query
     * @param userId the virtual user ID
     * @return list of {@link ApplicationInfo} for installed applications
     */
    public List<ApplicationInfo> getInstalledApplications(int flags, int userId) {
        return getBPackageManager().getInstalledApplications(flags, userId);
    }

    /**
     * Returns the list of installed packages in the virtual environment for the specified user.
     *
     * @param flags  additional flags to pass to the package manager query
     * @param userId the virtual user ID
     * @return list of {@link PackageInfo} for installed packages
     */
    public List<PackageInfo> getInstalledPackages(int flags, int userId) {
        return getBPackageManager().getInstalledPackages(flags, userId);
    }

    /**
     * Clears all data for the specified package in the virtual environment.
     *
     * @param packageName the package name whose data should be cleared
     * @param userId      the virtual user ID
     */
    public void clearPackage(String packageName, int userId) {
        BPackageManager.get().clearPackage(packageName, userId);
    }

    /**
     * Force-stops the specified package in the virtual environment.
     *
     * @param packageName the package name to stop
     * @param userId      the virtual user ID
     */
    public void stopPackage(String packageName, int userId) {
        BPackageManager.get().stopPackage(packageName, userId);
    }

    /**
     * Returns the list of all virtual users in the virtual environment.
     *
     * @return list of {@link BUserInfo} objects
     */
    public List<BUserInfo> getUsers() {
        return BUserManager.get().getUsers();
    }

    /**
     * Creates a new virtual user with the specified user ID.
     *
     * @param userId the user ID for the new virtual user
     * @return the created {@link BUserInfo}
     */
    public BUserInfo createUser(int userId) {
        return BUserManager.get().createUser(userId);
    }

    /**
     * Deletes the virtual user with the specified user ID.
     *
     * @param userId the user ID of the virtual user to delete
     */
    public void deleteUser(int userId) {
        BUserManager.get().deleteUser(userId);
    }

    /**
     * Returns the list of registered {@link AppLifecycleCallback} instances.
     *
     * @return the list of lifecycle callbacks
     */
    public List<AppLifecycleCallback> getAppLifecycleCallbacks() {
        return mAppLifecycleCallbacks;
    }

    /**
     * Removes a previously registered {@link AppLifecycleCallback}.
     *
     * @param appLifecycleCallback the callback to remove
     */
    public void removeAppLifecycleCallback(AppLifecycleCallback appLifecycleCallback) {
        mAppLifecycleCallbacks.remove(appLifecycleCallback);
    }

    /**
     * Registers an {@link AppLifecycleCallback} to receive virtual app lifecycle events.
     *
     * @param appLifecycleCallback the callback to add
     */
    public void addAppLifecycleCallback(AppLifecycleCallback appLifecycleCallback) {
        mAppLifecycleCallbacks.add(appLifecycleCallback);
    }

    /**
     * Checks whether Google Mobile Services (GMS) is available on the host device.
     *
     * @return {@code true} if GMS packages are installed on the host, {@code false} otherwise
     */
    public boolean isSupportGms() {
        return GmsCore.isSupportGms();
    }

    /**
     * Checks whether Google services are installed in the virtual environment for the specified user.
     *
     * @param userId the virtual user ID
     * @return {@code true} if Google services are installed, {@code false} otherwise
     */
    public boolean isInstallGms(int userId) {
        return GmsCore.isInstalledGoogleService(userId);
    }

    /**
     * Installs Google Mobile Services (GMS) into the virtual environment for the specified user.
     *
     * @param userId the virtual user ID to install GMS for
     * @return the {@link InstallResult} indicating success or failure
     */
    public InstallResult installGms(int userId) {
        return GmsCore.installGApps(userId);
    }

    /**
     * Uninstalls Google Mobile Services (GMS) from the virtual environment for the specified user.
     *
     * @param userId the virtual user ID to uninstall GMS from
     * @return {@code true} if GMS was successfully uninstalled, {@code false} otherwise
     */
    public boolean uninstallGms(int userId) {
        GmsCore.uninstallGApps(userId);
        return !GmsCore.isInstalledGoogleService(userId);
    }

    /**
     * Retrieves or creates a service binder by name from the BlackBox server process.
     * <p>
     * The binder is obtained via a ContentProvider call to the server process. Results are cached
     * in {@link #mServices} for subsequent lookups.
     *
     * @param name the service name to look up
     * @return the {@link IBinder} for the requested service
     */
    public IBinder getService(String name) {
        IBinder binder = mServices.get(name);
        if (binder != null && binder.isBinderAlive()) {
            return binder;
        }

        Bundle bundle = new Bundle();
        bundle.putString("_B_|_server_name_", name);
        Bundle vm = ProviderCall.callSafely(ProxyManifest.getBindProvider(), "VM", null, bundle);
        binder = BundleCompat.getBinder(vm, "_B_|_server_");

        Slog.d(TAG, "getService: " + name + ", " + binder);
        mServices.put(name, binder);
        return binder;
    }

    /**
     * Process type
     */
    private enum ProcessType {
        /**
         * Server process
         */
        Server,
        /**
         * Black app process
         */
        BAppClient,
        /**
         * Main process
         */
        Main,
    }

    /**
     * Checks whether the current process is a virtual app (BAppClient) process.
     *
     * @return {@code true} if running in a BAppClient process, {@code false} otherwise
     */
    public boolean isBlackProcess() {
        return mProcessType == ProcessType.BAppClient;
    }

    /**
     * Checks whether the current process is the main (host) process.
     *
     * @return {@code true} if running in the main process, {@code false} otherwise
     */
    public boolean isMainProcess() {
        return mProcessType == ProcessType.Main;
    }

    /**
     * Checks whether the current process is the BlackBox server process.
     *
     * @return {@code true} if running in the server process, {@code false} otherwise
     */
    public boolean isServerProcess() {
        return mProcessType == ProcessType.Server;
    }

    /**
     * {@inheritDoc}
     * Delegates to the host application's {@link ClientConfiguration}.
     */
    @Override
    public boolean isHideRoot() {
        return mClientConfiguration.isHideRoot();
    }

    /**
     * {@inheritDoc}
     * Delegates to the host application's {@link ClientConfiguration}.
     */
    @Override
    public boolean isHideXposed() {
        return mClientConfiguration.isHideXposed();
    }

    /**
     * {@inheritDoc}
     * Delegates to the host application's {@link ClientConfiguration}.
     */
    @Override
    public String getHostPackageName() {
        return mClientConfiguration.getHostPackageName();
    }

    /**
     * {@inheritDoc}
     * Delegates to the host application's {@link ClientConfiguration}.
     */
    @Override
    public boolean requestInstallPackage(File file, int userId) {
        return mClientConfiguration.requestInstallPackage(file, userId);
    }

    /**
     * Starts logcat capture to a file in the Downloads directory.
     * Only runs in the main process.
     */
    private void startLogcat() {
        new Thread(() -> {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), getContext().getPackageName() + "_logcat.txt");
            FileUtils.deleteDir(file);
            ShellUtils.execCommand("logcat -c", false);
            ShellUtils.execCommand("logcat -f " + file.getAbsolutePath(), false);
        }).start();
    }

    /**
     * Retrieves the current process name from the system's ActivityManager.
     *
     * @param context the application context
     * @return the name of the current process
     * @throws RuntimeException if the process name cannot be determined
     */
    private static String getProcessName(Context context) {
        int pid = Process.myPid();
        String processName = null;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
            if (info.pid == pid) {
                processName = info.processName;
                break;
            }
        }

        if (processName == null) {
            throw new RuntimeException("processName = null");
        }
        return processName;
    }

    /**
     * Checks whether the current process is running as a 64-bit process.
     *
     * @return {@code true} if the process is 64-bit, {@code false} otherwise
     */
    public static boolean is64Bit() {
        if (BuildCompat.isM()) {
            return Process.is64Bit();
        } else {
            return Build.CPU_ABI.equals("arm64-v8a");
        }
    }

    /**
     * Initializes the notification channel for the BlackBox foreground service (required on Android O+).
     */
    private void initNotificationManager() {
        NotificationManager nm = (NotificationManager) BlackBoxCore.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        String CHANNEL_ONE_ID = BlackBoxCore.getContext().getPackageName() + ".blackbox_core";
        String CHANNEL_ONE_NAME = "blackbox_core";

        if (BuildCompat.isOreo()) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ONE_ID, CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            nm.createNotificationChannel(notificationChannel);
        }
    }
}
