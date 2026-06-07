package com.vcore.fake.hook;

/**
 * Interface for components that inject system service hooks into the virtual environment.
 *
 * <p>Implementations are responsible for replacing or intercepting system service calls
 * to provide virtual environment functionality. Each implementation typically targets
 * a specific system service (e.g., ActivityManager, PackageManager).</p>
 *
 * @see HookManager
 * @see BinderInvocationStub
 */
public interface IInjectHook {

    /**
     * Performs the actual hook injection, replacing or wrapping the target system service
     * with a proxy implementation.
     */
    void injectHook();

    /**
     * Checks whether the current hook environment has been invalidated.
     *
     * <p>Returns {@code true} if another framework or system change has overwritten
     * the hook, requiring re-injection. The {@link HookManager} calls this to verify
     * hook integrity and re-inject if necessary.</p>
     *
     * @return {@code true} if the environment is bad and re-injection is needed
     */
    boolean isBadEnv();
}
