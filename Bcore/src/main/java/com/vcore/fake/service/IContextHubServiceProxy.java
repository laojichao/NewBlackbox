package com.vcore.fake.service;

import black.android.hardware.location.IContextHubService;
import black.android.os.ServiceManager;
import com.vcore.fake.hook.BinderInvocationStub;
import com.vcore.fake.service.base.ValueMethodProxy;
import com.vcore.utils.compat.BuildCompat;

/**
 * Proxy for IContextHubService system service that intercepts context hub (sensor hub) operations including registerCallback, getContextHubInfo, and getContextHubHandles, returning stub values.
 */
public class IContextHubServiceProxy extends BinderInvocationStub {
    public IContextHubServiceProxy() {
        super(ServiceManager.getService.call(getServiceName()));
    }

    private static String getServiceName() {
        return BuildCompat.isOreo() ? "contexthub" : "contexthub_service";
    }


    /**
     * Returns the IContextHubService binder interface from ServiceManager.
     * @return the IContextHubService proxy instance
     */
    @Override
    protected Object getWho() {
        return IContextHubService.Stub.asInterface.call(ServiceManager.getService.call(getServiceName()));
    }


    /**
     * Replaces the contexthub system service with the proxied version.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(getServiceName());
    }

    @Override
    protected void onBindMethod() {
        super.onBindMethod();
        addMethodHook(new ValueMethodProxy("registerCallback", 0));
        addMethodHook(new ValueMethodProxy("getContextHubInfo", null));
        addMethodHook(new ValueMethodProxy("getContextHubHandles",new int[]{}));
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
