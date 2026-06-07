package com.vcore.fake.service;

import android.content.Context;

import java.lang.reflect.Method;

import black.android.content.pm.ILauncherApps;
import black.android.os.ServiceManager;
import com.vcore.fake.hook.BinderInvocationStub;
import com.vcore.utils.MethodParameterUtils;

/**
 * Proxy for ILauncherApps system service that intercepts launcher app queries, replacing the calling package name with the virtual app package.
 */
public class ILauncherAppsProxy extends BinderInvocationStub {
    public ILauncherAppsProxy() {
        super(ServiceManager.getService.call(Context.LAUNCHER_APPS_SERVICE));
    }


    /**
     * Returns the ILauncherApps binder interface from ServiceManager.
     * @return the ILauncherApps proxy instance
     */
    @Override
    protected Object getWho() {
        return ILauncherApps.Stub.asInterface.call(ServiceManager.getService.call(Context.LAUNCHER_APPS_SERVICE));
    }


    /**
     * Replaces the system LAUNCHER_APPS_SERVICE with the proxied version.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.LAUNCHER_APPS_SERVICE);
    }


    /**
     * Checks if the hook environment is compromised.
     * @return always returns false
     */
    @Override
    public boolean isBadEnv() {
        return false;
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
        // TODO: shouldHideFromSuggestions
        return super.invoke(proxy, method, args);
    }
}
