package com.vcore.fake.delegate;

import android.content.IIntentReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import com.vcore.app.BActivityThread;
import com.vcore.proxy.record.ProxyBroadcastRecord;

/**
 * Delegate class that wraps an {@link IIntentReceiver} to intercept broadcast delivery
 * within the virtual environment.
 *
 * <p>This class creates proxy instances of broadcast receivers that unwrap proxied
 * intents before forwarding them to the original receiver. It maintains a cache of
 * delegate instances keyed by the receiver's binder to avoid creating duplicates.
 * Death recipients are registered to automatically clean up entries when the
 * original receiver's process dies.</p>
 *
 * @see ProxyBroadcastRecord
 */
public class InnerReceiverDelegate extends IIntentReceiver.Stub {
    public static final String TAG = "InnerReceiverDelegate";

    /** Cache of delegate instances keyed by the original receiver's binder. */
    private static final Map<IBinder, InnerReceiverDelegate> sInnerReceiverDelegate = new HashMap<>();

    /** Weak reference to the original intent receiver to avoid preventing garbage collection. */
    private final WeakReference<IIntentReceiver> mIntentReceiver;

    /**
     * Private constructor to enforce creation through the static factory method.
     *
     * @param iIntentReceiver the original intent receiver to delegate to
     */
    private InnerReceiverDelegate(IIntentReceiver iIntentReceiver) {
        this.mIntentReceiver = new WeakReference<>(iIntentReceiver);
    }

    /**
     * Creates a proxy for the given {@link IIntentReceiver}, reusing an existing delegate
     * if one already exists for the same binder. If the receiver is already a delegate,
     * it is returned as-is.
     *
     * @param base the original intent receiver to proxy
     * @return a delegate that intercepts broadcast delivery, or the original if already delegated
     */
    public static IIntentReceiver createProxy(IIntentReceiver base) {
        if (base instanceof InnerReceiverDelegate) {
            return base;
        }
        final IBinder iBinder = base.asBinder();
        InnerReceiverDelegate delegate = sInnerReceiverDelegate.get(iBinder);
        if (delegate == null) {
            try {
                iBinder.linkToDeath(new DeathRecipient() {
                    @Override
                    public void binderDied() {
                        sInnerReceiverDelegate.remove(iBinder);
                        iBinder.unlinkToDeath(this, 0);
                    }
                }, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            delegate = new InnerReceiverDelegate(base);
            sInnerReceiverDelegate.put(iBinder, delegate);
        }
        return delegate;
    }

    /**
     * Receives a broadcast intent, unwraps any proxied intent data, and forwards
     * the (possibly original) intent to the wrapped receiver.
     *
     * @param intent        the broadcast intent being received
     * @param resultCode    the result code
     * @param data          the result data string
     * @param extras        the extras bundle
     * @param ordered       whether this is an ordered broadcast
     * @param sticky        whether this is a sticky broadcast
     * @param sendingUser   the user ID of the sender
     */
    @Override
    public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) {
        intent.setExtrasClassLoader(BActivityThread.getApplication().getClassLoader());
        ProxyBroadcastRecord proxyBroadcastRecord = ProxyBroadcastRecord.create(intent);
        Intent perIntent;
        if (proxyBroadcastRecord.mIntent != null) {
            proxyBroadcastRecord.mIntent.setExtrasClassLoader(BActivityThread.getApplication().getClassLoader());
            perIntent = proxyBroadcastRecord.mIntent;
        } else {
            perIntent = intent;
        }

        IIntentReceiver iIntentReceiver = mIntentReceiver.get();
        if (iIntentReceiver != null) {
            black.android.content.IIntentReceiver.performReceive.call(iIntentReceiver, perIntent, resultCode, data, extras, ordered, sticky, sendingUser);
        }
    }
}
