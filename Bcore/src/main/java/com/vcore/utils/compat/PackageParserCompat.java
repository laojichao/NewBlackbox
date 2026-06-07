package com.vcore.utils.compat;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;

import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Package;
import android.os.Build;
import android.util.DisplayMetrics;

import java.io.File;

import black.android.content.pm.PackageParserLollipop;
import black.android.content.pm.PackageParserLollipop22;
import black.android.content.pm.PackageParserMarshmallow;
import black.android.content.pm.PackageParserNougat;
import black.android.content.pm.PackageParserPie;
import com.vcore.BlackBoxCore;

/**
 * Compatibility wrapper for Android's internal {@link PackageParser} API.
 * Provides version-aware factory and invocation methods for parsing APK files,
 * since the {@code PackageParser} constructor and method signatures changed across
 * multiple Android versions (Lollipop through Pie/Q).
 */
public class PackageParserCompat {
    /** Cached API level for fast comparisons. */
    private static final int API_LEVEL = Build.VERSION.SDK_INT;

    /**
     * Creates a {@link PackageParser} instance appropriate for the current Android version.
     * On Q+ (API 29), also installs a callback so the parser can access the host's
     * {@code PackageManager} for shared library resolution.
     *
     * @return a new {@link PackageParser} instance, or {@code null} if the current API level
     *         is below Lollipop (API 21)
     */
    public static PackageParser createParser() {
        if (BuildCompat.isQ()) {
            PackageParser packageParser = PackageParserPie._new.newInstance();
            packageParser.setCallback(new PackageParser.CallbackImpl(BlackBoxCore.getPackageManager()));
            return packageParser;
        } else if (API_LEVEL >= 28) {
            return PackageParserPie._new.newInstance();
        } else if (API_LEVEL >= M) {
            return PackageParserMarshmallow._new.newInstance();
        } else if (API_LEVEL >= LOLLIPOP_MR1) {
            return PackageParserLollipop22._new.newInstance();
        } else if (API_LEVEL >= LOLLIPOP) {
            return PackageParserLollipop._new.newInstance();
        }
        return null;
    }

    /**
     * Parses an APK file into a {@link Package} object using the version-appropriate
     * {@code PackageParser.parsePackage} method.
     *
     * @param parser     the {@link PackageParser} instance (created by {@link #createParser()})
     * @param packageFile the APK file to parse
     * @param flags      parse flags controlling which sections to include
     * @return the parsed {@link Package} object
     */
    public static Package parsePackage(PackageParser parser, File packageFile, int flags) {
        if (BuildCompat.isPie()) {
            return PackageParserPie.parsePackage.call(parser, packageFile, flags);
        } else if (API_LEVEL >= M) {
            return PackageParserMarshmallow.parsePackage.call(parser, packageFile, flags);
        } else if (API_LEVEL >= LOLLIPOP_MR1) {
            return PackageParserLollipop22.parsePackage.call(parser, packageFile, flags);
        } else if (API_LEVEL >= LOLLIPOP) {
            return PackageParserLollipop.parsePackage.call(parser, packageFile, flags);
        }
        return black.android.content.pm.PackageParser.parsePackage.call(parser, packageFile, null, new DisplayMetrics(), flags);
    }

    /**
     * Collects and verifies the signing certificates for a parsed package using the
     * version-appropriate API. On Pie (API 28+), certificates are collected as a static
     * method; on earlier versions, it is an instance method on the parser.
     *
     * @param parser the {@link PackageParser} instance
     * @param p      the parsed {@link Package} whose certificates to collect
     * @param flags  certificate collection flags
     */
    public static void collectCertificates(PackageParser parser, Package p, int flags) {
        if (BuildCompat.isPie()) {
            PackageParserPie.collectCertificates.call(p, true);
        } else if (API_LEVEL >= N) {
            PackageParserNougat.collectCertificates.call(p, flags);
        } else if (API_LEVEL >= M) {
            PackageParserMarshmallow.collectCertificates.call(parser, p, flags);
        } else if (API_LEVEL >= LOLLIPOP_MR1) {
            PackageParserLollipop22.collectCertificates.call(parser, p, flags);
        } else if (API_LEVEL >= LOLLIPOP) {
            PackageParserLollipop.collectCertificates.call(parser, p, flags);
        }
        black.android.content.pm.PackageParser.collectCertificates.call(parser, p, flags);
    }
}
