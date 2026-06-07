package com.vcore.fake.service;

import java.lang.reflect.Method;

import black.android.os.INetworkManagementService;
import black.android.os.ServiceManager;
import com.vcore.fake.hook.BinderInvocationStub;
import com.vcore.fake.hook.MethodHook;
import com.vcore.fake.hook.ProxyMethod;
import com.vcore.fake.service.base.UidMethodProxy;
import com.vcore.utils.MethodParameterUtils;

/**
 * Proxy for INetworkManagementService system service that intercepts network management operations including cleartext network policy and metered network blacklist/whitelist, replacing UIDs and package names for the virtual environment.
 */
public class INetworkManagementServiceProxy extends BinderInvocationStub {

    public INetworkManagementServiceProxy() {
        super(ServiceManager.getService.call("network_management"));
    }


    /**
     * Returns the INetworkManagementService binder interface from ServiceManager.
     * @return the INetworkManagementService proxy instance
     */
    @Override
    protected Object getWho() {
        return INetworkManagementService.Stub.asInterface.call(ServiceManager.getService.call("network_management"));
    }


    /**
     * Replaces the network_management system service with the proxied version.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("network_management");
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
        addMethodHook(new UidMethodProxy("setUidCleartextNetworkPolicy", 0));
        addMethodHook(new UidMethodProxy("setUidMeteredNetworkBlacklist", 0));
        addMethodHook(new UidMethodProxy("setUidMeteredNetworkWhitelist", 0));
    }

    @ProxyMethod("getNetworkStatsUidDetail")
    public static class GetNetworkStatsUidDetail extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstUid(args);
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }
}
