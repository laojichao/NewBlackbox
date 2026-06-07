package com.vcore.core.system.location;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.AtomicFile;
import android.util.SparseArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import black.android.location.ILocationListener;
import com.vcore.BlackBoxCore;
import com.vcore.core.env.BEnvironment;
import com.vcore.core.system.ISystemService;
import com.vcore.entity.location.BCell;
import com.vcore.entity.location.BLocation;
import com.vcore.entity.location.BLocationConfig;
import com.vcore.fake.frameworks.BLocationManager;
import com.vcore.utils.CloseUtils;
import com.vcore.utils.FileUtils;
import com.vcore.utils.Slog;

/**
 * Virtual location manager service for the BlackBox environment.
 * <p>
 * Supports three location modes per package:
 * <ul>
 *   <li><b>CLOSE_MODE</b>: Location spoofing is disabled; real GPS data is returned.</li>
 *   <li><b>OWN_MODE</b>: Uses per-package fake location/cell configuration.</li>
 *   <li><b>GLOBAL_MODE</b>: Uses a globally configured fake location/cell for all packages.</li>
 * </ul>
 * Manages location update listeners, persists configurations to disk, and
 * dispatches fake location updates on a cached thread pool.
 */
public class BLocationManagerService extends IBLocationManagerService.Stub implements ISystemService {
    public static final String TAG = "BLocationManagerService";

    /** Singleton instance. */
    private static final BLocationManagerService sService = new BLocationManagerService();

    /** Per-user, per-package location configuration map. */
    private final SparseArray<HashMap<String, BLocationConfig>> mLocationConfigs = new SparseArray<>();

    /** Global location configuration used when a package is in GLOBAL_MODE. */
    private final BLocationConfig mGlobalConfig = new BLocationConfig();

    /** Map of active location listener binders to their associated LocationRecords. */
    private final Map<IBinder, LocationRecord> mLocationListeners = new HashMap<>();

    /** Thread pool for dispatching location updates to listeners. */
    private final Executor mThreadPool = Executors.newCachedThreadPool();

    /**
     * Returns the singleton instance of BLocationManagerService.
     *
     * @return the singleton service instance
     */
    public static BLocationManagerService get() {
        return sService;
    }

    /**
     * Returns the existing location configuration for the given user and package,
     * creating a new one with CLOSE_MODE if none exists.
     *
     * @param userId the virtual user ID
     * @param pkg    the package name
     * @return the BLocationConfig for the package
     */
    private BLocationConfig getOrCreateConfig(int userId, String pkg) {
        synchronized (mLocationConfigs) {
            HashMap<String, BLocationConfig> pkgs = mLocationConfigs.get(userId);
            if (pkgs == null) {
                pkgs = new HashMap<>();
                mLocationConfigs.put(userId, pkgs);
            }

            BLocationConfig config = pkgs.get(pkg);
            if (config == null) {
                config = new BLocationConfig();
                config.pattern = BLocationManager.CLOSE_MODE;
                pkgs.put(pkg, config);
            }
            return config;
        }
    }

    /**
     * Returns the location mode pattern for the given user and package.
     *
     * @param userId the virtual user ID
     * @param pkg    the package name
     * @return the pattern constant (CLOSE_MODE, OWN_MODE, or GLOBAL_MODE)
     */
    public int getPattern(int userId, String pkg) {
        synchronized (mLocationConfigs) {
            BLocationConfig config = getOrCreateConfig(userId, pkg);
            return config.pattern;
        }
    }

    /**
     * Sets the location mode pattern for the given user and package.
     *
     * @param userId  the virtual user ID
     * @param pkg     the package name
     * @param pattern the pattern constant to set
     */
    @Override
    public void setPattern(int userId, String pkg, int pattern) {
        synchronized (mLocationConfigs) {
            getOrCreateConfig(userId, pkg).pattern = pattern;
            save();
        }
    }

    /**
     * Sets the fake cell info for the given user and package.
     *
     * @param userId the virtual user ID
     * @param pkg    the package name
     * @param cell   the BCell to set as the fake cell
     */
    @Override
    public void setCell(int userId, String pkg, BCell cell) {
        synchronized (mLocationConfigs) {
            getOrCreateConfig(userId, pkg).cell = cell;
            save();
        }
    }

    /**
     * Sets all cell info for the given user and package.
     *
     * @param userId the virtual user ID
     * @param pkg    the package name
     * @param cells  the list of BCell objects
     */
    @Override
    public void setAllCell(int userId, String pkg, List<BCell> cells) {
        synchronized (mLocationConfigs) {
            getOrCreateConfig(userId, pkg).allCell = cells;
            save();
        }
    }

    /**
     * Sets the neighboring cell info for the given user and package.
     *
     * @param userId the virtual user ID
     * @param pkg    the package name
     * @param cells  the list of neighboring BCell objects
     */
    @Override
    public void setNeighboringCell(int userId, String pkg, List<BCell> cells) {
        synchronized (mLocationConfigs) {
            getOrCreateConfig(userId, pkg).allCell = cells;
            save();
        }
    }

    /**
     * Returns the neighboring cell info for the given user and package.
     *
     * @param userId the virtual user ID
     * @param pkg    the package name
     * @return the list of neighboring BCell objects
     */
    @Override
    public List<BCell> getNeighboringCell(int userId, String pkg) {
        synchronized (mLocationConfigs) {
            return getOrCreateConfig(userId, pkg).allCell;
        }
    }

    /**
     * Sets the global fake cell info, used by packages in GLOBAL_MODE.
     *
     * @param cell the BCell to set as the global fake cell
     */
    @Override
    public void setGlobalCell(BCell cell) {
        synchronized (mGlobalConfig) {
            mGlobalConfig.cell = cell;
            save();
        }
    }

    /**
     * Sets the global all-cell info.
     *
     * @param cells the list of BCell objects
     */
    @Override
    public void setGlobalAllCell(List<BCell> cells) {
        synchronized (mGlobalConfig) {
            mGlobalConfig.allCell = cells;
            save();
        }
    }

    /**
     * Sets the global neighboring cell info.
     *
     * @param cells the list of neighboring BCell objects
     */
    @Override
    public void setGlobalNeighboringCell(List<BCell> cells) {
        synchronized (mGlobalConfig) {
            mGlobalConfig.neighboringCellInfo = cells;
            save();
        }
    }

    /**
     * Returns the global neighboring cell info.
     *
     * @return the list of global neighboring BCell objects
     */
    @Override
    public List<BCell> getGlobalNeighboringCell() {
        synchronized (mGlobalConfig) {
            return mGlobalConfig.neighboringCellInfo;
        }
    }

    /**
     * Returns the cell info for the given user and package, respecting the current mode.
     *
     * @param userId the virtual user ID
     * @param pkg    the package name
     * @return the BCell for OWN_MODE or GLOBAL_MODE, null for CLOSE_MODE
     */
    @Override
    public BCell getCell(int userId, String pkg) {
        BLocationConfig config = getOrCreateConfig(userId, pkg);
        switch (config.pattern) {
            case BLocationManager.OWN_MODE:
                return config.cell;
            case BLocationManager.GLOBAL_MODE:
                return mGlobalConfig.cell;
            case BLocationManager.CLOSE_MODE:
            default:
                return null;
        }
    }

    /**
     * Returns the all-cell info for the given user and package, respecting the current mode.
     *
     * @param userId the virtual user ID
     * @param pkg    the package name
     * @return the list of BCell objects for OWN_MODE or GLOBAL_MODE, null for CLOSE_MODE
     */
    @Override
    public List<BCell> getAllCell(int userId, String pkg) {
        BLocationConfig config = getOrCreateConfig(userId, pkg);
        switch (config.pattern) {
            case BLocationManager.OWN_MODE:
                return config.allCell;
            case BLocationManager.GLOBAL_MODE:
                return mGlobalConfig.allCell;
            case BLocationManager.CLOSE_MODE:
            default:
                return null;
        }
    }

    /**
     * Sets the fake location for the given user and package.
     *
     * @param userId   the virtual user ID
     * @param pkg      the package name
     * @param location the BLocation to set
     */
    @Override
    public void setLocation(int userId, String pkg, BLocation location) {
        synchronized (mLocationConfigs) {
            getOrCreateConfig(userId, pkg).location = location;
            save();
        }
    }

    /**
     * Returns the fake location for the given user and package, respecting the current mode.
     *
     * @param userId the virtual user ID
     * @param pkg    the package name
     * @return the BLocation for OWN_MODE or GLOBAL_MODE, null for CLOSE_MODE
     */
    @Override
    public BLocation getLocation(int userId, String pkg) {
        BLocationConfig config = getOrCreateConfig(userId, pkg);
        switch (config.pattern) {
            case BLocationManager.OWN_MODE:
                return config.location;
            case BLocationManager.GLOBAL_MODE:
                return mGlobalConfig.location;
            case BLocationManager.CLOSE_MODE:
            default:
                return null;
        }
    }

    /**
     * Sets the global fake location, used by packages in GLOBAL_MODE.
     *
     * @param location the BLocation to set as the global fake location
     */
    @Override
    public void setGlobalLocation(BLocation location) {
        synchronized (mGlobalConfig) {
            mGlobalConfig.location = location;
            save();
        }
    }

    /**
     * Returns the global fake location.
     *
     * @return the global BLocation
     */
    @Override
    public BLocation getGlobalLocation() {
        synchronized (mGlobalConfig) {
            return mGlobalConfig.location;
        }
    }

    /**
     * Registers a location update listener. The listener receives periodic fake
     * location updates as long as its binder is alive.
     *
     * @param listener    the binder of the location listener
     * @param packageName the package registering the listener
     * @param userId      the virtual user ID
     * @throws RemoteException if the listener binder is dead
     */
    @Override
    public void requestLocationUpdates(IBinder listener, String packageName, int userId) throws RemoteException {
        if (listener == null || !listener.pingBinder()) {
            return;
        }

        if (mLocationListeners.containsKey(listener)) {
            return;
        }

        listener.linkToDeath(new DeathRecipient() {
            @Override
            public void binderDied() {
                listener.unlinkToDeath(this, 0);
                mLocationListeners.remove(listener);
            }
        }, 0);
        LocationRecord record = new LocationRecord(packageName, userId);
        mLocationListeners.put(listener, record);
        addTask(listener);
    }

    /**
     * Unregisters a location update listener.
     *
     * @param listener the binder of the location listener to remove
     * @throws RemoteException if the listener binder check fails
     */
    @Override
    public void removeUpdates(IBinder listener) throws RemoteException {
        if (listener == null || !listener.pingBinder()) {
            return;
        }
        mLocationListeners.remove(listener);
    }

    /**
     * Submits a location update task to the thread pool for the given listener.
     * The task periodically checks for location changes and dispatches updates.
     *
     * @param locationListener the binder of the location listener
     */
    private void addTask(IBinder locationListener) {
        mThreadPool.execute(() -> {
            BLocation lastLocation = null;
            long l = System.currentTimeMillis();

            while (locationListener.pingBinder()) {
                IInterface iInterface = ILocationListener.Stub.asInterface.call(locationListener);
                LocationRecord locationRecord = mLocationListeners.get(locationListener);
                if (locationRecord == null) {
                    continue;
                }

                BLocation location = getLocation(locationRecord.userId, locationRecord.packageName);
                if (location == null) {
                    continue;
                }

                // Skip duplicate location updates within 3 seconds
                if (location.equals(lastLocation) && (System.currentTimeMillis() - l) < 3000) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) { }
                    continue;
                }

                lastLocation = location;
                l = System.currentTimeMillis();
                BlackBoxCore.get().getHandler().post(() -> ILocationListener.onLocationChanged.call(iInterface, location.convert2SystemLocation()));
            }
        });
    }

    /**
     * Persists the current global and per-user location configurations to disk
     * using atomic file writes.
     */
    public void save() {
        synchronized (mGlobalConfig) {
            synchronized (mLocationConfigs) {
                Parcel parcel = Parcel.obtain();
                AtomicFile atomicFile = new AtomicFile(BEnvironment.getFakeLocationConf());
                FileOutputStream fileOutputStream = null;

                try {
                    mGlobalConfig.writeToParcel(parcel, 0);
                    parcel.writeInt(mLocationConfigs.size());

                    for (int i = 0; i < mLocationConfigs.size(); i++) {
                        int tmpUserId = mLocationConfigs.keyAt(i);
                        HashMap<String, BLocationConfig> configArrayMap = mLocationConfigs.valueAt(i);
                        parcel.writeInt(tmpUserId);
                        parcel.writeMap(configArrayMap);
                    }

                    parcel.setDataPosition(0);
                    fileOutputStream = atomicFile.startWrite();
                    FileUtils.writeParcelToOutput(parcel, fileOutputStream);
                    atomicFile.finishWrite(fileOutputStream);
                } catch (Throwable e) {
                    e.printStackTrace();
                    atomicFile.failWrite(fileOutputStream);
                } finally {
                    parcel.recycle();
                    CloseUtils.close(fileOutputStream);
                }
            }
        }
    }

    /**
     * Loads location configuration from disk, restoring the global config and
     * all per-user, per-package configs.
     */
    public void loadConfig() {
        Parcel parcel = Parcel.obtain();
        InputStream is = null;

        try {
            File fakeLocationConf = BEnvironment.getFakeLocationConf();
            if (!fakeLocationConf.exists()) {
                return;
            }

            is = new FileInputStream(BEnvironment.getFakeLocationConf());
            byte[] bytes = FileUtils.toByteArray(is);
            parcel.unmarshall(bytes, 0, bytes.length);
            parcel.setDataPosition(0);

            synchronized (mGlobalConfig) {
                mGlobalConfig.refresh(parcel);
            }

            synchronized (mLocationConfigs) {
                mLocationConfigs.clear();

                int size = parcel.readInt();
                for (int i = 0; i < size; i++) {
                    int userId = parcel.readInt();
                    HashMap<String, BLocationConfig> configArrayMap = parcel.readHashMap(BLocationConfig.class.getClassLoader());
                    mLocationConfigs.put(userId, configArrayMap);
                    Slog.d(TAG, "load userId: " + userId + ", config: " + configArrayMap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Slog.d(TAG, "bad config");
            FileUtils.deleteDir(BEnvironment.getFakeLocationConf());
        } finally {
            parcel.recycle();
            CloseUtils.close(is);
        }
    }

    /**
     * Called when the system is ready. Loads persisted configuration and
     * restarts location update tasks for all active listeners.
     */
    @Override
    public void systemReady() {
        loadConfig();
        for (IBinder iBinder : mLocationListeners.keySet()) {
            addTask(iBinder);
        }
    }
}
