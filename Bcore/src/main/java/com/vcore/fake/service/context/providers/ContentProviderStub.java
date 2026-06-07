package com.vcore.fake.service.context.providers;

import android.os.Build;
import android.os.Bundle;
import android.os.IInterface;

import java.lang.reflect.Method;

import black.android.content.AttributionSource;
import com.vcore.app.BActivityThread;
import com.vcore.fake.hook.ClassInvocationStub;
import com.vcore.utils.compat.ContextCompat;

/**
 * ContentProvider wrapper that intercepts ContentProvider calls, replacing the calling package name with the virtual app package name and handling special cases like android_id queries and AttributionSource fixes.
 */
public class ContentProviderStub extends ClassInvocationStub implements BContentProvider {
    public static final String TAG = "ContentProviderStub";
    private IInterface mBase;
    private String mAppPkg;


    /**
     * Wraps a real ContentProvider proxy with virtual package name substitution.
     * @param contentProviderProxy the original ContentProvider proxy
     * @param appPkg              the virtual app package name to substitute
     * @return the wrapped ContentProvider proxy
     */
    public IInterface wrapper(final IInterface contentProviderProxy, final String appPkg) {
        mBase = contentProviderProxy;
        mAppPkg = appPkg;

        injectHook();
        return (IInterface) getProxyInvocation();
    }

    private Bundle wrapBundle(String name, String value) {
        Bundle bundle = new Bundle();
        if (Build.VERSION.SDK_INT >= 24) {
            bundle.putString("name", name);
            bundle.putString("value", value);
        } else {
            bundle.putString(name, value);
        }
        return bundle;
    }


    /**
     * Returns the original ContentProvider proxy.
     * @return the base ContentProvider proxy
     */
    @Override
    protected Object getWho() {
        return mBase;
    }


    /**
     * No-op injection; the proxy wrapping is handled by the wrapper method.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) { }


    /**
     * Intercepts ContentProvider calls to replace the calling package and handle special cases like android_id and AttributionSource.
     * @param proxy  the proxy object
     * @param method the method being invoked
     * @param args   the method arguments
     * @return the result of the method invocation
     * @throws Throwable if the invocation fails
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("asBinder".equals(method.getName())) {
            return method.invoke(mBase, args);
        }
        if (args != null && args.length > 0) {
            Object arg = args[0];
            if (arg instanceof String) {
                args[0] = mAppPkg;
                if ("android_id".equals(arg)) {
                    return wrapBundle("android_id", "");
                }
            } else if (arg.getClass().getName().equals(AttributionSource.REF.getClazz().getName())) {
                ContextCompat.fixAttributionSourceState(arg, BActivityThread.getBUid());
            }
        }
        return method.invoke(mBase, args);
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
