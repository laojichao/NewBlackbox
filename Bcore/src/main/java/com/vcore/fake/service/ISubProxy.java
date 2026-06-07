package com.vcore.fake.service;

import black.android.os.ServiceManager;
import black.com.android.internal.telephony.ISub;
import com.vcore.fake.hook.BinderInvocationStub;
import com.vcore.fake.service.base.ValueMethodProxy;

/**
 * Proxy for ISub (telephony subscription info) system service that intercepts SIM subscription queries including active subscription info, subscription counts, and subscription list queries, returning null or -1 stub values.
 */
public class ISubProxy extends BinderInvocationStub {
    public static final String TAG = "ISubProxy";

    public ISubProxy() {
        super(ServiceManager.getService.call("isub"));
    }


    /**
     * Returns the ISub binder interface from ServiceManager.
     * @return the ISub proxy instance
     */
    @Override
    protected Object getWho() {
        return ISub.Stub.asInterface.call(ServiceManager.getService.call("isub"));
    }


    /**
     * Replaces the isub system service with the proxied version.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("isub");
    }


    /**
     * Checks if the hook environment is compromised.
     * @return always returns false
     */
    @Override
    public boolean isBadEnv() {
        return false;
    }

    @Override
    protected void onBindMethod() {
        super.onBindMethod();
        addMethodHook(new ValueMethodProxy("getAllSubInfoList", null));
        addMethodHook(new ValueMethodProxy("getAllSubInfoCount", -1));
        addMethodHook(new ValueMethodProxy("getActiveSubscriptionInfo", null));
        addMethodHook(new ValueMethodProxy("getActiveSubscriptionInfoForIccId", null));
        addMethodHook(new ValueMethodProxy("getActiveSubscriptionInfoForSimSlotIndex", null));
        addMethodHook(new ValueMethodProxy("getActiveSubscriptionInfoList", null));
        addMethodHook(new ValueMethodProxy("getActiveSubInfoCount", -1));
        addMethodHook(new ValueMethodProxy("getActiveSubInfoCountMax", -1));
        addMethodHook(new ValueMethodProxy("getAvailableSubscriptionInfoList", null));
        addMethodHook(new ValueMethodProxy("getAccessibleSubscriptionInfoList", null));
        addMethodHook(new ValueMethodProxy("addSubInfoRecord", -1));
        addMethodHook(new ValueMethodProxy("addSubInfo", -1));
        addMethodHook(new ValueMethodProxy("removeSubInfo", -1));
    }
}
