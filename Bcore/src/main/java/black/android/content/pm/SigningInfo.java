package black.android.content.pm;

import android.content.pm.PackageParser.SigningDetails;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.content.pm.SigningInfo} class (Android P+).
 * Provides a constructor that creates a SigningInfo from PackageParser.SigningDetails,
 * which is the internal representation of APK signing information.
 */
public class SigningInfo {
    public static final Reflector REF = Reflector.on("android.content.pm.SigningInfo");

    /**
     * Creates a new SigningInfo from PackageParser.SigningDetails.
     *
     * @param signingDetails the internal SigningDetails to wrap
     */
    public static Reflector.ConstructorWrapper<android.content.pm.SigningInfo> _new = REF.constructor(SigningDetails.class);
}
