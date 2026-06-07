package com.vcore.entity.pm;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

import com.vcore.BlackBoxCore;

/**
 * Represents an installed package inside the virtual environment. Tracks the package name
 * and the virtual user ID under which it is installed. Provides convenience methods to query
 * the package's {@link ApplicationInfo} and {@link PackageInfo} from the virtual package
 * manager, and implements equality/hashing based solely on {@link #packageName} (ignoring
 * {@link #userId}) so that two entries for the same package across different users compare
 * as equal.
 */
public class InstalledPackage implements Parcelable {
    /** Virtual user ID under which this package is installed. */
    public int userId;

    /** The package name (e.g. {@code "com.example.app"}). */
    public String packageName;

    /**
     * Returns the {@link ApplicationInfo} for this package from the virtual package manager,
     * queried with {@code GET_META_DATA} under this instance's {@link #userId}.
     *
     * @return the application info
     */
    public ApplicationInfo getApplication() {
        return BlackBoxCore.getBPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA, userId);
    }

    /**
     * Returns the {@link PackageInfo} for this package from the virtual package manager,
     * queried with {@code GET_META_DATA} under this instance's {@link #userId}.
     *
     * @return the package info
     */
    public PackageInfo getPackageInfo() {
        return BlackBoxCore.getBPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA, userId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.userId);
        dest.writeString(this.packageName);
    }

    /** Default no-arg constructor. */
    public InstalledPackage() { }

    /**
     * Constructs an {@code InstalledPackage} with the given package name (userId defaults to 0).
     *
     * @param packageName the package name
     */
    public InstalledPackage(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Constructs an {@code InstalledPackage} by reading all fields from a Parcel.
     *
     * @param in the Parcel to deserialize from
     */
    protected InstalledPackage(Parcel in) {
        this.userId = in.readInt();
        this.packageName = in.readString();
    }

    /**
     * Two {@code InstalledPackage} instances are considered equal if their {@link #packageName}
     * values are equal (userId is intentionally ignored).
     *
     * @param o the object to compare with
     * @return {@code true} if the package names match
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InstalledPackage that = (InstalledPackage) o;
        return Objects.equals(packageName, that.packageName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageName);
    }

    /** Parcelable {@code Creator} for {@code InstalledPackage}. */
    public static final Parcelable.Creator<InstalledPackage> CREATOR = new Parcelable.Creator<InstalledPackage>() {
        @Override
        public InstalledPackage createFromParcel(Parcel source) {
            return new InstalledPackage(source);
        }

        @Override
        public InstalledPackage[] newArray(int size) {
            return new InstalledPackage[size];
        }
    };
}
