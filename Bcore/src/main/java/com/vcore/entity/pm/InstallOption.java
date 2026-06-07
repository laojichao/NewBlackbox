package com.vcore.entity.pm;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Bit-flag configuration for package installation options inside the virtual environment.
 * <p>
 * Supported flags:
 * <ul>
 *   <li>{@link #FLAG_SYSTEM} &mdash; Install as a system-level package</li>
 *   <li>{@link #FLAG_STORAGE} &mdash; Install from external storage path</li>
 *   <li>{@link #FLAG_XPOSED} &mdash; Mark as an Xposed/LSPosed module</li>
 *   <li>{@link #FLAG_URI_FILE} &mdash; Install from a content URI</li>
 * </ul>
 * Uses the builder pattern via static factory methods and chainable mutators.
 */
public class InstallOption implements Parcelable {
    /** Flag indicating a system-level installation. */
    public static final int FLAG_SYSTEM = 1;

    /** Flag indicating the APK resides on external storage. */
    public static final int FLAG_STORAGE = 1 << 1;

    /** Flag indicating the package is an Xposed/LSPosed module. */
    public static final int FLAG_XPOSED = 1 << 2;

    /** Flag indicating the source is a content URI rather than a file path. */
    public static final int FLAG_URI_FILE = 1 << 3;

    /** The combined bit-field of active flags. */
    public int flags = 0;

    /**
     * Creates an {@code InstallOption} with the {@link #FLAG_SYSTEM} flag set.
     *
     * @return a new option configured for system installation
     */
    public static InstallOption installBySystem() {
        InstallOption installOption = new InstallOption();
        installOption.flags = installOption.flags | FLAG_SYSTEM;
        return installOption;
    }

    /**
     * Creates an {@code InstallOption} with the {@link #FLAG_STORAGE} flag set.
     *
     * @return a new option configured for storage-based installation
     */
    public static InstallOption installByStorage() {
        InstallOption installOption = new InstallOption();
        installOption.flags = installOption.flags | FLAG_STORAGE;
        return installOption;
    }

    /**
     * Adds the {@link #FLAG_XPOSED} flag, marking this package as an Xposed module.
     *
     * @return this instance for chaining
     */
    public InstallOption makeXposed() {
        this.flags |= FLAG_XPOSED;
        return this;
    }

    /**
     * Adds the {@link #FLAG_URI_FILE} flag, indicating the install source is a content URI.
     *
     * @return this instance for chaining
     */
    public InstallOption makeUriFile() {
        this.flags |= FLAG_URI_FILE;
        return this;
    }

    /**
     * Tests whether a specific flag is set in the current flags bitmask.
     *
     * @param flag the flag constant to check (e.g. {@link #FLAG_SYSTEM})
     * @return {@code true} if the flag is set
     */
    public boolean isFlag(int flag) {
        return (flags & flag) != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.flags);
    }

    /** Default no-arg constructor. */
    public InstallOption() { }

    /**
     * Constructs an {@code InstallOption} by reading the flags field from a Parcel.
     *
     * @param in the Parcel to deserialize from
     */
    protected InstallOption(Parcel in) {
        this.flags = in.readInt();
    }

    /** Parcelable {@code Creator} for {@code InstallOption}. */
    public static final Parcelable.Creator<InstallOption> CREATOR = new Parcelable.Creator<InstallOption>() {
        @Override
        public InstallOption createFromParcel(Parcel source) {
            return new InstallOption(source);
        }

        @Override
        public InstallOption[] newArray(int size) {
            return new InstallOption[size];
        }
    };
}
