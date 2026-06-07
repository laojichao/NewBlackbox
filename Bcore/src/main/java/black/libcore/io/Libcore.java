package black.libcore.io;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code libcore.io.Libcore} class.
 * Provides access to the raw OS (Linux syscall) interface used by the
 * core Java libraries for low-level I/O operations.
 */
public class Libcore {
    public static final Reflector REF = Reflector.on("libcore.io.Libcore");

    /** The raw Os interface providing POSIX-like system calls. */
    public static Reflector.FieldWrapper<Object> os = REF.field("os");
}
