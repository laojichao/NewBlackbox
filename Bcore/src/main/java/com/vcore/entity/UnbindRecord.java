package com.vcore.entity;

import android.content.ComponentName;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Parcelable record used during service unbind operations to carry the bind count, start ID,
 * and component name of the service being unbound. Enables the virtual ActivityManager to
 * decide whether a full unbind (and potential {@code onUnbind} / {@code onDestroy} lifecycle
 * callback) should be triggered.
 */
public class UnbindRecord implements Parcelable {
    /** Number of active bind connections at the time of unbind. */
    private int mBindCount;

    /** The start ID associated with the most recent {@code onStartCommand} call. */
    private int mStartId;

    /** The {@link ComponentName} of the service being unbound. */
    private ComponentName mComponentName;

    /**
     * Returns the start ID.
     *
     * @return the start ID
     */
    public int getStartId() {
        return mStartId;
    }

    /**
     * Sets the start ID.
     *
     * @param startId the start ID to set
     */
    public void setStartId(int startId) {
        mStartId = startId;
    }

    /**
     * Sets the number of active bind connections.
     *
     * @param bindCount the bind count to set
     */
    public void setBindCount(int bindCount) {
        mBindCount = bindCount;
    }

    /**
     * Returns the component name of the service being unbound.
     *
     * @return the component name
     */
    public ComponentName getComponentName() {
        return mComponentName;
    }

    /**
     * Sets the component name of the service being unbound.
     *
     * @param componentName the component name to set
     */
    public void setComponentName(ComponentName componentName) {
        mComponentName = componentName;
    }

    /**
     * Returns the Parcelable {@link Creator} for {@code UnbindRecord}.
     *
     * @return the CREATOR instance
     */
    public static Creator<UnbindRecord> getCREATOR() {
        return CREATOR;
    }

    /** Default no-arg constructor. */
    public UnbindRecord() { }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mBindCount);
        dest.writeInt(this.mStartId);
        dest.writeParcelable(this.mComponentName, flags);
    }

    /**
     * Constructs an {@code UnbindRecord} by reading fields from a Parcel.
     *
     * @param in the Parcel to deserialize from
     */
    protected UnbindRecord(Parcel in) {
        this.mBindCount = in.readInt();
        this.mStartId = in.readInt();
        this.mComponentName = in.readParcelable(ComponentName.class.getClassLoader());
    }

    /** Parcelable {@code Creator} for {@code UnbindRecord}. */
    public static final Creator<UnbindRecord> CREATOR = new Creator<UnbindRecord>() {
        @Override
        public UnbindRecord createFromParcel(Parcel source) {
            return new UnbindRecord(source);
        }

        @Override
        public UnbindRecord[] newArray(int size) {
            return new UnbindRecord[size];
        }
    };
}
