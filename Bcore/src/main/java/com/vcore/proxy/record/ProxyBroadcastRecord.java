package com.vcore.proxy.record;

import android.content.Intent;

import androidx.annotation.NonNull;

/**
 * Data record that carries the metadata needed to deliver a virtual broadcast
 * through a proxy stub receiver.
 * <p>
 * Encapsulates the target broadcast intent and the virtual user ID. Provides
 * static factory methods to serialize the record into a proxy intent (via {@link #saveStub})
 * and deserialize it back (via {@link #create}).
 * </p>
 */
public class ProxyBroadcastRecord {
    /** The real broadcast intent to be delivered to the guest application. */
    public final Intent mIntent;
    /** The virtual user ID under which the broadcast should be delivered. */
    public final int mUserId;

    /**
     * Constructs a new proxy broadcast record.
     *
     * @param intent the real broadcast intent
     * @param userId the virtual user ID
     */
    public ProxyBroadcastRecord(Intent intent, int userId) {
        this.mIntent = intent;
        this.mUserId = userId;
    }

    /**
     * Serializes the broadcast record fields into extras on the shadow (proxy) intent.
     *
     * @param shadow the proxy intent to store the record data in
     * @param target the real broadcast intent
     * @param userId the virtual user ID
     */
    public static void saveStub(Intent shadow, Intent target, int userId) {
        shadow.putExtra("_B_|_target_", target);
        shadow.putExtra("_B_|_user_id_", userId);
    }

    /**
     * Deserializes a {@link ProxyBroadcastRecord} from the extras of the given intent.
     *
     * @param intent the intent containing the serialized record data
     * @return a new {@link ProxyBroadcastRecord} populated from the intent extras
     */
    public static ProxyBroadcastRecord create(Intent intent) {
        Intent target = intent.getParcelableExtra("_B_|_target_");
        int userId = intent.getIntExtra("_B_|_user_id_", 0);
        return new ProxyBroadcastRecord(target, userId);
    }

    /**
     * Returns a string representation of this record for debugging.
     *
     * @return a string containing the intent and user ID
     */
    @NonNull
    @Override
    public String toString() {
        return "ProxyBroadcastRecord{" + "mIntent=" + mIntent + ", mUserId=" + mUserId + '}';
    }
}
