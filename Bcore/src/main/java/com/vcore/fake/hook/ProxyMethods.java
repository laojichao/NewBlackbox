package com.vcore.fake.hook;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to bind a {@link MethodHook} class to multiple target method names.
 *
 * <p>When placed on an inner class that extends {@link MethodHook}, the {@link ClassInvocationStub}
 * will register the hook for each method name specified in {@link #value()}. This is useful when
 * a single hook implementation handles multiple related methods.</p>
 *
 * @see ClassInvocationStub#initAnnotation(Class)
 * @see ProxyMethod
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProxyMethods {

    /**
     * The names of the methods to hook.
     *
     * @return an array of target method names
     */
    String[] value() default {};
}
