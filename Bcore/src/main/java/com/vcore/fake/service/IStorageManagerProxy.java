package com.vcore.fake.service;

import android.os.IInterface;
import android.os.storage.StorageVolume;

import java.lang.reflect.Method;

import black.android.os.ServiceManager;
import black.android.os.mount.IMountService;
import black.android.os.storage.IStorageManager;
import com.vcore.BlackBoxCore;
import com.vcore.app.BActivityThread;
import com.vcore.fake.hook.BinderInvocationStub;
import com.vcore.fake.hook.MethodHook;
import com.vcore.fake.hook.ProxyMethod;
import com.vcore.utils.compat.BuildCompat;

/**
 * Proxy for IStorageManager (mount service) system service that intercepts storage volume queries and directory creation, redirecting volume list queries through the virtual environment's BStorageManager.
 */
public class IStorageManagerProxy extends BinderInvocationStub {
    public IStorageManagerProxy() {
        super(ServiceManager.getService.call("mount"));
    }


    /**
     * Returns the IStorageManager/IMountService binder interface from ServiceManager.
     * @return the storage manager proxy instance
     */
    @Override
    protected Object getWho() {
        IInterface mount;
        if (BuildCompat.isOreo()) {
            mount = IStorageManager.Stub.asInterface.call(ServiceManager.getService.call("mount"));
        } else {
            mount = IMountService.Stub.asInterface.call(ServiceManager.getService.call("mount"));
        }
        return mount;
    }


    /**
     * Replaces the mount system service with the proxied version.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("mount");
    }


    /**
     * Checks if the hook environment is compromised.
     * @return always returns false
     */
    @Override
    public boolean isBadEnv() {
        return false;
    }

    @ProxyMethod("getVolumeList")
    public static class GetVolumeList extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args == null) {
                StorageVolume[] volumeList = BlackBoxCore.getBStorageManager().getVolumeList(BActivityThread.getBUid(), null, 0, BActivityThread.getUserId());
                if (volumeList == null) {
                    return method.invoke(who, args);
                }
                return volumeList;
            }

            try {
                int uid = (int) args[0];
                String packageName = (String) args[1];
                int flags = (int) args[2];

                StorageVolume[] volumeList = BlackBoxCore.getBStorageManager().getVolumeList(uid, packageName, flags, BActivityThread.getUserId());
                if (volumeList == null) {
                    return method.invoke(who, args);
                }
                return volumeList;
            } catch (Throwable t) {
                return method.invoke(who, args);
            }
        }
    }

    @ProxyMethod("mkdirs")
    public static class MkDirs extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return 0;
        }
    }
}
