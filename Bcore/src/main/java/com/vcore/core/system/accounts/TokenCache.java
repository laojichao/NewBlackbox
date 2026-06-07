package com.vcore.core.system.accounts;

import android.accounts.Account;

import java.util.Objects;

/**
 * Represents a cached authentication token entry with an expiry time.
 * <p>
 * Used by {@link BAccountManagerService} to cache custom tokens returned by
 * authenticators that declare {@code customTokens=true}. Each entry includes
 * the user ID, account, calling package, token type, token value, and expiry
 * timestamp. Expired entries are automatically evicted on cache reads.
 */
public class TokenCache {
    /** The virtual user ID this cached token belongs to. */
    public final int userId;

    /** The account associated with this cached token. */
    public final Account account;

    /** The expiry time in epoch milliseconds; the token is considered expired after this time. */
    public final long expiryEpochMillis;

    /** The cached authentication token value. */
    public final String authToken;

    /** The type of the authentication token. */
    public final String authTokenType;

    /** The package name that requested this token. */
    public final String packageName;

    /**
     * Constructs a new TokenCache entry.
     *
     * @param userId      the virtual user ID
     * @param account     the account this token belongs to
     * @param callerPkg   the package name of the caller that requested the token
     * @param tokenType   the authentication token type
     * @param token       the authentication token value
     * @param expiryMillis the expiry time in epoch milliseconds
     */
    public TokenCache(int userId, Account account, String callerPkg, String tokenType, String token, long expiryMillis) {
        this.userId = userId;
        this.account = account;
        this.expiryEpochMillis = expiryMillis;
        this.authToken = token;
        this.authTokenType = tokenType;
        this.packageName = callerPkg;
    }

    /**
     * Compares this TokenCache to another object for equality based on all fields.
     *
     * @param o the object to compare
     * @return true if the objects are equal
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof TokenCache)) {
            return false;
        }
        TokenCache that = (TokenCache) o;
        return userId == that.userId && expiryEpochMillis == that.expiryEpochMillis && Objects.equals(account, that.account) &&
                Objects.equals(authToken, that.authToken) && Objects.equals(authTokenType, that.authTokenType) && Objects.equals(packageName, that.packageName);
    }

    /**
     * Returns a hash code based on all fields.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(userId, account, expiryEpochMillis, authToken, authTokenType, packageName);
    }
}
