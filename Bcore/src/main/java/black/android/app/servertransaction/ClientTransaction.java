package black.android.app.servertransaction;

import android.os.IBinder;

import java.util.List;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.servertransaction.ClientTransaction} class.
 * ClientTransaction represents a transaction of lifecycle items (callbacks and state changes)
 * sent from the server (system_server) to the client (app process).
 */
public class ClientTransaction {
    public static final Reflector REF = Reflector.on("android.app.servertransaction.ClientTransaction");

    /** The list of activity callback items (e.g., LaunchActivityItem) in this transaction. */
    public static Reflector.FieldWrapper<List<Object>> mActivityCallbacks = REF.field("mActivityCallbacks");

    /** The IBinder token of the activity this transaction targets. */
    public static Reflector.FieldWrapper<IBinder> mActivityToken = REF.field("mActivityToken");
}
