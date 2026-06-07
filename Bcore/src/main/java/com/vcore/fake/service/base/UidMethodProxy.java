package com.vcore.fake.service.base;

import java.lang.reflect.Method;

import com.vcore.BlackBoxCore;
import com.vcore.app.BActivityThread;
import com.vcore.fake.hook.MethodHook;

/**
 * A reusable method hook that replaces the UID argument at a specified index in method calls with the host UID when the UID matches the virtual app UID. Used to ensure system service calls appear to originate from the host process.
 */
public class UidMethodProxy extends MethodHook {
    private final int index;
    private final String name;

    public UidMethodProxy(String name, int index) {
        this.index = index;
        this.name = name;
    }


    /**
     * Returns the name of the system service method to hook.
     * @return the method name
     */
    @Override
    protected String getMethodName() {
        return name;
    }


    /**
     * Replaces the UID argument at the configured index with the host UID if it matches the virtual app UID.
     * @param who    the original object being hooked
     * @param method the original method being intercepted
     * @param args   the method arguments
     * @return the result of the original method invocation
     * @throws Throwable if the underlying method invocation fails
     */
    @Override
    protected Object hook(Object who, Method method, Object[] args) throws Throwable {
        int uid = (int) args[index];
        if (uid == BActivityThread.getBUid()) {
            args[index] = BlackBoxCore.getHostUid();
        }
        return method.invoke(who, args);
    }
}
