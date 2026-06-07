package com.vcore.proxy.record;

import android.content.Intent;
import android.content.pm.ActivityInfo;

/**
 * Data record that carries the metadata needed to launch a virtual activity through
 * a proxy stub.
 * <p>
 * Encapsulates the target intent, activity info, user ID, and activity token. Provides
 * static factory methods to serialize the record into a proxy intent (via {@link #saveStub})
 * and deserialize it back (via {@link #create}).
 * </p>
 */
public class ProxyActivityRecord {
    /** The virtual user ID under which the activity should run. */
    public final int mUserId;
    /** The {@link ActivityInfo} of the target activity being proxied. */
    public final ActivityInfo mActivityInfo;
    /** The intent that will be used to start the real target activity. */
    public final Intent mTarget;
    /** The unique activity token identifying this activity instance. */
    public final String mActivityToken;

    /**
     * Constructs a new proxy activity record.
     *
     * @param userId       the virtual user ID
     * @param activityInfo the activity info of the target activity
     * @param target       the intent to start the real target activity
     * @param activityToken the unique activity token
     */
    public ProxyActivityRecord(int userId, ActivityInfo activityInfo, Intent target, String activityToken) {
        this.mUserId = userId;
        this.mActivityInfo = activityInfo;
        this.mTarget = target;
        this.mActivityToken = activityToken;
    }

    /**
     * Serializes the activity record fields into extras on the shadow (proxy) intent.
     *
     * @param shadow       the proxy intent to store the record data in
     * @param target       the intent for the real target activity
     * @param activityInfo the activity info of the target activity
     * @param activityToken the unique activity token
     * @param userId       the virtual user ID
     */
    public static void saveStub(Intent shadow, Intent target, ActivityInfo activityInfo, String activityToken, int userId) {
        shadow.putExtra("_B_|_user_id_", userId);
        shadow.putExtra("_B_|_activity_info_", activityInfo);
        shadow.putExtra("_B_|_target_", target);
        shadow.putExtra("_B_|_activity_token_v_", activityToken);
    }

    /**
     * Deserializes a {@link ProxyActivityRecord} from the extras of the given intent.
     *
     * @param intent the intent containing the serialized record data
     * @return a new {@link ProxyActivityRecord} populated from the intent extras
     */
    public static ProxyActivityRecord create(Intent intent) {
        int userId = intent.getIntExtra("_B_|_user_id_", 0);
        ActivityInfo activityInfo = intent.getParcelableExtra("_B_|_activity_info_");

        Intent target = intent.getParcelableExtra("_B_|_target_");
        String activityToken = intent.getStringExtra("_B_|_activity_token_v_");
        return new ProxyActivityRecord(userId, activityInfo, target, activityToken);
    }
}
