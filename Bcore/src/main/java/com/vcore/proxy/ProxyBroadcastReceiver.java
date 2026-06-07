package com.vcore.proxy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;

import com.vcore.BlackBoxCore;
import com.vcore.entity.am.PendingResultData;
import com.vcore.proxy.record.ProxyBroadcastRecord;

/**
 * Proxy broadcast receiver that acts as a stub entry point for broadcast intents
 * in the host application manifest.
 * <p>
 * Receives broadcasts on behalf of guest applications, extracts the
 * {@link ProxyBroadcastRecord} from the intent, and schedules the actual broadcast
 * delivery through the {@link BlackBoxCore} activity manager. Uses
 * {@link #goAsync()} to keep the receiver alive while the asynchronous scheduling completes.
 * </p>
 */
public class ProxyBroadcastReceiver extends BroadcastReceiver {
    /** Tag for logging. */
    public static final String TAG = "ProxyBroadcastReceiver";

    /**
     * Called when a broadcast is received. Extracts the real broadcast intent and user ID
     * from the {@link ProxyBroadcastRecord} embedded in the incoming intent, then schedules
     * the broadcast through the virtual activity manager.
     *
     * @param context the context in which the receiver is running
     * @param intent  the intent being received, containing the embedded {@link ProxyBroadcastRecord}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        intent.setExtrasClassLoader(context.getClassLoader());
        ProxyBroadcastRecord record = ProxyBroadcastRecord.create(intent);
        if (record.mIntent == null) {
            return;
        }

        PendingResult pendingResult = goAsync();
        try {
            BlackBoxCore.getBActivityManager().scheduleBroadcastReceiver(record.mIntent, new PendingResultData(pendingResult), record.mUserId);
        } catch (RemoteException e) {
            pendingResult.finish();
        }
    }
}