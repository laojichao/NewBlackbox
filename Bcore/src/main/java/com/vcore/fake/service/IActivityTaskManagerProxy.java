package com.vcore.fake.service;

import android.app.ActivityManager;

import java.lang.reflect.Method;

import black.android.app.ActivityTaskManager;
import black.android.app.IActivityTaskManager;
import black.android.os.ServiceManager;
import black.android.util.Singleton;
import com.vcore.fake.hook.BinderInvocationStub;
import com.vcore.fake.hook.MethodHook;
import com.vcore.fake.hook.ProxyMethod;
import com.vcore.fake.hook.ScanClass;
import com.vcore.utils.compat.TaskDescriptionCompat;

@ScanClass(ActivityManagerCommonProxy.class)
/**
 * Proxy for IActivityTaskManager system service (Android 10+) that intercepts activity task management operations such as setTaskDescription, redirecting them through the virtual environment.
 */
public class IActivityTaskManagerProxy extends BinderInvocationStub {
    public static final String TAG = "ActivityTaskManager";

    public IActivityTaskManagerProxy() {
        super(ServiceManager.getService.call("activity_task"));
    }


    /**
     * Returns the IActivityTaskManager binder interface from ServiceManager.
     * @return the IActivityTaskManager proxy instance
     */
    @Override
    protected Object getWho() {
        return IActivityTaskManager.Stub.asInterface.call(ServiceManager.getService.call("activity_task"));
    }


    /**
     * Replaces the activity_task system service and the ActivityTaskManager singleton with the proxy.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("activity_task");

        Object o = ActivityTaskManager.IActivityTaskManagerSingleton.get();
        Singleton.mInstance.set(o, IActivityTaskManager.Stub.asInterface.call(this));

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
        addMethodHook(new SetTaskDescription());
    }

    // for >= Android 10 && < Android 12
    @ProxyMethod("setTaskDescription")
    public static class SetTaskDescription extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            ActivityManager.TaskDescription td = (ActivityManager.TaskDescription) args[1];
            args[1] = TaskDescriptionCompat.fix(td);
            return method.invoke(who, args);
        }
    }
}
