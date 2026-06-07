package black.android.os;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.os.Process#setArgV0} method.
 * Provides access to the method that changes the process name visible in tools like ps.
 */
public class Process {
    public static final Reflector REF = Reflector.on("android.os.Process");

    /**
     * Sets the process name (argv[0]) for the current process.
     *
     * @param name the new process name
     */
    public static Reflector.StaticMethodWrapper<Void> setArgV0 = REF.staticMethod("setArgV0", String.class);
}
