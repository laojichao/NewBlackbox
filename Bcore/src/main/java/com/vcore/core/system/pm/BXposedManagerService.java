package com.vcore.core.system.pm;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Parcel;

import androidx.core.util.AtomicFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vcore.core.env.BEnvironment;
import com.vcore.core.system.ISystemService;
import com.vcore.core.system.user.BUserHandle;
import com.vcore.entity.pm.InstalledModule;
import com.vcore.entity.pm.XposedConfig;
import com.vcore.utils.CloseUtils;
import com.vcore.utils.FileUtils;
import com.vcore.utils.compat.XposedParserCompat;

/**
 * Manages Xposed module state within the virtual environment.
 *
 * <p>This service handles enabling/disabling the Xposed framework and individual Xposed
 * modules. It persists module configuration to disk and listens for package install/uninstall
 * events to keep the module list synchronized. Implements {@link PackageMonitor} to react
 * to package lifecycle changes for the Xposed user space.</p>
 */
public class BXposedManagerService extends IBXposedManagerService.Stub implements ISystemService, PackageMonitor {

    /** Singleton instance of the service. */
    private static final BXposedManagerService sService = new BXposedManagerService();

    /** The current Xposed configuration (enable state and per-module states). */
    private XposedConfig mXposedConfig;

    /** Lock object for synchronizing Xposed config access. */
    private final Object mLock = new Object();

    /** The package manager service for querying installed packages. */
    private BPackageManagerService mPms;

    /** Cache of parsed module metadata keyed by package name. */
    private final Map<String, InstalledModule> mCacheModule = new HashMap<>();

    /**
     * Returns the singleton instance of the service.
     *
     * @return the global {@link BXposedManagerService} instance
     */
    public static BXposedManagerService get() {
        return sService;
    }

    public BXposedManagerService() { }

    @Override
    public void systemReady() {
        loadModuleStateLr();
        mPms = BPackageManagerService.get();
        mPms.addPackageMonitor(this);
    }

    @Override
    public boolean isXPEnable() {
        synchronized (mLock) {
            return mXposedConfig.enable;
        }
    }

    @Override
    public void setXPEnable(boolean enable) {
        synchronized (mLock) {
            mXposedConfig.enable = enable;
            saveModuleStateLw();
        }
    }

    @Override
    public boolean isModuleEnable(String packageName) {
        synchronized (mLock) {
            Boolean enable = mXposedConfig.moduleState.get(packageName);
            return enable != null && enable;
        }
    }

    @Override
    public void setModuleEnable(String packageName, boolean enable) {
        synchronized (mLock) {
            if (!mPms.isInstalled(packageName, BUserHandle.USER_XPOSED)) {
                return;
            }
            mXposedConfig.moduleState.put(packageName, enable);
            saveModuleStateLw();
        }
    }

    @Override
    public List<InstalledModule> getInstalledModules() {
        List<ApplicationInfo> installedApplications = mPms.getInstalledApplications(PackageManager.GET_META_DATA, BUserHandle.USER_XPOSED);
        synchronized (mCacheModule) {
            for (ApplicationInfo installedApplication : installedApplications) {
                if (mCacheModule.containsKey(installedApplication.packageName)) {
                    continue;
                }

                InstalledModule installedModule = XposedParserCompat.parseModule(installedApplication);
                if (installedModule != null) {
                    mCacheModule.put(installedApplication.packageName, installedModule);
                }
            }

            ArrayList<InstalledModule> installedModules = new ArrayList<>(mCacheModule.values());
            for (InstalledModule installedModule : installedModules) {
                installedModule.enable = isModuleEnable(installedModule.packageName);
            }
            return installedModules;
        }
    }

    /**
     * Loads the Xposed module configuration from disk.
     * Creates a default configuration if the file does not exist.
     */
    private void loadModuleStateLr() {
        File xpModuleConf = BEnvironment.getXPModuleConf();
        if (!xpModuleConf.exists()) {
            mXposedConfig = new XposedConfig();
            saveModuleStateLw();
            return;
        }

        Parcel parcel = null;
        try {
            parcel = FileUtils.readToParcel(xpModuleConf);
            mXposedConfig = new XposedConfig(parcel);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (parcel != null) {
                parcel.recycle();
            }
        }
    }

    /**
     * Saves the current Xposed configuration to disk using atomic file writes.
     */
    private void saveModuleStateLw() {
        Parcel parcel = Parcel.obtain();
        AtomicFile atomicFile = new AtomicFile(BEnvironment.getXPModuleConf());
        FileOutputStream fileOutputStream = null;

        try {
            mXposedConfig.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            fileOutputStream = atomicFile.startWrite();

            FileUtils.writeParcelToOutput(parcel, fileOutputStream);
            atomicFile.finishWrite(fileOutputStream);
        } catch (Exception ignored) {
            atomicFile.failWrite(fileOutputStream);
        } finally {
            parcel.recycle();
            CloseUtils.close(fileOutputStream);
        }
    }

    @Override
    public void onPackageUninstalled(String packageName, boolean removeApp, int userId) {
        if (userId != BUserHandle.USER_XPOSED && userId != BUserHandle.USER_ALL) {
            return;
        }

        synchronized (mCacheModule) {
            mCacheModule.remove(packageName);
        }

        synchronized (mLock) {
            mXposedConfig.moduleState.remove(packageName);
            saveModuleStateLw();
        }
    }

    @Override
    public void onPackageInstalled(String packageName, int userId) {
        if (userId != BUserHandle.USER_XPOSED && userId != BUserHandle.USER_ALL) {
            return;
        }

        synchronized (mCacheModule) {
            mCacheModule.remove(packageName);
        }

        synchronized (mLock) {
            mXposedConfig.moduleState.put(packageName, false);
            saveModuleStateLw();
        }
    }
}
