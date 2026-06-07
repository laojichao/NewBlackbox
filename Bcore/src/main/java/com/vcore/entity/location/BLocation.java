package com.vcore.entity.location;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * Parcelable location data used for GPS/network location spoofing inside the virtual environment.
 * Stores latitude, longitude, altitude, speed, bearing, and accuracy, and can convert itself into
 * a system {@link Location} object that appears as a genuine GPS fix to apps.
 * <p>
 * Also provides static utility methods for NMEA sentence construction (direction labels,
 * degree-to-DMS conversion, and XOR checksum computation).
 */
public class BLocation implements Parcelable {
    /** Latitude in degrees, positive = North, negative = South. */
    private double mLatitude = 0.0;

    /** Longitude in degrees, positive = East, negative = West. */
    private double mLongitude = 0.0;

    /** Altitude in meters above the WGS 84 reference ellipsoid. */
    private double mAltitude = 0.0f;

    /** Ground speed in meters per second. */
    private float mSpeed = 0.0f;

    /** Bearing (heading) in degrees clockwise from true North. */
    private float mBearing = 0.0f;

    /** Estimated horizontal accuracy radius in meters. */
    private float mAccuracy = 0.0f;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.mLatitude);
        dest.writeDouble(this.mLongitude);
        dest.writeDouble(this.mAltitude);
        dest.writeFloat(this.mSpeed);
        dest.writeFloat(this.mBearing);
        dest.writeFloat(this.mAccuracy);
    }

    /**
     * Returns the latitude.
     *
     * @return latitude in degrees
     */
    public double getLatitude() {
        return mLatitude;
    }

    /**
     * Returns the longitude.
     *
     * @return longitude in degrees
     */
    public double getLongitude() {
        return mLongitude;
    }

    /** Default no-arg constructor. Coordinates default to (0, 0). */
    public BLocation() { }

    /**
     * Constructs a location with the given latitude and longitude.
     *
     * @param latitude   latitude in degrees
     * @param mLongitude longitude in degrees
     */
    public BLocation(double latitude, double mLongitude) {
        this.mLatitude = latitude;
        this.mLongitude = mLongitude;
    }

    /**
     * Constructs a {@code BLocation} by reading all fields from a Parcel.
     *
     * @param in the Parcel to deserialize from
     */
    public BLocation(Parcel in) {
        this.mLatitude = in.readDouble();
        this.mLongitude = in.readDouble();
        this.mAltitude = in.readDouble();
        this.mAccuracy = in.readFloat();
        this.mSpeed = in.readFloat();
        this.mBearing = in.readFloat();
    }

    /**
     * Checks whether this location is effectively empty (both coordinates are zero).
     *
     * @return {@code true} if latitude and longitude are both 0
     */
    public boolean isEmpty() {
        return mLatitude == 0 && mLongitude == 0;
    }

    /** Parcelable {@code Creator} for {@code BLocation}. */
    public static final Parcelable.Creator<BLocation> CREATOR = new Parcelable.Creator<BLocation>() {
        @Override
        public BLocation createFromParcel(Parcel source) {
            return new BLocation(source);
        }

        @Override
        public BLocation[] newArray(int size) {
            return new BLocation[size];
        }
    };

    @NonNull
    @Override
    public String toString() {
        return "BLocation{" + "latitude: " + mLatitude + ", longitude: " + mLongitude + ", altitude: " + mAltitude + ", speed: " + mSpeed
                + ", bearing: " + mBearing + ", accuracy: " + mAccuracy + '}';
    }

    /**
     * Converts this spoofed location into a real Android {@link Location} object backed by the
     * GPS provider. Sets accuracy to 40m and injects a synthetic satellite count of 10 in the
     * extras bundle to appear more realistic to location consumers.
     *
     * @return a system {@link Location} object with this instance's coordinates
     */
    public Location convert2SystemLocation() {
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(mLatitude);
        location.setLongitude(mLongitude);
        location.setSpeed(mSpeed);
        location.setBearing(mBearing);
        location.setAccuracy(40f);
        location.setTime(System.currentTimeMillis());
        Bundle extraBundle = new Bundle();
        // GPS satellite number
        int satelliteCount = 10;
        extraBundle.putInt("satellites", satelliteCount);
        extraBundle.putInt("satellitesvalue", satelliteCount);
        location.setExtras(extraBundle);
        return location;
    }

    /**
     * Returns the East/West direction indicator for the given location's longitude.
     *
     * @param location the location to inspect
     * @return {@code "E"} if longitude is positive, {@code "W"} otherwise
     */
    public static String getSouthEast(BLocation location) {
        if (location.mLongitude > 0.0d) {
            return "E";
        }
        return "W";
    }

    /**
     * Returns the North/South direction indicator for the given location's latitude.
     *
     * @param location the location to inspect
     * @return {@code "N"} if latitude is positive, {@code "S"} otherwise
     */
    public static String getNorthWest(BLocation location) {
        if (location.mLatitude > 0.0d) {
            return "N";
        }
        return "S";
    }

    /**
     * Converts a decimal-degree latitude value into NMEA-style DMS (degrees:minutes) format.
     * Example: 39.531734 becomes "3931:...".
     *
     * @param v the decimal degree value
     * @return the formatted DMS string
     */
    public static String getGPSLatitude(double v) {
        int du = (int) v;
        double fen = (v - (double) du) * 60.0d;

        return du + leftZeroPad((int) fen) + ":" + String.valueOf(fen).substring(2);
    }

    /**
     * Left-zero-pads an integer to 2 digits.
     *
     * @param num the integer to pad
     * @return the zero-padded string
     */
    private static String leftZeroPad(int num) {
        return leftZeroPad(String.valueOf(num));
    }

    /**
     * Left-zero-pads a numeric string to 2 characters.
     *
     * @param num the numeric string to pad (may be {@code null})
     * @return the zero-padded string
     */
    private static String leftZeroPad(String num) {
        StringBuilder sb = new StringBuilder(2);
        int i;

        if (num == null) {
            for (i = 0; i < 2; i++) {
                sb.append('0');
            }
        } else {
            for (i = 0; i < 2 - num.length(); i++) {
                sb.append('0');
            }
            sb.append(num);
        }
        return sb.toString();
    }

    /**
     * Computes an NMEA-style XOR checksum over the given sentence (excluding the leading '$'
     * if present) and appends it in {@code *HH} format.
     *
     * @param nema the NMEA sentence body (with or without leading '$')
     * @return the sentence with the checksum appended (e.g. {@code "$GPGGA...*7a"})
     */
    public static String checkSum(String nema) {
        String checkStr = nema;
        if (nema.startsWith("$")) {
            checkStr = nema.substring(1);
        }

        int sum = 0;
        for (int i = 0; i < checkStr.length(); i++) {
            sum ^= (byte) checkStr.charAt(i);
        }
        return nema + "*" + String.format("%02X", sum).toLowerCase();
    }
}
