package com.vcore.fake.service;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import java.lang.reflect.Method;

import black.android.os.ServiceManager;
import black.android.view.accessibility.IAccessibilityManager;
import com.vcore.BlackBoxCore;
import com.vcore.core.system.user.BUserHandle;
import com.vcore.fake.hook.BinderInvocationStub;
import com.vcore.fake.hook.MethodHook;
import com.vcore.fake.hook.ProxyMethods;

/**
 * Proxy for IAccessibilityManager system service that intercepts accessibility-related calls and replaces the user ID parameter with the virtual environment user ID.
 */
public class IAccessibilityManagerProxy extends BinderInvocationStub {
    public IAccessibilityManagerProxy() {
        super(ServiceManager.getService.call(Context.ACCESSIBILITY_SERVICE));
    }


    /**
     * Returns the IAccessibilityManager binder interface from ServiceManager.
     * @return the IAccessibilityManager proxy instance
     */
    @Override
    protected Object getWho() {
        return IAccessibilityManager.Stub.asInterface.call(ServiceManager.getService.call(Context.ACCESSIBILITY_SERVICE));
    }


    /**
     * Replaces the system ACCESSIBILITY_SERVICE with the proxied version.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.ACCESSIBILITY_SERVICE);
    }


    /**
     * Checks if the hook environment is compromised.
     * @return always returns false
     */
    @Override
    public boolean isBadEnv() {
        return false;
    }

    @ProxyMethods({"interrupt", "sendAccessibilityEvent", "addClient", "removeClient", "getInstalledAccessibilityServiceList",
            "getEnabledAccessibilityServiceList", "addAccessibilityInteractionConnection", "getWindowToken", "setSystemAudioCaptioningEnabled",
            "isSystemAudioCaptioningUiEnabled", "setSystemAudioCaptioningUiEnabled"})
    public static class ReplaceUserId extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args != null) {
                int index = args.length - 1;
                Object arg = args[index];

                if (arg instanceof Integer) {
                    ApplicationInfo applicationInfo = BlackBoxCore.getContext().getApplicationInfo();
                    args[index] = BUserHandle.getUserId(applicationInfo.uid);
                }
            }
            return method.invoke(who, args);
        }
    }
}
