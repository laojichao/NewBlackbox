package com.vcore.core.env;

import java.io.File;
import java.util.Locale;

import com.vcore.BlackBoxCore;
import com.vcore.app.BActivityThread;
import com.vcore.utils.FileUtils;

/**
 * Provides the virtual file system directory structure for the BlackBox virtual environment.
 * <p>
 * This class defines and manages all virtual directories used by the engine, mirroring the
 * standard Android directory layout under a private {@code blackbox} root. Directories include:
 * <ul>
 *   <li><b>App data:</b> per-user, per-package data, cache, database, and lib directories</li>
 *   <li><b>External storage:</b> per-user external files and cache directories</li>
 *   <li><b>System:</b> configuration files for users, accounts, UIDs, Xposed modules, etc.</li>
 *   <li><b>Process:</b> virtual {@code /proc} entries for PID spoofing</li>
 *   <li><b>App install:</b> installed APK and native library directories</li>
 * </ul>
 * The root directory is {@code <app_cache_dir>/../blackbox/} on internal storage and
 * {@code <app_external_files>/blackbox/} on external storage.
 */
public class BEnvironment {
    /** Root directory of the virtual file system on internal storage. */
    private static final File sVirtualRoot = new File(BlackBoxCore.getContext().getCacheDir().getParent(), "blackbox");
    /** Root directory of the virtual file system on external storage. */
    private static final File sExternalVirtualRoot = BlackBoxCore.getContext().getExternalFilesDir("blackbox");

    /**
     * Creates the core virtual directories if they do not already exist.
     * Called during virtual process initialization.
     */
    public static void load() {
        FileUtils.mkdirs(sVirtualRoot);
        FileUtils.mkdirs(sExternalVirtualRoot);
        FileUtils.mkdirs(getSystemDir());
        FileUtils.mkdirs(getCacheDir());
        FileUtils.mkdirs(getProcDir());
    }

    /**
     * Returns the root directory of the virtual file system on internal storage.
     *
     * @return the virtual root {@link File}
     */
    public static File getVirtualRoot() {
        return sVirtualRoot;
    }

    /**
     * Returns the root directory of the virtual file system on external storage.
     *
     * @return the external virtual root {@link File}
     */
    public static File getExternalVirtualRoot() {
        return sExternalVirtualRoot;
    }

    /**
     * Returns the virtual system directory for configuration files.
     *
     * @return the system directory {@link File}
     */
    public static File getSystemDir() {
        return new File(sVirtualRoot, "system");
    }

    /**
     * Returns the virtual proc directory for PID spoofing files.
     *
     * @return the proc directory {@link File}
     */
    public static File getProcDir() {
        return new File(sVirtualRoot, "proc");
    }

    /**
     * Returns the virtual cache directory.
     *
     * @return the cache directory {@link File}
     */
    public static File getCacheDir() {
        return new File(sVirtualRoot, "cache");
    }

    /**
     * Returns the virtual user information configuration file.
     *
     * @return the user.conf {@link File}
     */
    public static File getUserInfoConf() {
        return new File(getSystemDir(), "user.conf");
    }

    /**
     * Returns the virtual accounts configuration file.
     *
     * @return the accounts.conf {@link File}
     */
    public static File getAccountsConf() {
        return new File(getSystemDir(), "accounts.conf");
    }

    /**
     * Returns the virtual UID allocation configuration file.
     *
     * @return the uid.conf {@link File}
     */
    public static File getUidConf() {
        return new File(getSystemDir(), "uid.conf");
    }

    /**
     * Returns the shared user configuration file.
     *
     * @return the shared-user.conf {@link File}
     */
    public static File getSharedUserConf() {
        return new File(getSystemDir(), "shared-user.conf");
    }

    /**
     * Returns the Xposed module configuration file.
     *
     * @return the xposed-module.conf {@link File}
     */
    public static File getXPModuleConf() {
        return new File(getSystemDir(), "xposed-module.conf");
    }

    /**
     * Returns the fake location configuration file.
     *
     * @return the fake-location.conf {@link File}
     */
    public static File getFakeLocationConf() {
        return new File(getSystemDir(), "fake-location.conf");
    }

    /**
     * Returns the fake device configuration file.
     *
     * @return the fake-device.conf {@link File}
     */
    public static File getFakeDeviceConf() {
        return new File(getSystemDir(), "fake-device.conf");
    }

    /**
     * Returns the package-specific configuration file for the given package.
     *
     * @param packageName the virtual package name
     * @return the package.conf {@link File}
     */
    public static File getPackageConf(String packageName) {
        return new File(getAppDir(packageName), "package.conf");
    }

    /**
     * Returns the external storage directory for the specified virtual user.
     *
     * @param userId the virtual user ID
     * @return the external user directory {@link File}
     */
    public static File getExternalUserDir(int userId) {
        return new File(sExternalVirtualRoot, String.format(Locale.CHINA, "storage/emulated/%d/", userId));
    }

    /**
     * Returns the user data directory for the specified virtual user.
     *
     * @param userId the virtual user ID
     * @return the user directory {@link File}
     */
    public static File getUserDir(int userId) {
        return new File(sVirtualRoot, String.format(Locale.CHINA, "data/user/%d", userId));
    }

    /**
     * Returns the device-encrypted data directory for the specified package and user.
     *
     * @param packageName the virtual package name
     * @param userId      the virtual user ID
     * @return the device-encrypted data directory {@link File}
     */
    public static File getDeDataDir(String packageName, int userId) {
        return new File(sVirtualRoot, String.format(Locale.CHINA, "data/user_de/%d/%s", userId, packageName));
    }

    /**
     * Returns the external data directory for the specified package and user.
     *
     * @param packageName the virtual package name
     * @param userId      the virtual user ID
     * @return the external data directory {@link File}
     */
    public static File getExternalDataDir(String packageName, int userId) {
        return new File(getExternalUserDir(userId), String.format(Locale.CHINA, "Android/data/%s", packageName));
    }

    /**
     * Returns the internal data directory for the specified package and user.
     *
     * @param packageName the virtual package name
     * @param userId      the virtual user ID
     * @return the data directory {@link File}
     */
    public static File getDataDir(String packageName, int userId) {
        return new File(sVirtualRoot, String.format(Locale.CHINA, "data/user/%d/%s", userId, packageName));
    }

    /**
     * Returns the virtual proc directory for a specific virtual PID.
     * Creates the directory if it does not exist.
     *
     * @param pid the virtual PID
     * @return the proc directory for the given PID
     */
    public static File getProcDir(int pid) {
        File file = new File(getProcDir(), String.format(Locale.CHINA, "%d", pid));
        FileUtils.mkdirs(file);
        return file;
    }

    /**
     * Returns the external files directory for the specified package and user.
     *
     * @param packageName the virtual package name
     * @param userId      the virtual user ID
     * @return the external files directory {@link File}
     */
    public static File getExternalDataFilesDir(String packageName, int userId) {
        return new File(getExternalDataDir(packageName, userId), "files");
    }

    /**
     * Returns the internal files directory for the specified package and user.
     *
     * @param packageName the virtual package name
     * @param userId      the virtual user ID
     * @return the files directory {@link File}
     */
    public static File getDataFilesDir(String packageName, int userId) {
        return new File(getDataDir(packageName, userId), "files");
    }

    /**
     * Returns the external cache directory for the specified package and user.
     *
     * @param packageName the virtual package name
     * @param userId      the virtual user ID
     * @return the external cache directory {@link File}
     */
    public static File getExternalDataCacheDir(String packageName, int userId) {
        return new File(getExternalDataDir(packageName, userId), "cache");
    }

    /**
     * Returns the internal cache directory for the specified package and user.
     *
     * @param packageName the virtual package name
     * @param userId      the virtual user ID
     * @return the cache directory {@link File}
     */
    public static File getDataCacheDir(String packageName, int userId) {
        return new File(getDataDir(packageName, userId), "cache");
    }

    /**
     * Returns the native library directory for the specified package and user.
     *
     * @param packageName the virtual package name
     * @param userId      the virtual user ID
     * @return the lib directory {@link File}
     */
    public static File getDataLibDir(String packageName, int userId) {
        return new File(getDataDir(packageName, userId), "lib");
    }

    /**
     * Returns the databases directory for the specified package and user.
     *
     * @param packageName the virtual package name
     * @param userId      the virtual user ID
     * @return the databases directory {@link File}
     */
    public static File getDataDatabasesDir(String packageName, int userId) {
        return new File(getDataDir(packageName, userId), "databases");
    }

    /**
     * Returns the root directory for installed virtual applications.
     *
     * @return the app root directory {@link File}
     */
    public static File getAppRootDir() {
        return getAppDir("");
    }

    /**
     * Returns the installation directory for the specified virtual package.
     *
     * @param packageName the virtual package name
     * @return the app installation directory {@link File}
     */
    public static File getAppDir(String packageName) {
        return new File(sVirtualRoot, "data/app/" + packageName);
    }

    /**
     * Returns the base APK file path for the specified virtual package.
     *
     * @param packageName the virtual package name
     * @return the base.apk {@link File}
     */
    public static File getBaseApkDir(String packageName) {
        return new File(sVirtualRoot, "data/app/" + packageName + "/base.apk");
    }

    /**
     * Returns the native library directory within the app installation directory.
     *
     * @param packageName the virtual package name
     * @return the app lib directory {@link File}
     */
    public static File getAppLibDir(String packageName) {
        return new File(getAppDir(packageName), "lib");
    }

    /**
     * Returns the path to a specific SharedPreferences XML file for a virtual package.
     * <p>
     * This is used by the Xposed module bridge to allow modules to read a virtual app's
     * SharedPreferences.
     *
     * @param packageName  the virtual package name
     * @param prefFileName the SharedPreferences file name (without extension)
     * @return the SharedPreferences XML {@link File}
     */
    public static File getXSharedPreferences(String packageName, String prefFileName) {
       return new File(BEnvironment.getDataDir(packageName, BActivityThread.getUserId()), "shared_prefs/" + prefFileName + ".xml");
    }
}
