package com.vcore.core.system;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.vcore.BlackBoxCore;
import com.vcore.core.env.AppSystemEnv;
import com.vcore.core.env.BEnvironment;
import com.vcore.core.system.accounts.BAccountManagerService;
import com.vcore.core.system.am.BActivityManagerService;
import com.vcore.core.system.am.BJobManagerService;
import com.vcore.core.system.location.BLocationManagerService;
import com.vcore.core.system.notification.BNotificationManagerService;
import com.vcore.core.system.os.BStorageManagerService;
import com.vcore.core.system.pm.BPackageInstallerService;
import com.vcore.core.system.pm.BPackageManagerService;
import com.vcore.core.system.pm.BXposedManagerService;
import com.vcore.core.system.user.BUserHandle;
import com.vcore.core.system.user.BUserManagerService;
import com.vcore.entity.pm.InstallOption;

/**
 * The central system bootstrap class for BlackBox.
 * <p>
 * Initializes and registers all core system services (package manager, user manager,
 * activity manager, etc.), invokes their {@code systemReady()} lifecycle callbacks,
 * and handles pre-installation of configured packages. Ensures single-startup via
 * an {@link AtomicBoolean} guard.
 */
public class BlackBoxSystem {
    /** Ordered list of all registered system services. */
    private final List<ISystemService> mServices = new ArrayList<>();

    /** Atomic flag ensuring the system is started only once. */
    private final static AtomicBoolean isStartup = new AtomicBoolean(false);

    /**
     * Lazy holder for the singleton BlackBoxSystem instance.
     */
    private static final class SBlackBoxSystemHolder {
        static final BlackBoxSystem sBlackBoxSystem = new BlackBoxSystem();
    }

    /**
     * Returns the singleton BlackBoxSystem instance.
     *
     * @return the BlackBoxSystem instance
     */
    public static BlackBoxSystem getSystem() {
        return SBlackBoxSystemHolder.sBlackBoxSystem;
    }

    /**
     * Boots the BlackBox system. This method is idempotent -- calling it multiple
     * times has no effect after the first successful invocation.
     * <p>
     * Performs the following steps:
     * <ol>
     *   <li>Loads the BEnvironment configuration.</li>
     *   <li>Registers all core system services.</li>
     *   <li>Calls {@code systemReady()} on each registered service.</li>
     *   <li>Pre-installs any packages defined in {@link AppSystemEnv#getPreInstallPackages()}.</li>
     * </ol>
     */
    public void startup() {
        if (isStartup.getAndSet(true)) {
            return;
        }

        BEnvironment.load();
        mServices.add(BPackageManagerService.get());
        mServices.add(BUserManagerService.get());
        mServices.add(BActivityManagerService.get());
        mServices.add(BJobManagerService.get());
        mServices.add(BStorageManagerService.get());
        mServices.add(BPackageInstallerService.get());
        mServices.add(BXposedManagerService.get());
        mServices.add(BProcessManagerService.get());
        mServices.add(BAccountManagerService.get());
        mServices.add(BLocationManagerService.get());
        mServices.add(BNotificationManagerService.get());

        for (ISystemService service : mServices) {
            service.systemReady();
        }

        List<String> preInstallPackages = AppSystemEnv.getPreInstallPackages();
        for (String preInstallPackage : preInstallPackages) {
            try {
                if (!BPackageManagerService.get().isInstalled(preInstallPackage, BUserHandle.USER_ALL)) {
                    PackageInfo packageInfo = BlackBoxCore.getPackageManager().getPackageInfo(preInstallPackage, 0);
                    BPackageManagerService.get().installPackageAsUser(packageInfo.applicationInfo.sourceDir, InstallOption.installBySystem(), BUserHandle.USER_ALL);
                }
            } catch (PackageManager.NameNotFoundException ignored) { }
        }
    }
}
