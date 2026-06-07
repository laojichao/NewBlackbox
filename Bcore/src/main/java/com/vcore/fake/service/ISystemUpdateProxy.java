package com.vcore.fake.service;

import black.android.os.ServiceManager;
import black.android.view.IAutoFillManager;
import com.vcore.fake.hook.BinderInvocationStub;

/**
 * Proxy for the system_update system service that intercepts system update queries, providing a no-op implementation to prevent update checks in the virtual environment.
 */
public class ISystemUpdateProxy extends BinderInvocationStub {
    public ISystemUpdateProxy() {
        super(ServiceManager.getService.call("system_update"));
    }


    /**
     * Returns the system update service binder interface from ServiceManager.
     * @return the system update service proxy instance
     */
    @Override
    protected Object getWho() {
        return IAutoFillManager.Stub.asInterface.call(ServiceManager.getService.call("system_update"));
    }


    /**
     * Replaces the system_update system service with the proxied version.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("system_update");
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
