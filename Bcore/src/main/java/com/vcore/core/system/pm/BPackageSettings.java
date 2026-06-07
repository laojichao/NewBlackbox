package com.vcore.core.system.pm;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.AtomicFile;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vcore.core.env.BEnvironment;
import com.vcore.core.system.user.BUserHandle;
import com.vcore.entity.pm.InstallOption;
import com.vcore.utils.CloseUtils;
import com.vcore.utils.FileUtils;

/**
 * Represents the settings and metadata for an installed package in the virtual environment.
 *
 * <p>Contains the package reference, app ID, install options, and per-user installation
 * state. Supports serialization to/from Parcel for IPC and persistence to disk via
 * {@link #save()}. Handles Xposed module visibility across users when the module is enabled.</p>
 */
public class BPackageSettings implements Parcelable {
    public BPackage pkg;
    public int appId;
    public InstallOption installOption;
    public Map<Integer, BPackageUserState> userState = new HashMap<>();

    public BPackageSettings() { }

    /**
     * Returns a copy of the per-user installation states.
     *
     * @return a list of all {@link BPackageUserState} entries
     */
    public List<BPackageUserState> getUserState() {
        return new ArrayList<>(userState.values());
    }

    /**
     * Returns a list of user IDs that have this package installed.
     *
     * @return a list of virtual user IDs
     */
    public List<Integer> getUserIds() {
        return new ArrayList<>(userState.keySet());
    }

    /**
     * Sets the installed state for the given user.
     *
     * @param inst   true to mark as installed, false otherwise
     * @param userId the virtual user ID
     */
    public void setInstalled(boolean inst, int userId) {
        modifyUserState(userId).installed = inst;
    }

    /**
     * Returns whether this package is installed for the given user.
     *
     * @param userId the virtual user ID
     * @return true if the package is installed for the user
     */
    public boolean getInstalled(int userId) {
        return readUserState(userId).installed;
    }

    /**
     * Removes the user state entry for the given user ID.
     *
     * @param userId the virtual user ID to remove
     */
    public void removeUser(int userId) {
        userState.remove(userId);
    }

    /**
     * Reads the user state for the given user ID, returning a copy.
     *
     * <p>For Xposed modules that are enabled and Xposed is active, the package is
     * marked as installed for all users. Packages queried with USER_ALL are also
     * always marked as installed.</p>
     *
     * @param userId the virtual user ID
     * @return a copy of the {@link BPackageUserState} for the user
     */
    public BPackageUserState readUserState(int userId) {
        BPackageUserState state = userState.get(userId);
        if (state == null) {
            state = new BPackageUserState();
        }

        state = new BPackageUserState(state);
        // xp模块所有用户可见、如果开启的话
        if (installOption.isFlag(InstallOption.FLAG_XPOSED) &&
                BXposedManagerService.get().isModuleEnable(pkg.packageName) &&
                BXposedManagerService.get().isXPEnable()) {
            state.installed = true;
        }

        if (userId == BUserHandle.USER_ALL) {
            state.installed = true;
        }
        return state;
    }

    private BPackageUserState modifyUserState(int userId) {
        BPackageUserState state = userState.get(userId);
        if (state == null) {
            state = new BPackageUserState();
            userState.put(userId, state);
        }
        return state;
    }

    /**
     * Saves the package settings to disk using Parcel serialization with AtomicFile.
     * Uses atomic write semantics to prevent corruption on failure.
     */
    public void save() {
        synchronized (this) {
            Parcel parcel = Parcel.obtain();
            AtomicFile atomicFile = new AtomicFile(BEnvironment.getPackageConf(pkg.packageName));
            FileOutputStream fileOutputStream = null;

            try {
                writeToParcel(parcel, 0);
                parcel.setDataPosition(0);
                fileOutputStream = atomicFile.startWrite();

                FileUtils.writeParcelToOutput(parcel, fileOutputStream);
                atomicFile.finishWrite(fileOutputStream);
            } catch (Throwable e) {
                e.printStackTrace();
                atomicFile.failWrite(fileOutputStream);
            } finally {
                parcel.recycle();
                CloseUtils.close(fileOutputStream);
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.pkg, flags);
        dest.writeInt(this.appId);
        dest.writeParcelable(this.installOption, flags);
        dest.writeInt(this.userState.size());

        for (Map.Entry<Integer, BPackageUserState> entry : this.userState.entrySet()) {
            dest.writeValue(entry.getKey());
            dest.writeParcelable(entry.getValue(), flags);
        }
    }

    protected BPackageSettings(Parcel in) {
        this.pkg = in.readParcelable(BPackage.class.getClassLoader());
        this.appId = in.readInt();
        this.installOption = in.readParcelable(InstallOption.class.getClassLoader());
        int userStateSize = in.readInt();
        this.userState = new HashMap<>(userStateSize);

        for (int i = 0; i < userStateSize; i++) {
            Integer key = (Integer) in.readValue(Integer.class.getClassLoader());
            BPackageUserState value = in.readParcelable(BPackageUserState.class.getClassLoader());
            this.userState.put(key, value);
        }
    }

    public static final Creator<BPackageSettings> CREATOR = new Creator<BPackageSettings>() {
        @Override
        public BPackageSettings createFromParcel(Parcel source) {
            return new BPackageSettings(source);
        }

        @Override
        public BPackageSettings[] newArray(int size) {
            return new BPackageSettings[size];
        }
    };
}
