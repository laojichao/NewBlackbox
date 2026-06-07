package com.vcore.core.env;

import android.content.pm.ApplicationInfo;

import black.android.ddm.DdmHandleAppName;
import black.android.os.Process;

/**
 * Manages the virtual runtime environment identity for a virtual application process.
 * <p>
 * This class stores and configures the process name and package name that the virtual runtime
 * reports to the system and to debugging tools. When a virtual app process starts, this class
 * is used to:
 * <ul>
 *   <li>Override the process name visible via {@code /proc/self/cmdline}</li>
 *   <li>Update the Dalvik VM's process name argument</li>
 *   <li>Set the app name reported to DDMS (Dalvik Debug Monitor Server)</li>
 * </ul>
 * The runtime can only be set up once per process; subsequent calls to {@link #setupRuntime} are ignored.
 */
public class VirtualRuntime {
    /** The initial package name of the virtual application, set once during setup. */
    private static String sInitialPackageName;
    /** The virtual process name, set once during setup. */
    private static String sProcessName;

    /**
     * Returns the virtual process name configured for this runtime.
     *
     * @return the process name, or {@code null} if the runtime has not been set up
     */
    public static String getProcessName() {
        return sProcessName;
    }

    /**
     * Returns the initial package name of the virtual application.
     *
     * @return the package name, or {@code null} if the runtime has not been set up
     */
    public static String getInitialPackageName() {
        return sInitialPackageName;
    }

    /**
     * Configures the virtual runtime with the given process name and application info.
     * <p>
     * This method sets the process name visible to the VM and DDMS, and stores the package
     * name for later retrieval. It is idempotent: if the runtime has already been set up,
     * subsequent calls are silently ignored.
     *
     * @param processName the virtual process name to report
     * @param appInfo     the virtual application's {@link ApplicationInfo}
     */
    public static void setupRuntime(String processName, ApplicationInfo appInfo) {
        if (sProcessName != null) {
            return;
        }

        sInitialPackageName = appInfo.packageName;
        sProcessName = processName;
        Process.setArgV0.call(processName);
        DdmHandleAppName.setAppName.call(processName, 0);
    }
}
