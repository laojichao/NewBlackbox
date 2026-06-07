package black.com.android.internal.net;

import java.util.List;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code com.android.internal.net.VpnConfig} class.
 * Contains VPN connection configuration including the VPN user identity and
 * per-application VPN routing rules (allowed/disallowed application lists).
 */
public class VpnConfig {
    public static final Reflector REF = Reflector.on("com.android.internal.net.VpnConfig");

    /** The user identity associated with this VPN connection. */
    public static Reflector.FieldWrapper<String> user = REF.field("user");

    /** The list of package names disallowed from using this VPN. */
    public static Reflector.FieldWrapper<List<String>> disallowedApplications = REF.field("disallowedApplications");

    /** The list of package names allowed to use this VPN. */
    public static Reflector.FieldWrapper<List<String>> allowedApplications = REF.field("allowedApplications");
}
