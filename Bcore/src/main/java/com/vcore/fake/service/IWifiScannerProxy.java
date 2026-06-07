package com.vcore.fake.service;

import black.android.net.wifi.IWifiManager;
import black.android.os.ServiceManager;
import com.vcore.fake.hook.BinderInvocationStub;

/**
 * Proxy for the wifiscanner system service that provides a no-op proxy implementation to intercept Wi-Fi scanning operations in the virtual environment.
 */
public class IWifiScannerProxy extends BinderInvocationStub {
    public IWifiScannerProxy() {
        super(ServiceManager.getService.call("wifiscanner"));
    }


    /**
     * Returns the wifiscanner service binder interface from ServiceManager.
     * @return the IWifiManager proxy instance for wifiscanner
     */
    @Override
    protected Object getWho() {
        return IWifiManager.Stub.asInterface.call(ServiceManager.getService.call("wifiscanner"));
    }


    /**
     * Replaces the wifiscanner system service with the proxied version.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("wifiscanner");
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
