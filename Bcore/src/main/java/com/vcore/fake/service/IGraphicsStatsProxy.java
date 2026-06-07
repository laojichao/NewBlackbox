package com.vcore.fake.service;

import java.lang.reflect.Method;

import black.android.os.ServiceManager;
import black.android.view.IGraphicsStats;
import com.vcore.fake.hook.BinderInvocationStub;
import com.vcore.fake.hook.MethodHook;
import com.vcore.fake.hook.ProxyMethod;
import com.vcore.utils.MethodParameterUtils;

/**
 * Proxy for IGraphicsStats system service that intercepts graphics statistics requests, replacing the calling package name with the virtual app package.
 */
public class IGraphicsStatsProxy extends BinderInvocationStub {
    public IGraphicsStatsProxy() {
        super(ServiceManager.getService.call("graphicsstats"));
    }


    /**
     * Returns the IGraphicsStats binder interface from ServiceManager.
     * @return the IGraphicsStats proxy instance
     */
    @Override
    protected Object getWho() {
        return IGraphicsStats.Stub.asInterface.call(ServiceManager.getService.call("graphicsstats"));
    }


    /**
     * Replaces the graphicsstats system service with the proxied version.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("graphicsstats");

    }


    /**
     * Checks if the hook environment is compromised.
     * @return always returns false
     */
    @Override
    public boolean isBadEnv() {
        return false;
    }


    @ProxyMethod("requestBufferForProcess")
    public static class RequestBufferForProcess extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }
}
