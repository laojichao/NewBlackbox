package com.vcore.app.configuration;

import java.io.File;

/**
 * Abstract configuration class that host applications must extend to customize the behavior of the
 * BlackBox virtual engine.
 * <p>
 * Host applications provide an instance of this class when calling
 * {@link com.vcore.BlackBoxCore#doAttachBaseContext}. The methods in this class control features such as
 * root/Xposed hiding, daemon service startup, launcher activity behavior, and install request handling.
 * <p>
 * All methods have sensible default implementations. Override only the methods whose behavior you want
 * to change.
 */
public abstract class ClientConfiguration {
    /**
     * Returns whether root-related artifacts should be hidden from virtual applications.
     * <p>
     * When {@code true}, paths to {@code su} binaries and Superuser APKs are redirected to
     * non-existent files, preventing apps from detecting root access.
     *
     * @return {@code true} to hide root, {@code false} to allow detection; default is {@code false}
     */
    public boolean isHideRoot() {
        return false;
    }

    /**
     * Returns whether Xposed framework artifacts should be hidden from virtual applications.
     * <p>
     * When {@code true}, Xposed installer packages and related indicators are hidden to prevent
     * apps from detecting the Xposed framework.
     *
     * @return {@code true} to hide Xposed, {@code false} to allow detection; default is {@code false}
     */
    public boolean isHideXposed() {
        return false;
    }

    /**
     * Returns the package name of the host (outer) application.
     * <p>
     * This value is used by the virtual engine to identify the host process and to construct
     * virtual process names.
     *
     * @return the host application's package name; must not be {@code null}
     */
    public abstract String getHostPackageName();

    /**
     * Returns whether the BlackBox daemon service should be started in the server process.
     * <p>
     * The daemon service keeps the server process alive as a foreground service, ensuring
     * virtual apps continue running even when the host app is killed.
     *
     * @return {@code true} to enable the daemon service, {@code false} to disable; default is {@code true}
     */
    public boolean isEnableDaemonService() {
        return true;
    }

    /**
     * Returns whether the {@link com.vcore.app.LauncherActivity} splash screen should be used
     * when launching virtual activities.
     * <p>
     * When enabled, a transparent activity with the app icon is displayed briefly while the
     * virtual activity starts in the background.
     *
     * @return {@code true} to enable the launcher activity, {@code false} to start directly; default is {@code true}
     */
    public boolean isEnableLauncherActivity() {
        return true;
    }

    /**
     * Called when an internal (virtual) application requests to install a new application.
     * <p>
     * The host application can intercept this request, for example by displaying a confirmation
     * dialog or delegating to its own install flow.
     *
     * @param file   the APK file that the virtual application is requesting to install
     * @param userId the virtual user ID that initiated the install request
     * @return {@code true} if the host handled the install request, {@code false} to proceed with
     *         the default installation; default is {@code false}
     */
    public boolean requestInstallPackage(File file, int userId) {
        return false;
    }
}
