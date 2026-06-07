package com.vcore.entity.pm;

import android.os.Parcel;
import android.os.Parcelable;

import com.vcore.utils.Slog;

/**
 * Parcelable result object returned after a package installation attempt inside the virtual
 * environment. Carries a success flag, the target package name, and an optional error message.
 * Provides convenience methods to mark the result as failed with logging.
 */
public class InstallResult implements Parcelable {
    /** Logging tag. */
    public static final String TAG = "InstallResult";

    /** Whether the installation succeeded. Defaults to {@code true}. */
    public boolean success = true;

    /** The package name of the installed (or failed-to-install) application. */
    public String packageName;

    /** Human-readable message, typically populated on failure. */
    public String msg;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.success ? (byte) 1 : (byte) 0);
        dest.writeString(this.packageName);
        dest.writeString(this.msg);
    }

    /** Default no-arg constructor. */
    public InstallResult() { }

    /**
     * Constructs an {@code InstallResult} by reading all fields from a Parcel.
     *
     * @param in the Parcel to deserialize from
     */
    protected InstallResult(Parcel in) {
        this.success = in.readByte() != 0;
        this.packageName = in.readString();
        this.msg = in.readString();
    }

    /**
     * Marks this result as a failure for the given package, logs the error, and returns
     * {@code this} for fluent chaining.
     *
     * @param packageName the package that failed to install
     * @param msg         the error message
     * @return this instance (with {@link #success} set to {@code false})
     */
    public InstallResult installError(String packageName, String msg) {
        this.msg = msg;
        this.success = false;
        this.packageName = packageName;
        Slog.d(TAG, msg);
        return this;
    }

    /**
     * Marks this result as a failure with the given error message, logs it, and returns
     * {@code this} for fluent chaining. Does not set the package name.
     *
     * @param msg the error message
     * @return this instance (with {@link #success} set to {@code false})
     */
    public InstallResult installError(String msg) {
        this.msg = msg;
        this.success = false;
        Slog.d(TAG, msg);
        return this;
    }

    /** Parcelable {@code Creator} for {@code InstallResult}. */
    public static final Parcelable.Creator<InstallResult> CREATOR = new Parcelable.Creator<InstallResult>() {
        @Override
        public InstallResult createFromParcel(Parcel source) {
            return new InstallResult(source);
        }

        @Override
        public InstallResult[] newArray(int size) {
            return new InstallResult[size];
        }
    };
}
