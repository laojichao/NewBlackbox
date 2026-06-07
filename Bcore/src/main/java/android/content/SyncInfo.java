package android.content;

import android.accounts.Account;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Information about the sync operation that is currently underway.
 *
 * <p>Contains details about the account being synced, the content provider authority,
 * and the start time of the sync operation. This object is typically returned by
 * the sync framework to report active sync operations.</p>
 *
 * @see SyncStatusInfo
 */
public class SyncInfo implements Parcelable {
    /**
     * Used when the caller receiving this object doesn't have permission to access the accounts
     * on device.
     * @See Manifest.permission.GET_ACCOUNTS
     */
    private static final Account REDACTED_ACCOUNT = new Account("*****", "*****");

    /**
     * The unique identifier for the content authority being synced.
     *
     * @hide
     */
    public final int authorityId;

    /**
     * The {@link Account} that is currently being synced.
     */
    public final Account account;

    /**
     * The authority of the provider that is currently being synced.
     */
    public final String authority;

    /**
     * The start time of the current sync operation in milliseconds since boot.
     * This is represented in elapsed real time.
     * See {@link android.os.SystemClock#elapsedRealtime()}.
     */
    public final long startTime;

    /**
     * Creates a SyncInfo object with an unusable Account. Used when the caller receiving this
     * object doesn't have access to the accounts on the device.
     *
     * @param authorityId   the unique identifier for the content authority
     * @param authority     the authority string of the content provider being synced
     * @param startTime     the elapsed real-time start time of the sync in milliseconds
     * @return a new {@link SyncInfo} instance with a redacted account
     * @hide
     * @See Manifest.permission.GET_ACCOUNTS
     */
    public static SyncInfo createAccountRedacted(
        int authorityId, String authority, long startTime) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Constructs a new {@link SyncInfo} with the specified parameters.
     *
     * @param authorityId   the unique identifier for the content authority
     * @param account       the {@link Account} being synced
     * @param authority     the authority string of the content provider being synced
     * @param startTime     the elapsed real-time start time of the sync in milliseconds
     * @hide
     */
    public SyncInfo(int authorityId, Account account, String authority, long startTime) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Copy constructor that creates a {@link SyncInfo} from an existing instance.
     *
     * @param other the {@link SyncInfo} instance to copy
     * @hide
     */
    public SyncInfo(SyncInfo other) {
        throw new RuntimeException("Stub!");
    }

    /**
     * {@inheritDoc}
     *
     * @return 0, as this object contains no special objects
     * @hide
     */
    public int describeContents() {
        return 0;
    }

    /**
     * Writes this {@link SyncInfo} to the given {@link Parcel}.
     *
     * @param parcel the {@link Parcel} to write to
     * @param flags  additional flags about how the object should be written
     * @hide
     */
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(authorityId);
        parcel.writeParcelable(account, flags);
        parcel.writeString(authority);
        parcel.writeLong(startTime);
    }

    /**
     * Constructs a {@link SyncInfo} by reading from the given {@link Parcel}.
     *
     * @param parcel the {@link Parcel} to read from
     */
    SyncInfo(Parcel parcel) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Parcelable creator for {@link SyncInfo} instances.
     */
    public static final Creator<SyncInfo> CREATOR = new Creator<SyncInfo>() {
        public SyncInfo createFromParcel(Parcel in) {
            return new SyncInfo(in);
        }

        public SyncInfo[] newArray(int size) {
            return new SyncInfo[size];
        }
    };
}
