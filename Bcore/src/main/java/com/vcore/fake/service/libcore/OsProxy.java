package com.vcore.fake.service.libcore;

import android.os.Process;

import java.lang.reflect.Method;
import java.util.Objects;

import black.Reflector;
import black.libcore.io.Libcore;
import com.vcore.BlackBoxCore;
import com.vcore.app.BActivityThread;
import com.vcore.fake.hook.ClassInvocationStub;
import com.vcore.fake.hook.MethodHook;
import com.vcore.fake.hook.ProxyMethod;
import com.vcore.fake.hook.ProxyMethods;

/**
 * Proxy for libcore.io.Libcore.os that intercepts native OS calls (getuid, stat, lstat) to return spoofed UIDs matching the virtual app identity instead of the real process UID. This prevents UID-based fingerprinting of the virtual environment.
 */
public class OsProxy extends ClassInvocationStub {
    public static final String TAG = "OsProxy";
    private final Object mBase;

    public OsProxy() {
        this.mBase = Libcore.os.get();
    }


    /**
     * Returns the current libcore.os instance to be proxied.
     * @return the libcore.os object
     */
    @Override
    protected Object getWho() {
        return mBase;
    }


    /**
     * Replaces the libcore.os static field with the proxy instance.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        Libcore.os.set(proxyInvocation);
    }


    /**
     * Checks if the hook environment is compromised by verifying libcore.os still points to the proxy.
     * @return true if the proxy has been replaced
     */
    @Override
    public boolean isBadEnv() {
        return Libcore.os.get() != getProxyInvocation();
    }

    @ProxyMethod("getuid")
    public static class GetUID extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            int callUid = (int) method.invoke(who, args);
            return getFakeUid(callUid);
        }
    }

    @ProxyMethods({"lstat", "stat"})
    public static class Stat extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            Object invoke;
            try {
                invoke = method.invoke(who, args);
            } catch (Throwable e) {
                throw Objects.requireNonNull(e.getCause());
            }

            Reflector.on("android.system.StructStat")
                    .field("st_uid").set(invoke, getFakeUid(-1));
            return invoke;
        }
    }

    private static int getFakeUid(int callUid) {
        if (callUid > 0 && callUid <= Process.FIRST_APPLICATION_UID) {
            return callUid;
        }

        if (BActivityThread.isThreadInit() && BActivityThread.currentActivityThread().isInit()) {
            return BActivityThread.getBAppId();
        }
        return BlackBoxCore.getHostUid();
    }
}
