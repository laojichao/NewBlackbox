package android.location;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Stub implementation of Android's {@code LocationRequest} class.
 *
 * <p>A data object used to request location updates from the Android location framework.
 * It specifies parameters such as the desired accuracy, update interval, and power
 * consumption level for location providers.</p>
 *
 * <p>This stub provides the minimal {@link Parcelable} interface required for
 * inter-process communication of location request objects in sandboxed or
 * virtualized environments.</p>
 *
 * @see android.location.LocationManager
 */
public final class LocationRequest implements Parcelable {
    /**
     * Parcelable creator for {@link LocationRequest} instances.
     */
    public static final Creator<LocationRequest> CREATOR = new Creator<LocationRequest>() {
        @Override
        public LocationRequest createFromParcel(Parcel in) {
            return null;
        }

        @Override
        public LocationRequest[] newArray(int size) {
            return null;
        }
    };

    /**
     * {@inheritDoc}
     *
     * @return 0, as this object contains no special objects
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Writes this location request to the given {@link Parcel}.
     *
     * @param dest  the {@link Parcel} to write to
     * @param flags additional flags about how the object should be written
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) { }
}
