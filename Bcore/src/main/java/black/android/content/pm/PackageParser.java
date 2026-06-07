package black.android.content.pm;

import android.content.pm.ApplicationInfo;
import android.content.pm.Signature;
import android.util.DisplayMetrics;

import java.io.File;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.content.pm.PackageParser} class
 * on Android Q+ (API 29+). Provides access to the package parsing and certificate
 * collection methods for analyzing APK files outside the package manager.
 */
public class PackageParser {
    public static final Reflector REF = Reflector.on("android.content.pm.PackageParser");

    /**
     * Collects certificates and verifies the signing info for a parsed package.
     *
     * @param pkg       the Package to collect certificates for
     * @param flags     parsing flags
     */
    public static Reflector.MethodWrapper<Void> collectCertificates = REF.method("collectCertificates", android.content.pm.PackageParser.Package.class, int.class);

    /**
     * Parses an APK file into a Package object.
     *
     * @param apkFile        the APK file to parse
     * @param packageName    the expected package name (may be null)
     * @param metrics        the DisplayMetrics for resource configuration
     * @param flags          parsing flags
     * @return the parsed Package object
     */
    public static Reflector.MethodWrapper<android.content.pm.PackageParser.Package> parsePackage = REF.method("parsePackage", File.class, String.class, DisplayMetrics.class, int.class);

    /**
     * Reflection wrapper for {@code android.content.pm.PackageParser$Package}.
     */
    public static class Package {
        public static final Reflector REF = Reflector.on("android.content.pm.PackageParser$Package");

        /** The ApplicationInfo for the parsed package. */
        public static Reflector.FieldWrapper<ApplicationInfo> applicationInfo = REF.field("applicationInfo");
    }

    /**
     * Reflection wrapper for {@code android.content.pm.PackageParser$SigningDetails} (P+).
     * Contains the signing certificate details for a parsed package.
     */
    public static class SigningDetails {
        public static final Reflector REF = Reflector.on("android.content.pm.PackageParser$SigningDetails");

        /** The array of signing signatures. */
        public static Reflector.FieldWrapper<Signature[]> signatures = REF.field("signatures");
    }
}
