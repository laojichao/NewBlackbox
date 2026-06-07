package com.vcore.fake.frameworks;

import android.os.RemoteException;

import java.util.Collections;
import java.util.List;

import com.vcore.core.system.ServiceManager;
import com.vcore.core.system.pm.IBXposedManagerService;
import com.vcore.entity.pm.InstalledModule;

/**
 * Virtual environment manager for Xposed module management.
 *
 * <p>Wraps {@link IBXposedManagerService} to provide Xposed framework functionality
 * within the virtual environment. Handles enabling/disabling the Xposed framework,
 * managing individual module states, and listing installed Xposed modules.</p>
 *
 * @see BlackManager
 * @see IBXposedManagerService
 */
public class BXposedManager extends BlackManager<IBXposedManagerService> {
    private static final BXposedManager sXposedManager = new BXposedManager();

    /**
     * Returns the singleton instance of {@link BXposedManager}.
     *
     * @return the global BXposedManager instance
     */
    public static BXposedManager get() {
        return sXposedManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getServiceName() {
        return ServiceManager.XPOSED_MANAGER;
    }

    /**
     * Checks whether the Xposed framework is enabled in the virtual environment.
     *
     * @return {@code true} if Xposed is enabled, {@code false} on error or if disabled
     */
    public boolean isXPEnable() {
        try {
            return getService().isXPEnable();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Enables or disables the Xposed framework in the virtual environment.
     *
     * @param enable {@code true} to enable, {@code false} to disable
     */
    public void setXPEnable(boolean enable) {
        try {
            getService().setXPEnable(enable);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks whether a specific Xposed module is enabled.
     *
     * @param packageName the module's package name
     * @return {@code true} if the module is enabled, {@code false} on error or if disabled
     */
    public boolean isModuleEnable(String packageName) {
        try {
            return getService().isModuleEnable(packageName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Enables or disables a specific Xposed module.
     *
     * @param packageName the module's package name
     * @param enable      {@code true} to enable, {@code false} to disable
     */
    public void setModuleEnable(String packageName, boolean enable) {
        try {
            getService().setModuleEnable(packageName, enable);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns all installed Xposed modules in the virtual environment.
     *
     * @return a list of installed modules, or an empty list on error
     */
    public List<InstalledModule> getInstalledModules() {
        try {
            return getService().getInstalledModules();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
