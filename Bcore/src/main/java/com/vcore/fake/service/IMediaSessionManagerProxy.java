package com.vcore.fake.service;

import android.content.Context;

import java.lang.reflect.Method;

import black.android.media.session.ISessionManager;
import black.android.os.ServiceManager;
import com.vcore.BlackBoxCore;
import com.vcore.fake.hook.BinderInvocationStub;
import com.vcore.fake.hook.MethodHook;
import com.vcore.fake.hook.ProxyMethod;

/**
 * Proxy for ISessionManager (Media Session) system service that intercepts media session creation, replacing the calling package name with the host package name.
 */
public class IMediaSessionManagerProxy extends BinderInvocationStub {
    public IMediaSessionManagerProxy() {
        super(ServiceManager.getService.call(Context.MEDIA_SESSION_SERVICE));
    }


    /**
     * Returns the ISessionManager binder interface from ServiceManager.
     * @return the ISessionManager proxy instance
     */
    @Override
    protected Object getWho() {
        return ISessionManager.Stub.asInterface.call(ServiceManager.getService.call(Context.MEDIA_SESSION_SERVICE));
    }


    /**
     * Replaces the system MEDIA_SESSION_SERVICE with the proxied version.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.MEDIA_SESSION_SERVICE);

    }


    /**
     * Checks if the hook environment is compromised.
     * @return always returns false
     */
    @Override
    public boolean isBadEnv() {
        return false;
    }


    @ProxyMethod("createSession")
    public static class CreateSession extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args != null && args.length > 0 && args[0] instanceof String) {
                args[0] = BlackBoxCore.getHostPkg();
            }
            return method.invoke(who, args);
        }
    }
}
