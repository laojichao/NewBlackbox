package com.vcore.fake.service;

import android.content.Context;

import java.lang.reflect.Method;

import black.android.media.IMediaRouterService;
import black.android.os.ServiceManager;
import com.vcore.core.system.accounts.RegisteredServicesParser;
import com.vcore.fake.hook.BinderInvocationStub;
import com.vcore.fake.hook.MethodHook;
import com.vcore.fake.hook.ProxyMethod;
import com.vcore.utils.MethodParameterUtils;

/**
 * Proxy for IMediaRouterService system service that intercepts media routing operations, replacing the calling package name with the virtual app package.
 */
public class IMediaRouterServiceProxy extends BinderInvocationStub {
    public IMediaRouterServiceProxy() {
        super(ServiceManager.getService.call(Context.MEDIA_ROUTER_SERVICE));
    }


    /**
     * Returns the IMediaRouterService binder interface from ServiceManager.
     * @return the IMediaRouterService proxy instance
     */
    @Override
    protected Object getWho() {
        return IMediaRouterService.Stub.asInterface.call(ServiceManager.getService.call(Context.MEDIA_ROUTER_SERVICE));
    }


    /**
     * Replaces the system MEDIA_ROUTER_SERVICE with the proxied version.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.MEDIA_ROUTER_SERVICE);

    }


    /**
     * Intercepts all method calls to replace the first package name argument.
     * @param proxy  the proxy object
     * @param method the method being invoked
     * @param args   the method arguments
     * @return the result of the method invocation
     * @throws Throwable if the invocation fails
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodParameterUtils.replaceFirstAppPkg(args);
        return super.invoke(proxy, method, args);
    }


    /**
     * Checks if the hook environment is compromised.
     * @return always returns false
     */
    @Override
    public boolean isBadEnv() {
        return false;
    }

    @ProxyMethod("registerClientAsUser")
    public static class registerClientAsUser extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("registerRouter2")
    public static class registerRouter2 extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return method.invoke(who, args);
        }
    }
}
