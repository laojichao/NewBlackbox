package com.vcore.core;

import android.os.Binder;
import android.os.Process;

import androidx.annotation.Keep;

import java.io.File;

import com.vcore.BlackBoxCore;
import com.vcore.app.BActivityThread;

/**
 * JNI bridge to the native {@code libvcore} library that provides low-level I/O redirection,
 * UID spoofing, and Xposed hiding capabilities.
 * <p>
 * This class loads the native library at class initialization time and exposes:
 * <ul>
 *   <li>Native methods for adding and enabling I/O redirect rules</li>
 *   <li>A {@code @Keep}-annotated callback for translating caller UIDs in Binder transactions</li>
 *   <li>A {@code @Keep}-annotated callback for redirecting file paths from native code</li>
 *   <li>A method to hide Xposed framework artifacts at the native level</li>
 * </ul>
 * The {@code @Keep} methods are called from native code and must not be renamed or removed by ProGuard.
 */
public class NativeCore {
    /** Logging tag for this class. */
    public static final String TAG = "NativeCore";

    static {
        System.loadLibrary("vcore");
    }

    /**
     * Initializes the native I/O interception engine with the current Android API level.
     *
     * @param apiLevel the Android SDK version (e.g., {@link android.os.Build.VERSION_CODES#S})
     */
    public static native void init(int apiLevel);

    /**
     * Activates I/O path interception. After this call, all file operations in the process
     * will be subject to the previously registered redirect rules.
     */
    public static native void enableIO();

    /**
     * Adds a path to the native-level whitelist. File operations on paths matching this prefix
     * will not be redirected even if other rules would apply.
     *
     * @param path the path prefix to whitelist
     */
    public static native void addWhiteList(String path);

    /**
     * Registers a native-level I/O redirect rule mapping a target path to a relocation path.
     *
     * @param targetPath   the original file path prefix to intercept
     * @param relocatePath the replacement file path prefix
     */
    public static native void addIORule(String targetPath, String relocatePath);

    /**
     * Low-level native method for performing an I/O path redirect (private, used internally).
     *
     * @param origPath the original path
     * @param newPath  the new redirected path
     */
    private static native void nativeIORedirect(String origPath, String newPath);

    /**
     * Hides Xposed framework artifacts at the native level by modifying system property reads
     * and library lookups.
     */
    public static native void hideXposed();

    /**
     * Translates a Binder caller UID to the virtual UID of the corresponding virtual application.
     * <p>
     * Called from native code when intercepting Binder transactions. System UIDs and non-application
     * UIDs are returned unchanged. If the caller UID matches the host UID, the method queries
     * the virtual package manager to resolve the virtual UID from the caller's PID.
     *
     * @param origCallingUid the original caller UID from the Binder transaction
     * @return the translated virtual UID, or the original UID if no translation is needed
     */
    @Keep
    public static int getCallingUid(int origCallingUid) {
        // 系统uid
        if (origCallingUid > 0 && origCallingUid < Process.FIRST_APPLICATION_UID) {
            return origCallingUid;
        }
        // 非用户应用
        if (origCallingUid > Process.LAST_APPLICATION_UID) {
            return origCallingUid;
        }

        if (origCallingUid == BlackBoxCore.getHostUid()) {
            int callingPid = Binder.getCallingPid();
            int bUid = BlackBoxCore.getBPackageManager().getUidByPid(callingPid);
            if (bUid != -1) {
                return bUid;
            }
            return BActivityThread.getCallingBUid();
        }
        return origCallingUid;
    }

    /**
     * Redirects a file path string using the Java-level I/O rules.
     * <p>
     * Called from native code to delegate path redirection to {@link IOCore}.
     *
     * @param path the original file path to redirect
     * @return the redirected path
     */
    @Keep
    public static String redirectPath(String path) {
        return IOCore.get().redirectPath(path);
    }

    /**
     * Redirects a {@link File} path using the Java-level I/O rules.
     * <p>
     * Called from native code to delegate file path redirection to {@link IOCore}.
     *
     * @param path the original file to redirect
     * @return a new {@link File} with the redirected path
     */
    @Keep
    public static File redirectPath(File path) {
        return IOCore.get().redirectPath(path);
    }
}
