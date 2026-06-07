package com.vcore.core.system.pm.installer;

import com.vcore.core.env.BEnvironment;
import com.vcore.core.system.pm.BPackageSettings;
import com.vcore.entity.pm.InstallOption;
import com.vcore.utils.FileUtils;

/**
 * Executor that removes the entire application directory during uninstallation.
 *
 * <p>This executor deletes the app directory (including APK, native libraries,
 * and all package-level data) when a package is completely removed from the
 * virtual environment.</p>
 */
public class RemoveAppExecutor implements Executor {

    /**
     * Executes the removal of the application directory.
     *
     * <p>Deletes the entire app directory for the package, which includes
     * the APK file, native libraries, and package metadata.</p>
     *
     * @param ps      the package settings containing the package name
     * @param option  the install options (unused in this executor)
     * @param userId  the virtual user ID (unused in this executor)
     * @return always returns 0 (success)
     */
    @Override
    public int exec(BPackageSettings ps, InstallOption option, int userId) {
        FileUtils.deleteDir(BEnvironment.getAppDir(ps.pkg.packageName));
        return 0;
    }
}
