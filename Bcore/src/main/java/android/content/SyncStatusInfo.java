package android.content;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;

/**
 * Stub implementation of Android's {@code SyncStatusInfo} class.
 *
 * <p>Tracks statistics and status information about sync operations for a particular
 * content authority. This includes the total elapsed time, number of syncs by source,
 * last success/failure times, and periodic sync scheduling data.</p>
 *
 * @see SyncInfo
 */
public class SyncStatusInfo implements Parcelable {
    /** Serialization version for this class. */
    static final int VERSION = 2;

    /**
     * The unique identifier for the content authority this status info tracks.
     */
    public final int authorityId;

    /** Total elapsed time in milliseconds spent performing syncs. */
    public long totalElapsedTime;

    /** Total number of sync operations performed. */
    public int numSyncs;

    /** Number of syncs triggered by polling. */
    public int numSourcePoll;

    /** Number of syncs triggered by the server. */
    public int numSourceServer;

    /** Number of syncs triggered by local changes. */
    public int numSourceLocal;

    /** Number of syncs triggered by the user. */
    public int numSourceUser;

    /** Number of syncs triggered on a periodic schedule. */
    public int numSourcePeriodic;

    /** The time of the last successful sync, in milliseconds since epoch. */
    public long lastSuccessTime;

    /** The source that triggered the last successful sync. */
    public int lastSuccessSource;

    /** The time of the last failed sync, in milliseconds since epoch. */
    public long lastFailureTime;

    /** The source that triggered the last failed sync. */
    public int lastFailureSource;

    /** The error message from the last failed sync, or {@code null} if none. */
    public String lastFailureMesg;

    /** The time of the initial failure, used to track consecutive failures. */
    public long initialFailureTime;

    /** Whether a sync is currently pending for this authority. */
    public boolean pending;

    /** Whether this sync status has been initialized. */
    public boolean initialize;

    // Warning: It is up to the external caller to ensure there are
    // no race conditions when accessing this list
    private ArrayList<Long> periodicSyncTimes;

    private static final String TAG = "Sync";

    /**
     * Constructs a new {@link SyncStatusInfo} for the given authority ID.
     *
     * @param authorityId the unique identifier for the content authority
     */
    public SyncStatusInfo(int authorityId) {
        this.authorityId = authorityId;
    }

    /**
     * {@inheritDoc}
     *
     * @return 0, as this object contains no special objects
     */
    public int describeContents() {
        return 0;
    }

    /**
     * Writes this {@link SyncStatusInfo} to the given {@link Parcel}.
     *
     * @param parcel the {@link Parcel} to write to
     * @param flags  additional flags about how the object should be written
     */
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(VERSION);
        parcel.writeInt(authorityId);
        parcel.writeLong(totalElapsedTime);
        parcel.writeInt(numSyncs);
        parcel.writeInt(numSourcePoll);
        parcel.writeInt(numSourceServer);
        parcel.writeInt(numSourceLocal);
        parcel.writeInt(numSourceUser);
        parcel.writeLong(lastSuccessTime);
        parcel.writeInt(lastSuccessSource);
        parcel.writeLong(lastFailureTime);
        parcel.writeInt(lastFailureSource);
        parcel.writeString(lastFailureMesg);
        parcel.writeLong(initialFailureTime);
        parcel.writeInt(pending ? 1 : 0);
        parcel.writeInt(initialize ? 1 : 0);

        if (periodicSyncTimes != null) {
            parcel.writeInt(periodicSyncTimes.size());
            for (long periodicSyncTime : periodicSyncTimes) {
                parcel.writeLong(periodicSyncTime);
            }
        } else {
            parcel.writeInt(-1);
        }
    }

    /**
     * Constructs a {@link SyncStatusInfo} by reading from the given {@link Parcel}.
     *
     * @param parcel the {@link Parcel} to read from
     */
    public SyncStatusInfo(Parcel parcel) {
        int version = parcel.readInt();
        if (version != VERSION && version != 1) {
            Log.w("SyncStatusInfo", "Unknown version: " + version);
        }

        authorityId = parcel.readInt();
        totalElapsedTime = parcel.readLong();
        numSyncs = parcel.readInt();
        numSourcePoll = parcel.readInt();
        numSourceServer = parcel.readInt();
        numSourceLocal = parcel.readInt();
        numSourceUser = parcel.readInt();
        lastSuccessTime = parcel.readLong();
        lastSuccessSource = parcel.readInt();
        lastFailureTime = parcel.readLong();
        lastFailureSource = parcel.readInt();
        lastFailureMesg = parcel.readString();
        initialFailureTime = parcel.readLong();
        pending = parcel.readInt() != 0;
        initialize = parcel.readInt() != 0;

        if (version == 1) {
            periodicSyncTimes = null;
        } else {
            int N = parcel.readInt();
            if (N < 0) {
                periodicSyncTimes = null;
            } else {
                periodicSyncTimes = new ArrayList<>();
                for (int i = 0; i < N; i++) {
                    periodicSyncTimes.add(parcel.readLong());
                }
            }
        }
    }

    /**
     * Copy constructor that creates a {@link SyncStatusInfo} from an existing instance.
     *
     * @param other the {@link SyncStatusInfo} instance to copy from
     */
    public SyncStatusInfo(SyncStatusInfo other) {
        authorityId = other.authorityId;
        totalElapsedTime = other.totalElapsedTime;
        numSyncs = other.numSyncs;
        numSourcePoll = other.numSourcePoll;
        numSourceServer = other.numSourceServer;
        numSourceLocal = other.numSourceLocal;
        numSourceUser = other.numSourceUser;
        numSourcePeriodic = other.numSourcePeriodic;
        lastSuccessTime = other.lastSuccessTime;
        lastSuccessSource = other.lastSuccessSource;
        lastFailureTime = other.lastFailureTime;
        lastFailureSource = other.lastFailureSource;
        lastFailureMesg = other.lastFailureMesg;
        initialFailureTime = other.initialFailureTime;
        pending = other.pending;
        initialize = other.initialize;

        if (other.periodicSyncTimes != null) {
            periodicSyncTimes = new ArrayList<>(other.periodicSyncTimes);
        }
    }

    /**
     * Sets the periodic sync time at the given index. Initializes any preceding
     * entries to zero if necessary.
     *
     * @param index the index into the periodic sync time list
     * @param when  the scheduled time for the periodic sync in milliseconds
     */
    public void setPeriodicSyncTime(int index, long when) {
        // The list is initialized lazily when scheduling occurs so we need to make sure
        // we initialize elements < index to zero (zero is ignore for scheduling purposes)
        ensurePeriodicSyncTimeSize(index);
        periodicSyncTimes.set(index, when);
    }

    /**
     * Returns the periodic sync time at the given index.
     *
     * @param index the index into the periodic sync time list
     * @return the scheduled time in milliseconds, or {@code 0} if the index is out of
     *         bounds or the list is not initialized
     */
    public long getPeriodicSyncTime(int index) {
        if (periodicSyncTimes != null && index < periodicSyncTimes.size()) {
            return periodicSyncTimes.get(index);
        }
        return 0;
    }

    /**
     * Removes the periodic sync time at the given index.
     *
     * @param index the index of the entry to remove from the periodic sync time list
     */
    public void removePeriodicSyncTime(int index) {
        if (periodicSyncTimes != null && index < periodicSyncTimes.size()) {
            periodicSyncTimes.remove(index);
        }
    }

    /**
     * Parcelable creator for {@link SyncStatusInfo} instances.
     */
    public static final Creator<SyncStatusInfo> CREATOR = new Creator<SyncStatusInfo>() {
        public SyncStatusInfo createFromParcel(Parcel in) {
            return new SyncStatusInfo(in);
        }

        public SyncStatusInfo[] newArray(int size) {
            return new SyncStatusInfo[size];
        }
    };

    /**
     * Ensures that the periodic sync time list is large enough to hold an entry
     * at the given index. Initializes missing entries to {@code 0}.
     *
     * @param index the minimum required index (inclusive)
     */
    private void ensurePeriodicSyncTimeSize(int index) {
        if (periodicSyncTimes == null) {
            periodicSyncTimes = new ArrayList<>(0);
        }

        final int requiredSize = index + 1;
        if (periodicSyncTimes.size() < requiredSize) {
            for (int i = periodicSyncTimes.size(); i < requiredSize; i++) {
                periodicSyncTimes.add((long) 0);
            }
        }
    }
}
