package com.vcore.entity.device;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Parcelable device configuration for the virtual environment. Currently a placeholder with no
 * fields, serving as an extension point for future device-level spoofing settings such as
 * IMEI, Android ID, Build properties, or display parameters. All parcel read/write methods
 * are no-ops until fields are added.
 */
public class BDeviceConfig implements Parcelable {
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) { }
}
