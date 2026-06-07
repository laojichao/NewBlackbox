package com.vcore.fake.frameworks;

import android.os.RemoteException;

import java.util.Collections;
import java.util.List;

import com.vcore.core.system.ServiceManager;
import com.vcore.core.system.user.BUserInfo;
import com.vcore.core.system.user.IBUserManagerService;

/**
 * Virtual environment manager for user management operations.
 *
 * <p>Wraps {@link IBUserManagerService} to provide user management functionality
 * within the virtual environment. Handles creation, deletion, and listing
 * of virtual users.</p>
 *
 * @see BlackManager
 * @see IBUserManagerService
 */
public class BUserManager extends BlackManager<IBUserManagerService> {
    private static final BUserManager sUserManager = new BUserManager();

    /**
     * Returns the singleton instance of {@link BUserManager}.
     *
     * @return the global BUserManager instance
     */
    public static BUserManager get() {
        return sUserManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getServiceName() {
        return ServiceManager.USER_MANAGER;
    }

    /**
     * Creates a new virtual user with the given user ID.
     *
     * @param userId the user ID for the new user
     * @return the created user info, or {@code null} on error
     */
    public BUserInfo createUser(int userId) {
        try {
            return getService().createUser(userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Deletes a virtual user.
     *
     * @param userId the user ID to delete
     */
    public void deleteUser(int userId) {
        try {
            getService().deleteUser(userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns all virtual users.
     *
     * @return a list of user info, or an empty list on error
     */
    public List<BUserInfo> getUsers() {
        try {
            return getService().getUsers();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
