package com.vcore.fake.service;

import black.android.net.IVpnManager;
import black.android.os.ServiceManager;
import com.vcore.fake.hook.BinderInvocationStub;
import com.vcore.fake.hook.ScanClass;

@ScanClass(VpnCommonProxy.class)
/**
 * Proxy for IVpnManager system service that intercepts VPN management operations, delegating VPN-related method hooks to VpnCommonProxy.
 */
public class IVpnManagerProxy extends BinderInvocationStub {
    public static final String TAG = "IVpnManagerProxy";

    public IVpnManagerProxy() {
        super(ServiceManager.getService.call("vpn_management"));
    }


    /**
     * Returns the IVpnManager binder interface from ServiceManager.
     * @return the IVpnManager proxy instance
     */
    @Override
    protected Object getWho() {
        return IVpnManager.Stub.asInterface.call(ServiceManager.getService.call("vpn_management"));
    }


    /**
     * Replaces the vpn_management system service with the proxied version.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("vpn_management");
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
