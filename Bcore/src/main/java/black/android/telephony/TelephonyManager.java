package black.android.telephony;

import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for hidden fields and methods in {@code android.telephony.TelephonyManager}.
 * Provides access to the IPhoneSubInfo service interface and subscriber info methods
 * used for retrieving SIM and telephony subscription data.
 */
public class TelephonyManager {
    public static final Reflector REF = Reflector.on("android.telephony.TelephonyManager");

    /**
     * Returns the ITelephonySubInfo service for subscriber information access.
     *
     * @return the IInterface for the subscriber info service
     */
    public static Reflector.StaticMethodWrapper<Object> getSubscriberInfoService = REF.staticMethod("getSubscriberInfoService");

    /** Whether the service handle cache is enabled. */
    public static Reflector.FieldWrapper<Boolean> sServiceHandleCacheEnabled = REF.field("sServiceHandleCacheEnabled");

    /** Cached IPhoneSubInfo binder interface. */
    public static Reflector.FieldWrapper<IInterface> sIPhoneSubInfo = REF.field("sIPhoneSubInfo");

    /**
     * Returns the IPhoneSubInfo interface for the current subscription.
     *
     * @return the IPhoneSubInfo IInterface
     */
    public static Reflector.MethodWrapper<IInterface> getSubscriberInfo = REF.method("getSubscriberInfo");
}
