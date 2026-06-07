package com.vcore.core.system.pm;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents the per-user state of a package in the virtual environment.
 *
 * <p>Tracks whether a package is installed, stopped, or hidden for a specific virtual user.
 * Supports copy construction for safe state snapshots and Parcel serialization for IPC.</p>
 */
public class BPackageUserState implements Parcelable {

    /** Whether the package is installed for this user. */
    public boolean installed;

    /** Whether the package is in a stopped state. */
    public final boolean stopped;

    /** Whether the package is hidden from the user. */
    public final boolean hidden;

    /**
     * Creates a default user state with installed=false, stopped=true, hidden=false.
     */
    public BPackageUserState() {
        this.installed = false;
        this.stopped = true;
        this.hidden = false;
    }

    /**
     * Factory method that creates a user state marked as installed.
     *
     * @return a new {@link BPackageUserState} with installed=true
     */
    public static BPackageUserState create() {
        BPackageUserState state = new BPackageUserState();
        state.installed = true;
        return state;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.installed ? (byte) 1 : (byte) 0);
        dest.writeByte(this.stopped ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hidden ? (byte) 1 : (byte) 0);
    }

    protected BPackageUserState(Parcel in) {
        this.installed = in.readByte() != 0;
        this.stopped = in.readByte() != 0;
        this.hidden = in.readByte() != 0;
    }

    /**
     * Creates a copy of the given user state.
     *
     * @param state the state to copy
     */
    public BPackageUserState(BPackageUserState state) {
        this.installed = state.installed;
        this.stopped = state.stopped;
        this.hidden = state.hidden;
    }

    public static final Parcelable.Creator<BPackageUserState> CREATOR = new Parcelable.Creator<BPackageUserState>() {
        @Override
        public BPackageUserState createFromParcel(Parcel source) {
            return new BPackageUserState(source);
        }

        @Override
        public BPackageUserState[] newArray(int size) {
            return new BPackageUserState[size];
        }
    };
}
