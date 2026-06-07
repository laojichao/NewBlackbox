package com.vcore.fake.service.context;

import android.location.Location;

import java.lang.reflect.Method;
import java.util.List;

import com.vcore.app.BActivityThread;
import com.vcore.fake.frameworks.BLocationManager;
import com.vcore.fake.hook.ClassInvocationStub;
import com.vcore.fake.hook.MethodHook;
import com.vcore.fake.hook.ProxyMethod;

/**
 * Proxy for location listener callbacks that intercepts location change notifications, replacing real location data with fake location data from BLocationManager when virtual location is enabled.
 */
public class LocationListenerProxy extends ClassInvocationStub {
    public static final String TAG = "LocationListenerProxy";
    private Object mBase;


    /**
     * Wraps a real location listener with the proxy to intercept location callbacks.
     * @param locationListenerProxy the original location listener
     * @return the proxied location listener
     */
    public Object wrapper(final Object locationListenerProxy) {
        mBase = locationListenerProxy;
        injectHook();
        return getProxyInvocation();
    }


    /**
     * Returns the original location listener.
     * @return the base location listener object
     */
    @Override
    protected Object getWho() {
        return mBase;
    }


    /**
     * No-op injection; the proxy wrapping is handled by the wrapper method.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) { }

    @ProxyMethod("onLocationChanged")
    public static class OnLocationChanged extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args[0] instanceof List) {
                List<Location> locations = (List<Location>) args[0];
                locations.clear();
                locations.add(BLocationManager.get().getLocation(BActivityThread.getUserId(), BActivityThread.getAppPackageName()).convert2SystemLocation());
                args[0] = locations;
            } else if (args[0] instanceof Location) {
                args[0] = BLocationManager.get().getLocation(BActivityThread.getUserId(), BActivityThread.getAppPackageName()).convert2SystemLocation();
            }
            return method.invoke(who, args);
        }
    }


    /**
     * Checks if the hook environment is compromised.
     * @return always returns false
     */
    @Override
    public boolean isBadEnv() {
        return false;
    }
}
