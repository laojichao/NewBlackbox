package com.vcore.jnihook;

import androidx.annotation.Keep;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Utility class for converting Java {@link Method} objects into JNI-compatible
 * descriptor strings used by the native hook framework.
 * <p>
 * Provides methods to obtain the declaring class name (in JNI internal format),
 * method name, and full JNI method descriptor (e.g., {@code (ILjava/lang/String;)V}).
 * These descriptors are consumed by native code to locate and hook specific methods
 * at the JNI/ART level.
 * </p>
 * <p>
 * Annotated with {@link Keep} to prevent ProGuard/R8 from stripping or renaming
 * these methods, as they are called from native code.
 * </p>
 */
@Keep
public class MethodUtils {
    /**
     * Returns the JNI internal class name of the declaring class of the given method.
     * The name uses forward slashes as package separators (e.g., {@code com/example/Foo}).
     * Called from native code.
     *
     * @param method the Java method to inspect
     * @return the declaring class name in JNI internal format
     */
    // native call
    public static String getDeclaringClass(final Method method) {
        return method.getDeclaringClass().getName().replace(".", "/");
    }

    /**
     * Returns the simple name of the given method.
     * Called from native code.
     *
     * @param method the Java method to inspect
     * @return the method name
     */
    // native call
    public static String getMethodName(final Method method) {
        return method.getName();
    }

    /**
     * Returns the full JNI method descriptor for the given method.
     * The descriptor follows the JNI specification format, e.g., {@code (ILjava/lang/String;)V}.
     * Called from native code.
     *
     * @param method the Java method to inspect
     * @return the JNI method descriptor string
     */
    // native call
    public static String getDesc(final Method method) {
        final StringBuilder buf = new StringBuilder();
        buf.append("(");

        final Class<?>[] types = method.getParameterTypes();
        for (Class<?> type : types) {
            buf.append(getDesc(type));
        }

        buf.append(")");
        buf.append(getDesc(method.getReturnType()));
        return buf.toString();
    }

    /**
     * Converts a Java class type to its JNI type descriptor string.
     * Handles primitives, arrays, and reference types.
     *
     * @param returnType the class to convert
     * @return the JNI type descriptor (e.g., {@code I} for int, {@code [B} for byte[],
     *         {@code Lcom/example/Foo;} for a reference type)
     */
    private static String getDesc(final Class<?> returnType) {
        if (returnType.isPrimitive()) {
            return getPrimitiveLetter(returnType);
        }

        if (returnType.isArray()) {
            return "[" + getDesc(Objects.requireNonNull(returnType.getComponentType()));
        }
        return "L" + getType(returnType) + ";";
    }

    /**
     * Converts a parameter type to its JNI internal type string.
     * Handles arrays and non-primitive types differently from {@link #getDesc(Class)}.
     *
     * @param parameterType the class to convert
     * @return the JNI internal type string
     */
    private static String getType(final Class<?> parameterType) {
        if (parameterType.isArray()) {
            return "[" + getDesc(Objects.requireNonNull(parameterType.getComponentType()));
        }

        if (!parameterType.isPrimitive()) {
            final String clsName = parameterType.getName();
            return clsName.replaceAll("\\.", "/");
        }
        return getPrimitiveLetter(parameterType);
    }

    /**
     * Returns the single-character JNI type descriptor for a primitive Java type.
     *
     * @param type the primitive class to convert (e.g., {@code Integer.TYPE})
     * @return the JNI type letter (e.g., {@code I}, {@code V}, {@code Z}, etc.)
     * @throws IllegalStateException if the type is not a recognized primitive type
     */
    private static String getPrimitiveLetter(final Class<?> type) {
        if (Integer.TYPE.equals(type)) {
            return "I";
        }

        if (Void.TYPE.equals(type)) {
            return "V";
        }

        if (Boolean.TYPE.equals(type)) {
            return "Z";
        }

        if (Character.TYPE.equals(type)) {
            return "C";
        }

        if (Byte.TYPE.equals(type)) {
            return "B";
        }

        if (Short.TYPE.equals(type)) {
            return "S";
        }

        if (Float.TYPE.equals(type)) {
            return "F";
        }

        if (Long.TYPE.equals(type)) {
            return "J";
        }

        if (Double.TYPE.equals(type)) {
            return "D";
        }
        throw new IllegalStateException("Type: " + type.getCanonicalName() + " is not a primitive type");
    }
}
