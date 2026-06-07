package com.vcore.entity.am;

import android.app.ActivityManager;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Parcelable wrapper around a list of {@link ActivityManager.RunningServiceInfo} objects.
 * Used to transfer the virtual environment's running-service list across Binder boundaries,
 * for example when a caller invokes {@code ActivityManager.getRunningServices()} inside
 * the container.
 */
public class RunningServiceInfo implements Parcelable {
    /** The list of running service info records inside the virtual environment. */
    public final List<ActivityManager.RunningServiceInfo> mRunningServiceInfoList;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.mRunningServiceInfoList);
    }

    /** Default constructor, initializes an empty service info list. */
    public RunningServiceInfo() {
        this.mRunningServiceInfoList = new ArrayList<>();
    }

    /**
     * Constructs from a Parcel by reading the typed list of running service info objects.
     *
     * @param in the Parcel to deserialize from
     */
    protected RunningServiceInfo(Parcel in) {
        this.mRunningServiceInfoList = in.createTypedArrayList(ActivityManager.RunningServiceInfo.CREATOR);
    }

    /** Parcelable {@code Creator} for {@code RunningServiceInfo}. */
    public static final Parcelable.Creator<RunningServiceInfo> CREATOR = new Parcelable.Creator<RunningServiceInfo>() {
        @Override
        public RunningServiceInfo createFromParcel(Parcel source) {
            return new RunningServiceInfo(source);
        }

        @Override
        public RunningServiceInfo[] newArray(int size) {
            return new RunningServiceInfo[size];
        }
    };
}
