package com.vcore.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * Utility class for safely closing {@link Closeable} resources without throwing exceptions.
 * Provides a varargs-based close method that silently ignores {@link IOException}s,
 * which is useful in {@code finally} blocks where exceptions should not mask the original error.
 */
public class CloseUtils {
    /**
     * Closes one or more {@link Closeable} resources, silently ignoring any {@link IOException}
     * thrown during closure. Each resource is closed individually so that a failure closing one
     * does not prevent the others from being closed.
     *
     * @param closeables the {@link Closeable} resources to close; may be {@code null} or contain
     *                   {@code null} entries (which are skipped)
     */
    public static void close(Closeable... closeables) {
        if (closeables == null) {
            return;
        }

        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException ignored) { }
            }
        }
    }
}
