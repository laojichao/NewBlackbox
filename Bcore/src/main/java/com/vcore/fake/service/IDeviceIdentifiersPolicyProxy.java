package com.vcore.fake.service;

import java.lang.reflect.Method;

import black.android.os.IDeviceIdentifiersPolicyService;
import black.android.os.ServiceManager;
import com.vcore.BlackBoxCore;
import com.vcore.fake.hook.BinderInvocationStub;
import com.vcore.fake.hook.MethodHook;
import com.vcore.fake.hook.ProxyMethod;
import com.vcore.fake.service.base.PkgMethodProxy;
import com.vcore.utils.Md5Utils;

/**
 * Proxy for IDeviceIdentifiersPolicyService that intercepts device identifier queries, returning an MD5 hash of the host package name as the device serial number.
 */
public class IDeviceIdentifiersPolicyProxy extends BinderInvocationStub {
    public IDeviceIdentifiersPolicyProxy() {
        super(ServiceManager.getService.call("device_identifiers"));
    }


    /**
     * Returns the IDeviceIdentifiersPolicyService binder interface from ServiceManager.
     * @return the IDeviceIdentifiersPolicyService proxy instance
     */
    @Override
    protected Object getWho() {
        return IDeviceIdentifiersPolicyService.Stub.asInterface.call(ServiceManager.getService.call("device_identifiers"));
    }


    /**
     * Replaces the device_identifiers system service with the proxied version.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("device_identifiers");

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
        addMethodHook(new PkgMethodProxy("getSerialForPackage"));
    }

    @ProxyMethod("getSerialForPackage")
    public static class GetSerialForPackage extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return Md5Utils.md5(BlackBoxCore.getHostPkg());
        }
    }
}
