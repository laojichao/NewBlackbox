package com.vcore.core.system.pm;

/**
 * Monitors package installation and uninstallation events within the virtual environment.
 *
 * <p>Implementations of this interface are registered with {@link BPackageManagerService}
 * to receive callbacks when packages are installed or uninstalled, allowing system
 * components (such as {@link com.vcore.core.system.am.BroadcastManager} and
 * {@link BXposedManagerService}) to react to package state changes.</p>
 */
public interface PackageMonitor {

    /**
     * Called when a package is uninstalled from the virtual environment.
     *
     * @param packageName the package name of the uninstalled application
     * @param isRemove    true if the package was completely removed (all users),
     *                    false if only removed for a specific user
     * @param userId      the virtual user ID from which the package was uninstalled
     */
    void onPackageUninstalled(String packageName, boolean isRemove, int userId);

    /**
     * Called when a package is installed into the virtual environment.
     *
     * @param packageName the package name of the installed application
     * @param userId      the virtual user ID for which the package was installed
     */
    void onPackageInstalled(String packageName, int userId);
}
