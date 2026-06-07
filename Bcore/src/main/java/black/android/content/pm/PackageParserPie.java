package black.android.content.pm;

import android.content.pm.PackageParser;

import java.io.File;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.content.pm.PackageParser} class
 * on Android P (Pie, API 28). Uses a boolean parameter for skipVerify in
 * certificate collection, replacing the earlier int flags approach.
 */
public class PackageParserPie {
    public static final Reflector REF = Reflector.on("android.content.pm.PackageParser");

    /**
     * Creates a new PackageParser instance.
     */
    public static Reflector.ConstructorWrapper<PackageParser> _new = REF.constructor();

    /**
     * Collects certificates for a parsed package (Pie static variant with skipVerify flag).
     *
     * @param pkg        the Package to collect certificates for
     * @param skipVerify whether to skip signature verification
     */
    public static Reflector.StaticMethodWrapper<Void> collectCertificates = REF.staticMethod("collectCertificates", PackageParser.Package.class, boolean.class);

    /**
     * Parses an APK file into a Package object.
     *
     * @param apkFile the APK file to parse
     * @param flags   parsing flags
     * @return the parsed Package object
     */
    public static Reflector.MethodWrapper<PackageParser.Package> parsePackage = REF.method("parsePackage", File.class, int.class);
}
