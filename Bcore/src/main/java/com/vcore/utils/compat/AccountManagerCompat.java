package com.vcore.utils.compat;

/**
 * Compatibility constants for Android's {@code AccountManager} operations.
 * Provides access to internal AccountManager bundle keys that are not part of the public SDK.
 */
public class AccountManagerCompat {
    /**
     * Boolean, if set and 'customTokens' the authenticator is responsible for
     * notifications.
     */
    public static final String KEY_NOTIFY_ON_FAILURE = "notifyOnAuthFailure";
}
