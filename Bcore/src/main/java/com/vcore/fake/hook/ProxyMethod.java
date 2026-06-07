package com.vcore.fake.hook;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to bind a {@link MethodHook} class to a single target method name.
 *
 * <p>When placed on an inner class that extends {@link MethodHook}, the {@link ClassInvocationStub}
 * will automatically register the hook for the method specified by {@link #value()}.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @ProxyMethod("getPackageName")
 * public class GetPackageNameHook extends MethodHook {
 *     @Override
 *     protected Object hook(Object who, Method method, Object[] args) throws Throwable {
 *         return "com.virtual.package";
 *     }
 * }
 * }</pre>
 *
 * @see ClassInvocationStub#initAnnotation(Class)
 * @see ProxyMethods
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProxyMethod {

    /**
     * The name of the method to hook.
     *
     * @return the target method name
     */
    String value();
}
