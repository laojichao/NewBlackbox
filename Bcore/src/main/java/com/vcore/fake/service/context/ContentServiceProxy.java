package com.vcore.fake.service.context;

import java.lang.reflect.Method;

import black.android.content.IContentService;
import black.android.os.ServiceManager;
import com.vcore.fake.hook.BinderInvocationStub;
import com.vcore.fake.hook.MethodHook;
import com.vcore.fake.hook.ProxyMethod;

/**
 * Proxy for IContentService system service that intercepts content observer registration and change notification calls, providing no-op implementations to isolate the virtual environment from the host content service.
 */
public class ContentServiceProxy extends BinderInvocationStub {
    public ContentServiceProxy() {
        super(ServiceManager.getService.call("content"));
    }


    /**
     * Returns the IContentService binder interface from ServiceManager.
     * @return the IContentService proxy instance
     */
    @Override
    protected Object getWho() {
        return IContentService.Stub.asInterface.call(ServiceManager.getService.call("content"));
    }


    /**
     * Replaces the content system service with the proxied version.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("content");
    }


    /**
     * Checks if the hook environment is compromised.
     * @return always returns false
     */
    @Override
    public boolean isBadEnv() {
        return false;
    }

    @ProxyMethod("registerContentObserver")
    public static class RegisterContentObserver extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return 0;
        }
    }

    @ProxyMethod("notifyChange")
    public static class NotifyChange extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return 0;
        }
    }
}
