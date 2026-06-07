package black.android.os;

import android.os.IBinder;

import black.Reflector;

/**
 * Reflection wrapper for hidden methods in {@code android.os.Bundle}.
 * Provides access to IBinder storage methods that are not part of the public SDK
 * but are used internally for inter-process communication.
 */
public class Bundle {
    public static final Reflector REF = Reflector.on("android.os.Bundle");

    /**
     * Retrieves an IBinder value associated with the given key.
     *
     * @param key the key to look up
     * @return the IBinder value, or null if not present
     */
    public static Reflector.MethodWrapper<IBinder> getIBinder = REF.method("getIBinder", String.class);

    /**
     * Stores an IBinder value with the given key.
     *
     * @param key    the key to store under
     * @param binder the IBinder value to store
     */
    public static Reflector.MethodWrapper<Void> putIBinder = REF.method("putIBinder", String.class, IBinder.class);
}
