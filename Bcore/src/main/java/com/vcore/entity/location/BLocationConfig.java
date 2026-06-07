package com.vcore.entity.location;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Parcelable container for a complete virtual location configuration. Bundles a spoofing mode
 * pattern, primary cell tower data, all cell towers, neighboring cell info, and the GPS
 * coordinates together so they can be transferred across process boundaries in one parcel.
 */
public class BLocationConfig implements Parcelable {
    /** Spoofing pattern/mode identifier (e.g. GPS-only, cell-only, combined). */
    public int pattern;

    /** The primary cell tower to spoof. */
    public BCell cell;

    /** List of all cell towers associated with the spoofed location. */
    public List<BCell> allCell;

    /** List of neighboring cell towers to report alongside the primary cell. */
    public List<BCell> neighboringCellInfo;

    /** The GPS coordinates to spoof. */
    public BLocation location;

    @Override
    public int describeContents() {
        return 0;
    }

    /** Default no-arg constructor. */
    public BLocationConfig() { }

    /**
     * Constructs a {@code BLocationConfig} by reading all fields from a Parcel.
     *
     * @param in the Parcel to deserialize from
     */
    public BLocationConfig(Parcel in) {
        refresh(in);
    }

    /**
     * Reads (or re-reads) all fields from the given Parcel. Can be used to reinitialize this
     * instance with new data from a fresh Parcel.
     *
     * @param in the Parcel to read from
     */
    public void refresh(Parcel in) {
        this.pattern = in.readInt();
        this.cell = in.readParcelable(BCell.class.getClassLoader());
        this.allCell = in.createTypedArrayList(BCell.CREATOR);
        this.neighboringCellInfo = in.createTypedArrayList(BCell.CREATOR);
        this.location = in.readParcelable(BLocation.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.pattern);
        dest.writeParcelable(this.cell, flags);
        dest.writeTypedList(this.allCell);
        dest.writeTypedList(this.neighboringCellInfo);
        dest.writeParcelable(this.location, flags);
    }

    /** Parcelable {@code Creator} for {@code BLocationConfig}. */
    public static final Creator<BLocationConfig> CREATOR = new Creator<BLocationConfig>() {
        @Override
        public BLocationConfig createFromParcel(Parcel source) {
            return new BLocationConfig(source);
        }

        @Override
        public BLocationConfig[] newArray(int size) {
            return new BLocationConfig[size];
        }
    };
}
