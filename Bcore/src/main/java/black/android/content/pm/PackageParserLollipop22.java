package black.android.content.pm;

import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Package;

import java.io.File;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.content.pm.PackageParser} class
 * on Android L MR1 (Lollipop 5.1, API 22). Provides access to the instance-based
 * package parsing and certificate collection methods.
 */
public class PackageParserLollipop22 {
    public static final Reflector REF = Reflector.on("android.content.pm.PackageParser");

    /**
     * Creates a new PackageParser instance.
     */
    public static Reflector.ConstructorWrapper<PackageParser> _new = REF.constructor();

    /**
     * Collects certificates for a parsed package (Lollipop 5.1 variant).
     *
     * @param pkg   the Package to collect certificates for
     * @param flags parsing flags
     */
    public static Reflector.MethodWrapper<Void> collectCertificates = REF.method("collectCertificates", Package.class, int.class);

    /**
     * Parses an APK file into a Package object (Lollipop 5.1 variant).
     *
     * @param apkFile the APK file to parse
     * @param flags   parsing flags
     * @return the parsed Package object
     */
    public static Reflector.MethodWrapper<Package> parsePackage = REF.method("parsePackage", File.class, int.class);
}
