package com.vcore.fake.hook;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to specify additional classes whose inner classes should be scanned
 * for {@link ProxyMethod} and {@link ProxyMethods} annotations during hook initialization.
 *
 * <p>When placed on a class that extends {@link ClassInvocationStub}, the stub will additionally
 * scan the declared inner classes of each class specified in {@link #value()} for proxy method
 * annotations, allowing hook methods to be defined in external classes.</p>
 *
 * @see ClassInvocationStub#injectHook()
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ScanClass {

    /**
     * The classes whose inner classes will be scanned for proxy method annotations.
     *
     * @return an array of classes to scan
     */
    Class<?>[] value() default {};
}
