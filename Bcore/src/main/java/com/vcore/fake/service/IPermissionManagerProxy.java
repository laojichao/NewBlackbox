package com.vcore.fake.service;

import android.content.pm.PackageManager;
import android.os.Build;

import java.lang.reflect.Method;

import black.android.app.ActivityThread;
import black.android.app.ApplicationPackageManager;
import black.android.app.ContextImpl;
import black.android.os.ServiceManager;
import black.android.permission.IPermissionManager;
import com.vcore.BlackBoxCore;
import com.vcore.fake.hook.BinderInvocationStub;
import com.vcore.fake.hook.MethodHook;
import com.vcore.fake.service.base.PkgMethodProxy;
import com.vcore.fake.service.base.UidMethodProxy;
import com.vcore.utils.MethodParameterUtils;
import com.vcore.utils.compat.BuildCompat;

/**
 * Proxy for IPermissionManager system service (Android 10+) that intercepts permission management operations including runtime permission grants, permission flag queries, and permission checks, replacing package names and UIDs for the virtual environment.
 */
public class IPermissionManagerProxy extends BinderInvocationStub {
    public static final String TAG = "IPermissionManagerProxy";

    public IPermissionManagerProxy() {
        super(ServiceManager.getService.call("permissionmgr"));
    }


    /**
     * Returns the IPermissionManager binder interface from ServiceManager.
     * @return the IPermissionManager proxy instance
     */
    @Override
    protected Object getWho() {
        return IPermissionManager.Stub.asInterface.call(ServiceManager.getService.call("permissionmgr"));
    }


    /**
     * Replaces the permissionmgr system service, ActivityThread.sPermissionManager, and the ApplicationPackageManager permission manager reference.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("permissionmgr");
        ActivityThread.sPermissionManager.set(proxyInvocation);

        Object systemContext = ActivityThread.getSystemContext.call(BlackBoxCore.mainThread());
        PackageManager packageManager = ContextImpl.mPackageManager.get(systemContext);
        if (packageManager != null) {
            try {
                ApplicationPackageManager.mPermissionManager.set(packageManager, proxyInvocation);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onBindMethod() {
        super.onBindMethod();
        addMethodHook(new PkgMethodProxy("getPermissionInfo"));
        addMethodHook(new PkgMethodProxy("getPermissionFlags"));
        addMethodHook(new PkgMethodProxy("updatePermissionFlags"));
        addMethodHook(new PkgMethodProxy("grantRuntimePermission"));
        addMethodHook(new PkgMethodProxy("revokeRuntimePermission"));
        addMethodHook(new PkgMethodProxy("shouldShowRequestPermissionRationale"));
        addMethodHook(new PkgMethodProxy("isPermissionRevokedByPolicy"));
        addMethodHook(new PkgMethodProxy("startOneTimePermissionSession"));
        addMethodHook(new PkgMethodProxy("stopOneTimePermissionSession"));
        addMethodHook(new PkgMethodProxy("setAutoRevokeExempted"));
        addMethodHook(new PkgMethodProxy("isAutoRevokeExempted"));

        if (BuildCompat.isT()) {
            addMethodHook(new PkgMethodProxy("getAllowlistedRestrictedPermissions"));
            addMethodHook(new PkgMethodProxy("addAllowlistedRestrictedPermission"));
            addMethodHook(new PkgMethodProxy("removeAllowlistedRestrictedPermission"));
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.S) {
            addMethodHook(new PkgMethodProxy("revokePostNotificationPermissionWithoutKillForTest"));
        } else {
            addMethodHook(new PkgMethodProxy("checkPermission"));
            addMethodHook(new UidMethodProxy("checkUidPermission", 1));
            addMethodHook(new PkgMethodProxy("getWhitelistedRestrictedPermissions"));
            addMethodHook(new PkgMethodProxy("addWhitelistedRestrictedPermission"));
            addMethodHook(new PkgMethodProxy("removeWhitelistedRestrictedPermission"));
            addMethodHook(new PkgMethodProxy("setDefaultBrowser"));
            addMethodHook(new PkgMethodProxy("grantDefaultPermissionsToActiveLuiApp"));
            addMethodHook("checkDeviceIdentifierAccess", new MethodHook() {
                @Override
                protected Object hook(Object who, Method method, Object[] args) throws Throwable {
                    MethodParameterUtils.replaceFirstAppPkg(args);
                    MethodParameterUtils.replaceLastUid(args);
                    return method.invoke(who, args);
                }
            });
        }
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
