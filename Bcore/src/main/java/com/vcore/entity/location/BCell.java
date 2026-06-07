package com.vcore.entity.location;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents a cellular base station (cell tower) used for network-based location spoofing
 * inside the virtual environment. Encodes standard GSM/LTE/WCDMA/CDMA cell identifiers.
 * <p>
 * Key acronyms:
 * <ul>
 *   <li>MCC &mdash; Mobile Country Code (e.g. 460 for China)</li>
 *   <li>MNC &mdash; Mobile Network Code (e.g. 00 for China Mobile, 01 for China Unicom)</li>
 *   <li>LAC/TAC &mdash; Location/Tracking Area Code (1&ndash;65535)</li>
 *   <li>CID/CI &mdash; Cell Identity (2G: 1&ndash;65535; 3G/4G: 1&ndash;268435455)</li>
 *   <li>TYPE &mdash; Radio access technology (Cdma, Lte, Gsm, Wcdma)</li>
 * </ul>
 *
 * @see <a href="https://liuschen.top/2016/09/15/BLocation.html">Reference blog</a>
 */
public class BCell implements Parcelable {
    /** Mobile Country Code (e.g. 460 for China). */
    public int MCC;

    /** Mobile Network Code (e.g. 00=China Mobile, 01=China Unicom, 11=China Telecom 4G). */
    public int MNC;

    /** Location Area Code / Tracking Area Code (1&ndash;65535). */
    public int LAC;

    /** Cell Identity / Cell ID. Range depends on radio type. */
    public int CID;

    /** Radio access technology type (e.g. GSM, LTE). */
    public int TYPE;

    /** Constant indicating GSM phone type. */
    public static final int PHONE_TYPE_GSM = 1;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.MCC);
        dest.writeInt(this.MNC);
        dest.writeInt(this.LAC);
        dest.writeInt(this.CID);
        dest.writeInt(this.TYPE);
    }

    /** Default no-arg constructor. */
    public BCell() { }

    /**
     * Constructs a GSM cell with the given identifiers. The {@link #TYPE} is automatically
     * set to {@link #PHONE_TYPE_GSM}.
     *
     * @param MCC Mobile Country Code
     * @param MNC Mobile Network Code
     * @param LAC Location Area Code
     * @param CID Cell Identity
     */
    public BCell(int MCC, int MNC, int LAC, int CID) {
        this.TYPE = PHONE_TYPE_GSM;
        this.MCC = MCC;
        this.CID = CID;
        this.MNC = MNC;
        this.LAC = LAC;
    }

    /**
     * Constructs a {@code BCell} by reading all fields from a Parcel.
     *
     * @param in the Parcel to deserialize from
     */
    public BCell(Parcel in) {
        this.MCC = in.readInt();
        this.MNC = in.readInt();
        this.LAC = in.readInt();
        this.CID = in.readInt();
        this.TYPE = in.readInt();
    }

    /** Parcelable {@code Creator} for {@code BCell}. */
    public static final Parcelable.Creator<BCell> CREATOR = new Parcelable.Creator<BCell>() {
        @Override
        public BCell createFromParcel(Parcel source) {
            return new BCell(source);
        }

        @Override
        public BCell[] newArray(int size) {
            return new BCell[size];
        }
    };
}
