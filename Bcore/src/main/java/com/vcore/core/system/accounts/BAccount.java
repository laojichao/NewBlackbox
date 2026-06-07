package com.vcore.core.system.accounts;

import android.accounts.Account;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Represents a virtual account within the BlackBox environment.
 * <p>
 * Wraps an Android {@link Account} along with its password, user data, visibility
 * settings per package, and authentication tokens. Implements {@link Parcelable}
 * for IPC serialization.
 */
public class BAccount implements Parcelable {
    /** The underlying Android account (name + type). */
    public Account account;

    /** The password associated with this account. */
    public String password;

    /** Key-value user data attached to this account. */
    public HashMap<String, String> accountUserData = new LinkedHashMap<>();

    /** Per-package visibility settings for this account. */
    public HashMap<String, Integer> visibility = new LinkedHashMap<>();

    /** Authentication tokens keyed by token type. */
    public HashMap<String, String> authTokens = new LinkedHashMap<>();

    /** Timestamp (millis) of the last successful authentication. */
    public long updateLastAuthenticatedTime;

    /**
     * Checks whether the given account matches this BAccount.
     *
     * @param account the account to compare against
     * @return true if the account matches, false if null or not equal
     */
    public boolean isMatch(Account account) {
        if (account == null) {
            return false;
        }
        return account.equals(this.account);
    }

    /**
     * Inserts a key-value pair into the account's user data.
     *
     * @param key   the user data key
     * @param value the user data value
     */
    public void insertExtra(String key, String value) {
        this.accountUserData.put(key, value);
    }

    /** {@inheritDoc} */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Writes this account's data to a Parcel for IPC transport.
     *
     * @param dest  the Parcel to write to
     * @param flags additional flags forParcelable writing
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.account, flags);
        dest.writeString(this.password);
        dest.writeSerializable(this.accountUserData);
        dest.writeSerializable(this.visibility);
        dest.writeSerializable(this.authTokens);
        dest.writeLong(this.updateLastAuthenticatedTime);
    }

    /** Default constructor. */
    public BAccount() { }

    /**
     * Constructs a BAccount by reading from a Parcel.
     *
     * @param in the Parcel to read from
     */
    protected BAccount(Parcel in) {
        this.account = in.readParcelable(Account.class.getClassLoader());
        this.password = in.readString();
        this.accountUserData = (HashMap<String, String>) in.readSerializable();
        this.visibility = (HashMap<String, Integer>) in.readSerializable();
        this.authTokens = (HashMap<String, String>) in.readSerializable();
        this.updateLastAuthenticatedTime = in.readLong();
    }

    /** Parcelable CREATOR for BAccount instances. */
    public static final Creator<BAccount> CREATOR = new Creator<BAccount>() {
        @Override
        public BAccount createFromParcel(Parcel source) {
            return new BAccount(source);
        }

        @Override
        public BAccount[] newArray(int size) {
            return new BAccount[size];
        }
    };
}
