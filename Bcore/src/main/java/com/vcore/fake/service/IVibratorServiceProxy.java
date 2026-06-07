package com.vcore.fake.service;

import android.content.Context;
import android.os.IBinder;

import java.lang.reflect.Method;

import black.android.os.IVibratorManagerService;
import black.android.os.ServiceManager;
import black.com.android.internal.os.IVibratorService;
import com.vcore.fake.hook.BinderInvocationStub;
import com.vcore.utils.MethodParameterUtils;
import com.vcore.utils.compat.BuildCompat;

/**
 * Proxy for IVibratorService/IVibratorManagerService system service that intercepts vibrator control operations, replacing UIDs and package names for the virtual environment.
 */
public class IVibratorServiceProxy extends BinderInvocationStub {
    private static final String NAME;

    static {
        if (BuildCompat.isS()) {
            NAME = "vibrator_manager";
        } else {
            NAME = Context.VIBRATOR_SERVICE;
        }
    }

    public IVibratorServiceProxy() {
        super(ServiceManager.getService.call(NAME));
    }


    /**
     * Returns the IVibratorService/IVibratorManagerService binder interface from ServiceManager.
     * @return the vibrator service proxy instance
     */
    @Override
    protected Object getWho() {
        IBinder service = ServiceManager.getService.call(NAME);
        if (BuildCompat.isS()) {
            return IVibratorManagerService.Stub.asInterface.call(service);
        }
        return IVibratorService.Stub.asInterface.call(service);
    }


    /**
     * Replaces the vibrator system service with the proxied version.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(NAME);
    }


    /**
     * Intercepts all method calls to replace the first UID and package name arguments.
     * @param proxy  the proxy object
     * @param method the method being invoked
     * @param args   the method arguments
     * @return the result of the method invocation
     * @throws Throwable if the invocation fails
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodParameterUtils.replaceFirstUid(args);
        MethodParameterUtils.replaceFirstAppPkg(args);
        return super.invoke(proxy, method, args);
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
