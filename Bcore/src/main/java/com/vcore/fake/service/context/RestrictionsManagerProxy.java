package com.vcore.fake.service.context;

import android.content.Context;

import java.lang.reflect.Method;

import black.android.content.IRestrictionsManager;
import black.android.os.ServiceManager;
import com.vcore.BlackBoxCore;
import com.vcore.fake.hook.BinderInvocationStub;
import com.vcore.fake.hook.MethodHook;
import com.vcore.fake.hook.ProxyMethod;

/**
 * Proxy for IRestrictionsManager system service that intercepts application restriction queries, replacing the package name with the host package to retrieve correct restrictions.
 */
public class RestrictionsManagerProxy extends BinderInvocationStub {
    public RestrictionsManagerProxy() {
        super(ServiceManager.getService.call(Context.RESTRICTIONS_SERVICE));
    }


    /**
     * Returns the IRestrictionsManager binder interface from ServiceManager.
     * @return the IRestrictionsManager proxy instance
     */
    @Override
    protected Object getWho() {
        return IRestrictionsManager.Stub.asInterface.call(ServiceManager.getService.call(Context.RESTRICTIONS_SERVICE));
    }


    /**
     * Replaces the system RESTRICTIONS_SERVICE with the proxied version.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.RESTRICTIONS_SERVICE);
    }


    /**
     * Checks if the hook environment is compromised.
     * @return always returns false
     */
    @Override
    public boolean isBadEnv() {
        return false;
    }

    @ProxyMethod("getApplicationRestrictions")
    public static class GetApplicationRestrictions extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            args[0] = BlackBoxCore.getHostPkg();
            return method.invoke(who, args);
        }
    }
}
