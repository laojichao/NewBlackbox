package black.android.location;

import black.Reflector;

/**
 * Reflection wrapper for hidden inner classes of {@code android.location.LocationManager}.
 * Provides access to GNSS status listeners, GPS status listeners, and location listener
 * transport mechanisms used internally by the location manager.
 */
public class LocationManager {
    /**
     * Reflection wrapper for {@code android.location.LocationManager$GnssStatusListenerTransport}.
     * Transports GNSS (Global Navigation Satellite System) status callbacks.
     */
    public static class GnssStatusListenerTransport {
        public static final Reflector REF = Reflector.on("android.location.LocationManager$GnssStatusListenerTransport");

        /**
         * Called when GNSS navigation starts.
         */
        public static Reflector.MethodWrapper<Void> onGnssStarted = REF.method("onGnssStarted");

        /**
         * Called when an NMEA sentence is received from GNSS.
         *
         * @param timestamp the timestamp in milliseconds
         * @param nmea      the NMEA sentence string
         */
        public static Reflector.MethodWrapper<Void> onNmeaReceived = REF.method("onNmeaReceived", long.class, String.class);
    }

    /**
     * Reflection wrapper for {@code android.location.LocationManager$GpsStatusListenerTransport}.
     * Transports GPS status callbacks (legacy API).
     */
    public static class GpsStatusListenerTransport {
        public static final Reflector REF = Reflector.on("android.location.LocationManager$GpsStatusListenerTransport");

        /**
         * Called when an NMEA sentence is received from GPS.
         *
         * @param timestamp the timestamp in milliseconds
         * @param nmea      the NMEA sentence string
         */
        public static Reflector.MethodWrapper<Void> onNmeaReceived = REF.method("onNmeaReceived", long.class, String.class);
    }

    /**
     * Reflection wrapper for {@code android.location.LocationManager$LocationListenerTransport}.
     * Wraps a LocationListener for binder transport.
     */
    public static class LocationListenerTransport {
        public static final Reflector REF = Reflector.on("android.location.LocationManager$LocationListenerTransport");

        /** The LocationListener being transported. */
        public static Reflector.FieldWrapper<Object> mListener = REF.field("mListener");
    }
}
