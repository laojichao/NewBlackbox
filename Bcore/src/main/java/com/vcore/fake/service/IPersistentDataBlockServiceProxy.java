package com.vcore.fake.service;

import black.android.os.ServiceManager;
import black.android.service.persistentdata.IPersistentDataBlockService;
import com.vcore.fake.hook.BinderInvocationStub;
import com.vcore.fake.service.base.ValueMethodProxy;

/**
 * Proxy for IPersistentDataBlockService system service that intercepts persistent data block operations (read, write, wipe, getOemUnlockEnabled), returning stub/default values.
 */
public class IPersistentDataBlockServiceProxy extends BinderInvocationStub {

    public IPersistentDataBlockServiceProxy() {
        super(ServiceManager.getService.call("persistent_data_block"));
    }


    /**
     * Returns the IPersistentDataBlockService binder interface from ServiceManager.
     * @return the IPersistentDataBlockService proxy instance
     */
    @Override
    protected Object getWho() {
        return IPersistentDataBlockService.Stub.asInterface.call(ServiceManager.getService.call("persistent_data_block"));
    }


    /**
     * Replaces the persistent_data_block system service with the proxied version.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("persistent_data_block");
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
        super.onBindMethod();
        addMethodHook(new ValueMethodProxy("write", -1));
        addMethodHook(new ValueMethodProxy("read", new byte[0]));
        addMethodHook(new ValueMethodProxy("wipe", null));
        addMethodHook(new ValueMethodProxy("getDataBlockSize", 0));
        addMethodHook(new ValueMethodProxy("getMaximumDataBlockSize", 0));
        addMethodHook(new ValueMethodProxy("setOemUnlockEnabled", 0));
        addMethodHook(new ValueMethodProxy("getOemUnlockEnabled", false));
    }
}
