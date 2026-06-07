package com.vcore.core;

import androidx.annotation.NonNull;

import com.vcore.BlackBoxCore;

/**
 * Custom uncaught exception handler that wraps the system default handler and optionally delegates
 * to a host-provided exception handler registered via {@link BlackBoxCore#setExceptionHandler}.
 * <p>
 * This handler is installed during virtual app process initialization to ensure that uncaught
 * exceptions are first passed to the host application's custom handler (if set) before falling
 * back to the system default handler.
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    /** The previously registered default uncaught exception handler (system or app-level). */
    private final Thread.UncaughtExceptionHandler mDefaultHandler;

    /**
     * Factory method that creates and installs a new {@link CrashHandler} as the default
     * uncaught exception handler for the current thread.
     */
    public static void create() {
        new CrashHandler();
    }

    /**
     * Constructs a new {@link CrashHandler}, saves a reference to the current default handler,
     * and installs itself as the new default uncaught exception handler.
     */
    public CrashHandler() {
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * Invoked when an uncaught exception occurs in any thread.
     * <p>
     * If a custom exception handler has been registered on {@link BlackBoxCore}, it is called first.
     * Then the original default handler is invoked to perform standard crash handling (logging, process exit).
     *
     * @param t the thread that threw the exception
     * @param e the uncaught exception
     */
    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        if (BlackBoxCore.get().getExceptionHandler() != null) {
            BlackBoxCore.get().getExceptionHandler().uncaughtException(t, e);
        }
        mDefaultHandler.uncaughtException(t, e);
    }
}
