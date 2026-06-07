package com.vcore.fake.service;

import android.os.IBinder;

import black.android.os.ServiceManager;
import black.oem.vivo.ISystemDefenceManager;
import com.vcore.fake.hook.BinderInvocationStub;
import com.vcore.fake.service.base.PkgMethodProxy;

/**
 * @author Findger
 * @function
 * @date :2023/10/8 20:30
 **/
public class ISystemDefenceManagerProxy extends BinderInvocationStub {
    public ISystemDefenceManagerProxy() {
        super(ServiceManager.getService.call("system_defence_service"));
    }


    /**
     * Returns the ISystemDefenceManager binder interface from ServiceManager.
     * @return the ISystemDefenceManager proxy instance
     */
    @Override
    protected Object getWho() {
        return ISystemDefenceManager.Stub.asInterface.call(ServiceManager.getService.call("system_defence_service"));
    }


    /**
     * Replaces the system_defence_service system service with the proxied version.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("system_defence_service");
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
        addMethodHook(new PkgMethodProxy("checkTransitionTimoutErrorDefence"));
        addMethodHook(new PkgMethodProxy("checkSkipKilledByRemoveTask"));
        addMethodHook(new PkgMethodProxy("checkSmallIconNULLPackage"));
        addMethodHook(new PkgMethodProxy("checkDelayUpdate"));
        addMethodHook(new PkgMethodProxy("onSetActivityResumed"));
        addMethodHook(new PkgMethodProxy("checkReinstallPacakge"));
        addMethodHook(new PkgMethodProxy("reportFgCrashData"));
    }
}
