package com.vcore.entity.am;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Parcelable data container that bundles together the information required to deliver a broadcast
 * to a {@link android.content.BroadcastReceiver} inside the virtual environment. Carries the
 * original broadcast {@link Intent}, the receiver's {@link ActivityInfo}, and the associated
 * {@link PendingResultData} for ordered/sticky broadcast result handling.
 */
public class ReceiverData implements Parcelable {
    /** The broadcast intent to deliver to the receiver. */
    public Intent intent;

    /** Manifest metadata for the target broadcast receiver component. */
    public ActivityInfo activityInfo;

    /** Pending result state for ordered or sticky broadcasts (may be {@code null} for normal broadcasts). */
    public PendingResultData data;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.intent, flags);
        dest.writeParcelable(this.activityInfo, flags);
        dest.writeParcelable(this.data, flags);
    }

    /** Default no-arg constructor. */
    public ReceiverData() { }

    /**
     * Constructs a {@code ReceiverData} by reading all fields from a Parcel.
     *
     * @param in the Parcel to deserialize from
     */
    protected ReceiverData(Parcel in) {
        this.intent = in.readParcelable(Intent.class.getClassLoader());
        this.activityInfo = in.readParcelable(ActivityInfo.class.getClassLoader());
        this.data = in.readParcelable(PendingResultData.class.getClassLoader());
    }

    /** Parcelable {@code Creator} for {@code ReceiverData}. */
    public static final Parcelable.Creator<ReceiverData> CREATOR = new Parcelable.Creator<ReceiverData>() {
        @Override
        public ReceiverData createFromParcel(Parcel source) {
            return new ReceiverData(source);
        }

        @Override
        public ReceiverData[] newArray(int size) {
            return new ReceiverData[size];
        }
    };
}
