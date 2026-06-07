package com.vcore.core.system.user;

import android.os.Parcel;
import android.os.RemoteException;

import androidx.core.util.AtomicFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.vcore.core.env.BEnvironment;
import com.vcore.core.system.ISystemService;
import com.vcore.core.system.pm.BPackageManagerService;
import com.vcore.utils.CloseUtils;
import com.vcore.utils.FileUtils;


/**
 * Virtual user manager service for the BlackBox environment.
 * <p>
 * Manages the lifecycle of virtual users including creation, deletion, querying,
 * and persistent storage. Each virtual user is identified by a unique integer ID
 * and is represented by a {@link BUserInfo} object. User data is persisted to disk
 * and loaded on system startup.
 */
public class BUserManagerService extends IBUserManagerService.Stub implements ISystemService {
    /** Singleton instance. */
    private static final BUserManagerService sService = new BUserManagerService();

    /** Map from user ID to BUserInfo for all virtual users. */
    public final HashMap<Integer, BUserInfo> mUsers = new HashMap<>();

    /** Lock object for user creation/deletion operations. */
    public final Object mUserLock = new Object();

    /**
     * Returns the singleton instance of BUserManagerService.
     *
     * @return the singleton service instance
     */
    public static BUserManagerService get() {
        return sService;
    }

    /**
     * Called when the system is ready. Loads persisted user information from disk.
     */
    @Override
    public void systemReady() {
        scanUserL();
    }

    /**
     * Returns the BUserInfo for the specified user ID.
     *
     * @param userId the virtual user ID to look up
     * @return the BUserInfo, or null if no user with that ID exists
     */
    @Override
    public BUserInfo getUserInfo(int userId) {
        synchronized (mUserLock) {
            return mUsers.get(userId);
        }
    }

    /**
     * Checks whether a virtual user with the given ID exists.
     *
     * @param userId the virtual user ID to check
     * @return true if the user exists
     */
    @Override
    public boolean exists(int userId) {
        synchronized (mUsers) {
            return mUsers.get(userId) != null;
        }
    }

    /**
     * Creates a new virtual user with the given ID. If a user with that ID
     * already exists, returns the existing user info.
     *
     * @param userId the virtual user ID to create
     * @return the BUserInfo for the created or existing user
     */
    @Override
    public BUserInfo createUser(int userId) {
        synchronized (mUserLock) {
            if (exists(userId)) {
                return getUserInfo(userId);
            }
            return createUserLocked(userId);
        }
    }

    /**
     * Returns a list of all virtual users with non-negative IDs (excludes
     * special/system user handles).
     *
     * @return a list of BUserInfo for all normal users
     */
    @Override
    public List<BUserInfo> getUsers() {
        synchronized (mUsers) {
            ArrayList<BUserInfo> bUsers = new ArrayList<>();
            for (BUserInfo value : mUsers.values()) {
                if (value.id >= 0) {
                    bUsers.add(value);
                }
            }
            return bUsers;
        }
    }

    /**
     * Returns a list of all virtual users regardless of their ID values.
     *
     * @return a list of all BUserInfo entries
     */
    public List<BUserInfo> getAllUsers() {
        synchronized (mUsers) {
            return new ArrayList<>(mUsers.values());
        }
    }

    /**
     * Deletes the virtual user with the given ID. Removes the user from the
     * user map, persists the change, and deletes the user's data directories.
     *
     * @param userId the virtual user ID to delete
     * @throws RemoteException if package deletion fails
     */
    @Override
    public void deleteUser(int userId) throws RemoteException {
        synchronized (mUserLock) {
            synchronized (mUsers) {
                BPackageManagerService.get().deleteUser(userId);

                mUsers.remove(userId);
                saveUserInfoLocked();
                FileUtils.deleteDir(BEnvironment.getUserDir(userId));
                FileUtils.deleteDir(BEnvironment.getExternalUserDir(userId));
            }
        }
    }

    /**
     * Internal method to create a new user and persist the change.
     *
     * @param userId the virtual user ID to create
     * @return the newly created BUserInfo
     */
    private BUserInfo createUserLocked(int userId) {
        BUserInfo bUserInfo = new BUserInfo();
        bUserInfo.id = userId;
        bUserInfo.status = BUserStatus.ENABLE;
        mUsers.put(userId, bUserInfo);
        synchronized (mUsers) {
            saveUserInfoLocked();
        }
        return bUserInfo;
    }

    /**
     * Persists all user information to disk using atomic file writes.
     */
    private void saveUserInfoLocked() {
        Parcel parcel = Parcel.obtain();
        AtomicFile atomicFile = new AtomicFile(BEnvironment.getUserInfoConf());
        FileOutputStream fileOutputStream = null;

        try {
            ArrayList<BUserInfo> bUsers = new ArrayList<>(mUsers.values());
            parcel.writeTypedList(bUsers);

            try {
                fileOutputStream = atomicFile.startWrite();
                FileUtils.writeParcelToOutput(parcel, fileOutputStream);
                atomicFile.finishWrite(fileOutputStream);
            } catch (IOException e) {
                e.printStackTrace();
                atomicFile.failWrite(fileOutputStream);
            } finally {
                CloseUtils.close(fileOutputStream);
            }
        } finally {
            parcel.recycle();
        }
    }

    /**
     * Loads user information from the persisted configuration file on disk.
     */
    private void scanUserL() {
        synchronized (mUserLock) {
            Parcel parcel = Parcel.obtain();
            InputStream is = null;

            try {
                File userInfoConf = BEnvironment.getUserInfoConf();
                if (!userInfoConf.exists()) {
                    return;
                }

                is = new FileInputStream(BEnvironment.getUserInfoConf());
                byte[] bytes = FileUtils.toByteArray(is);
                parcel.unmarshall(bytes, 0, bytes.length);
                parcel.setDataPosition(0);

                ArrayList<BUserInfo> loadUsers = parcel.createTypedArrayList(BUserInfo.CREATOR);
                if (loadUsers == null) {
                    return;
                }

                synchronized (mUsers) {
                    mUsers.clear();
                    for (BUserInfo loadUser : loadUsers) {
                        mUsers.put(loadUser.id, loadUser);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                parcel.recycle();
                CloseUtils.close(is);
            }
        }
    }
}
