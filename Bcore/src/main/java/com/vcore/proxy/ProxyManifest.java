package com.vcore.proxy;

import java.util.Locale;

import com.vcore.BlackBoxCore;

/**
 * Utility class that provides naming conventions and lookups for proxy component
 * stubs registered in the host application manifest.
 * <p>
 * Generates fully-qualified class names and content provider authorities for
 * proxy activities, services, job services, content providers, and other components
 * by index. The host application pre-registers {@link #FREE_COUNT} stub subclasses
 * for each proxy type to support multiple concurrent virtual processes.
 * </p>
 */
public class ProxyManifest {
    /** The number of pre-registered proxy stub subclasses per component type. */
    public static final int FREE_COUNT = 50;

    /**
     * Determines whether the given authority string refers to a proxy content provider.
     *
     * @param msg the authority string to check
     * @return {@code true} if the string matches the bind provider or contains the
     *         proxy content provider prefix; {@code false} otherwise
     */
    public static boolean isProxy(String msg) {
        return getBindProvider().equals(msg) || msg.contains("proxy_content_provider_");
    }

    /**
     * Returns the fully-qualified authority string for the system call bind provider.
     *
     * @return the authority string in the format {@code <hostPkg>.blackbox.SystemCallProvider}
     */
    public static String getBindProvider() {
        return BlackBoxCore.getHostPkg() + ".blackbox.SystemCallProvider";
    }

    /**
     * Returns the authority string for a proxy content provider at the given index.
     *
     * @param index the proxy stub index (0-based)
     * @return the authority string in the format {@code <hostPkg>.proxy_content_provider_<index>}
     */
    public static String getProxyAuthorities(int index) {
        return String.format(Locale.CHINA, "%s.proxy_content_provider_%d", BlackBoxCore.getHostPkg(), index);
    }

    /**
     * Returns the fully-qualified class name for a proxy pending activity at the given index.
     *
     * @param index the proxy stub index (0-based)
     * @return the class name in the format {@code com.vcore.proxy.ProxyPendingActivity$P<index>}
     */
    public static String getProxyPendingActivity(int index) {
        return String.format(Locale.CHINA, "com.vcore.proxy.ProxyPendingActivity$P%d", index);
    }

    /**
     * Returns the fully-qualified class name for a proxy activity at the given index.
     *
     * @param index the proxy stub index (0-based)
     * @return the class name in the format {@code com.vcore.proxy.ProxyActivity$P<index>}
     */
    public static String getProxyActivity(int index) {
        return String.format(Locale.CHINA, "com.vcore.proxy.ProxyActivity$P%d", index);
    }

    /**
     * Returns the fully-qualified class name for a transparent proxy activity at the given index.
     *
     * @param index the proxy stub index (0-based)
     * @return the class name in the format {@code com.vcore.proxy.TransparentProxyActivity$P<index>}
     */
    public static String TransparentProxyActivity(int index) {
        return String.format(Locale.CHINA, "com.vcore.proxy.TransparentProxyActivity$P%d", index);
    }

    /**
     * Returns the fully-qualified class name for a proxy service at the given index.
     *
     * @param index the proxy stub index (0-based)
     * @return the class name in the format {@code com.vcore.proxy.ProxyService$P<index>}
     */
    public static String getProxyService(int index) {
        return String.format(Locale.CHINA, "com.vcore.proxy.ProxyService$P%d", index);
    }

    /**
     * Returns the fully-qualified class name for a proxy job service at the given index.
     *
     * @param index the proxy stub index (0-based)
     * @return the class name in the format {@code com.vcore.proxy.ProxyJobService$P<index>}
     */
    public static String getProxyJobService(int index) {
        return String.format(Locale.CHINA, "com.vcore.proxy.ProxyJobService$P%d", index);
    }

    /**
     * Returns the fully-qualified authority string for the proxy file provider.
     *
     * @return the authority string in the format {@code <hostPkg>.blackbox.FileProvider}
     */
    public static String getProxyFileProvider() {
        return BlackBoxCore.getHostPkg() + ".blackbox.FileProvider";
    }

    /**
     * Returns the process name for a virtual process identified by its blackbox PID.
     *
     * @param bPid the blackbox process ID
     * @return the process name in the format {@code <hostPkg>:p<bPid>}
     */
    public static String getProcessName(int bPid) {
        return BlackBoxCore.getHostPkg() + ":p" + bPid;
    }
}
