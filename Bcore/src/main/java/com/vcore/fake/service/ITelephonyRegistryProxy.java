package com.vcore.fake.service;

import java.lang.reflect.Method;

import black.android.os.ServiceManager;
import black.com.android.internal.telephony.ITelephonyRegistry;
import com.vcore.fake.hook.BinderInvocationStub;
import com.vcore.fake.hook.MethodHook;
import com.vcore.fake.hook.ProxyMethod;
import com.vcore.utils.MethodParameterUtils;

/**
 * Proxy for ITelephonyRegistry system service that intercepts telephony state listener registration calls, replacing package names for the virtual environment.
 */
public class ITelephonyRegistryProxy extends BinderInvocationStub {
    public ITelephonyRegistryProxy() {
        super(ServiceManager.getService.call("telephony.registry"));
    }


    /**
     * Returns the ITelephonyRegistry binder interface from ServiceManager.
     * @return the ITelephonyRegistry proxy instance
     */
    @Override
    protected Object getWho() {
        return ITelephonyRegistry.Stub.asInterface.call(ServiceManager.getService.call("telephony.registry"));
    }


    /**
     * Replaces the telephony.registry system service with the proxied version.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("telephony.registry");
    }


    /**
     * Checks if the hook environment is compromised.
     * @return always returns false
     */
    @Override
    public boolean isBadEnv() {
        return false;
    }

    @ProxyMethod("listenForSubscriber")
    public static class ListenForSubscriber extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("listen")
    public static class Listen extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }
}
