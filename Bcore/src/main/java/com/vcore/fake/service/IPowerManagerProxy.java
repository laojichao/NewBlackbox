package com.vcore.fake.service;

import android.content.Context;

import black.android.os.IPowerManager;
import black.android.os.ServiceManager;
import com.vcore.fake.hook.BinderInvocationStub;
import com.vcore.fake.service.base.ValueMethodProxy;

/**
 * Proxy for IPowerManager system service that intercepts power management operations including wake lock acquisition/release, reboot, and shutdown commands, returning stub values.
 */
public class IPowerManagerProxy extends BinderInvocationStub {
    public IPowerManagerProxy() {
        super(ServiceManager.getService.call(Context.POWER_SERVICE));
    }


    /**
     * Returns the IPowerManager binder interface from ServiceManager.
     * @return the IPowerManager proxy instance
     */
    @Override
    protected Object getWho() {
        return IPowerManager.Stub.asInterface.call(ServiceManager.getService.call(Context.POWER_SERVICE));
    }


    /**
     * Replaces the system POWER_SERVICE with the proxied version.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.POWER_SERVICE);
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
        addMethodHook(new ValueMethodProxy("acquireWakeLock", 0));
        addMethodHook(new ValueMethodProxy("acquireWakeLockWithUid", 0));
        addMethodHook(new ValueMethodProxy("releaseWakeLock", 0));
        addMethodHook(new ValueMethodProxy("updateWakeLockWorkSource", 0));
        addMethodHook(new ValueMethodProxy("isWakeLockLevelSupported", true));
        addMethodHook(new ValueMethodProxy("reboot", null));
        addMethodHook(new ValueMethodProxy("rebootSafeMode", null));
        addMethodHook(new ValueMethodProxy("shutdown", null));
    }
}
