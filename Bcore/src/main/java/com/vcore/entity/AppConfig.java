package com.vcore.entity;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Parcelable configuration object representing the client-side settings for a virtual application
 * running inside the BlackBox container. Carries the identity and process context needed to route
 * IPC calls back to the correct virtual process.
 */
public class AppConfig implements Parcelable {
    /** Parcelable key used to store/retrieve this config from Bundles. */
    public static final String KEY = "BlackBox_client_config";

    /** Package name of the virtual application. */
    public String packageName;

    /** Process name assigned to the virtual application (may differ from packageName for multi-process apps). */
    public String processName;

    /** Virtual PID assigned to the process inside the container. */
    public int bPID;

    /** Virtual UID assigned to the process inside the container. */
    public int bUID;

    /** Actual Linux UID of the process on the host. */
    public int uid;

    /** Virtual user ID (multi-user / work-profile style) under which this app runs. */
    public int userId;

    /** UID of the caller that initiated the current Binder transaction, used for permission checks. */
    public int callingBUid;

    /** Binder token used to identify and communicate with the client process. */
    public IBinder token;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.packageName);
        dest.writeString(this.processName);
        dest.writeInt(this.bPID);
        dest.writeInt(this.bUID);
        dest.writeInt(this.uid);
        dest.writeInt(this.userId);
        dest.writeInt(this.callingBUid);
        dest.writeStrongBinder(token);
    }

    /** Default no-arg constructor. */
    public AppConfig() { }

    /**
     * Constructs an {@code AppConfig} by reading all fields from a Parcel.
     *
     * @param in the Parcel to read from
     */
    protected AppConfig(Parcel in) {
        this.packageName = in.readString();
        this.processName = in.readString();
        this.bPID = in.readInt();
        this.bUID = in.readInt();
        this.uid = in.readInt();
        this.userId = in.readInt();
        this.callingBUid = in.readInt();
        this.token = in.readStrongBinder();
    }

    /** Parcelable {@code Creator} that deserializes {@code AppConfig} instances from Parcels. */
    public static final Parcelable.Creator<AppConfig> CREATOR = new Parcelable.Creator<AppConfig>() {
        @Override
        public AppConfig createFromParcel(Parcel source) {
            return new AppConfig(source);
        }

        @Override
        public AppConfig[] newArray(int size) {
            return new AppConfig[size];
        }
    };
}
