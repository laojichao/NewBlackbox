package black.com.android.internal.infra;

import android.content.Context;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code com.android.internal.infra.AndroidFuture} class.
 * AndroidFuture extends CompletableFuture and is used internally by the Android framework
 * for asynchronous IPC result handling. Provides access to the constructor and
 * the complete method for resolving the future.
 */
public class AndroidFuture {
    public static final Reflector REF = Reflector.on("com.android.internal.infra.AndroidFuture");

    /**
     * Completes this future with the given value.
     *
     * @param value the value to complete with
     * @return true if this invocation caused the future to transition to a completed state
     */
    public static Reflector.MethodWrapper<Boolean> complete = REF.method("complete", Object.class);

    /**
     * Creates a new AndroidFuture instance.
     */
    public static Reflector.ConstructorWrapper<Object> ctor = REF.constructor();
}
