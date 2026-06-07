package com.vcore.core.system.user;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents information about a virtual user in the BlackBox environment.
 * <p>
 * Each virtual user has a unique ID, a status (enabled/disabled), an optional
 * display name, and a creation timestamp. Implements {@link Parcelable} for
 * IPC transport and persistent storage.
 */
public class BUserInfo implements Parcelable {
    /** The unique virtual user ID. */
    public int id;

    /** The current status of this user (ENABLE or DISABLE). */
    public BUserStatus status;

    /** The display name of this user. */
    public String name;

    /** The creation timestamp in epoch milliseconds. */
    public long createTime;

    /** Package-private default constructor. */
    BUserInfo() { }

    /** {@inheritDoc} */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Writes this user info to a Parcel.
     *
     * @param dest  the Parcel to write to
     * @param flags additional flags for Parcelable writing
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.status == null ? -1 : this.status.ordinal());
        dest.writeString(this.name);
        dest.writeLong(this.createTime);
    }

    /**
     * Constructs a BUserInfo by reading from a Parcel.
     *
     * @param in the Parcel to read from
     */
    protected BUserInfo(Parcel in) {
        this.id = in.readInt();
        int tmpStatus = in.readInt();
        this.status = tmpStatus == -1 ? null : BUserStatus.values()[tmpStatus];
        this.name = in.readString();
        this.createTime = in.readLong();
    }

    /** Parcelable CREATOR for BUserInfo instances. */
    public static final Creator<BUserInfo> CREATOR = new Creator<BUserInfo>() {

        @Override
        public BUserInfo createFromParcel(Parcel source) {
            return new BUserInfo(source);
        }

        @Override
        public BUserInfo[] newArray(int size) {
            return new BUserInfo[size];
        }
    };

    /**
     * Returns a string representation of this user info for debugging.
     *
     * @return a formatted string with id, status, name, and createTime
     */
    @Override
    public String toString() {
        return "BUserInfo{" + "id=" + id + ", status=" + status + ", name='" + name + '\'' + ", createTime=" + createTime + '}';
    }
}
