package com.vcore.fake.service;

import android.os.IBinder;

import black.android.os.ServiceManager;
import black.oem.flyme.IFlymePermissionService;
import com.vcore.fake.hook.BinderInvocationStub;
import com.vcore.fake.service.base.PkgMethodProxy;

/**
 * @author Findger
 * @function
 * @date :2023/10/9 12:34
 **/
public class IFlymePermissionServiceProxy extends BinderInvocationStub {
    public IFlymePermissionServiceProxy() {
        super(ServiceManager.getService.call("flyme_permission"));
    }


    /**
     * Returns the Flyme permission service binder interface.
     * @return the IFlymePermissionService type
     */
    @Override
    protected Object getWho() {
        return IFlymePermissionService.Stub.TYPE;
    }


    /**
     * Registers the noteIntentOperation method hook.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        addMethodHook(new PkgMethodProxy("noteIntentOperation"));
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
