package com.vcore.core.system.pm;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.os.Parcel;
import android.os.Process;
import android.util.ArrayMap;
import android.util.AtomicFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.vcore.BlackBoxCore;
import com.vcore.core.env.BEnvironment;
import com.vcore.core.system.BProcessManagerService;
import com.vcore.core.system.user.BUserHandle;
import com.vcore.entity.pm.InstallOption;
import com.vcore.utils.FileUtils;
import com.vcore.utils.Slog;
import com.vcore.utils.compat.PackageParserCompat;

/**
 * Manages package settings, UID allocation, and package persistence in the virtual environment.
 *
 * <p>This class handles the lifecycle of {@link BPackageSettings} entries including
 * UID registration, package scanning from disk, and package configuration persistence.
 * It coordinates with {@link SharedUserSetting} for shared UID management.</p>
 */
/*public*/ class Settings {
    public static final String TAG = "Settings";

    /** Map of package names to their settings. */
    final ArrayMap<String, BPackageSettings> mPackages = new ArrayMap<>();

    /** Map of package names to their allocated app IDs. */
    private final Map<String, Integer> mAppIds = new HashMap<>();

    /** Map of shared user IDs to their settings. */
    private final Map<String, SharedUserSetting> mSharedUsers = SharedUserSetting.sSharedUsers;

    /** The current UID counter for allocating new app IDs. */
    private int mCurrUid = 0;

    /**
     * Constructs Settings, loading persisted UIDs and shared user settings from disk.
     */
    public Settings() {
        synchronized (mPackages) {
            loadUidLP();
            SharedUserSetting.loadSharedUsers();
        }
    }

    /**
     * Retrieves or creates package settings for the given package name.
     *
     * <p>If the package already exists, reuses its app ID and user state.
     * Otherwise, registers a new app ID for the package.</p>
     *
     * @param name           the package name
     * @param aPackage       the parsed package from the system PackageParser
     * @param installOption  the install options for the package
     * @return the package settings for the given package
     */
    BPackageSettings getPackageLPw(String name, PackageParser.Package aPackage, InstallOption installOption) {
        BPackageSettings pkgSettings;
        BPackageSettings origSettings = new BPackageSettings();
        origSettings.pkg = new BPackage(aPackage);
        origSettings.pkg.installOption = installOption;
        origSettings.installOption = installOption;
        origSettings.pkg.mExtras = origSettings;
        origSettings.pkg.applicationInfo = PackageManagerCompat.generateApplicationInfo(origSettings.pkg, 0, BPackageUserState.create(), 0);

        synchronized (mPackages) {
            pkgSettings = mPackages.get(name);
            if (pkgSettings != null) {
                origSettings.appId = pkgSettings.appId;
                origSettings.userState = pkgSettings.userState;
            } else {
                boolean b = registerAppIdLPw(origSettings);
                if (!b) {
                    throw new RuntimeException("registerAppIdLPw err.");
                }
            }
        }
        return origSettings;
    }

    /**
     * Registers an app ID for the given package settings.
     * Handles shared user ID resolution and allocation.
     *
     * @param p the package settings to register
     * @return true if a valid app ID was assigned, false on failure
     */
    boolean registerAppIdLPw(BPackageSettings p) {
        boolean createdNew;
        String sharedUserId = p.pkg.mSharedUserId;
        SharedUserSetting sharedUserSetting = null;

        if (sharedUserId != null) {
            sharedUserSetting = mSharedUsers.get(sharedUserId);
            if (sharedUserSetting == null) {
                sharedUserSetting = new SharedUserSetting(sharedUserId);
                sharedUserSetting.userId = acquireAndRegisterNewAppIdLPw(p);
                mSharedUsers.put(sharedUserId, sharedUserSetting);
            }
        }

        if (sharedUserSetting != null) {
            p.appId = sharedUserSetting.userId;
            Slog.d(TAG, p.pkg.packageName + " sharedUserId = " + sharedUserId + ", setAppId = " + p.appId);
        }

        if (p.appId == 0) {
            // Assign new user ID
            p.appId = acquireAndRegisterNewAppIdLPw(p);
        }
        createdNew = p.appId >= 0;
        saveUidLP();
        SharedUserSetting.saveSharedUsers();
        return createdNew;
    }

    private int acquireAndRegisterNewAppIdLPw(BPackageSettings obj) {
        // Let's be stupidly inefficient for now...
        Integer integer = mAppIds.get(obj.pkg.packageName);
        if (integer != null) {
            return integer;
        }

        if (mCurrUid >= Process.LAST_APPLICATION_UID) {
            return -1;
        }
        mCurrUid++;
        mAppIds.put(obj.pkg.packageName, mCurrUid);
        return Process.FIRST_APPLICATION_UID + mCurrUid;
    }

    private void saveUidLP() {
        Parcel parcel = Parcel.obtain();
        FileOutputStream fileOutputStream = null;
        AtomicFile atomicFile = new AtomicFile(BEnvironment.getUidConf());

        try {
            Set<String> pkgName = mPackages.keySet();
            for (String s : new HashSet<>(mAppIds.keySet())) {
                if (!pkgName.contains(s)) {
                    mAppIds.remove(s);
                }
            }
            parcel.writeInt(mCurrUid);
            parcel.writeMap(mAppIds);

            fileOutputStream = atomicFile.startWrite();
            FileUtils.writeParcelToOutput(parcel, fileOutputStream);
            atomicFile.finishWrite(fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
            atomicFile.failWrite(fileOutputStream);
        } finally {
            parcel.recycle();
        }
    }

    private void loadUidLP() {
        Parcel parcel = Parcel.obtain();
        try {
            byte[] uidBytes = FileUtils.toByteArray(BEnvironment.getUidConf());
            parcel.unmarshall(uidBytes, 0, uidBytes.length);
            parcel.setDataPosition(0);

            mCurrUid = parcel.readInt();
            HashMap hashMap = parcel.readHashMap(HashMap.class.getClassLoader());
            synchronized (mAppIds) {
                mAppIds.clear();
                mAppIds.putAll(hashMap);
            }
        } catch (Exception e) {
			// e.printStackTrace();
        } finally {
            parcel.recycle();
        }
    }

    /**
     * Scans all package directories under the app root and loads their settings.
     */
    public void scanPackage() {
        synchronized (mPackages) {
            File appRootDir = BEnvironment.getAppRootDir();
            FileUtils.mkdirs(appRootDir);
            File[] apps = appRootDir.listFiles();

            if (apps != null) {
                for (File app : apps) {
                    if (!app.isDirectory()) {
                        continue;
                    }
                    scanPackage(app.getName());
                }
            }
        }
    }

    /**
     * Scans and loads the settings for a specific package.
     *
     * @param packageName the package name to scan
     */
    public void scanPackage(String packageName) {
        synchronized (mPackages) {
            updatePackageLP(BEnvironment.getAppDir(packageName));
        }
    }

    private void updatePackageLP(File app) {
        String packageName = app.getName();
        Parcel packageSettingsIn = Parcel.obtain();
        File packageConf = BEnvironment.getPackageConf(packageName);

        try {
            byte[] bPackageSettingsBytes = FileUtils.toByteArray(packageConf);

            packageSettingsIn.unmarshall(bPackageSettingsBytes, 0, bPackageSettingsBytes.length);
            packageSettingsIn.setDataPosition(0);

            BPackageSettings bPackageSettings = new BPackageSettings(packageSettingsIn);
            bPackageSettings.pkg.mExtras = bPackageSettings;
            if (bPackageSettings.installOption.isFlag(InstallOption.FLAG_SYSTEM)) {
                PackageInfo packageInfo = BlackBoxCore.getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
                String currPackageSourcePath = packageInfo.applicationInfo.sourceDir;
                if (!currPackageSourcePath.equals(bPackageSettings.pkg.baseCodePath)) {
                    BProcessManagerService.get().killAllByPackageName(bPackageSettings.pkg.packageName);
                    BPackageSettings newPkg = reInstallBySystem(packageInfo, bPackageSettings.installOption);
                    bPackageSettings.pkg = newPkg.pkg;
                }
            } else {
                bPackageSettings.pkg.applicationInfo = PackageManagerCompat.generateApplicationInfo(bPackageSettings.pkg, 0, BPackageUserState.create(), 0);
            }

            bPackageSettings.save();
            mPackages.put(bPackageSettings.pkg.packageName, bPackageSettings);
            Slog.d(TAG, "Loaded package: " + packageName);
        } catch (Throwable e) {
            e.printStackTrace();

            FileUtils.deleteDir(app);
            removePackage(packageName);
            BProcessManagerService.get().killAllByPackageName(packageName);
            BPackageManagerService.get().onPackageUninstalled(packageName, true, BUserHandle.USER_ALL);
            Slog.d(TAG, "Bad package: " + packageName);
        } finally {
            packageSettingsIn.recycle();
        }
    }

    private BPackageSettings reInstallBySystem(PackageInfo systemPackageInfo, InstallOption option) throws Exception {
        Slog.d(TAG, "reInstallBySystem: " + systemPackageInfo.packageName);
        PackageParser.Package aPackage = parserApk(systemPackageInfo.applicationInfo.sourceDir);
        if (aPackage == null) {
            throw new RuntimeException("parser apk error.");
        }

        aPackage.applicationInfo = BlackBoxCore.getPackageManager().getPackageInfo(aPackage.packageName, 0).applicationInfo;
        return getPackageLPw(aPackage.packageName, aPackage, option);
    }

    /**
     * Removes a package from the settings map.
     *
     * @param packageName the package name to remove
     */
    public void removePackage(String packageName) {
        mPackages.remove(packageName);
    }

    private PackageParser.Package parserApk(String file) {
        try {
            PackageParser parser = PackageParserCompat.createParser();
            PackageParser.Package aPackage = PackageParserCompat.parsePackage(parser, new File(file), 0);
            PackageParserCompat.collectCertificates(parser, aPackage, 0);
            return aPackage;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }
}
