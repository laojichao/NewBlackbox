package black.android.location.provider;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.location.provider.ProviderProperties} class.
 * Contains properties describing the capabilities of a location provider,
 * such as network and cell tower requirements.
 */
public class ProviderProperties {
    public static final Reflector REF = Reflector.on("android.location.provider.ProviderProperties");

    /** Whether this provider requires a network connection. */
    public static Reflector.FieldWrapper<Boolean> mHasNetworkRequirement = REF.field("mHasNetworkRequirement");

    /** Whether this provider requires cell tower connectivity. */
    public static Reflector.FieldWrapper<Boolean> mHasCellRequirement = REF.field("mHasCellRequirement");
}
