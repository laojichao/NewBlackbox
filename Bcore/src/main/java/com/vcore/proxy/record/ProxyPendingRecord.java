package com.vcore.proxy.record;

import android.content.Intent;

import androidx.annotation.NonNull;

/**
 * Data record that carries the metadata needed to resolve a pending intent
 * through a proxy pending activity stub.
 * <p>
 * Encapsulates the target intent and the virtual user ID. Provides static factory
 * methods to serialize the record into a proxy intent (via {@link #saveStub})
 * and deserialize it back (via {@link #create}).
 * </p>
 */
public class ProxyPendingRecord {
    /** The virtual user ID under which the pending activity should run. */
    public final int mUserId;
    /** The intent that will be used to start the real target activity. */
    public final Intent mTarget;

    /**
     * Constructs a new proxy pending record.
     *
     * @param target the intent for the real target activity
     * @param userId the virtual user ID
     */
    public ProxyPendingRecord(Intent target, int userId) {
        this.mUserId = userId;
        this.mTarget = target;
    }

    /**
     * Serializes the pending record fields into extras on the shadow (proxy) intent.
     *
     * @param shadow the proxy intent to store the record data in
     * @param target the intent for the real target activity
     * @param userId the virtual user ID
     */
    public static void saveStub(Intent shadow, Intent target, int userId) {
        shadow.putExtra("_B_|_P_user_id_", userId);
        shadow.putExtra("_B_|_P_target_", target);
    }

    /**
     * Deserializes a {@link ProxyPendingRecord} from the extras of the given intent.
     *
     * @param intent the intent containing the serialized record data
     * @return a new {@link ProxyPendingRecord} populated from the intent extras
     */
    public static ProxyPendingRecord create(Intent intent) {
        int userId = intent.getIntExtra("_B_|_P_user_id_", 0);
        Intent target = intent.getParcelableExtra("_B_|_P_target_");
        return new ProxyPendingRecord(target, userId);
    }

    /**
     * Returns a string representation of this record for debugging.
     *
     * @return a string containing the user ID and target intent
     */
    @NonNull
    @Override
    public String toString() {
        return "ProxyPendingActivityRecord{" + "mUserId=" + mUserId + ", mTarget=" + mTarget + '}';
    }
}
