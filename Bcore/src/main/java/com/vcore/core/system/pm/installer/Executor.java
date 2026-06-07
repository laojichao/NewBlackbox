package com.vcore.core.system.pm.installer;

import com.vcore.core.system.pm.BPackageSettings;
import com.vcore.entity.pm.InstallOption;

/**
 * Defines the contract for package installation executors in the virtual environment.
 *
 * <p>Each executor represents a single step in the package installation or uninstallation
 * pipeline. Implementations handle specific operations such as creating directories,
 * copying files, or cleaning up resources. Executors are chained together by
 * {@link com.vcore.core.system.pm.BPackageInstallerService} to form complete
 * install/uninstall workflows.</p>
 */
public interface Executor {

    /** Logging tag for install executors. */
    String TAG = "InstallExecutor";

    /**
     * Executes the installation step for the given package settings.
     *
     * @param ps      the package settings containing package metadata and configuration
     * @param option  the install options specifying flags and behavior
     * @param userId  the virtual user ID for which the operation is performed
     * @return 0 on success, a negative error code on failure
     */
    int exec(BPackageSettings ps, InstallOption option, int userId);
}
