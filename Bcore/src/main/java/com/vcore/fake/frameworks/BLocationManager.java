package com.vcore.fake.frameworks;

import android.os.IBinder;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

import com.vcore.app.BActivityThread;
import com.vcore.core.system.ServiceManager;
import com.vcore.core.system.location.IBLocationManagerService;
import com.vcore.entity.location.BCell;
import com.vcore.entity.location.BLocation;

/**
 * Virtual environment manager for location-related operations and fake location support.
 *
 * <p>Wraps {@link IBLocationManagerService} to provide location management functionality
 * within the virtual environment. Supports three location modes: close (use real location),
 * global (apply fake location to all apps), and own (apply fake location per-app).
 * Manages GPS location, cell tower information, and location update listeners.</p>
 *
 * @see BlackManager
 * @see IBLocationManagerService
 */
public class BLocationManager extends BlackManager<IBLocationManagerService> {
    private static final BLocationManager sLocationManager = new BLocationManager();

    /** Location mode: fake location is disabled; real location is used. */
    public static final int CLOSE_MODE = 0;

    /** Location mode: fake location is applied globally to all apps. */
    public static final int GLOBAL_MODE = 1;

    /** Location mode: fake location is applied per-app. */
    public static final int OWN_MODE = 2;

    /**
     * Returns the singleton instance of {@link BLocationManager}.
     *
     * @return the global BLocationManager instance
     */
    public static BLocationManager get() {
        return sLocationManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getServiceName() {
        return ServiceManager.LOCATION_MANAGER;
    }

    /**
     * Checks whether fake location is enabled for the current app in the virtual environment.
     *
     * @return {@code true} if fake location is not in CLOSE_MODE
     */
    public static boolean isFakeLocationEnable() {
        return get().getPattern(BActivityThread.getUserId(), BActivityThread.getAppPackageName()) != CLOSE_MODE;
    }

    /**
     * Disables fake location for the specified package in the virtual environment.
     *
     * @param userId the virtual user ID
     * @param pkg    the package name
     */
    public static void disableFakeLocation(int userId, String pkg) {
        get().setPattern(userId, pkg, CLOSE_MODE);
    }

    /**
     * Sets the location mode pattern for a package.
     *
     * @param userId  the virtual user ID
     * @param pkg     the package name
     * @param pattern the location mode ({@link #CLOSE_MODE}, {@link #GLOBAL_MODE}, or {@link #OWN_MODE})
     */
    public void setPattern(int userId, String pkg, int pattern) {
        try {
            getService().setPattern(userId, pkg, pattern);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the location mode pattern for a package.
     *
     * @param userId the virtual user ID
     * @param pkg    the package name
     * @return the location mode, or {@link #CLOSE_MODE} on error
     */
    public int getPattern(int userId, String pkg) {
        try {
            return getService().getPattern(userId, pkg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return CLOSE_MODE;
    }

    /**
     * Returns the neighboring cell tower information for the given package.
     *
     * @param userId the virtual user ID
     * @param pkg    the package name
     * @return a list of neighboring cells, or {@code null} on error
     */
    public List<BCell> getNeighboringCell(int userId, String pkg) {
        try {
            return getService().getNeighboringCell(userId, pkg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the current cell tower information for the given package.
     *
     * @param userId the virtual user ID
     * @param pkg    the package name
     * @return the current cell, or {@code null} on error
     */
    public BCell getCell(int userId, String pkg) {
        try {
            return getService().getCell(userId, pkg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns all cell tower information (current and neighboring) for the given package.
     *
     * @param userId the virtual user ID
     * @param pkg    the package name
     * @return a list of all cells, or an empty list on error
     */
    public List<BCell> getAllCell(int userId, String pkg) {
        try {
            return getService().getAllCell(userId, pkg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * Sets the fake location for a package in the virtual environment.
     *
     * @param userId   the virtual user ID
     * @param pkg      the package name
     * @param location the fake location to set
     */
    public void setLocation(int userId, String pkg, BLocation location) {
        try {
            getService().setLocation(userId, pkg, location);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the configured location for a package in the virtual environment.
     *
     * @param userId the virtual user ID
     * @param pkg    the package name
     * @return the configured location, or {@code null} on error
     */
    public BLocation getLocation(int userId, String pkg) {
        try {
            return getService().getLocation(userId, pkg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Registers a location update listener for the current app.
     *
     * @param listener the binder for the location update listener
     */
    public void requestLocationUpdates(IBinder listener) {
        try {
            getService().requestLocationUpdates(listener, BActivityThread.getAppPackageName(), BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes a previously registered location update listener.
     *
     * @param listener the binder for the location update listener
     */
    public void removeUpdates(IBinder listener) {
        try {
            getService().removeUpdates(listener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
