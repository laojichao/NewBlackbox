package com.vcore.utils;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.vcore.BlackBoxCore;

/**
 * Utility class for analyzing APK native library ABI (Application Binary Interface) support.
 * <p>
 * Inspects the {@code lib/} directory inside an APK file to determine which ARM architectures
 * (armeabi, armeabi-v7a, arm64-v8a) are packaged. Results are cached per APK file to avoid
 * repeated zip scanning.
 * </p>
 */
public class AbiUtils {
    /** Set of ABI names found in the APK (e.g. "arm64-v8a", "armeabi-v7a"). */
    private final Set<String> mLibs = new HashSet<>();
    /** Cache mapping APK files to their parsed {@link AbiUtils} instances. */
    private static final Map<File, AbiUtils> sAbiUtilsMap = new HashMap<>();

    /**
     * Checks whether the given APK file's native libraries are compatible with the current
     * host process architecture. An APK with no native libraries is always considered compatible.
     *
     * @param apkFile the APK file to check ABI compatibility for
     * @return {@code true} if the APK's native libraries match the host process bitness,
     *         or if the APK contains no native libraries at all
     */
    public static boolean isSupport(File apkFile) {
        AbiUtils abiUtils = sAbiUtilsMap.get(apkFile);
        if (abiUtils == null) {
            abiUtils = new AbiUtils(apkFile);
            sAbiUtilsMap.put(apkFile, abiUtils);
        }

        if (abiUtils.isEmptyAib()) {
            return true;
        }

        if (BlackBoxCore.is64Bit()) {
            return abiUtils.is64Bit();
        } else {
            return abiUtils.is32Bit();
        }
    }

    /**
     * Constructs an {@code AbiUtils} instance by scanning the given APK file's ZIP entries
     * for native library directories under {@code lib/}.
     *
     * @param apkFile the APK file to scan for native library ABIs
     */
    public AbiUtils(File apkFile) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(apkFile);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String name = zipEntry.getName();

                if (name.startsWith("lib/arm64-v8a")) {
                    mLibs.add("arm64-v8a");
                } else if (name.startsWith("lib/armeabi")) {
                    mLibs.add("armeabi");
                } else if (name.startsWith("lib/armeabi-v7a")) {
                    mLibs.add("armeabi-v7a");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseUtils.close(zipFile);
        }
    }

    /**
     * Checks whether the APK contains 64-bit (arm64-v8a) native libraries.
     *
     * @return {@code true} if the APK includes arm64-v8a libraries
     */
    public boolean is64Bit() {
        return mLibs.contains("arm64-v8a");
    }

    /**
     * Checks whether the APK contains 32-bit (armeabi or armeabi-v7a) native libraries.
     *
     * @return {@code true} if the APK includes armeabi or armeabi-v7a libraries
     */
    public boolean is32Bit() {
        return mLibs.contains("armeabi") || mLibs.contains("armeabi-v7a");
    }

    /**
     * Checks whether the APK contains no native libraries at all.
     *
     * @return {@code true} if no {@code lib/} entries were found in the APK
     */
    public boolean isEmptyAib() {
        return mLibs.isEmpty();
    }
}
