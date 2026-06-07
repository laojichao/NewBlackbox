package com.vcore.core.system.pm.installer;

import com.vcore.core.env.BEnvironment;
import com.vcore.core.system.pm.BPackageSettings;
import com.vcore.entity.pm.InstallOption;
import com.vcore.utils.FileUtils;

/**
 * Executor that creates the package directory structure during installation.
 *
 * <p>This executor removes any existing app directory and creates a fresh directory
 * layout for the package, including the main app directory and the native library directory.</p>
 */
public class CreatePackageExecutor implements Executor {

    /**
     * Executes the creation of package directories.
     *
     * <p>Deletes any existing app directory for the package, then creates fresh
     * app and lib directories.</p>
     *
     * @param ps      the package settings containing the package name
     * @param option  the install options (unused in this executor)
     * @param userId  the virtual user ID (unused in this executor)
     * @return always returns 0 (success)
     */
    @Override
    public int exec(BPackageSettings ps, InstallOption option, int userId) {
        FileUtils.deleteDir(BEnvironment.getAppDir(ps.pkg.packageName));
        FileUtils.mkdirs(BEnvironment.getAppDir(ps.pkg.packageName));
        FileUtils.mkdirs(BEnvironment.getAppLibDir(ps.pkg.packageName));
        return 0;
    }
}
