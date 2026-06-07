package com.vcore.entity.am;

import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.UUID;

import com.vcore.utils.compat.BuildCompat;

/**
 * Parcelable snapshot of a {@link BroadcastReceiver.PendingResult} that allows the broadcast
 * result state to be transferred across process boundaries inside the virtual environment.
 * <p>
 * Uses reflective access (via the {@code black.android.content} shadow classes) to read and
 * reconstruct PendingResult internals, adapting field layouts between Android M+ and earlier
 * versions. A unique {@link #mBToken} is generated to correlate this pending result across
 * Binder transactions.
 */
public class PendingResultData implements Parcelable {
    /** PendingResult type code (e.g. ordered, sticky). */
    public final int mType;

    /** Whether this is an ordered broadcast hint. */
    public final boolean mOrderedHint;

    /** Whether this is an initial sticky broadcast hint. */
    public final boolean mInitialStickyHint;

    /** Binder token associated with the original PendingResult. */
    public final IBinder mToken;

    /** The user ID that sent the broadcast. */
    public final int mSendingUser;

    /** Flags field present on Android M+. */
    public int mFlags;

    /** The result code to propagate (e.g. {@code Activity.RESULT_OK}). */
    public int mResultCode;

    /** The result data string payload. */
    public final String mResultData;

    /** The result extras Bundle. */
    public final Bundle mResultExtras;

    /** Whether the broadcast was aborted by the receiver. */
    public final boolean mAbortBroadcast;

    /** Whether the PendingResult has already been finished. */
    public final boolean mFinished;

    /** Unique token generated for this PendingResultData instance to correlate across IPC. */
    public final String mBToken;

    /**
     * Constructs a {@code PendingResultData} by reflectively extracting all fields from
     * a real {@link BroadcastReceiver.PendingResult}. Uses different shadow classes depending
     * on the Android version (M+ vs pre-M).
     *
     * @param pendingResult the PendingResult to snapshot
     */
    public PendingResultData(BroadcastReceiver.PendingResult pendingResult) {
        this.mBToken = UUID.randomUUID().toString();
        if (BuildCompat.isM()) {
            this.mType = black.android.content.BroadcastReceiver.PendingResultM.mType.get(pendingResult);
            this.mOrderedHint = black.android.content.BroadcastReceiver.PendingResultM.mOrderedHint.get(pendingResult);
            this.mInitialStickyHint = black.android.content.BroadcastReceiver.PendingResultM.mInitialStickyHint.get(pendingResult);
            this.mToken = black.android.content.BroadcastReceiver.PendingResultM.mToken.get(pendingResult);
            this.mSendingUser = black.android.content.BroadcastReceiver.PendingResultM.mSendingUser.get(pendingResult);
            this.mFlags = black.android.content.BroadcastReceiver.PendingResultM.mFlags.get(pendingResult);
            this.mResultData = black.android.content.BroadcastReceiver.PendingResultM.mResultData.get(pendingResult);
            this.mResultExtras = black.android.content.BroadcastReceiver.PendingResultM.mResultExtras.get(pendingResult);
            this.mAbortBroadcast = black.android.content.BroadcastReceiver.PendingResultM.mAbortBroadcast.get(pendingResult);
            this.mFinished = black.android.content.BroadcastReceiver.PendingResultM.mFinished.get(pendingResult);
        } else {
            this.mType = black.android.content.BroadcastReceiver.PendingResult.mType.get(pendingResult);
            this.mOrderedHint = black.android.content.BroadcastReceiver.PendingResult.mOrderedHint.get(pendingResult);
            this.mInitialStickyHint = black.android.content.BroadcastReceiver.PendingResult.mInitialStickyHint.get(pendingResult);
            this.mToken = black.android.content.BroadcastReceiver.PendingResult.mToken.get(pendingResult);
            this.mSendingUser = black.android.content.BroadcastReceiver.PendingResult.mSendingUser.get(pendingResult);
            this.mResultData = black.android.content.BroadcastReceiver.PendingResult.mResultData.get(pendingResult);
            this.mResultExtras = black.android.content.BroadcastReceiver.PendingResult.mResultExtras.get(pendingResult);
            this.mAbortBroadcast = black.android.content.BroadcastReceiver.PendingResult.mAbortBroadcast.get(pendingResult);
            this.mFinished = black.android.content.BroadcastReceiver.PendingResult.mFinished.get(pendingResult);
        }
    }

    /**
     * Reconstructs a real {@link BroadcastReceiver.PendingResult} from the stored fields
     * using reflective construction. Adapts the constructor signature based on Android version
     * (M+ includes the {@code flags} parameter).
     *
     * @return a new PendingResult with the snapshotted state
     */
    public BroadcastReceiver.PendingResult build() {
        if (BuildCompat.isM()) {
            return black.android.content.BroadcastReceiver.PendingResultM._new.newInstance(mResultCode, mResultData, mResultExtras, mType, mOrderedHint, mInitialStickyHint, mToken, mSendingUser, mFlags);
        }
        return black.android.content.BroadcastReceiver.PendingResult._new.newInstance(mResultCode, mResultData, mResultExtras, mType, mOrderedHint, mInitialStickyHint, mToken, mSendingUser);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mType);
        dest.writeByte(this.mOrderedHint ? (byte) 1 : (byte) 0);
        dest.writeByte(this.mInitialStickyHint ? (byte) 1 : (byte) 0);
        dest.writeStrongBinder(this.mToken);
        dest.writeInt(this.mSendingUser);
        dest.writeInt(this.mFlags);
        dest.writeInt(this.mResultCode);
        dest.writeString(this.mResultData);
        dest.writeBundle(this.mResultExtras);
        dest.writeByte(this.mAbortBroadcast ? (byte) 1 : (byte) 0);
        dest.writeByte(this.mFinished ? (byte) 1 : (byte) 0);
        dest.writeString(this.mBToken);
    }

    /**
     * Constructs a {@code PendingResultData} by reading all fields from a Parcel.
     *
     * @param in the Parcel to deserialize from
     */
    protected PendingResultData(Parcel in) {
        this.mType = in.readInt();
        this.mOrderedHint = in.readByte() != 0;
        this.mInitialStickyHint = in.readByte() != 0;
        this.mToken = in.readStrongBinder();
        this.mSendingUser = in.readInt();
        this.mFlags = in.readInt();
        this.mResultCode = in.readInt();
        this.mResultData = in.readString();
        this.mResultExtras = in.readBundle();
        this.mAbortBroadcast = in.readByte() != 0;
        this.mFinished = in.readByte() != 0;
        this.mBToken = in.readString();
    }

    /** Parcelable {@code Creator} for {@code PendingResultData}. */
    public static final Parcelable.Creator<PendingResultData> CREATOR = new Parcelable.Creator<PendingResultData>() {
        @Override
        public PendingResultData createFromParcel(Parcel source) {
            return new PendingResultData(source);
        }

        @Override
        public PendingResultData[] newArray(int size) {
            return new PendingResultData[size];
        }
    };

    @NonNull
    @Override
    public String toString() {
        return "PendingResultData{" + "mType=" + mType + ", mOrderedHint=" + mOrderedHint + ", mInitialStickyHint=" + mInitialStickyHint + ", mToken=" + mToken +
                ", mSendingUser=" + mSendingUser + ", mFlags=" + mFlags + ", mResultCode=" + mResultCode + ", mResultData='" + mResultData + '\'' +
                ", mResultExtras=" + mResultExtras + ", mAbortBroadcast=" + mAbortBroadcast + ", mFinished=" + mFinished + '}';
    }
}
