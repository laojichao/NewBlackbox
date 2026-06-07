package com.vcore.core.system.pm;

import java.util.ArrayList;
import java.util.List;

import com.vcore.core.system.ISystemService;
import com.vcore.core.system.pm.installer.CopyExecutor;
import com.vcore.core.system.pm.installer.CreatePackageExecutor;
import com.vcore.core.system.pm.installer.CreateUserExecutor;
import com.vcore.core.system.pm.installer.Executor;
import com.vcore.core.system.pm.installer.RemoveAppExecutor;
import com.vcore.core.system.pm.installer.RemoveUserExecutor;
import com.vcore.entity.pm.InstallOption;
import com.vcore.utils.Slog;



/**
 * Handles package installation, uninstallation, clearing, and updating in the virtual environment.
 *
 * <p>This service orchestrates the execution pipeline for package operations by composing
 * a sequence of {@link Executor} steps. Each operation (install, uninstall, clear, update)
 * uses a specific combination of executors to handle user environment creation, package
 * directory setup, file copying, and cleanup.</p>
 */
public class BPackageInstallerService extends IBPackageInstallerService.Stub implements ISystemService {

    /** Singleton instance of the service. */
    private static final BPackageInstallerService sService = new BPackageInstallerService();

    /**
     * Returns the singleton instance of the service.
     *
     * @return the global {@link BPackageInstallerService} instance
     */
    public static BPackageInstallerService get() {
        return sService;
    }

    public static final String TAG = "BPackageInstallerService";

    /**
     * {@inheritDoc}
     *
     * <p>Executes the install pipeline: creates user environment, creates package directories,
     * and copies APK and native library files.</p>
     */
    @Override
    public int installPackageAsUser(BPackageSettings ps, int userId) {
        List<Executor> executors = new ArrayList<>();
        // 创建用户环境相关操作
        executors.add(new CreateUserExecutor());
        // 创建应用环境相关操作
        executors.add(new CreatePackageExecutor());
        // 拷贝应用相关文件
        executors.add(new CopyExecutor());
        InstallOption option = ps.installOption;
        for (Executor executor : executors) {
            int exec = executor.exec(ps, option, userId);
            Slog.d(TAG, "installPackageAsUser: " + executor.getClass().getSimpleName() + " exec: " + exec);
            if (exec != 0) {
                return exec;
            }
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Executes the uninstall pipeline: optionally removes the app directory
     * (if removing the last user) and removes user-specific data directories.</p>
     */
    @Override
    public int uninstallPackageAsUser(BPackageSettings ps, boolean removeApp, int userId) {
        List<Executor> executors = new ArrayList<>();
        if (removeApp) {
            // 移除App
            executors.add(new RemoveAppExecutor());
        }
        // 移除用户相关目录
        executors.add(new RemoveUserExecutor());
        InstallOption option = ps.installOption;
        for (Executor executor : executors) {
            int exec = executor.exec(ps, option, userId);
            Slog.d(TAG, "uninstallPackageAsUser: " + executor.getClass().getSimpleName() + " exec: " + exec);
            if (exec != 0) {
                return exec;
            }
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Executes the clear pipeline: removes user data directories and recreates them
     * for a clean state without removing the package itself.</p>
     */
    @Override
    public int clearPackage(BPackageSettings ps, int userId) {
        List<Executor> executors = new ArrayList<>();
        // 移除用户相关目录
        executors.add(new RemoveUserExecutor());
        // 创建用户环境相关操作
        executors.add(new CreateUserExecutor());
        InstallOption option = ps.installOption;
        for (Executor executor : executors) {
            int exec = executor.exec(ps, option, userId);
            Slog.d(TAG, "uninstallPackageAsUser: " + executor.getClass().getSimpleName() + " exec: " + exec);
            if (exec != 0) {
                return exec;
            }
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Executes the update pipeline: recreates package directories and copies
     * updated APK and native library files.</p>
     */
    @Override
    public int updatePackage(BPackageSettings ps) {
        List<Executor> executors = new ArrayList<>();
        executors.add(new CreatePackageExecutor());
        executors.add(new CopyExecutor());
        InstallOption option = ps.installOption;
        for (Executor executor : executors) {
            int exec = executor.exec(ps, option, -1);
            if (exec != 0) {
                return exec;
            }
        }
        return 0;
    }

    @Override
    public void systemReady() { }
}
