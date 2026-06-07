package com.vcore.fake.hook;

import android.text.TextUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.vcore.utils.MethodParameterUtils;
import com.vcore.utils.Slog;

/**
 * Abstract base class providing dynamic proxy-based method interception for arbitrary objects.
 *
 * <p>This class creates a JDK dynamic proxy that wraps the target object returned by {@link #getWho()}.
 * Method calls on the proxy are intercepted and routed through registered {@link MethodHook} instances.
 * Hooks can be registered manually via {@link #addMethodHook(MethodHook)} or automatically via
 * {@link ProxyMethod} / {@link ProxyMethods} annotations on inner classes.</p>
 *
 * <p>Subclasses must implement {@link #getWho()} to provide the target object and
 * {@link #inject(Object, Object)} to install the proxy into the system (e.g., replacing
 * a system service reference). The {@link ScanClass} annotation can be used to scan
 * additional classes for annotated hook methods.</p>
 *
 * @see BinderInvocationStub
 * @see MethodHook
 * @see ProxyMethod
 */
public abstract class ClassInvocationStub implements InvocationHandler, IInjectHook {
    public static final String TAG = ClassInvocationStub.class.getSimpleName();

    /** Map of method names to their corresponding {@link MethodHook} implementations. */
    private final Map<String, MethodHook> mMethodHookMap = new HashMap<>();

    /** The original target object being proxied. */
    private Object mBase;

    /** The dynamically created proxy instance. */
    private Object mProxyInvocation;

    /** When {@code true}, only creates the proxy without calling {@link #inject}. */
    private boolean onlyProxy;

    /**
     * Returns the target object whose methods will be intercepted by the proxy.
     *
     * @return the original object to be proxied
     */
    protected abstract Object getWho();

    /**
     * Injects the proxy into the system by replacing references to the base object.
     * Called during {@link #injectHook()} unless {@link #onlyProxy(boolean)} is set to {@code true}.
     *
     * @param baseInvocation  the original object
     * @param proxyInvocation the proxy object to replace it with
     */
    protected abstract void inject(Object baseInvocation, Object proxyInvocation);

    /**
     * Called after the proxy is injected and methods are bound.
     * Subclasses can override this to perform additional initialization.
     */
    protected void onBindMethod() { }

    /**
     * Returns the dynamically created proxy instance.
     *
     * @return the proxy object, or {@code null} if {@link #injectHook()} has not been called
     */
    public Object getProxyInvocation() {
        return mProxyInvocation;
    }

    /**
     * Sets whether this stub should only create a proxy without injecting it.
     * When set to {@code true}, the proxy is created but {@link #inject} is not called.
     *
     * @param onlyStatus {@code true} to skip injection
     */
    protected void onlyProxy(boolean onlyStatus) {
        onlyProxy = onlyStatus;
    }

    /**
     * Initializes the hook by creating a dynamic proxy for the target object,
     * injecting it into the system, and scanning for annotated {@link MethodHook} classes.
     *
     * <p>This method:
     * <ol>
     *   <li>Gets the target object via {@link #getWho()}</li>
     *   <li>Creates a JDK dynamic proxy implementing all interfaces of the target</li>
     *   <li>Calls {@link #inject} to install the proxy (unless {@code onlyProxy} is true)</li>
     *   <li>Scans declared inner classes for {@link ProxyMethod}/{@link ProxyMethods} annotations</li>
     *   <li>Scans classes specified by {@link ScanClass} annotation</li>
     * </ol></p>
     */
    @Override
    public void injectHook() {
        mBase = getWho();
        mProxyInvocation = Proxy.newProxyInstance(mBase.getClass().getClassLoader(), MethodParameterUtils.getAllInterface(mBase.getClass()), this);
        if (!onlyProxy) {
            inject(mBase, mProxyInvocation);
        }

        onBindMethod();
        Class<?>[] declaredClasses = this.getClass().getDeclaredClasses();
        for (Class<?> declaredClass : declaredClasses) {
            initAnnotation(declaredClass);
        }

        ScanClass scanClass = this.getClass().getAnnotation(ScanClass.class);
        if (scanClass != null) {
            for (Class<?> aClass : scanClass.value()) {
                for (Class<?> declaredClass : aClass.getDeclaredClasses()) {
                    initAnnotation(declaredClass);
                }
            }
        }
    }

    /**
     * Initializes method hooks from {@link ProxyMethod} and {@link ProxyMethods} annotations
     * found on the given class. The class must extend {@link MethodHook}.
     *
     * @param clazz the class to scan for proxy method annotations
     */
    protected void initAnnotation(Class<?> clazz) {
        // Get proxy method annotation.
        ProxyMethod proxyMethod = clazz.getAnnotation(ProxyMethod.class);
        if (proxyMethod != null) {
            final String name = proxyMethod.value();
            if (!TextUtils.isEmpty(name)) {
                try {
                    addMethodHook(name, (MethodHook) clazz.newInstance());
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }

        ProxyMethods proxyMethods = clazz.getAnnotation(ProxyMethods.class);
        if (proxyMethods != null) {
            String[] value = proxyMethods.value();
            for (String name : value) {
                try {
                    addMethodHook(name, (MethodHook) clazz.newInstance());
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

    /**
     * Registers a {@link MethodHook} using the hook's own method name.
     *
     * @param methodHook the hook to register
     * @see MethodHook#getMethodName()
     */
    protected void addMethodHook(MethodHook methodHook) {
        mMethodHookMap.put(methodHook.getMethodName(), methodHook);
    }

    /**
     * Registers a {@link MethodHook} for the specified method name.
     *
     * @param name       the name of the method to hook
     * @param methodHook the hook to register
     */
    protected void addMethodHook(String name, MethodHook methodHook) {
        mMethodHookMap.put(name, methodHook);
    }

    /**
     * InvocationHandler implementation that intercepts method calls on the proxy.
     * If a registered {@link MethodHook} exists and is enabled for the method, the hook's
     * {@link MethodHook#beforeHook}, {@link MethodHook#hook}, and {@link MethodHook#afterHook}
     * methods are called in sequence. Otherwise, the call is forwarded to the original object.
     *
     * @param proxy  the proxy instance the method was invoked on
     * @param method the method being invoked
     * @param args   the arguments to the method
     * @return the result of the method invocation
     * @throws Throwable if the method invocation fails
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodHook methodHook = mMethodHookMap.get(method.getName());
        if (methodHook == null || !methodHook.isEnable()) {
            try {
                //Slog.e(TAG, mBase.getClass().getName() + ", " + method.getName());
                return method.invoke(mBase, args);
            } catch (Throwable e) {
                throw Objects.requireNonNull(e.getCause());
            }
        }

        Object result = methodHook.beforeHook(mBase, method, args);
        if (result != null) {
            return result;
        }

        result = methodHook.hook(mBase, method, args);
        result = methodHook.afterHook(result);
        return result;
    }
}
