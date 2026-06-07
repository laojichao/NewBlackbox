package black.dalvik.system;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code dalvik.system.VMRuntime} class.
 * Provides access to the Dalvik/ART virtual machine runtime for controlling
 * VM-level behavior such as the target SDK version which affects runtime
 * compatibility checks and behaviors.
 */
public class VMRuntime {
    public static final Reflector REF = Reflector.on("dalvik.system.VMRuntime");

    /**
     * Returns the VMRuntime singleton for the current VM.
     *
     * @return the VMRuntime instance
     */
    public static Reflector.StaticMethodWrapper<Object> getRuntime = REF.staticMethod("getRuntime");

    /**
     * Sets the target SDK version for runtime compatibility behavior.
     *
     * @param sdkVersion the target SDK version to set
     */
    public static Reflector.MethodWrapper<Void> setTargetSdkVersion = REF.method("setTargetSdkVersion", int.class);
}
