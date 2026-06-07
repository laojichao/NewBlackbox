package com.vcore.proxy.record;

import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.IBinder;

import com.vcore.utils.compat.BundleCompat;

/**
 * Data record that carries the metadata needed to dispatch a virtual service
 * lifecycle event through a proxy stub.
 * <p>
 * Encapsulates the service intent, service info, token binder, user ID, and start ID.
 * Provides static factory methods to serialize the record into a proxy intent
 * (via {@link #saveStub}) and deserialize it back (via {@link #create}).
 * </p>
 */
public class ProxyServiceRecord {
    /** The intent used to start or bind the real service. */
    public final Intent mServiceIntent;
    /** The {@link ServiceInfo} of the target service being proxied. */
    public final ServiceInfo mServiceInfo;
    /** The unique binder token identifying this service instance. */
    public final IBinder mToken;
    /** The virtual user ID under which the service should run. */
    public final int mUserId;
    /** The start ID for this particular service start request. */
    public final int mStartId;

    /**
     * Constructs a new proxy service record.
     *
     * @param serviceIntent the intent for the real service
     * @param serviceInfo   the service info of the target service
     * @param token         the unique binder token for this service instance
     * @param userId        the virtual user ID
     * @param startId       the start ID for this start request
     */
    public ProxyServiceRecord(Intent serviceIntent, ServiceInfo serviceInfo, IBinder token, int userId, int startId) {
        this.mServiceIntent = serviceIntent;
        this.mServiceInfo = serviceInfo;
        this.mUserId = userId;
        this.mStartId = startId;
        this.mToken = token;
    }

    /**
     * Serializes the service record fields into extras on the shadow (proxy) intent.
     * The binder token is stored using {@link BundleCompat#putBinder} since standard
     * intents do not natively support binder extras.
     *
     * @param shadow      the proxy intent to store the record data in
     * @param target      the intent for the real service
     * @param serviceInfo the service info of the target service
     * @param token       the unique binder token for this service instance
     * @param userId      the virtual user ID
     * @param startId     the start ID for this start request
     */
    public static void saveStub(Intent shadow, Intent target, ServiceInfo serviceInfo, IBinder token, int userId, int startId) {
        shadow.putExtra("_B_|_target_", target);
        shadow.putExtra("_B_|_service_info_", serviceInfo);
        shadow.putExtra("_B_|_user_id_", userId);
        shadow.putExtra("_B_|_start_id_", startId);
        BundleCompat.putBinder(shadow, "_B_|_token_", token);
    }

    /**
     * Deserializes a {@link ProxyServiceRecord} from the extras of the given intent.
     * The binder token is extracted using {@link BundleCompat#getBinder}.
     *
     * @param intent the intent containing the serialized record data
     * @return a new {@link ProxyServiceRecord} populated from the intent extras
     */
    public static ProxyServiceRecord create(Intent intent) {
        Intent target = intent.getParcelableExtra("_B_|_target_");
        ServiceInfo serviceInfo = intent.getParcelableExtra("_B_|_service_info_");

        int userId = intent.getIntExtra("_B_|_user_id_", 0);
        int startId = intent.getIntExtra("_B_|_start_id_", 0);

        IBinder token = BundleCompat.getBinder(intent, "_B_|_token_");
        return new ProxyServiceRecord(target, serviceInfo, token, userId, startId);
    }
}
