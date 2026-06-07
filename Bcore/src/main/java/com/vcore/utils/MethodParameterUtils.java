package com.vcore.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

import com.vcore.BlackBoxCore;
import com.vcore.app.BActivityThread;

/**
 * Utility class for manipulating method parameter arrays at runtime. Commonly used in
 * the virtual framework to intercept system API calls and replace virtual application
 * package names and UIDs with host application equivalents before forwarding to the real
 * Android framework.
 */
public class MethodParameterUtils {
    /**
     * Extracts the first parameter from the argument array that matches the given class type
     * using exact class comparison (not instanceof).
     *
     * @param <T>    the expected return type
     * @param args   the argument array to search; may be {@code null}
     * @param tClass the exact class to match
     * @return the first matching parameter cast to type {@code T}, or {@code null} if
     *         no match is found or the args array is {@code null}
     */
    public static <T> T getFirstParam(Object[] args, Class<T> tClass) {
        if (args == null) {
            return null;
        }

        int index = ArrayUtils.indexOfFirst(args, tClass);
        if (index != -1) {
            return (T) args[index];
        }
        return null;
    }

    /**
     * Extracts the first parameter from the argument array that is an instance of the given
     * class type (using instanceof semantics, supporting subclass matching).
     *
     * @param <T>    the expected return type
     * @param args   the argument array to search; may be {@code null}
     * @param tClass the class to test against using {@code instanceof}
     * @return the first matching parameter cast to type {@code T}, or {@code null} if
     *         no match is found or the args array is {@code null}
     */
    public static <T> T getFirstParamByInstance(Object[] args, Class<T> tClass) {
        if (args == null) {
            return null;
        }

        int index = ArrayUtils.indexOfObject(args, tClass, 0);
        if (index != -1) {
            return (T) args[index];
        }
        return null;
    }

    /**
     * Replaces the first String argument that is an installed virtual app package name with
     * the host application's package name. This is used to mask the virtual app's identity
     * when forwarding calls to the Android system.
     *
     * @param args the argument array to modify in-place; may be {@code null}
     * @return the original virtual app package name that was replaced, or {@code null} if
     *         no replacement was made or the args array is {@code null}
     */
    public static String replaceFirstAppPkg(Object[] args) {
        if (args == null) {
            return null;
        }

        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof String) {
                String value = (String) args[i];
                if (BlackBoxCore.get().isInstalled(value, BActivityThread.getUserId())) {
                    args[i] = BlackBoxCore.getHostPkg();
                    return value;
                }
            }
        }
        return null;
    }

    /**
     * Replaces all String arguments that are installed virtual app package names with
     * the host application's package name. This ensures complete package name masking
     * across all string parameters in a method call.
     *
     * @param args the argument array to modify in-place; may be {@code null}
     */
    public static void replaceAllAppPkg(Object[] args) {
        if (args == null) {
            return;
        }

        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                continue;
            }

            if (args[i] instanceof String) {
                String value = (String) args[i];
                if (BlackBoxCore.get().isInstalled(value, BActivityThread.getUserId())) {
                    args[i] = BlackBoxCore.getHostPkg();
                }
            }
        }
    }

    /**
     * Replaces the first Integer argument that matches the virtual app's UID with the
     * host application's UID. Used to mask the virtual process UID when making system calls.
     *
     * @param args the argument array to modify in-place; may be {@code null}
     */
    public static void replaceFirstUid(Object[] args) {
        if (args == null) {
            return;
        }

        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Integer) {
                int uid = (int) args[i];
                if (uid == BActivityThread.getBUid()) {
                    args[i] = BlackBoxCore.getHostUid();
                }
            }
        }
    }

    /**
     * Replaces the last Integer argument that matches the virtual app's UID with the
     * host application's UID.
     *
     * @param args the argument array to modify in-place
     */
    public static void replaceLastUid(Object[] args) {
        int index = ArrayUtils.indexOfLast(args, Integer.class);
        if (index != -1) {
            int uid = (int) args[index];
            if (uid == BActivityThread.getBUid()) {
                args[index] = BlackBoxCore.getHostUid();
            }
        }
    }

    /**
     * Replaces the last String argument that is an installed virtual app package name with
     * the host application's package name.
     *
     * @param args the argument array to modify in-place
     */
    public static void replaceLastAppPkg(Object[] args) {
        int index = ArrayUtils.indexOfLast(args, String.class);
        if (index != -1) {
            String pkg = (String) args[index];
            if (BlackBoxCore.get().isInstalled(pkg, BActivityThread.getUserId())) {
                args[index] = BlackBoxCore.getHostPkg();
            }
        }
    }

    /**
     * Retrieves all interfaces implemented or extended by the given class and all of its
     * superclasses, recursively.
     *
     * @param clazz the class to inspect
     * @return an array of all unique interfaces in the class hierarchy
     */
    public static Class<?>[] getAllInterface(Class<?> clazz) {
        HashSet<Class<?>> classes = new HashSet<>();
        getAllInterfaces(clazz, classes);

        Class<?>[] result = new Class[classes.size()];
        classes.toArray(result);
        return result;
    }

    /**
     * Recursively collects all interfaces implemented or extended by the given class and
     * its superclasses into the provided set.
     *
     * @param clazz               the class to inspect
     * @param interfaceCollection the accumulator set for discovered interfaces
     */
    public static void getAllInterfaces(Class<?> clazz, HashSet<Class<?>> interfaceCollection) {
        Class<?>[] classes = clazz.getInterfaces();
        if (classes.length != 0) {
            interfaceCollection.addAll(Arrays.asList(classes));
        }

        if (clazz.getSuperclass() != Object.class) {
            getAllInterfaces(Objects.requireNonNull(clazz.getSuperclass()), interfaceCollection);
        }
    }
}
