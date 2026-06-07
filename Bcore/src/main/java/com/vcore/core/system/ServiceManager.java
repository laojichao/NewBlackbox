package com.vcore.core.system;

import android.os.IBinder;

import java.util.HashMap;
import java.util.Map;

import com.vcore.BlackBoxCore;
import com.vcore.core.system.accounts.BAccountManagerService;
import com.vcore.core.system.am.BActivityManagerService;
import com.vcore.core.system.am.BJobManagerService;
import com.vcore.core.system.location.BLocationManagerService;
import com.vcore.core.system.notification.BNotificationManagerService;
import com.vcore.core.system.os.BStorageManagerService;
import com.vcore.core.system.pm.BPackageManagerService;
import com.vcore.core.system.pm.BXposedManagerService;
import com.vcore.core.system.user.BUserManagerService;

/**
 * Central registry for all BlackBox system service binder instances.
 * <p>
 * Maps well-known service name constants to their corresponding {@link IBinder}
 * implementations. Provides a lookup mechanism similar to Android's native
 * {@code ServiceManager}. The singleton is initialized with all core services
 * in its constructor.
 */
public class ServiceManager {
    /** Service name for the activity manager. */
    public static final String ACTIVITY_MANAGER = "activity_manager";

    /** Service name for the job scheduler manager. */
    public static final String JOB_MANAGER = "job_manager";

    /** Service name for the package manager. */
    public static final String PACKAGE_MANAGER = "package_manager";

    /** Service name for the storage manager. */
    public static final String STORAGE_MANAGER = "storage_manager";

    /** Service name for the user manager. */
    public static final String USER_MANAGER = "user_manager";

    /** Service name for the Xposed module manager. */
    public static final String XPOSED_MANAGER = "xposed_manager";

    /** Service name for the account manager. */
    public static final String ACCOUNT_MANAGER = "account_manager";

    /** Service name for the location manager. */
    public static final String LOCATION_MANAGER = "location_manager";

    /** Service name for the notification manager. */
    public static final String NOTIFICATION_MANAGER = "notification_manager";

    /** Cache mapping service names to their binder instances. */
    private final Map<String, IBinder> mCaches = new HashMap<>();

    /**
     * Lazy holder for the singleton ServiceManager instance.
     */
    private static final class SServiceManagerHolder {
        static final ServiceManager sServiceManager = new ServiceManager();
    }

    /**
     * Returns the singleton ServiceManager instance.
     *
     * @return the ServiceManager instance
     */
    public static ServiceManager get() {
        return SServiceManagerHolder.sServiceManager;
    }

    /**
     * Retrieves the binder for a service by its well-known name.
     *
     * @param name the service name constant (e.g., {@link #PACKAGE_MANAGER})
     * @return the IBinder for the service, or null if not registered
     */
    public static IBinder getService(String name) {
        return get().getServiceInternal(name);
    }

    /**
     * Private constructor that populates the service cache with all core services.
     */
    private ServiceManager() {
        mCaches.put(ACTIVITY_MANAGER, BActivityManagerService.get());
        mCaches.put(JOB_MANAGER, BJobManagerService.get());
        mCaches.put(PACKAGE_MANAGER, BPackageManagerService.get());
        mCaches.put(STORAGE_MANAGER, BStorageManagerService.get());
        mCaches.put(USER_MANAGER, BUserManagerService.get());
        mCaches.put(XPOSED_MANAGER, BXposedManagerService.get());
        mCaches.put(ACCOUNT_MANAGER, BAccountManagerService.get());
        mCaches.put(LOCATION_MANAGER, BLocationManagerService.get());
        mCaches.put(NOTIFICATION_MANAGER, BNotificationManagerService.get());
    }

    /**
     * Internal lookup for a service binder by name.
     *
     * @param name the service name
     * @return the IBinder, or null if no service is registered with that name
     */
    public IBinder getServiceInternal(String name) {
        return mCaches.get(name);
    }

    /**
     * Eagerly initializes all BlackBox manager services by requesting each
     * through {@link BlackBoxCore#getService(String)}. This forces the service
     * singletons to be created and cached.
     */
    public static void initBlackManager() {
        BlackBoxCore.get().getService(ACTIVITY_MANAGER);
        BlackBoxCore.get().getService(JOB_MANAGER);
        BlackBoxCore.get().getService(PACKAGE_MANAGER);
        BlackBoxCore.get().getService(STORAGE_MANAGER);
        BlackBoxCore.get().getService(USER_MANAGER);
        BlackBoxCore.get().getService(XPOSED_MANAGER);
        BlackBoxCore.get().getService(ACCOUNT_MANAGER);
        BlackBoxCore.get().getService(LOCATION_MANAGER);
        BlackBoxCore.get().getService(NOTIFICATION_MANAGER);
    }
}
