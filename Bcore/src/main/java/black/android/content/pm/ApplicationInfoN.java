package black.android.content.pm;

import black.Reflector;

/**
 * Reflection wrapper for hidden fields in {@code android.content.pm.ApplicationInfo}
 * on Android N (Nougat, API 24+). Provides access to the device-protected and
 * credential-protected storage directory paths introduced with direct boot.
 */
public class ApplicationInfoN {
    public static final Reflector REF = Reflector.on("android.content.pm.ApplicationInfo");

    /** The credential-encrypted data directory path (deprecated name). */
    public static Reflector.FieldWrapper<String> credentialEncryptedDataDir = REF.field("credentialEncryptedDataDir");

    /** The credential-protected data directory path. */
    public static Reflector.FieldWrapper<String> credentialProtectedDataDir = REF.field("credentialProtectedDataDir");

    /** The device-protected data directory path. */
    public static Reflector.FieldWrapper<String> deviceProtectedDataDir = REF.field("deviceProtectedDataDir");
}
