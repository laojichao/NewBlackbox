package black.android.os.storage;

import android.os.storage.StorageVolume;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.os.storage.StorageManager#getVolumeList} method.
 * Provides access to the static method that returns the list of storage volumes
 * available to a specific user and flags combination.
 */
public class StorageManager {
    public static final Reflector REF = Reflector.on("android.os.storage.StorageManager");

    /**
     * Returns the list of storage volumes for the given user and flags.
     *
     * @param userId the user ID to query volumes for
     * @param flags  storage flags
     * @return an array of StorageVolume objects
     */
    public static Reflector.StaticMethodWrapper<StorageVolume[]> getVolumeList = REF.staticMethod("getVolumeList", int.class, int.class);
}
