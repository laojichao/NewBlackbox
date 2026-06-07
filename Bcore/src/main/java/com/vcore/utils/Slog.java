package com.vcore.utils;

import android.util.Log;

/**
 * Simple logging wrapper around Android's {@link Log} class. Provides convenience methods
 * for all standard log levels (VERBOSE, DEBUG, INFO, WARN, ERROR) with optional
 * {@link Throwable} stack trace inclusion.
 * <p>
 * This class mirrors the hidden {@code android.util.Slog} API to allow consistent
 * logging across the virtual framework without direct dependency on the internal Android class.
 * </p>
 *
 * @hide
 */
public final class Slog {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Slog() { }

    /**
     * Logs a verbose message.
     *
     * @param tag the log tag identifier
     * @param msg the message to log
     */
    public static void v(String tag, String msg) {
        println(Log.VERBOSE, tag, msg);
    }

    /**
     * Logs a verbose message with an associated throwable stack trace.
     *
     * @param tag the log tag identifier
     * @param msg the message to log
     * @param tr  the throwable whose stack trace will be appended
     */
    public static void v(String tag, String msg, Throwable tr) {
        println(Log.VERBOSE, tag, msg + '\n' + Log.getStackTraceString(tr));
    }

    /**
     * Logs a debug message.
     *
     * @param tag the log tag identifier
     * @param msg the message to log
     */
    public static void d(String tag, String msg) {
        println(Log.DEBUG, tag, msg);
    }

    /**
     * Logs a debug message with an associated throwable stack trace.
     *
     * @param tag the log tag identifier
     * @param msg the message to log
     * @param tr  the throwable whose stack trace will be appended
     */
    public static void d(String tag, String msg, Throwable tr) {
        println(Log.DEBUG, tag, msg + '\n' + Log.getStackTraceString(tr));
    }

    /**
     * Logs an info message.
     *
     * @param tag the log tag identifier
     * @param msg the message to log
     */
    public static void i(String tag, String msg) {
        println(Log.INFO, tag, msg);
    }

    /**
     * Logs an info message with an associated throwable stack trace.
     *
     * @param tag the log tag identifier
     * @param msg the message to log
     * @param tr  the throwable whose stack trace will be appended
     */
    public static void i(String tag, String msg, Throwable tr) {
        println(Log.INFO, tag, msg + '\n' + Log.getStackTraceString(tr));
    }

    /**
     * Logs a warning message.
     *
     * @param tag the log tag identifier
     * @param msg the message to log
     */
    public static void w(String tag, String msg) {
        println(Log.WARN, tag, msg);
    }

    /**
     * Logs a warning message with an associated throwable stack trace.
     *
     * @param tag the log tag identifier
     * @param msg the message to log
     * @param tr  the throwable whose stack trace will be appended
     */
    public static void w(String tag, String msg, Throwable tr) {
        println(Log.WARN, tag, msg + '\n' + Log.getStackTraceString(tr));
    }

    /**
     * Logs a warning message from a throwable only (no additional message text).
     *
     * @param tag the log tag identifier
     * @param tr  the throwable whose stack trace will be logged
     */
    public static void w(String tag, Throwable tr) {
        println(Log.WARN, tag, Log.getStackTraceString(tr));
    }

    /**
     * Logs an error message.
     *
     * @param tag the log tag identifier
     * @param msg the message to log
     */
    public static void e(String tag, String msg) {
        println(Log.ERROR, tag, msg);
    }

    /**
     * Logs an error message with an associated throwable stack trace.
     *
     * @param tag the log tag identifier
     * @param msg the message to log
     * @param tr  the throwable whose stack trace will be appended
     */
    public static void e(String tag, String msg, Throwable tr) {
        println(Log.ERROR, tag, msg + '\n' + Log.getStackTraceString(tr));
    }

    /**
     * Writes a log message at the specified priority level to the Android log.
     *
     * @param priority the log priority level (e.g. {@link Log#DEBUG}, {@link Log#ERROR})
     * @param tag      the log tag identifier
     * @param msg      the message to log
     */
    public static void println(int priority, String tag, String msg) {
        Log.println(priority, tag, msg);
    }
}
