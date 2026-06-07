package black.android.app;

import android.content.ComponentName;
import android.os.IBinder;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.IServiceConnection} interface
 * on Android O (Oreo, API 26+). This callback interface is invoked when a service
 * connection is established or disconnected.
 */
public class IServiceConnectionO {
    public static final Reflector REF = Reflector.on("android.app.IServiceConnection");

    /**
     * Called when connected to a service.
     *
     * @param componentName the ComponentName of the connected service
     * @param binder        the IBinder of the service
     * @param dead          whether the service process has died
     */
    public static Reflector.MethodWrapper<Void> connected = REF.method("connected", ComponentName.class, IBinder.class, boolean.class);
}
