package com.vcore.fake.frameworks;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.RemoteException;

import java.util.Collections;
import java.util.List;

import com.vcore.BlackBoxCore;
import com.vcore.app.BActivityThread;
import com.vcore.core.system.ServiceManager;
import com.vcore.core.system.pm.IBPackageManagerService;
import com.vcore.entity.pm.InstallOption;
import com.vcore.entity.pm.InstallResult;

/**
 * Virtual environment manager for package-related operations.
 *
 * <p>Wraps {@link IBPackageManagerService} to provide PackageManager functionality
 * scoped to the virtual environment. Handles package installation, resolution of
 * intents to activities/services/receivers/providers, querying package information,
 * and managing installed packages within the virtual space.</p>
 *
 * @see BlackManager
 * @see IBPackageManagerService
 */
public class BPackageManager extends BlackManager<IBPackageManagerService> {
    private static final BPackageManager sPackageManager = new BPackageManager();

    /**
     * Returns the singleton instance of {@link BPackageManager}.
     *
     * @return the global BPackageManager instance
     */
    public static BPackageManager get() {
        return sPackageManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getServiceName() {
        return ServiceManager.PACKAGE_MANAGER;
    }

    /**
     * Returns a launch intent for the given package, first trying CATEGORY_INFO
     * then falling back to CATEGORY_LAUNCHER.
     *
     * @param packageName the package name to launch
     * @param userId      the virtual user ID
     * @return the launch intent, or {@code null} if no launchable activity is found
     */
    public Intent getLaunchIntentForPackage(String packageName, int userId) {
        Intent intentToResolve = new Intent(Intent.ACTION_MAIN);
        intentToResolve.addCategory(Intent.CATEGORY_INFO);
        intentToResolve.setPackage(packageName);
        List<ResolveInfo> ris = queryIntentActivities(intentToResolve, 0,
                intentToResolve.resolveTypeIfNeeded(BlackBoxCore.getContext().getContentResolver()), userId);

        // Otherwise, try to find a main launcher activity.
        if (ris == null || ris.size() <= 0) {
            // Reuse the intent instance.
            intentToResolve.removeCategory(Intent.CATEGORY_INFO);
            intentToResolve.addCategory(Intent.CATEGORY_LAUNCHER);
            intentToResolve.setPackage(packageName);
            ris = queryIntentActivities(intentToResolve, 0, intentToResolve.resolveTypeIfNeeded(BlackBoxCore.getContext().getContentResolver()), userId);
        }

        if (ris == null || ris.size() <= 0) {
            return null;
        }

        Intent intent = new Intent(intentToResolve);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(ris.get(0).activityInfo.packageName, ris.get(0).activityInfo.name);
        return intent;
    }

    /**
     * Resolves a service intent to its {@link ServiceInfo}.
     *
     * @param intent       the service intent
     * @param flags        query flags
     * @param resolvedType the resolved MIME type
     * @param userId       the virtual user ID
     * @return the resolved service info, or {@code null} if not found or on error
     */
    public ResolveInfo resolveService(Intent intent, int flags, String resolvedType, int userId) {
        try {
            return getService().resolveService(intent, flags, resolvedType, userId);
        } catch (RemoteException e) {
            crash(e);
        }
        return null;
    }

    /**
     * Resolves an activity intent to its {@link ActivityInfo}.
     *
     * @param intent       the activity intent
     * @param flags        query flags
     * @param resolvedType the resolved MIME type
     * @param userId       the virtual user ID
     * @return the resolved activity info, or {@code null} if not found or on error
     */
    public ResolveInfo resolveActivity(Intent intent, int flags, String resolvedType, int userId) {
        try {
            return getService().resolveActivity(intent, flags, resolvedType, userId);
        } catch (RemoteException e) {
            crash(e);
        }
        return null;
    }

    /**
     * Resolves a content provider by authority.
     *
     * @param authority the content provider authority
     * @param flags     query flags
     * @param userId    the virtual user ID
     * @return the provider info, or {@code null} if not found or on error
     */
    public ProviderInfo resolveContentProvider(String authority, int flags, int userId) {
        try {
            return getService().resolveContentProvider(authority, flags, userId);
        } catch (RemoteException e) {
            crash(e);
        }
        return null;
    }

    /**
     * Resolves an intent to its best matching {@link ResolveInfo}.
     *
     * @param intent       the intent to resolve
     * @param resolvedType the resolved MIME type
     * @param flags        query flags
     * @param userId       the virtual user ID
     * @return the resolved intent info, or {@code null} if not found or on error
     */
    public ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags, int userId) {
        try {
            return getService().resolveIntent(intent, resolvedType, flags, userId);
        } catch (RemoteException e) {
            crash(e);
        }
        return null;
    }

    /**
     * Retrieves application info for the given package.
     *
     * @param packageName the package name
     * @param flags       query flags
     * @param userId      the virtual user ID
     * @return the application info, or {@code null} if not found or on error
     */
    public ApplicationInfo getApplicationInfo(String packageName, int flags, int userId) {
        try {
            return getService().getApplicationInfo(packageName, flags, userId);
        } catch (RemoteException e) {
            crash(e);
        }
        return null;
    }

    /**
     * Returns the UID for a given process ID.
     *
     * @param pid the process ID
     * @return the UID, or -1 on error
     */
    public int getUidByPid(int pid) {
        try {
            return getService().getUidByPid(pid);
        } catch (RemoteException e) {
            crash(e);
        }
        return -1;
    }

    /**
     * Retrieves package info for the given package.
     *
     * @param packageName the package name
     * @param flags       query flags
     * @param userId      the virtual user ID
     * @return the package info, or {@code null} if not found or on error
     */
    public PackageInfo getPackageInfo(String packageName, int flags, int userId) {
        try {
            return getService().getPackageInfo(packageName, flags, userId);
        } catch (RemoteException e) {
            crash(e);
        }
        return null;
    }

    /**
     * Retrieves service info for the given component.
     *
     * @param component the service component
     * @param flags     query flags
     * @param userId    the virtual user ID
     * @return the service info, or {@code null} if not found or on error
     */
    public ServiceInfo getServiceInfo(ComponentName component, int flags, int userId) {
        try {
            return getService().getServiceInfo(component, flags, userId);
        } catch (RemoteException e) {
            crash(e);
        }
        return null;
    }

    /**
     * Retrieves receiver info for the given component.
     *
     * @param componentName the receiver component
     * @param flags         query flags
     * @param userId        the virtual user ID
     * @return the receiver activity info, or {@code null} if not found or on error
     */
    public ActivityInfo getReceiverInfo(ComponentName componentName, int flags, int userId) {
        try {
            return getService().getReceiverInfo(componentName, flags, userId);
        } catch (RemoteException e) {
            crash(e);
        }
        return null;
    }

    /**
     * Retrieves activity info for the given component.
     *
     * @param component the activity component
     * @param flags     query flags
     * @param userId    the virtual user ID
     * @return the activity info, or {@code null} if not found or on error
     */
    public ActivityInfo getActivityInfo(ComponentName component, int flags, int userId) {
        try {
            return getService().getActivityInfo(component, flags, userId);
        } catch (RemoteException e) {
            crash(e);
        }
        return null;
    }

    /**
     * Retrieves provider info for the given component.
     *
     * @param component the provider component
     * @param flags     query flags
     * @param userId    the virtual user ID
     * @return the provider info, or {@code null} if not found or on error
     */
    public ProviderInfo getProviderInfo(ComponentName component, int flags, int userId) {
        try {
            return getService().getProviderInfo(component, flags, userId);
        } catch (RemoteException e) {
            crash(e);
        }
        return null;
    }

    /**
     * Queries activities that can handle the given intent.
     *
     * @param intent       the intent to query
     * @param flags        query flags
     * @param resolvedType the resolved MIME type
     * @param userId       the virtual user ID
     * @return a list of matching activities, or {@code null} on error
     */
    public List<ResolveInfo> queryIntentActivities(Intent intent, int flags, String resolvedType, int userId) {
        try {
            return getService().queryIntentActivities(intent, flags, resolvedType, userId);
        } catch (RemoteException e) {
            crash(e);
        }
        return null;
    }

    /**
     * Queries broadcast receivers that can handle the given intent.
     *
     * @param intent       the intent to query
     * @param flags        query flags
     * @param resolvedType the resolved MIME type
     * @param userId       the virtual user ID
     * @return a list of matching receivers, or {@code null} on error
     */
    public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags, String resolvedType, int userId) {
        try {
            return getService().queryBroadcastReceivers(intent, flags, resolvedType, userId);
        } catch (RemoteException e) {
            crash(e);
        }
        return null;
    }

    /**
     * Queries content providers for the given process.
     *
     * @param processName the process name
     * @param uid         the process UID
     * @param flags       query flags
     * @param userId      the virtual user ID
     * @return a list of matching providers, or {@code null} on error
     */
    public List<ProviderInfo> queryContentProviders(String processName, int uid, int flags, int userId) {
        try {
            return getService().queryContentProviders(processName, uid, flags, userId);
        } catch (RemoteException e) {
            crash(e);
        }
        return null;
    }

    /**
     * Installs a package within the virtual environment.
     *
     * @param file   the APK file path
     * @param option the installation options
     * @param userId the virtual user ID
     * @return the install result, or {@code null} on error
     */
    public InstallResult installPackageAsUser(String file, InstallOption option, int userId) {
        try {
            return getService().installPackageAsUser(file, option, userId);
        } catch (RemoteException e) {
            crash(e);
        }
        return null;
    }

    /**
     * Returns all installed applications in the virtual environment.
     *
     * @param flags  query flags
     * @param userId the virtual user ID
     * @return a list of application info, or an empty list on error
     */
    public List<ApplicationInfo> getInstalledApplications(int flags, int userId) {
        try {
            return getService().getInstalledApplications(flags, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * Returns all installed packages in the virtual environment.
     *
     * @param flags  query flags
     * @param userId the virtual user ID
     * @return a list of package info, or an empty list on error
     */
    public List<PackageInfo> getInstalledPackages(int flags, int userId) {
        try {
            return getService().getInstalledPackages(flags, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * Clears data for the specified package in the virtual environment.
     *
     * @param packageName the package to clear
     * @param userId      the virtual user ID
     */
    public void clearPackage(String packageName, int userId) {
        try {
            getService().clearPackage(packageName, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Force-stops the specified package in the virtual environment.
     *
     * @param packageName the package to stop
     * @param userId      the virtual user ID
     */
    public void stopPackage(String packageName, int userId) {
        try {
            getService().stopPackage(packageName, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Uninstalls a package for the specified user in the virtual environment.
     *
     * @param packageName the package to uninstall
     * @param userId      the virtual user ID
     */
    public void uninstallPackageAsUser(String packageName, int userId) {
        try {
            getService().uninstallPackageAsUser(packageName, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Uninstalls a package from the virtual environment.
     *
     * @param packageName the package to uninstall
     */
    public void uninstallPackage(String packageName) {
        try {
            getService().uninstallPackage(packageName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks whether a package is installed in the virtual environment.
     *
     * @param packageName the package name
     * @param userId      the virtual user ID
     * @return {@code true} if the package is installed
     */
    public boolean isInstalled(String packageName, int userId) {
        try {
            return getService().isInstalled(packageName, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Returns the package names associated with the given UID.
     *
     * @param uid the UID to look up
     * @return an array of package names, or an empty array on error
     */
    public String[] getPackagesForUid(int uid) {
        try {
            return getService().getPackagesForUid(uid, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return new String[]{};
    }

    /**
     * Logs an error and prints the stack trace for a remote exception.
     *
     * @param e the exception to log
     */
    private void crash(Throwable e) {
        e.printStackTrace();
    }
}
