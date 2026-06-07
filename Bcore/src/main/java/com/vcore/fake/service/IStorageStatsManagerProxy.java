package com.vcore.fake.service;

import android.content.Context;

import java.lang.reflect.Method;

import black.android.app.usage.IStorageStatsManager;
import black.android.os.ServiceManager;
import com.vcore.fake.hook.BinderInvocationStub;
import com.vcore.utils.MethodParameterUtils;

/**
 * Proxy for IStorageStatsManager system service that intercepts storage statistics queries, replacing package names and UIDs for the virtual environment.
 */
public class IStorageStatsManagerProxy extends BinderInvocationStub {
    public IStorageStatsManagerProxy() {
        super(ServiceManager.getService.call(Context.STORAGE_STATS_SERVICE));
    }


    /**
     * Returns the IStorageStatsManager binder interface from ServiceManager.
     * @return the IStorageStatsManager proxy instance
     */
    @Override
    protected Object getWho() {
        return IStorageStatsManager.Stub.asInterface.call(ServiceManager.getService.call(Context.STORAGE_STATS_SERVICE));
    }


    /**
     * Replaces the system STORAGE_STATS_SERVICE with the proxied version.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.STORAGE_STATS_SERVICE);
    }


    /**
     * Intercepts all method calls to replace the first package name and last UID arguments.
     * @param proxy  the proxy object
     * @param method the method being invoked
     * @param args   the method arguments
     * @return the result of the method invocation
     * @throws Throwable if the invocation fails
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodParameterUtils.replaceFirstAppPkg(args);
        MethodParameterUtils.replaceLastUid(args);
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
}
