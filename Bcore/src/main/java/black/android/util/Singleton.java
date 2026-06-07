package black.android.util;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.util.Singleton} class.
 * Singleton is a lazy initialization helper used internally by Android to hold
 * system service binder interfaces. Provides access to the cached instance
 * and the get() method that performs lazy initialization.
 */
public class Singleton {
    public static final Reflector REF = Reflector.on("android.util.Singleton");

    /** The cached singleton instance (may be null before first get() call). */
    public static Reflector.FieldWrapper<Object> mInstance = REF.field("mInstance");

    /**
     * Returns the singleton instance, creating it on first access.
     *
     * @return the singleton instance
     */
    public static Reflector.MethodWrapper<Object> get = REF.method("get");
}
