package com.vcore.core;

import android.content.pm.PackageManager;

import java.util.HashSet;
import java.util.Set;

import com.vcore.BlackBoxCore;
import com.vcore.entity.pm.InstallResult;

/**
 * Manages the installation and uninstallation of Google Mobile Services (GMS) packages
 * within the BlackBox virtual environment.
 * <p>
 * This class maintains two sets of package names: Google service packages (GMS core, GSF,
 * backup transport, etc.) and Google application packages (Play Store, Play Games, Wear OS, etc.).
 * It provides methods to install/uninstall these packages for a given virtual user and to check
 * whether GMS is available on the host device or installed in the virtual environment.
 */
public class GmsCore {
    /** Set of Google application package names. */
    private static final HashSet<String> GOOGLE_APP = new HashSet<>();
    /** Set of Google service package names (must be installed before apps). */
    private static final HashSet<String> GOOGLE_SERVICE = new HashSet<>();
    /** Package name of Google Play Services. */
    public static final String GMS_PKG = "com.google.android.gms";
    /** Package name of Google Services Framework. */
    public static final String GSF_PKG = "com.google.android.gsf";
    /** Package name of Google Play Store. */
    public static final String VENDING_PKG = "com.android.vending";

    static {
        GOOGLE_APP.add(VENDING_PKG);
        GOOGLE_APP.add("com.google.android.play.games");
        GOOGLE_APP.add("com.google.android.wearable.app");
        GOOGLE_APP.add("com.google.android.wearable.app.cn");

        // GMS must install at first
        GOOGLE_SERVICE.add(GMS_PKG);
        GOOGLE_SERVICE.add(GSF_PKG);
        GOOGLE_SERVICE.add("com.google.android.gsf.login");
        GOOGLE_SERVICE.add("com.google.android.backuptransport");
        GOOGLE_SERVICE.add("com.google.android.backup");
        GOOGLE_SERVICE.add("com.google.android.configupdater");
        GOOGLE_SERVICE.add("com.google.android.syncadapters.contacts");
        GOOGLE_SERVICE.add("com.google.android.feedback");
        GOOGLE_SERVICE.add("com.google.android.onetimeinitializer");
        GOOGLE_SERVICE.add("com.google.android.partnersetup");
        GOOGLE_SERVICE.add("com.google.android.setupwizard");
        GOOGLE_SERVICE.add("com.google.android.syncadapters.calendar");
    }

    /**
     * Checks whether the given package name belongs to a known Google app or service.
     *
     * @param str the package name to check
     * @return {@code true} if the package is a Google app or service, {@code false} otherwise
     */
    public static boolean isGoogleAppOrService(String str) {
        return GOOGLE_APP.contains(str) || GOOGLE_SERVICE.contains(str);
    }

    /**
     * Installs a set of packages into the virtual environment for the specified user.
     * <p>
     * Packages that are already installed or not available on the host device are skipped.
     * If any package fails to install, the process stops and the failure result is returned.
     *
     * @param list   the set of package names to install
     * @param userId the virtual user ID to install for
     * @return an {@link InstallResult} indicating success or the first failure
     */
    private static InstallResult installPackages(Set<String> list, int userId) {
        BlackBoxCore blackBoxCore = BlackBoxCore.get();
        for (String packageName : list) {
            if (blackBoxCore.isInstalled(packageName, userId)) {
                continue;
            }

            try {
                BlackBoxCore.getContext().getPackageManager().getApplicationInfo(packageName, 0);
            } catch (PackageManager.NameNotFoundException ignored) {
                continue;
            }

            InstallResult installResult = blackBoxCore.installPackageAsUser(packageName, userId);
            if (!installResult.success) {
                return installResult;
            }
        }
        return new InstallResult();
    }

    /**
     * Uninstalls a set of packages from the virtual environment for the specified user.
     *
     * @param list   the set of package names to uninstall
     * @param userId the virtual user ID to uninstall from
     */
    private static void uninstallPackages(Set<String> list, int userId) {
        BlackBoxCore blackBoxCore = BlackBoxCore.get();
        for (String packageName : list) {
            blackBoxCore.uninstallPackageAsUser(packageName, userId);
        }
    }

    /**
     * Installs all Google services and applications into the virtual environment for the specified user.
     * <p>
     * Google services are installed first (GMS, GSF, etc.), followed by Google applications (Play Store, etc.).
     * If installation fails at any point, all previously installed Google packages are uninstalled to
     * ensure a consistent state.
     *
     * @param userId the virtual user ID to install GMS for
     * @return an {@link InstallResult} indicating success or the failure details
     */
    public static InstallResult installGApps(int userId) {
        Set<String> googleApps = new HashSet<>();

        googleApps.addAll(GOOGLE_SERVICE);
        googleApps.addAll(GOOGLE_APP);

        InstallResult installResult = installPackages(googleApps, userId);
        if (!installResult.success) {
            uninstallGApps(userId);
            return installResult;
        }
        return installResult;
    }

    /**
     * Uninstalls all Google services and applications from the virtual environment for the specified user.
     *
     * @param userId the virtual user ID to uninstall GMS from
     */
    public static void uninstallGApps(int userId) {
        uninstallPackages(GOOGLE_SERVICE, userId);
        uninstallPackages(GOOGLE_APP, userId);
    }

    /**
     * Removes a package name from the known Google app and service sets.
     *
     * @param packageName the package name to remove
     */
    public static void remove(String packageName) {
        GOOGLE_SERVICE.remove(packageName);
        GOOGLE_APP.remove(packageName);
    }

    /**
     * Checks whether Google Play Services (GMS) is installed on the host device.
     *
     * @return {@code true} if GMS is available on the host, {@code false} otherwise
     */
    public static boolean isSupportGms() {
        try {
            BlackBoxCore.getPackageManager().getPackageInfo(GMS_PKG, 0);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) { }
        return false;
    }

    /**
     * Checks whether Google Play Services is installed in the virtual environment for the specified user.
     *
     * @param userId the virtual user ID to check
     * @return {@code true} if GMS is installed in the virtual environment, {@code false} otherwise
     */
    public static boolean isInstalledGoogleService(int userId) {
        return BlackBoxCore.get().isInstalled(GMS_PKG, userId);
    }
}
