package com.vcore.fake.service;

import android.os.IInterface;

import java.lang.reflect.Method;

import black.android.hardware.display.DisplayManagerGlobal;
import com.vcore.fake.hook.ClassInvocationStub;
import com.vcore.fake.hook.MethodHook;
import com.vcore.fake.hook.ProxyMethod;
import com.vcore.utils.MethodParameterUtils;

/**
 * Proxy for IDisplayManager (via DisplayManagerGlobal) that intercepts virtual display creation, replacing the calling package name with the virtual app package.
 */
public class IDisplayManagerProxy extends ClassInvocationStub {
    public IDisplayManagerProxy() { }


    /**
     * Returns the IDisplayManager from DisplayManagerGlobal.
     * @return the IDisplayManager instance
     */
    @Override
    protected Object getWho() {
        return DisplayManagerGlobal.mDm.get(DisplayManagerGlobal.getInstance.call());
    }


    /**
     * Replaces the DisplayManagerGlobal IDisplayManager reference with the proxy.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        Object dmg = DisplayManagerGlobal.getInstance.call();
        DisplayManagerGlobal.mDm.set(dmg, getProxyInvocation());

    }

    @Override
    protected void onBindMethod() {
        addMethodHook(new CreateVirtualDisplay());
    }


    /**
     * Checks if the hook environment is compromised by verifying DisplayManagerGlobal still points to the proxy.
     * @return true if the proxy has been replaced
     */
    @Override
    public boolean isBadEnv() {
        Object dmg = DisplayManagerGlobal.getInstance.call();
        IInterface mDm = DisplayManagerGlobal.mDm.get(dmg);
        return mDm != getProxyInvocation();
    }

    @ProxyMethod("createVirtualDisplay")
    public static class CreateVirtualDisplay extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }
}
