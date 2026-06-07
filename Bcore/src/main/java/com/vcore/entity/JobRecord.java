package com.vcore.entity;

import android.app.job.JobInfo;
import android.app.job.JobService;
import android.content.pm.ServiceInfo;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Parcelable record that binds a {@link JobInfo} scheduling request with its associated
 * {@link ServiceInfo}. Used by the virtual JobScheduler subsystem to track which scheduled jobs
 * belong to which services inside the container. The {@code mJobService} field is not parcelled
 * because it is a live runtime reference resolved on demand.
 */
public class JobRecord implements Parcelable {
    /** The job scheduling parameters (constraints, interval, extras, etc.). */
    public JobInfo mJobInfo;

    /** Manifest-declared metadata for the service that owns this job. */
    public ServiceInfo mServiceInfo;

    /** Live reference to the running {@link JobService} instance (not parcelled). */
    public JobService mJobService;

    /** Default no-arg constructor. */
    public JobRecord() { }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mJobInfo, flags);
        dest.writeParcelable(this.mServiceInfo, flags);
    }

    /**
     * Constructs a {@code JobRecord} by reading parcelled fields from the given Parcel.
     *
     * @param in the Parcel to deserialize from
     */
    protected JobRecord(Parcel in) {
        this.mJobInfo = in.readParcelable(JobInfo.class.getClassLoader());
        this.mServiceInfo = in.readParcelable(ServiceInfo.class.getClassLoader());
    }

    /** Parcelable {@code Creator} for {@code JobRecord}. */
    public static final Creator<JobRecord> CREATOR = new Creator<JobRecord>() {
        @Override
        public JobRecord createFromParcel(Parcel source) {
            return new JobRecord(source);
        }

        @Override
        public JobRecord[] newArray(int size) {
            return new JobRecord[size];
        }
    };
}
