package black.android.ddm;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.ddm.DdmHandleAppName} class.
 * Provides access to the static setAppName method used to set the application name
 * in the Dalvik Debug Monitor (DDM) protocol for debugging purposes.
 */
public class DdmHandleAppName {
    public static final Reflector REF = Reflector.on("android.ddm.DdmHandleAppName");

    /**
     * Sets the application name reported to DDM (Dalvik Debug Monitor).
     *
     * @param appName the application name to report
     * @param pid     the process ID
     */
    public static Reflector.StaticMethodWrapper<Void> setAppName = REF.staticMethod("setAppName", String.class, int.class);
}
