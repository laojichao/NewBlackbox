package com.vcore.entity;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks the lifecycle state of a bound/started {@link Service} inside the virtual environment.
 * Maintains a map of per-Intent binding information ({@link BoundInfo}), including the {@link IBinder}
 * returned by each bind call and the number of active connections. Also records whether the service
 * was re-bound and the latest {@code startId} from {@link Service#onStartCommand}.
 */
public class ServiceRecord {
    /** Live reference to the running Service instance. */
    private Service mService;

    /**
     * Map from each bind {@link Intent} (wrapped as {@link Intent.FilterComparison}) to its
     * connection tracking data.
     */
    private final Map<Intent.FilterComparison, BoundInfo> mBounds = new HashMap<>();

    /** Whether this service was re-bound after all clients had previously unbound. */
    private boolean rebind;

    /** The most recent {@code startId} passed to {@link Service#onStartCommand}. */
    private int mStartId;

    /**
     * Holds binding state for a single Intent-based connection to the service.
     * Tracks the {@link IBinder} returned by the service and the number of concurrent
     * bind requests using atomic counters for thread safety.
     */
    public static class BoundInfo {
        /** The IBinder returned by the service for this particular binding intent. */
        private IBinder mIBinder;

        /** Thread-safe counter of active bind connections for this intent. */
        private final AtomicInteger mBindCount = new AtomicInteger(0);

        /**
         * Atomically increments the bind count by one.
         */
        public void incrementAndGetBindCount() {
            mBindCount.incrementAndGet();
        }

        /**
         * Atomically decrements the bind count by one.
         *
         * @return the new bind count after decrementing
         */
        public int decrementAndGetBindCount() {
            return mBindCount.decrementAndGet();
        }

        /**
         * Returns the {@link IBinder} associated with this binding.
         *
         * @return the binder, or {@code null} if not yet set
         */
        public IBinder getIBinder() {
            return mIBinder;
        }

        /**
         * Sets the {@link IBinder} for this binding.
         *
         * @param IBinder the binder returned by the service
         */
        public void setIBinder(IBinder IBinder) {
            mIBinder = IBinder;
        }
    }

    /**
     * Returns the most recent start ID from {@code onStartCommand}.
     *
     * @return the start ID
     */
    public int getStartId() {
        return mStartId;
    }

    /**
     * Sets the start ID, typically from the latest {@code onStartCommand} invocation.
     *
     * @param startId the new start ID
     */
    public void setStartId(int startId) {
        mStartId = startId;
    }

    /**
     * Returns the live {@link Service} reference.
     *
     * @return the service instance
     */
    public Service getService() {
        return mService;
    }

    /**
     * Sets the live {@link Service} reference.
     *
     * @param service the service instance to track
     */
    public void setService(Service service) {
        mService = service;
    }

    /**
     * Retrieves the {@link IBinder} associated with the given bind intent.
     *
     * @param intent the intent used in the bind call
     * @return the binder, or {@code null} if no binder has been set for this intent
     */
    public IBinder getBinder(Intent intent) {
        BoundInfo boundInfo = getOrCreateBoundInfo(intent);
        return boundInfo.getIBinder();
    }

    /**
     * Checks whether a non-null {@link IBinder} has been recorded for the given bind intent.
     *
     * @param intent the intent used in the bind call
     * @return {@code true} if a binder exists for this intent
     */
    public boolean hasBinder(Intent intent) {
        BoundInfo boundInfo = getOrCreateBoundInfo(intent);
        return boundInfo.getIBinder() != null;
    }

    /**
     * Registers a new {@link IBinder} for the given intent and links a death recipient that
     * automatically removes the binding entry when the remote process dies.
     *
     * @param intent   the intent used in the bind call
     * @param iBinder  the binder returned by the service
     */
    public void addBinder(Intent intent, final IBinder iBinder) {
        final Intent.FilterComparison filterComparison = new Intent.FilterComparison(intent);
        BoundInfo boundInfo = getOrCreateBoundInfo(intent);
        if (boundInfo == null) {
            boundInfo = new BoundInfo();
            mBounds.put(filterComparison, boundInfo);
        }

        boundInfo.setIBinder(iBinder);
        try {
            iBinder.linkToDeath(new IBinder.DeathRecipient() {
                @Override
                public void binderDied() {
                    iBinder.unlinkToDeath(this, 0);
                    mBounds.remove(filterComparison);
                }
            }, 0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Increments the bind connection count for the given intent.
     *
     * @param intent the intent used in the bind call
     */
    public void incrementAndGetBindCount(Intent intent) {
        BoundInfo boundInfo = getOrCreateBoundInfo(intent);
        boundInfo.incrementAndGetBindCount();
    }

    /**
     * Decrements the bind connection count for the given intent.
     *
     * @param intent the intent used in the unbind call
     * @return {@code true} if the bind count has reached zero (i.e., all clients unbound)
     */
    public boolean decreaseConnectionCount(Intent intent) {
        Intent.FilterComparison filterComparison = new Intent.FilterComparison(intent);
        BoundInfo boundInfo = mBounds.get(filterComparison);
        if (boundInfo == null) {
            return true;
        }
        int i = boundInfo.decrementAndGetBindCount();
        return i <= 0;
    }

    /**
     * Returns the existing {@link BoundInfo} for the given intent, or creates and registers a new one
     * if none exists yet.
     *
     * @param intent the bind intent
     * @return the bound info (never {@code null})
     */
    public BoundInfo getOrCreateBoundInfo(Intent intent) {
        Intent.FilterComparison filterComparison = new Intent.FilterComparison(intent);
        BoundInfo boundInfo = mBounds.get(filterComparison);
        if (boundInfo == null) {
            boundInfo = new BoundInfo();
            mBounds.put(filterComparison, boundInfo);
        }
        return boundInfo;
    }

    /**
     * Returns whether this service was re-bound after a previous unbind-all.
     *
     * @return {@code true} if this is a rebind
     */
    public boolean isRebind() {
        return rebind;
    }

    /**
     * Marks whether this service is being re-bound.
     *
     * @param rebind {@code true} if the service is being re-bound
     */
    public void setRebind(boolean rebind) {
        this.rebind = rebind;
    }
}
