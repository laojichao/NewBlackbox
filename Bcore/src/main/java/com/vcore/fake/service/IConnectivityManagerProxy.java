package com.vcore.fake.service;

import android.content.Context;

import black.android.net.IConnectivityManager;
import black.android.os.ServiceManager;
import com.vcore.fake.hook.BinderInvocationStub;
import com.vcore.fake.service.base.ValueMethodProxy;

/**
 * Proxy for IConnectivityManager system service that intercepts network connectivity queries such as getAllNetworkInfo and getAllNetworks, returning null to prevent network enumeration.
 */
public class IConnectivityManagerProxy extends BinderInvocationStub {
    public static final String TAG = "IConnectivityManagerProxy";

    public IConnectivityManagerProxy() {
        super(ServiceManager.getService.call(Context.CONNECTIVITY_SERVICE));
    }


    /**
     * Returns the IConnectivityManager binder interface from ServiceManager.
     * @return the IConnectivityManager proxy instance
     */
    @Override
    protected Object getWho() {
        return IConnectivityManager.Stub.asInterface.call(ServiceManager.getService.call(Context.CONNECTIVITY_SERVICE));
    }


    /**
     * Replaces the system CONNECTIVITY_SERVICE with the proxied version.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    protected void onBindMethod() {
        super.onBindMethod();
        addMethodHook(new ValueMethodProxy("getAllNetworkInfo", null));
        addMethodHook(new ValueMethodProxy("getAllNetworks",null));
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
