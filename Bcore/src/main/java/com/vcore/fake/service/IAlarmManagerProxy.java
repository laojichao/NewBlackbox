package com.vcore.fake.service;

import android.content.Context;
import android.os.Build;
import android.os.WorkSource;

import java.lang.reflect.Method;

import black.android.app.IAlarmManager;
import black.android.os.ServiceManager;
import com.vcore.BlackBoxCore;
import com.vcore.fake.hook.BinderInvocationStub;
import com.vcore.fake.hook.MethodHook;
import com.vcore.fake.hook.ProxyMethod;
import com.vcore.fake.service.base.ValueMethodProxy;
import com.vcore.utils.ArrayUtils;

/**
 * Proxy for IAlarmManager system service that intercepts alarm scheduling operations (set, setTime, setTimeZone), replacing package names and handling WorkSource parameters for the virtual environment.
 */
public class IAlarmManagerProxy extends BinderInvocationStub {
    public IAlarmManagerProxy() {
        super(ServiceManager.getService.call(Context.ALARM_SERVICE));
    }


    /**
     * Returns the IAlarmManager binder interface from ServiceManager.
     * @return the IAlarmManager proxy instance
     */
    @Override
    protected Object getWho() {
        return IAlarmManager.Stub.asInterface.call(ServiceManager.getService.call(Context.ALARM_SERVICE));
    }


    /**
     * Replaces the system ALARM_SERVICE with the proxied version.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.ALARM_SERVICE);
    }

    @Override
    protected void onBindMethod() {
        super.onBindMethod();
        //addMethodHook(new ValueMethodProxy("set", 0));
        addMethodHook(new ValueMethodProxy("setTimeZone",null));
    }


    /**
     * Checks if the hook environment is compromised.
     * @return always returns false
     */
    @Override
    public boolean isBadEnv() {
        return false;
    }

    @ProxyMethod("set")
    public static class Set extends MethodHook {

        @Override
        protected String getMethodName() {
            return "set";
        }

        @Override
        protected Object beforeHook(Object who, Method method, Object[] args) throws Throwable {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && args[0] instanceof String) {
                args[0] = BlackBoxCore.getHostPkg();
            }
            int index = ArrayUtils.indexOfFirst(args, WorkSource.class);
            if (index >= 0) {
                args[index] = null;
            }
            return true;
        }

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                return method.invoke(who, method, args);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return 0;
        }
    }

    @ProxyMethod("setTime")
    public static class SetTime extends MethodHook {

        @Override
        protected String getMethodName() {
            return "setTime";
        }

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return false;
            }
            return null;
        }
    }
}
