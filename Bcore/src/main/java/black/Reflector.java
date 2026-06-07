package black;

import android.os.Build;
import android.util.Log;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Core reflection utility class that provides a fluent API for accessing hidden Android framework
 * APIs via reflection. Supports bypassing Android P+ hidden API restrictions using
 * {@link HiddenApiBypass}. Wraps {@link Method}, {@link Field}, and {@link Constructor} access
 * into convenient wrapper classes.
 */
@SuppressWarnings({"WeakerAccess", "unchecked"})
public class Reflector {
    private static final String TAG = "Reflector";
    private final Class<?> mClazz;

    private Reflector(Class<?> clazz) {
        mClazz = clazz;
    }

    /**
     * Returns the underlying {@link Class} object wrapped by this Reflector.
     *
     * @return the reflected class
     */
    public Class<?> getClazz() {
        return mClazz;
    }

    /**
     * Creates a new {@link Reflector} instance targeting the class with the given fully-qualified name.
     *
     * @param name the fully-qualified class name to reflect on
     * @return a new Reflector instance, or null if the class cannot be found
     */
    public static Reflector on(String name) {
        return new Reflector(findClass(name));
    }

    /**
     * Wraps an existing {@link Method} into a {@link MethodWrapper} for convenient invocation.
     *
     * @param <T>    the expected return type
     * @param method the method to wrap
     * @return a MethodWrapper wrapping the given method
     */
    public static <T> MethodWrapper<T> wrap(Method method) {
        return new MethodWrapper<>(method);
    }

    /**
     * Wraps an existing {@link Method} into a {@link StaticMethodWrapper} for convenient static invocation.
     *
     * @param <T>    the expected return type
     * @param method the static method to wrap
     * @return a StaticMethodWrapper wrapping the given method
     */
    public static <T> StaticMethodWrapper<T> wrapStatic(Method method) {
        return new StaticMethodWrapper<>(method);
    }

    /**
     * Finds and wraps an instance method of the wrapped class by name and parameter types.
     *
     * @param <T>            the expected return type
     * @param name           the method name
     * @param parameterTypes the parameter types of the method
     * @return a MethodWrapper for the found method
     */
    public <T> MethodWrapper<T> method(String name, Class<?>... parameterTypes) {
        return method(mClazz, name, parameterTypes);
    }

    /**
     * Finds and wraps a method of the given class by name and parameter types.
     * Falls back to name-only search if parameter types are empty.
     *
     * @param <T>            the expected return type
     * @param clazz          the class to search
     * @param name           the method name
     * @param parameterTypes the parameter types of the method
     * @return a MethodWrapper for the found method
     */
    public static <T> MethodWrapper<T> method(Class<?> clazz, String name, Class<?>... parameterTypes) {
        Method method = getMethod(clazz, name, parameterTypes);
        if ((parameterTypes == null || parameterTypes.length == 0) && method == null) {
            method = findMethodNoChecks(clazz, name);
        }
        return wrap(method);
    }

    /**
     * Finds and wraps a static method of the wrapped class by name and parameter types.
     *
     * @param <T>            the expected return type
     * @param name           the method name
     * @param parameterTypes the parameter types of the method
     * @return a StaticMethodWrapper for the found method
     */
    public <T> StaticMethodWrapper<T> staticMethod(String name, Class<?>... parameterTypes) {
        return staticMethod(mClazz, name, parameterTypes);
    }

    /**
     * Finds and wraps a static method of the given class by name and parameter types.
     *
     * @param <T>            the expected return type
     * @param clazz          the class to search
     * @param name           the method name
     * @param parameterTypes the parameter types of the method
     * @return a StaticMethodWrapper for the found method
     */
    public static <T> StaticMethodWrapper<T> staticMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        Method method = getMethod(clazz, name, parameterTypes);
        if ((parameterTypes == null || parameterTypes.length == 0) && method == null) {
            method = findMethodNoChecks(clazz, name);
        }
        return wrapStatic(method);
    }

    /**
     * Wraps an existing {@link Field} into a {@link FieldWrapper} for convenient access.
     *
     * @param <T>   the expected field type
     * @param field the field to wrap
     * @return a FieldWrapper wrapping the given field
     */
    public static <T> FieldWrapper<T> wrap(Field field) {
        return new FieldWrapper<>(field);
    }

    /**
     * Finds and wraps a field of the wrapped class by name.
     *
     * @param <T>  the expected field type
     * @param name the field name
     * @return a FieldWrapper for the found field
     */
    public <T> FieldWrapper<T> field(String name) {
        return field(mClazz, name);
    }

    /**
     * Finds and wraps a field of the given class by name.
     *
     * @param <T>   the expected field type
     * @param clazz the class to search
     * @param name  the field name
     * @return a FieldWrapper for the found field
     */
    public static <T> FieldWrapper<T> field(Class<?> clazz, String name) {
        return wrap(getField(clazz, name));
    }

    /**
     * Wraps an existing {@link Constructor} into a {@link ConstructorWrapper} for convenient instantiation.
     *
     * @param <T>         the type being constructed
     * @param constructor the constructor to wrap
     * @return a ConstructorWrapper wrapping the given constructor
     */
    public static <T> ConstructorWrapper<T> wrap(Constructor<T> constructor) {
        return new ConstructorWrapper<>(constructor);
    }

    /**
     * Finds and wraps a constructor of the wrapped class by parameter types.
     *
     * @param <T>            the expected type being constructed
     * @param parameterTypes the parameter types of the constructor
     * @return a ConstructorWrapper for the found constructor
     */
    public <T> ConstructorWrapper<T> constructor(Class<?>... parameterTypes) {
        return wrap(getConstructor(mClazz, parameterTypes));
    }

    /**
     * Loads a class by its fully-qualified name.
     *
     * @param name the fully-qualified class name
     * @return the loaded Class object, or null if not found
     */
    public static Class<?> findClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    /**
     * Finds a method in the given class by name and parameter types.
     *
     * @param clazz          the class to search
     * @param name           the method name
     * @param parameterTypes the parameter types
     * @return the found Method, or null
     */
    public static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        return findMethod(clazz, name, parameterTypes);
    }

    /**
     * Finds a method in the given class, validating parameter types first.
     *
     * @param clazz          the class to search
     * @param name           the method name
     * @param parameterTypes the parameter types
     * @return the found Method, or null
     * @throws NullPointerException if any element in parameterTypes is null
     */
    public static Method findMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        checkForFindMethod(parameterTypes);
        return findMethodNoChecks(clazz, name, parameterTypes);
    }

    /**
     * Finds a method in the given class without null-checking parameter types.
     * Walks up the class hierarchy. Uses {@link HiddenApiBypass} on Android P+ if direct access fails.
     *
     * @param clazz          the class to search
     * @param name           the method name
     * @param parameterTypes the parameter types
     * @return the found Method with accessible set to true, or null
     */
    public static Method findMethodNoChecks(Class<?> clazz, String name, Class<?>... parameterTypes) {
        while (clazz != null) {
            try {
                Method method = clazz.getDeclaredMethod(name, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException e) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    try {
                        Method method = HiddenApiBypass.getDeclaredMethod(clazz, name, parameterTypes);
                        method.setAccessible(true);
                        return method;
                    } catch (Exception ignored) { }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    /**
     * Finds a method in the given class by name only (ignoring overloads).
     * Uses {@link HiddenApiBypass} on Android P+ if direct access fails.
     *
     * @param clazz the class to search
     * @param name  the method name
     * @return the first matching Method with accessible set to true, or null
     */
    public static Method findMethodNoChecks(Class<?> clazz, String name) {
        try {
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(name)) {
                    method.setAccessible(true);
                    return method;
                }
            }
        } catch (Throwable e) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                List<Method> methods = HiddenApiBypass.getDeclaredMethods(clazz);
                for (Method method : methods) {
                    if (method.getName().equals(name)) {
                        method.setAccessible(true);
                        return method;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Finds a field in the given class by name.
     *
     * @param clazz the class to search
     * @param name  the field name
     * @return the found Field, or null
     */
    public static Field getField(Class<?> clazz, String name) {
        return findField(clazz, name);
    }

    /**
     * Finds a field in the given class by name.
     *
     * @param clazz the class to search
     * @param name  the field name
     * @return the found Field, or null
     */
    public static Field findField(Class<?> clazz, String name) {
        return findFieldNoChecks(clazz, name);
    }

    /**
     * Finds a field in the given class without null-checking.
     * Walks up the class hierarchy. Falls back to {@link HiddenApiBypass} instance/static field
     * lookups on Android P+ if direct access fails.
     *
     * @param clazz the class to search
     * @param name  the field name
     * @return the found Field with accessible set to true, or null
     */
    public static Field findFieldNoChecks(Class<?> clazz, String name) {
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {
                try {
                    return findInstanceField(clazz, name);
                } catch (NoSuchFieldException ex) {
                    try {
                        return findStaticField(clazz, name);
                    } catch (NoSuchFieldException ignored) { }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    /**
     * Finds a constructor in the given class by parameter types.
     *
     * @param <T>            the type being constructed
     * @param clazz          the class to search
     * @param parameterTypes the parameter types
     * @return the found Constructor, or null
     */
    public static <T> Constructor<T> getConstructor(Class<?> clazz, Class<?>... parameterTypes) {
        return findConstructor(clazz, parameterTypes);
    }

    /**
     * Finds a constructor in the given class, validating parameter types first.
     *
     * @param <T>            the type being constructed
     * @param clazz          the class to search
     * @param parameterTypes the parameter types
     * @return the found Constructor, or null
     * @throws NullPointerException if any element in parameterTypes is null
     */
    public static <T> Constructor<T> findConstructor(Class<?> clazz, Class<?>... parameterTypes) {
        checkForFindConstructor(parameterTypes);
        return findConstructorNoChecks(clazz, parameterTypes);
    }

    /**
     * Finds a constructor in the given class without null-checking parameter types.
     * Uses {@link HiddenApiBypass} on Android P+ if direct access fails.
     *
     * @param <T>            the type being constructed
     * @param clazz          the class to search
     * @param parameterTypes the parameter types
     * @return the found Constructor with accessible set to true, or null
     */
    public static <T> Constructor<T> findConstructorNoChecks(Class<?> clazz, Class<?>... parameterTypes) {
        try {
            Constructor<T> constructor = (Constructor<T>) clazz.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException e) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    Constructor<T> constructor = (Constructor<T>) HiddenApiBypass.getDeclaredConstructor(clazz, parameterTypes);
                    constructor.setAccessible(true);
                    return constructor;
                } catch (Exception ignored) { }
            }
        }
        return null;
    }

    /**
     * Finds an instance field using {@link HiddenApiBypass} (Android P+ only).
     *
     * @param clazz the class to search
     * @param name  the field name
     * @return the found Field
     * @throws NoSuchFieldException if the field is not found
     */
    private static Field findInstanceField(Class<?> clazz, String name) throws NoSuchFieldException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            List<Field> fields = HiddenApiBypass.getInstanceFields(clazz);
            for (Field field : fields) {
                if (field.getName().equals(name)) {
                    field.setAccessible(true);
                    return field;
                }
            }
        }
        throw new NoSuchFieldException();
    }

    /**
     * Finds a static field using {@link HiddenApiBypass} (Android P+ only).
     *
     * @param clazz the class to search
     * @param name  the field name
     * @return the found Field
     * @throws NoSuchFieldException if the field is not found
     */
    private static Field findStaticField(Class<?> clazz, String name) throws NoSuchFieldException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            List<Field> fields = HiddenApiBypass.getStaticFields(clazz);
            for (Field field : fields) {
                if (field.getName().equals(name)) {
                    field.setAccessible(true);
                    return field;
                }
            }
        }
        throw new NoSuchFieldException();
    }

    /**
     * Validates that no element in parameterTypes is null.
     *
     * @param parameterTypes the parameter types to validate
     * @throws NullPointerException if any element is null
     */
    private static void checkForFindMethod(Class<?>... parameterTypes) {
        if (parameterTypes != null) {
            for (int i = 0; i < parameterTypes.length; i++) {
                if (parameterTypes[i] == null) {
                    throw new NullPointerException("parameterTypes[" + i + "] == null");
                }
            }
        }
    }

    /**
     * Validates that no element in parameterTypes is null.
     *
     * @param parameterTypes the parameter types to validate
     * @throws NullPointerException if any element is null
     */
    private static void checkForFindConstructor(Class<?>... parameterTypes) {
        if (parameterTypes != null) {
            for (int i = 0; i < parameterTypes.length; i++) {
                if (parameterTypes[i] == null) {
                    throw new NullPointerException("parameterTypes[" + i + "] == null");
                }
            }
        }
    }

    /**
     * Base wrapper for reflected members ({@link Method}, {@link Field}, {@link Constructor}).
     * Automatically sets the member accessible on construction.
     *
     * @param <M> the type of accessible member
     */
    public static class MemberWrapper<M extends AccessibleObject & Member> {
        M member;

        MemberWrapper(M member) {
            if (member == null) {
                return;
            }

            member.setAccessible(true);
            this.member = member;
        }
    }

    /**
     * Wrapper for invoking an instance method via reflection.
     *
     * @param <T> the expected return type of the method
     */
    public static class MethodWrapper<T> extends MemberWrapper<Method> {
        MethodWrapper(Method method) {
            super(method);
        }

        /**
         * Invokes the wrapped instance method on the given object.
         *
         * @param instance the object to invoke the method on
         * @param args     the arguments to pass to the method
         * @return the method return value, or null if invocation fails
         */
        public T call(Object instance, Object... args) {
            try {
                return (T) member.invoke(instance, args);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * Wrapper for invoking a static method via reflection.
     *
     * @param <T> the expected return type of the method
     */
    public static class StaticMethodWrapper<T> extends MemberWrapper<Method> {
        StaticMethodWrapper(Method method) {
            super(method);
        }

        /**
         * Invokes the wrapped static method.
         *
         * @param args the arguments to pass to the method
         * @return the method return value, or null if invocation fails
         */
        public T call(Object... args) {
            try {
                return (T) member.invoke(null, args);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Invokes the wrapped static method with an explicit return type cast.
         *
         * @param <R>  the desired return type
         * @param args the arguments to pass to the method
         * @return the method return value cast to R, or null if invocation fails
         */
        public <R> R callWithClass(Object... args) {
            try {
                return (R) member.invoke(null, args);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * Wrapper for reading and writing a field via reflection.
     *
     * @param <T> the expected field type
     */
    public static class FieldWrapper<T> extends MemberWrapper<Field> {
        FieldWrapper(Field field) {
            super(field);
        }

        /**
         * Gets the field value from the given object instance.
         *
         * @param instance the object to read the field from (null for static fields)
         * @return the field value, or null if access fails
         */
        public T get(Object instance) {
            try {
                return (T) member.get(instance);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Gets the field value (for static fields or when instance is not needed).
         *
         * @return the field value, or null if access fails
         */
        public T get() {
            return get(null);
        }

        /**
         * Sets the field value on the given object instance.
         *
         * @param instance the object to set the field on (null for static fields)
         * @param value    the value to set
         */
        public void set(Object instance, Object value) {
            try {
                member.set(instance, value);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        /**
         * Sets the field value (for static fields or when instance is not needed).
         *
         * @param value the value to set
         */
        public void set(Object value) {
            set(null, value);
        }
    }

    /**
     * Wrapper for creating new instances via a reflected constructor.
     *
     * @param <T> the type being constructed
     */
    public static class ConstructorWrapper<T> extends MemberWrapper<Constructor<T>> {
        ConstructorWrapper(Constructor<T> constructor) {
            super(constructor);
        }

        /**
         * Creates a new instance using the wrapped constructor.
         *
         * @param args the constructor arguments
         * @return the new instance, or null if instantiation fails
         */
        public T newInstance(Object... args) {
            try {
                return member.newInstance(args);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
