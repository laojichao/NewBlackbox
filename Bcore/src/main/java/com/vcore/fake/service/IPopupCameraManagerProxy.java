package com.vcore.fake.service;

import android.content.Context;

import black.android.os.ServiceManager;
import black.oem.vivo.IPopupCameraManager;
import com.vcore.fake.hook.BinderInvocationStub;
import com.vcore.fake.service.base.PkgMethodProxy;

/**
 * @author Findger
 * @function
 * @date :2023/10/8 20:19
 **/
public class IPopupCameraManagerProxy extends BinderInvocationStub {

    public IPopupCameraManagerProxy() {
        super(ServiceManager.getService.call("popup_camera_service"));
    }


    /**
     * Returns the IPopupCameraManager binder interface from ServiceManager.
     * @return the IPopupCameraManager proxy instance
     */
    @Override
    protected Object getWho() {
        return IPopupCameraManager.Stub.asInterface.call(ServiceManager.getService.call("popup_camera_service"));
    }


    /**
     * Replaces the popup_camera_service system service with the proxied version.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("popup_camera_service");
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
        addMethodHook(new PkgMethodProxy("notifyCameraStatus"));
    }
}
