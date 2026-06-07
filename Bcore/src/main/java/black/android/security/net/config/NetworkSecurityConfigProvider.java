package black.android.security.net.config;

import android.content.Context;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.security.net.config.NetworkSecurityConfigProvider} class.
 * Provides access to the static install method which sets up the network security
 * configuration provider for an application context, enabling custom network
 * security trust configurations.
 */
public class NetworkSecurityConfigProvider {
    public static final Reflector REF = Reflector.on("android.security.net.config.NetworkSecurityConfigProvider");

    /**
     * Installs the network security configuration provider for the given context.
     *
     * @param context the application Context
     */
    public static Reflector.StaticMethodWrapper<Void> install = REF.staticMethod("install", Context.class);
}
