package com.vcore.fake.frameworks;

import android.net.Uri;
import android.os.RemoteException;
import android.os.storage.StorageVolume;

import com.vcore.core.system.ServiceManager;
import com.vcore.core.system.os.IBStorageManagerService;

/**
 * Virtual environment manager for storage-related operations.
 *
 * <p>Wraps {@link IBStorageManagerService} to provide storage management functionality
 * within the virtual environment. Handles storage volume listing and file URI generation
 * scoped to the virtual user space.</p>
 *
 * @see BlackManager
 * @see IBStorageManagerService
 */
public class BStorageManager extends BlackManager<IBStorageManagerService> {
    private static final BStorageManager sStorageManager = new BStorageManager();

    /**
     * Returns the singleton instance of {@link BStorageManager}.
     *
     * @return the global BStorageManager instance
     */
    public static BStorageManager get() {
        return sStorageManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getServiceName() {
        return ServiceManager.STORAGE_MANAGER;
    }

    /**
     * Returns the list of storage volumes available to the given package.
     *
     * @param uid         the UID of the package
     * @param packageName the package name
     * @param flags       query flags
     * @param userId      the virtual user ID
     * @return an array of storage volumes, or an empty array on error
     */
    public StorageVolume[] getVolumeList(int uid, String packageName, int flags, int userId) {
        try {
            return getService().getVolumeList(uid, packageName, flags, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return new StorageVolume[]{};
    }

    /**
     * Returns a content URI for the given file path, suitable for sharing via
     * a FileProvider.
     *
     * @param file the absolute file path
     * @return the content URI, or {@code null} on error
     */
    public Uri getUriForFile(String file) {
        try {
            return getService().getUriForFile(file);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }
}
