package com.vcore.core.system.os;

import android.net.Uri;
import android.os.Process;
import android.os.storage.StorageVolume;

import java.io.File;

import black.android.os.storage.StorageManager;
import com.vcore.BlackBoxCore;
import com.vcore.core.env.BEnvironment;
import com.vcore.core.system.ISystemService;
import com.vcore.core.system.user.BUserHandle;
import com.vcore.fake.provider.FileProvider;
import com.vcore.proxy.ProxyManifest;
import com.vcore.utils.compat.BuildCompat;

/**
 * Virtual storage manager service for the BlackBox environment.
 * <p>
 * Intercepts storage volume queries to redirect external storage paths to
 * per-user virtual directories within the BlackBox environment. Also provides
 * file URI generation through the proxy FileProvider.
 */
public class BStorageManagerService extends IBStorageManagerService.Stub implements ISystemService {
    /** Singleton instance. */
    private static final BStorageManagerService sService = new BStorageManagerService();

    /**
     * Returns the singleton instance of BStorageManagerService.
     *
     * @return the singleton service instance
     */
    public static BStorageManagerService get() {
        return sService;
    }

    /** Default constructor. */
    public BStorageManagerService() { }

    /**
     * Returns the storage volume list with paths redirected to the virtual user's
     * external directory. Each volume's internal and external paths are modified
     * to point to {@link BEnvironment#getExternalUserDir(int)}.
     *
     * @param uid         the calling UID (currently unused)
     * @param packageName the calling package (currently unused)
     * @param flags       storage flags (currently unused)
     * @param userId      the virtual user ID
     * @return an array of StorageVolume with redirected paths, or null on error
     */
    @Override
    public StorageVolume[] getVolumeList(int uid, String packageName, int flags, int userId) {
        if (StorageManager.getVolumeList == null) {
            return null;
        }

        try {
            StorageVolume[] storageVolumes = StorageManager.getVolumeList.call(BUserHandle.getUserId(Process.myUid()), 0);
            if (storageVolumes == null) {
                return null;
            }

            for (StorageVolume storageVolume : storageVolumes) {
                black.android.os.storage.StorageVolume.mPath.set(storageVolume, BEnvironment.getExternalUserDir(userId));
                if (BuildCompat.isPie()) {
                    black.android.os.storage.StorageVolume.mInternalPath.set(storageVolume, BEnvironment.getExternalUserDir(userId));
                }
            }
            return storageVolumes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns a content URI for the given file path using the proxy FileProvider.
     *
     * @param file the absolute file path
     * @return the content URI for the file
     */
    @Override
    public Uri getUriForFile(String file) {
        return FileProvider.getUriForFile(BlackBoxCore.getContext(), ProxyManifest.getProxyFileProvider(), new File(file));
    }

    /** Called when the system is ready. No initialization needed. */
    @Override
    public void systemReady() { }
}
