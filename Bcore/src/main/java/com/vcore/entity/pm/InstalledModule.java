package com.vcore.entity.pm;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.Parcelable;

import com.vcore.BlackBoxCore;
import com.vcore.core.system.user.BUserHandle;

/**
 * Represents an installed Xposed/LSPosed module inside the virtual environment. Stores the
 * module's package name, display name, description, main entry class, and enabled state.
 * Provides convenience methods to query the module's {@link ApplicationInfo} and
 * {@link PackageInfo} from the virtual package manager under the Xposed user profile.
 */
public class InstalledModule implements Parcelable {
    /** Package name of the Xposed module (e.g. {@code "com.example.module"}). */
    public String packageName;

    /** Human-readable display name of the module. */
    public String name;

    /** Description of what the module does. */
    public String desc;

    /** Fully-qualified class name of the module's main entry point (the IXposedHookLoadPackage implementation). */
    public String main;

    /** Whether this module is currently enabled in the virtual environment. */
    public boolean enable;

    /** Default no-arg constructor. */
    public InstalledModule() { }

    /**
     * Returns the {@link ApplicationInfo} for this module from the virtual package manager,
     * queried under the Xposed user profile with {@code GET_META_DATA}.
     *
     * @return the application info
     */
    public ApplicationInfo getApplication() {
        return BlackBoxCore.getBPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA, BUserHandle.USER_XPOSED);
    }

    /**
     * Returns the {@link PackageInfo} for this module from the virtual package manager,
     * queried under the Xposed user profile with {@code GET_META_DATA}.
     *
     * @return the package info
     */
    public PackageInfo getPackageInfo() {
        return BlackBoxCore.getBPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA, BUserHandle.USER_XPOSED);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.packageName);
        dest.writeString(this.name);
        dest.writeString(this.desc);
        dest.writeString(this.main);
        dest.writeByte(this.enable ? (byte) 1 : (byte) 0);
    }

    /**
     * Constructs an {@code InstalledModule} by reading all fields from a Parcel.
     *
     * @param in the Parcel to deserialize from
     */
    protected InstalledModule(Parcel in) {
        this.packageName = in.readString();
        this.name = in.readString();
        this.desc = in.readString();
        this.main = in.readString();
        this.enable = in.readByte() != 0;
    }

    /** Parcelable {@code Creator} for {@code InstalledModule}. */
    public static final Creator<InstalledModule> CREATOR = new Creator<InstalledModule>() {
        @Override
        public InstalledModule createFromParcel(Parcel source) {
            return new InstalledModule(source);
        }

        @Override
        public InstalledModule[] newArray(int size) {
            return new InstalledModule[size];
        }
    };
}
