package com.vcore.fake.service;

import android.content.Context;
import android.os.IInterface;

import java.lang.reflect.Method;
import java.util.Objects;

import black.android.os.ServiceManager;
import black.android.view.IWindowManager;
import black.android.view.WindowManagerGlobal;
import com.vcore.fake.hook.BinderInvocationStub;
import com.vcore.fake.hook.MethodHook;
import com.vcore.fake.hook.ProxyMethod;

/**
 * Proxy for IWindowManager system service that intercepts window management operations, wrapping the window session with IWindowSessionProxy for package name replacement.
 */
public class IWindowManagerProxy extends BinderInvocationStub {
    public static final String TAG = "WindowManagerStub";

    public IWindowManagerProxy() {
        super(ServiceManager.getService.call(Context.WINDOW_SERVICE));
    }


    /**
     * Returns the IWindowManager binder interface from ServiceManager.
     * @return the IWindowManager proxy instance
     */
    @Override
    protected Object getWho() {
        return IWindowManager.Stub.asInterface.call(ServiceManager.getService.call(Context.WINDOW_SERVICE));
    }


    /**
     * Replaces the system WINDOW_SERVICE with the proxied version and clears the cached WindowManagerGlobal reference.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.WINDOW_SERVICE);
        WindowManagerGlobal.sWindowManagerService.set(null);
    }


    /**
     * Checks if the hook environment is compromised.
     * @return always returns false
     */
    @Override
    public boolean isBadEnv() {
        return false;
    }


    @ProxyMethod("openSession")
    public static class OpenSession extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            IInterface session = (IInterface) method.invoke(who, args);
            IWindowSessionProxy IWindowSessionProxy = new IWindowSessionProxy(Objects.requireNonNull(session));
            IWindowSessionProxy.injectHook();
            return IWindowSessionProxy.getProxyInvocation();
        }
    }
}
