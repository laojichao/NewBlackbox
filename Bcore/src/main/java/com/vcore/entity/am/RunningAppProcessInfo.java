package com.vcore.entity.am;

import android.app.ActivityManager;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Parcelable wrapper around a list of {@link ActivityManager.RunningAppProcessInfo} objects.
 * Used to transfer the virtual environment's process list across Binder boundaries, for
 * example when a caller invokes {@code ActivityManager.getRunningAppProcesses()} inside
 * the container.
 */
public class RunningAppProcessInfo implements Parcelable {
    /** The list of running app process info records inside the virtual environment. */
    public final List<ActivityManager.RunningAppProcessInfo> mAppProcessInfoList;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.mAppProcessInfoList);
    }

    /** Default constructor, initializes an empty process list. */
    public RunningAppProcessInfo() {
        this.mAppProcessInfoList = new ArrayList<>();
    }

    /**
     * Constructs from a Parcel by reading the typed list of process info objects.
     *
     * @param in the Parcel to deserialize from
     */
    protected RunningAppProcessInfo(Parcel in) {
        this.mAppProcessInfoList = in.createTypedArrayList(ActivityManager.RunningAppProcessInfo.CREATOR);
    }

    /** Parcelable {@code Creator} for {@code RunningAppProcessInfo}. */
    public static final Parcelable.Creator<RunningAppProcessInfo> CREATOR = new Parcelable.Creator<RunningAppProcessInfo>() {
        @Override
        public RunningAppProcessInfo createFromParcel(Parcel source) {
            return new RunningAppProcessInfo(source);
        }

        @Override
        public RunningAppProcessInfo[] newArray(int size) {
            return new RunningAppProcessInfo[size];
        }
    };
}
