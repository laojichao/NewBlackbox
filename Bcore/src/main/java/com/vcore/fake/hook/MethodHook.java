package com.vcore.fake.hook;

import java.lang.reflect.Method;

import com.vcore.BlackBoxCore;

/**
 * Abstract base class for intercepting and modifying individual method calls within a proxied object.
 *
 * <p>Subclasses implement {@link #hook(Object, Method, Object[])} to provide the replacement
 * behavior for a specific method. The hook lifecycle is:</p>
 * <ol>
 *   <li>{@link #beforeHook} - called before the original method; return non-null to short-circuit</li>
 *   <li>{@link #hook} - called to provide the main replacement logic</li>
 *   <li>{@link #afterHook} - called after hook to post-process the result</li>
 * </ol>
 *
 * <p>By default, hooks are only active in virtual (black) processes, as determined by
 * {@link #isEnable()}. Subclasses can override this to change the activation condition.</p>
 *
 * @see ClassInvocationStub
 * @see ProxyMethod
 */
public abstract class MethodHook {

    /**
     * Returns the name of the method this hook targets.
     * Used by {@link ClassInvocationStub} for automatic registration via annotations.
     *
     * @return the target method name, or {@code null} if not specified
     */
    protected String getMethodName() {
        return null;
    }

    /**
     * Called after {@link #hook} to post-process the result before returning it to the caller.
     *
     * @param result the result produced by {@link #hook}
     * @return the (potentially modified) result to return to the caller
     */
    protected Object afterHook(Object result) {
        return result;
    }

    /**
     * Called before the original method would be invoked. If this method returns a non-null
     * value, the {@link #hook} method is skipped and the returned value is used as the result.
     *
     * @param who    the original object being proxied
     * @param method the method being intercepted
     * @param args   the arguments passed to the method
     * @return a non-null value to short-circuit the hook, or {@code null} to proceed with {@link #hook}
     * @throws Throwable if an error occurs during pre-processing
     */
    protected Object beforeHook(Object who, Method method, Object[] args) throws Throwable {
        return null;
    }

    /**
     * Provides the replacement implementation for the intercepted method.
     * Subclasses must implement this to define the hook behavior.
     *
     * @param who    the original object being proxied
     * @param method the method being intercepted
     * @param args   the arguments passed to the method
     * @return the result of the hooked method
     * @throws Throwable if an error occurs during hook execution
     */
    protected abstract Object hook(Object who, Method method, Object[] args) throws Throwable;

    /**
     * Determines whether this hook is currently active. By default, hooks are enabled
     * only when running in a virtual (black) process.
     *
     * @return {@code true} if the hook should be applied
     */
    protected boolean isEnable() {
        return BlackBoxCore.get().isBlackProcess();
    }
}
