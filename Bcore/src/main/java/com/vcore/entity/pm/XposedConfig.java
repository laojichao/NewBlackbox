package com.vcore.entity.pm;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

/**
 * Parcelable configuration for the Xposed/LSPosed framework integration inside the virtual
 * environment. Stores whether the Xposed bridge is globally enabled and maintains a per-module
 * enable/disable state map keyed by module package name.
 */
public class XposedConfig implements Parcelable {
    /** Whether the Xposed framework is globally enabled in this virtual environment. */
    public boolean enable;

    /**
     * Map from Xposed module package name to its enabled state.
     * {@code true} = enabled, {@code false} = disabled.
     */
    public Map<String, Boolean> moduleState = new HashMap<>();

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.enable ? (byte) 1 : (byte) 0);
        dest.writeInt(this.moduleState.size());
        for (Map.Entry<String, Boolean> entry : this.moduleState.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeValue(entry.getValue());
        }
    }

    /** Default no-arg constructor. */
    public XposedConfig() { }

    /**
     * Constructs a {@code XposedConfig} by reading all fields from a Parcel, including
     * the per-module state map.
     *
     * @param in the Parcel to deserialize from
     */
    public XposedConfig(Parcel in) {
        this.enable = in.readByte() != 0;
        int mModuleStateSize = in.readInt();
        this.moduleState = new HashMap<>(mModuleStateSize);

        for (int i = 0; i < mModuleStateSize; i++) {
            String key = in.readString();
            Boolean value = (Boolean) in.readValue(Boolean.class.getClassLoader());
            this.moduleState.put(key, value);
        }
    }

    /** Parcelable {@code Creator} for {@code XposedConfig}. */
    public static final Parcelable.Creator<XposedConfig> CREATOR = new Parcelable.Creator<XposedConfig>() {
        @Override
        public XposedConfig createFromParcel(Parcel source) {
            return new XposedConfig(source);
        }

        @Override
        public XposedConfig[] newArray(int size) {
            return new XposedConfig[size];
        }
    };
}
