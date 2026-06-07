package com.vcore.fake.service;

import android.content.Context;
import android.os.IBinder;

import black.android.os.ServiceManager;
import black.oem.vivo.IVivoPermissonService;
import com.vcore.fake.hook.BinderInvocationStub;
import com.vcore.fake.service.base.PkgMethodProxy;

/**
 * @author Findger
 * @function
 * @date :2023/10/8 20:36
 **/
public class IVivoPermissionServiceProxy extends BinderInvocationStub {
    public IVivoPermissionServiceProxy() {
        super(ServiceManager.getService.call("vivo_permission_service"));
    }


    /**
     * Returns the IVivoPermissonService binder interface from ServiceManager.
     * @return the IVivoPermissonService proxy instance
     */
    @Override
    protected Object getWho() {
        return IVivoPermissonService.Stub.asInterface.call(ServiceManager.getService.call("vivo_permission_service"));
    }


    /**
     * Replaces the vivo_permission_service system service with the proxied version.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("vivo_permission_service");
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
        addMethodHook(new PkgMethodProxy("checkPermission"));
        addMethodHook(new PkgMethodProxy("getAppPermission"));
        addMethodHook(new PkgMethodProxy("setAppPermission"));
        addMethodHook(new PkgMethodProxy("setWhiteListApp"));
        addMethodHook(new PkgMethodProxy("setBlackListApp"));
        addMethodHook(new PkgMethodProxy("noteStartActivityProcess"));
        addMethodHook(new PkgMethodProxy("isBuildInThirdPartApp"));
    }
}
