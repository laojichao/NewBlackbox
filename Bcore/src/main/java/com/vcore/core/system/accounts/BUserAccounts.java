package com.vcore.core.system.accounts;

import android.accounts.Account;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the collection of accounts belonging to a single virtual user.
 * <p>
 * Holds a list of {@link BAccount} objects along with a synchronization lock
 * for thread-safe access. Provides methods to add, remove, query, and update
 * account data. Implements {@link Parcelable} for IPC transport.
 */
public class BUserAccounts implements Parcelable {
    /** Lock object for synchronizing access to this user's account data. */
    public final Object lock = new Object();

    /** The virtual user ID these accounts belong to. */
    public int userId;

    /** The list of all accounts for this user. */
    public List<BAccount> accounts = new ArrayList<>();

    /**
     * Converts this user's accounts to an array of Android {@link Account} objects.
     *
     * @return an array of Account objects (name + type only)
     */
    public Account[] toAccounts() {
        List<Account> local = new ArrayList<>();
        for (BAccount account : accounts) {
            local.add(account.account);
        }
        return local.toArray(new Account[]{});
    }

    /**
     * Adds a new account to this user's account list.
     *
     * @param account the Android Account to add (name + type)
     * @return the newly created BAccount wrapper
     */
    public BAccount addAccount(Account account) {
        BAccount bAccount = new BAccount();
        bAccount.account = account;
        accounts.add(bAccount);
        return bAccount;
    }

    /**
     * Finds and returns the BAccount matching the given Android Account.
     *
     * @param account the account to search for
     * @return the matching BAccount, or null if not found
     */
    public BAccount getAccount(Account account) {
        for (BAccount bAccount : accounts) {
            if (bAccount.isMatch(account)) {
                return bAccount;
            }
        }
        return null;
    }

    /**
     * Deletes the account matching the given Android Account from this user's list.
     *
     * @param account the account to delete
     * @return true if the account was found and removed, false otherwise
     */
    public boolean delAccount(Account account) {
        BAccount bAccount = getAccount(account);
        return accounts.remove(bAccount);
    }

    /**
     * Returns the visibility map for the given account.
     *
     * @param account the account to query
     * @return the package-to-visibility map, or an empty map if not found
     */
    public Map<String, Integer> getVisibility(Account account) {
        BAccount bAccount = getAccount(account);
        if (bAccount == null) {
            return new HashMap<>();
        }
        return bAccount.visibility;
    }

    /**
     * Returns the user data map for the given account.
     *
     * @param account the account to query
     * @return the key-value user data map, or an empty map if not found
     */
    public Map<String, String> getAccountUserData(Account account) {
        BAccount bAccount = getAccount(account);
        if (bAccount == null) {
            return new HashMap<>();
        }
        return bAccount.accountUserData;
    }

    /**
     * Returns the auth tokens map for the given account.
     *
     * @param account the account to query
     * @return the token-type-to-token map, or an empty map if not found
     */
    public Map<String, String> getAuthToken(Account account) {
        BAccount bAccount = getAccount(account);
        if (bAccount == null) {
            return new HashMap<>();
        }
        return bAccount.authTokens;
    }

    /**
     * Returns all accounts of the specified type as Android Account objects.
     *
     * @param type the account type to filter by
     * @return an array of matching Account objects
     */
    public Account[] getAccountsByType(String type) {
        List<Account> local = new ArrayList<>();
        for (BAccount account : accounts) {
            if (account.account.type.equals(type)) {
                local.add(account.account);
            }
        }
        return local.toArray(new Account[]{});
    }

    /**
     * Updates the last authenticated timestamp for the given account to the current time.
     *
     * @param account the account to update
     */
    public void updateLastAuthenticatedTime(Account account) {
        BAccount bAccount = getAccount(account);
        if (bAccount != null) {
            bAccount.updateLastAuthenticatedTime = System.currentTimeMillis();
        }
    }

    /**
     * Returns the last authenticated timestamp for the given account.
     *
     * @param account the account to query
     * @return the timestamp in epoch millis, or -1 if the account is not found
     */
    public long findAccountLastAuthenticatedTime(Account account) {
        BAccount bAccount = getAccount(account);
        if (bAccount != null) {
            return bAccount.updateLastAuthenticatedTime;
        }
        return -1;
    }

    /** {@inheritDoc} */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Writes this user accounts data to a Parcel.
     *
     * @param dest  the Parcel to write to
     * @param flags additional flags for Parcelable writing
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.userId);
        dest.writeTypedList(this.accounts);
    }

    /** Default constructor. */
    public BUserAccounts() { }

    /**
     * Constructs a BUserAccounts from a Parcel.
     *
     * @param in the Parcel to read from
     */
    protected BUserAccounts(Parcel in) {
        this.userId = in.readInt();
        this.accounts = in.createTypedArrayList(BAccount.CREATOR);
    }
}
