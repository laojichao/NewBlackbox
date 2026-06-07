package com.vcore.core.system.pm.installer;

import com.vcore.core.env.BEnvironment;
import com.vcore.core.system.pm.BPackageSettings;
import com.vcore.entity.pm.InstallOption;
import com.vcore.utils.FileUtils;

/**
 * Executor that creates the per-user data directory structure during installation.
 *
 * <p>This executor sets up the virtual data directories for a package for a specific
 * user, including data, cache, files, databases, and device-encrypted data directories.
 * It removes any existing data lib directory first to ensure a clean state.</p>
 */
public class CreateUserExecutor implements Executor {

    /**
     * Executes the creation of per-user data directories for the package.
     *
     * <p>Removes the existing data lib directory, then creates all required
     * user data subdirectories (data, cache, files, databases, DE data).</p>
     *
     * @param ps      the package settings containing the package name
     * @param option  the install options (unused in this executor)
     * @param userId  the virtual user ID for which data directories are created
     * @return always returns 0 (success)
     */
    @Override
    public int exec(BPackageSettings ps, InstallOption option, int userId) {
        String packageName = ps.pkg.packageName;
        FileUtils.deleteDir(BEnvironment.getDataLibDir(packageName, userId));

        FileUtils.mkdirs(BEnvironment.getDataDir(packageName, userId));
        FileUtils.mkdirs(BEnvironment.getDataCacheDir(packageName, userId));
        FileUtils.mkdirs(BEnvironment.getDataFilesDir(packageName, userId));
        FileUtils.mkdirs(BEnvironment.getDataDatabasesDir(packageName, userId));
        FileUtils.mkdirs(BEnvironment.getDeDataDir(packageName, userId));
        return 0;
    }
}
