package black.android.content;

import android.content.Intent;
import android.os.Bundle;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.content.IIntentReceiver} AIDL interface.
 * This is the binder callback interface used by the system to deliver broadcast intents
 * to registered receivers in application processes.
 */
public class IIntentReceiver {
    public static final Reflector REF = Reflector.on("android.content.IIntentReceiver");

    /**
     * Called when a broadcast is received.
     *
     * @param intent     the broadcast Intent
     * @param resultCode the result code
     * @param resultData the result data string
     * @param extras     the result extras Bundle
     * @param ordered    whether this is an ordered broadcast
     * @param sticky     whether this is a sticky broadcast
     * @param sendingUser the user ID that sent the broadcast
     */
    public static Reflector.MethodWrapper<Void> performReceive = REF.method("performReceive", Intent.class, int.class, String.class, Bundle.class, boolean.class, boolean.class, int.class);
}
