package com.vcore.fake.delegate;

import android.app.IServiceConnection;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.HashMap;
import java.util.Map;

import black.android.app.IServiceConnectionO;
import com.vcore.utils.compat.BuildCompat;

/**
 * Delegate class that wraps an {@link IServiceConnection} to intercept service connection
 * callbacks within the virtual environment.
 *
 * <p>This class ensures that service connection callbacks use the correct (virtual)
 * {@link ComponentName} rather than the host system's component name. It maintains a cache
 * of delegate instances keyed by the connection's binder. On Android Oreo and above,
 * the {@link IServiceConnectionO} API is used for compatibility.</p>
 */
public class ServiceConnectionDelegate extends IServiceConnection.Stub {

    /** Cache of delegate instances keyed by the original connection's binder. */
    private static final Map<IBinder, ServiceConnectionDelegate> sServiceConnectDelegate = new HashMap<>();

    /** The original service connection being delegated to. */
    private final IServiceConnection mConn;

    /** The virtual component name to use in connection callbacks. */
    private final ComponentName mComponentName;

    /**
     * Private constructor to enforce creation through the static factory method.
     *
     * @param mConn           the original service connection
     * @param targetComponent the virtual component name for this connection
     */
    private ServiceConnectionDelegate(IServiceConnection mConn, ComponentName targetComponent) {
        this.mConn = mConn;
        this.mComponentName = targetComponent;
    }

    /**
     * Retrieves an existing delegate for the given binder, if one exists.
     *
     * @param iBinder the binder to look up
     * @return the associated delegate, or {@code null} if none exists
     */
    public static ServiceConnectionDelegate getDelegate(IBinder iBinder) {
        return sServiceConnectDelegate.get(iBinder);
    }

    /**
     * Creates a proxy for the given {@link IServiceConnection}, reusing an existing delegate
     * if one already exists for the same binder. Registers a death recipient to clean up
     * the cache when the connection's process dies.
     *
     * @param base   the original service connection to proxy
     * @param intent the intent used to bind the service, providing the target component name
     * @return a delegate that provides the correct virtual component name on connection
     */
    public static IServiceConnection createProxy(IServiceConnection base, Intent intent) {
        final IBinder iBinder = base.asBinder();
        ServiceConnectionDelegate delegate = sServiceConnectDelegate.get(iBinder);
        if (delegate == null) {
            try {
                iBinder.linkToDeath(new IBinder.DeathRecipient() {
                    @Override
                    public void binderDied() {
                        sServiceConnectDelegate.remove(iBinder);
                        iBinder.unlinkToDeath(this, 0);
                    }
                }, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            delegate = new ServiceConnectionDelegate(base, intent.getComponent());
            sServiceConnectDelegate.put(iBinder, delegate);
        }
        return delegate;
    }

    /**
     * Called when a service is connected. Delegates to {@link #connected(ComponentName, IBinder, boolean)}
     * with {@code dead} set to {@code false}.
     *
     * @param name    the component name of the connected service
     * @param service the binder of the connected service
     * @throws RemoteException if the remote call fails
     */
    @Override
    public void connected(ComponentName name, IBinder service) throws RemoteException {
        connected(name, service, false);
    }

    /**
     * Called when a service is connected, with a flag indicating whether the service
     * has died. Uses the virtual component name on Oreo and above.
     *
     * @param name    the component name (may be overridden by the virtual component)
     * @param service the binder of the connected service
     * @param dead    whether the service process has died
     * @throws RemoteException if the remote call fails
     */
    public void connected(ComponentName name, IBinder service, boolean dead) throws RemoteException {
        if (BuildCompat.isOreo()) {
            IServiceConnectionO.connected.call(mConn, mComponentName, service, dead);
        } else {
            mConn.connected(name, service);
        }
    }
}
