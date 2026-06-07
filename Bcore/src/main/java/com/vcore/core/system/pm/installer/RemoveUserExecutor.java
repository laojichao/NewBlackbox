package com.vcore.core.system.pm.installer;

import com.vcore.core.env.BEnvironment;
import com.vcore.core.system.pm.BPackageSettings;
import com.vcore.entity.pm.InstallOption;
import com.vcore.utils.FileUtils;

/**
 * Executor that removes per-user data directories during uninstallation.
 *
 * <p>This executor cleans up all user-specific data directories for a package,
 * including the internal data directory, device-encrypted data directory, and
 * external data directory.</p>
 */
public class RemoveUserExecutor implements Executor {

    /**
     * Executes the removal of per-user data directories for the package.
     *
     * <p>Deletes the main data directory, device-encrypted data directory,
     * and external data directory for the specified user.</p>
     *
     * @param ps      the package settings containing the package name
     * @param option  the install options (unused in this executor)
     * @param userId  the virtual user ID whose data directories are removed
     * @return always returns 0 (success)
     */
    @Override
    public int exec(BPackageSettings ps, InstallOption option, int userId) {
        String packageName = ps.pkg.packageName;

        FileUtils.deleteDir(BEnvironment.getDataDir(packageName, userId));
        FileUtils.deleteDir(BEnvironment.getDeDataDir(packageName, userId));
        FileUtils.deleteDir(BEnvironment.getExternalDataDir(packageName, userId));
        return 0;
    }
}
