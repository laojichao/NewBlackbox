package black.android.content.pm;

import android.content.pm.PackageParser;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.content.pm.PackageParser#collectCertificates}
 * static method on Android N (Nougat, API 24+). Certificate collection became a static method
 * in Nougat.
 */
public class PackageParserNougat {
    public static final Reflector REF = Reflector.on("android.content.pm.PackageParser");

    /**
     * Collects certificates for a parsed package (Nougat static variant).
     *
     * @param pkg   the Package to collect certificates for
     * @param flags parsing flags
     */
    public static Reflector.StaticMethodWrapper<Void> collectCertificates = REF.staticMethod("collectCertificates", PackageParser.Package.class, int.class);
}
