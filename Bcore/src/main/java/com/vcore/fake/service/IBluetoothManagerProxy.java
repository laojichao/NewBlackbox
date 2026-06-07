package com.vcore.fake.service;

import java.lang.reflect.Method;

import black.android.bluetooth.IBluetoothManager;
import black.android.os.ServiceManager;
import com.vcore.fake.hook.BinderInvocationStub;
import com.vcore.fake.hook.MethodHook;
import com.vcore.fake.hook.ProxyMethod;

/**
 * Proxy for IBluetoothManager system service that intercepts Bluetooth management operations, returning null for the device Bluetooth name to hide real device information.
 */
public class IBluetoothManagerProxy extends BinderInvocationStub {
    public static final String TAG = "IBluetoothManagerProxy";

    public IBluetoothManagerProxy() {
        super(ServiceManager.getService.call("bluetooth_manager"));
    }


    /**
     * Returns the IBluetoothManager binder interface from ServiceManager.
     * @return the IBluetoothManager proxy instance
     */
    @Override
    protected Object getWho() {
        return IBluetoothManager.Stub.asInterface.call(ServiceManager.getService.call("bluetooth_manager"));
    }


    /**
     * Replaces the bluetooth_manager system service with the proxied version.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("bluetooth_manager");

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
        addMethodHook(new GetName());
    }

    @ProxyMethod("getName")
    public static class GetName extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return null;
        }
    }
}
