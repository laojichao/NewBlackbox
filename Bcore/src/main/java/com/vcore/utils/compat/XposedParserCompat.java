package com.vcore.utils.compat;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.vcore.BlackBoxCore;
import com.vcore.entity.pm.InstalledModule;
import com.vcore.utils.CloseUtils;

/**
 * Utility class for parsing and identifying Xposed framework modules within APK files.
 * Determines whether an APK is an Xposed module by checking for the presence of the
 * {@code assets/xposed_init} file, which contains the fully-qualified class name of the
 * module's entry point. Can also construct an {@link InstalledModule} metadata object
 * from an application's info and Xposed manifest data.
 */
public class XposedParserCompat {
    /**
     * Parses an application's metadata into an {@link InstalledModule} object. Extracts
     * the package name, display label, Xposed description from metadata, and the main
     * entry class from {@code assets/xposed_init}.
     *
     * @param applicationInfo the application info of the potential Xposed module
     * @return an {@link InstalledModule} populated with the module's metadata, or
     *         {@code null} if parsing fails (e.g., missing metadata or not a valid module)
     */
    public static InstalledModule parseModule(ApplicationInfo applicationInfo) {
        try {
            PackageManager packageManager = BlackBoxCore.getPackageManager();
            InstalledModule module = new InstalledModule();
            module.packageName = applicationInfo.packageName;
            module.enable = false;
            module.desc = applicationInfo.metaData.getString("xposeddescription");
            module.name = applicationInfo.loadLabel(packageManager).toString();
            module.main = readMain(applicationInfo.sourceDir);
            return module;
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * Checks whether the given APK file is an Xposed module by looking for the
     * {@code assets/xposed_init} entry inside the APK archive.
     *
     * @param file the path to the APK file to check
     * @return {@code true} if the APK contains a valid {@code assets/xposed_init} file
     */
    public static boolean isXPModule(String file) {
        try {
            String s = readMain(file);
            return s != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * Reads the Xposed module main class entry from the {@code assets/xposed_init} file
     * inside the given APK. Comment lines (starting with {@code #}) are skipped.
     *
     * @param apk the path to the APK file
     * @return the trimmed content of {@code assets/xposed_init}, or {@code null} if the
     *         entry does not exist or cannot be read
     */
    private static String readMain(String apk) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(new File(apk));
            ZipEntry entry = zipFile.getEntry("assets/xposed_init");
            if (entry == null) {
                throw new RuntimeException();
            }
            return getInputStreamContent(zipFile.getInputStream(entry)).trim();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseUtils.close(zipFile);
        }
        return null;
    }

    /**
     * Reads the full text content from an {@link InputStream}, skipping lines that
     * start with {@code #} (comments).
     *
     * @param stream the input stream to read from
     * @return the concatenated text content with comment lines excluded
     */
    private static String getInputStreamContent(InputStream stream) {
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(stream));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                builder.append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseUtils.close(reader);
        }
        return builder.toString();
    }
}
