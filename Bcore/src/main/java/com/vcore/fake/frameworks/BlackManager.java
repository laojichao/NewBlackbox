package com.vcore.fake.frameworks;

import android.os.IBinder;
import android.os.IInterface;

import java.lang.reflect.ParameterizedType;

import black.Reflector;
import com.vcore.BlackBoxCore;

/**
 * Abstract base class for virtual environment system service managers.
 *
 * <p>This class provides a generic mechanism for obtaining and caching a typed service
 * interface (AIDL Stub proxy) for virtual system services running in the black server process.
 * It uses reflection to call the service's {@code Stub.asInterface(IBinder)} factory method
 * and automatically reconnects when the service binder dies.</p>
 *
 * <p>Subclasses must implement {@link #getServiceName()} to return the service name
 * used to look up the binder from {@link BlackBoxCore#getService(String)}.</p>
 *
 * @param <Service> the AIDL service interface type
 */
public abstract class BlackManager<Service extends IInterface> {
    public static final String TAG = "BlackManager";

    /** Cached service proxy instance. Nullified when the service binder dies. */
    private Service mService;

    /**
     * Returns the name of the virtual system service this manager connects to.
     *
     * @return the service name string used for binder lookup
     */
    protected abstract String getServiceName();

    /**
     * Returns the service proxy, creating or reconnecting as needed.
     *
     * <p>If the cached service is still alive (binder is pingable and alive), it is returned
     * directly. Otherwise, a new proxy is created by reflecting on the service's
     * {@code Stub.asInterface(IBinder)} method. A death recipient is registered to
     * nullify the cache when the service dies, triggering reconnection on the next call.</p>
     *
     * @return the service proxy, or {@code null} if connection fails
     */
    public Service getService() {
        if (mService != null && mService.asBinder().pingBinder() && mService.asBinder().isBinderAlive()) {
            return mService;
        }
        try {
            // Call T.Stub.asInterface via reflection
            mService = Reflector.on(getTClass().getName() + "$Stub").staticMethod("asInterface", IBinder.class)
                    .callWithClass(BlackBoxCore.get().getService(getServiceName()));
            mService.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                @Override
                public void binderDied() {
                    mService.asBinder().unlinkToDeath(this, 0);
                    mService = null;
                }
            }, 0);
            return getService();
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Resolves the generic type parameter {@code <Service>} from this class's superclass
     * declaration using reflection.
     *
     * @return the resolved service class
     */
    @SuppressWarnings("unchecked")
    private Class<Service> getTClass() {
        return (Class<Service>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }
}
