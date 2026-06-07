package com.vcore.utils;

import android.os.Build;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Utility class for extracting native shared libraries (.so files) from APK archives.
 * <p>
 * Scans the APK's {@code lib/} directory for the appropriate CPU architecture (matching the
 * device's {@link Build#CPU_ABI}) and copies the native libraries to a target directory.
 * If no architecture-specific libraries are found, falls back to {@code armeabi}. Existing
 * files with matching sizes are skipped to avoid redundant extraction.
 * </p>
 */
public class NativeUtils {
    /** Logging tag for this class. */
    public static final String TAG = "NativeUtils";

    /**
     * Extracts native shared libraries from the given APK file to the specified directory.
     * First attempts to match the device's primary CPU ABI, then falls back to {@code armeabi}.
     *
     * @param apk          the APK file to extract native libraries from
     * @param nativeLibDir the target directory to copy the extracted .so files into;
     *                     created if it does not exist
     * @throws Exception if an I/O error occurs during extraction
     */
    public static void copyNativeLib(File apk, File nativeLibDir) throws Exception {
        long startTime = System.currentTimeMillis();
        if (!nativeLibDir.exists()) {
            nativeLibDir.mkdirs();
        }

        try (ZipFile zipfile = new ZipFile(apk.getAbsolutePath())) {
            if (findAndCopyNativeLib(zipfile, Build.CPU_ABI, nativeLibDir)) {
                return;
            }

            findAndCopyNativeLib(zipfile, "armeabi", nativeLibDir);
        } finally {
            Log.d(TAG, "Done! +" + (System.currentTimeMillis() - startTime) + "ms");
        }
    }

    /**
     * Searches the ZIP entries for native libraries matching the given CPU architecture
     * and copies them to the target directory. Skips entries that already exist with
     * the same file size.
     *
     * @param zipfile      the ZIP file to search through
     * @param cpuArch      the CPU architecture string (e.g. "arm64-v8a", "armeabi-v7a")
     * @param nativeLibDir the target directory for extracted libraries
     * @return {@code true} if matching .so files were found and copied (or if no {@code lib/}
     *         entries exist at all, indicating a fast-skip), {@code false} if a {@code lib/}
     *         directory exists but no matching architecture was found
     * @throws Exception if an I/O error occurs during extraction
     */
    private static boolean findAndCopyNativeLib(ZipFile zipfile, String cpuArch, File nativeLibDir) throws Exception {
        Log.d(TAG, "Try to copy plugin's cup arch: " + cpuArch);
        boolean findLib = false;
        boolean findSo = false;
        byte[] buffer = null;

        String libPrefix = "lib/" + cpuArch + "/";
        ZipEntry entry;
        Enumeration<?> e = zipfile.entries();

        while (e.hasMoreElements()) {
            entry = (ZipEntry) e.nextElement();
            String entryName = entry.getName();
            if (!findLib && !entryName.startsWith("lib/")) {
                continue;
            }

            findLib = true;
            if (!entryName.endsWith(".so") || !entryName.startsWith(libPrefix)) {
                continue;
            }

            if (buffer == null) {
                findSo = true;
                Log.d(TAG, "Found plugin's cup arch dir: " + cpuArch);
                buffer = new byte[8192];
            }

            String libName = entryName.substring(entryName.lastIndexOf('/') + 1);
            Log.d(TAG, "verify so " + libName);

            File libFile = new File(nativeLibDir, libName);
            if (libFile.exists() && libFile.length() == entry.getSize()) {
                Log.d(TAG, libName + " skip copy");
                continue;
            }

            FileOutputStream fos = new FileOutputStream(libFile);
            Log.d(TAG, "copy so " + entry.getName() + " of " + cpuArch);
            copySo(buffer, zipfile.getInputStream(entry), fos);
        }

        if (!findLib) {
            Log.d(TAG, "Fast skip all!");
            return true;
        }
        return findSo;
    }

    /**
     * Copies data from an input stream to an output stream using buffered I/O with
     * the provided buffer. Both streams are closed after the copy completes.
     *
     * @param buffer the reusable byte buffer for read operations
     * @param input  the source input stream to read from
     * @param output the target output stream to write to
     * @throws IOException if an I/O error occurs during copying
     */
    private static void copySo(byte[] buffer, InputStream input, OutputStream output) throws IOException {
        BufferedInputStream bufferedInput = new BufferedInputStream(input);
        BufferedOutputStream bufferedOutput = new BufferedOutputStream(output);
        int count;

        while ((count = bufferedInput.read(buffer)) > 0) {
            bufferedOutput.write(buffer, 0, count);
        }

        bufferedOutput.flush();
        bufferedOutput.close();
        output.close();
        bufferedInput.close();
        input.close();
    }
}
