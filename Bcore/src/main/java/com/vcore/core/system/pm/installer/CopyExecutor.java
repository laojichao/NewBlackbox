package com.vcore.core.system.pm.installer;

import java.io.File;
import java.io.IOException;

import com.vcore.core.env.BEnvironment;
import com.vcore.core.system.pm.BPackageSettings;
import com.vcore.entity.pm.InstallOption;
import com.vcore.utils.FileUtils;
import com.vcore.utils.NativeUtils;

/**
 * Executor that copies application files during package installation.
 *
 * <p>This executor handles two main tasks:</p>
 * <ul>
 *   <li>Extracts and copies native libraries from the APK to the app's lib directory
 *       (skipped for system apps that use the host's native libs)</li>
 *   <li>For storage-based installs, copies or moves the APK to the virtual app directory
 *       and updates the base code path</li>
 * </ul>
 */
public class CopyExecutor implements Executor {

    /**
     * Executes the file copy operation for package installation.
     *
     * <p>For non-system packages, copies native libraries. For storage-flagged installs,
     * copies or renames the APK file to the virtual base APK directory.</p>
     *
     * @param ps      the package settings containing the APK path and package name
     * @param option  the install options specifying flags such as FLAG_SYSTEM and FLAG_STORAGE
     * @param userId  the virtual user ID (unused in this executor)
     * @return 0 on success, -1 on failure
     */
    @Override
    public int exec(BPackageSettings ps, InstallOption option, int userId) {
        try {
            if (!option.isFlag(InstallOption.FLAG_SYSTEM)) {
                NativeUtils.copyNativeLib(new File(ps.pkg.baseCodePath), BEnvironment.getAppLibDir(ps.pkg.packageName));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        if (option.isFlag(InstallOption.FLAG_STORAGE)) {
            // External install: copy or move APK to virtual storage
            File origFile = new File(ps.pkg.baseCodePath);
            File newFile = BEnvironment.getBaseApkDir(ps.pkg.packageName);
            try {
                if (option.isFlag(InstallOption.FLAG_URI_FILE)) {
                    boolean b = FileUtils.renameTo(origFile, newFile);
                    if (!b) {
                        FileUtils.copyFile(origFile, newFile);
                    }
                } else {
                    FileUtils.copyFile(origFile, newFile);
                }
                ps.pkg.baseCodePath = newFile.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
        }
        return 0;
    }
}
