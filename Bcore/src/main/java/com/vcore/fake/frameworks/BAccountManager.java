package com.vcore.fake.frameworks;

import android.accounts.Account;
import android.accounts.AuthenticatorDescription;
import android.accounts.IAccountManagerResponse;
import android.os.Bundle;
import android.os.RemoteException;

import java.util.Map;

import com.vcore.app.BActivityThread;
import com.vcore.core.system.ServiceManager;
import com.vcore.core.system.accounts.IBAccountManagerService;

/**
 * Virtual environment manager for account-related operations.
 *
 * <p>Wraps {@link IBAccountManagerService} to provide account management functionality
 * scoped to the virtual environment's user space. All operations automatically use
 * the current virtual user ID from {@link BActivityThread#getUserId()}.</p>
 *
 * @see BlackManager
 * @see IBAccountManagerService
 */
public class BAccountManager extends BlackManager<IBAccountManagerService> {
    private static final BAccountManager sBAccountManager = new BAccountManager();

    /**
     * Returns the singleton instance of {@link BAccountManager}.
     *
     * @return the global BAccountManager instance
     */
    public static BAccountManager get() {
        return sBAccountManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getServiceName() {
        return ServiceManager.ACCOUNT_MANAGER;
    }

    /**
     * Retrieves the password for the given account.
     *
     * @param account the account to query
     * @return the password string, or {@code null} if not found or on error
     */
    public String getPassword(Account account) {
        try {
            return getService().getPassword(account, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves user data associated with the given account and key.
     *
     * @param account the account to query
     * @param key     the user data key
     * @return the user data string, or {@code null} if not found or on error
     */
    public String getUserData(Account account, String key) {
        try {
            return getService().getUserData(account, key, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the authenticator types available in the virtual environment.
     *
     * @return an array of {@link AuthenticatorDescription}, or {@code null} on error
     */
    public AuthenticatorDescription[] getAuthenticatorTypes() {
        try {
            return getService().getAuthenticatorTypes(BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns accounts for a specific package.
     *
     * @param packageName the package name
     * @param uid         the UID of the package
     * @return an array of accounts, or {@code null} on error
     */
    public Account[] getAccountsForPackage(String packageName, int uid) {
        try {
            return getService().getAccountsForPackage(packageName, uid, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns accounts of a specific type for a specific package.
     *
     * @param type        the account type
     * @param packageName the package name
     * @return an array of accounts, or {@code null} on error
     */
    public Account[] getAccountsByTypeForPackage(String type, String packageName) {
        try {
            return getService().getAccountsByTypeForPackage(type, packageName, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns all accounts of the specified type in the virtual user space.
     *
     * @param type the account type, or {@code null} for all types
     * @return an array of accounts, or {@code null} on error
     */
    public Account[] getAccountsAsUser(String type) {
        try {
            return getService().getAccountsAsUser(type, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Asynchronously retrieves an account by type and features.
     *
     * @param response    the callback for the result
     * @param accountType the account type to search for
     * @param features    the required features
     */
    public void getAccountByTypeAndFeatures(IAccountManagerResponse response, String accountType,
                                            String[] features) {
        try {
            getService().getAccountByTypeAndFeatures(response, accountType, features, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Asynchronously retrieves accounts by type and features.
     *
     * @param response    the callback for the result
     * @param accountType the account type to search for
     * @param features    the required features
     */
    public void getAccountsByFeatures(IAccountManagerResponse response, String accountType, String[] features) {
        try {
            getService().getAccountsByFeatures(response, accountType, features, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Explicitly adds an account to the virtual environment.
     *
     * @param account  the account to add
     * @param password the account password
     * @param extras   additional data to associate with the account
     * @return {@code true} if the account was added successfully
     */
    public boolean addAccountExplicitly(Account account, String password, Bundle extras) {
        try {
            return getService().addAccountExplicitly(account, password, extras, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Removes an account asynchronously.
     *
     * @param response             the callback for the result
     * @param account              the account to remove
     * @param expectActivityLaunch whether an activity launch is expected
     */
    public void removeAccountAsUser(IAccountManagerResponse response, Account account, boolean expectActivityLaunch) {
        try {
            getService().removeAccountAsUser(response, account, expectActivityLaunch, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Explicitly removes an account from the virtual environment.
     *
     * @param account the account to remove
     * @return {@code true} if the account was removed successfully
     */
    public boolean removeAccountExplicitly(Account account) {
        try {
            return getService().removeAccountExplicitly(account, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Copies an account from one user to another.
     *
     * @param response  the callback for the result
     * @param account   the account to copy
     * @param userFrom  the source user ID
     * @param userTo    the destination user ID
     */
    public void copyAccountToUser(IAccountManagerResponse response, Account account, int userFrom, int userTo) {
        try {
            getService().copyAccountToUser(response, account, userFrom, userTo);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Invalidates an auth token for the given account type.
     *
     * @param accountType the account type
     * @param authToken   the auth token to invalidate
     */
    public void invalidateAuthToken(String accountType, String authToken) {
        try {
            getService().invalidateAuthToken(accountType, authToken, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Peeks at a cached auth token without making a network request.
     *
     * @param account       the account to query
     * @param authTokenType the auth token type
     * @return the cached auth token, or {@code null} if not found or on error
     */
    public String peekAuthToken(Account account, String authTokenType) {
        try {
            return getService().peekAuthToken(account, authTokenType, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Sets an auth token for the given account and token type.
     *
     * @param account       the account
     * @param authTokenType the auth token type
     * @param authToken     the auth token value
     */
    public void setAuthToken(Account account, String authTokenType, String authToken) {
        try {
            getService().setAuthToken(account, authTokenType, authToken, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the password for the given account.
     *
     * @param account  the account
     * @param password the new password
     */
    public void setPassword(Account account, String password) {
        try {
            getService().setPassword(account, password, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clears the password for the given account.
     *
     * @param account the account
     */
    public void clearPassword(Account account) {
        try {
            getService().clearPassword(account, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets user data for the given account.
     *
     * @param account the account
     * @param key     the user data key
     * @param value   the user data value
     */
    public void setUserData(Account account, String key, String value) {
        try {
            getService().setUserData(account, key, value, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates an app's permission for an auth token type.
     *
     * @param account       the account
     * @param authTokenType the auth token type
     * @param uid           the app's UID
     * @param value         the new permission value
     */
    public void updateAppPermission(Account account, String authTokenType, int uid, boolean value) {
        try {
            getService().updateAppPermission(account, authTokenType, uid, value);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves an auth token for the given account, optionally notifying on auth failure.
     *
     * @param response             the callback for the result
     * @param account              the account
     * @param authTokenType        the auth token type
     * @param notifyOnAuthFailure  whether to notify on auth failure
     * @param expectActivityLaunch whether an activity launch is expected
     * @param options              additional options
     */
    public void getAuthToken(IAccountManagerResponse response, Account account, String authTokenType, boolean notifyOnAuthFailure, boolean expectActivityLaunch,
                             Bundle options) {
        try {
            getService().getAuthToken(response, account, authTokenType, notifyOnAuthFailure, expectActivityLaunch, options, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds an account of the given type asynchronously.
     *
     * @param response             the callback for the result
     * @param accountType          the type of account to add
     * @param authTokenType        the auth token type
     * @param requiredFeatures     required features for the account
     * @param expectActivityLaunch whether an activity launch is expected
     * @param options              additional options
     */
    public void addAccount(IAccountManagerResponse response, String accountType, String authTokenType, String[] requiredFeatures, boolean expectActivityLaunch,
                           Bundle options) {
        try {
            getService().addAccount(response, accountType, authTokenType, requiredFeatures, expectActivityLaunch, options, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds an account of the given type as a specific user asynchronously.
     *
     * @param response             the callback for the result
     * @param accountType          the type of account to add
     * @param authTokenType        the auth token type
     * @param requiredFeatures     required features for the account
     * @param expectActivityLaunch whether an activity launch is expected
     * @param options              additional options
     */
    public void addAccountAsUser(IAccountManagerResponse response, String accountType, String authTokenType, String[] requiredFeatures,
                                 boolean expectActivityLaunch, Bundle options) {
        try {
            getService().addAccountAsUser(response, accountType, authTokenType, requiredFeatures, expectActivityLaunch, options, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates credentials for the given account.
     *
     * @param response             the callback for the result
     * @param account              the account to update
     * @param authTokenType        the auth token type
     * @param expectActivityLaunch whether an activity launch is expected
     * @param options              additional options
     */
    public void updateCredentials(IAccountManagerResponse response, Account account, String authTokenType, boolean expectActivityLaunch, Bundle options) {
        try {
            getService().updateCredentials(response, account, authTokenType, expectActivityLaunch, options, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Edits properties of an account type.
     *
     * @param response             the callback for the result
     * @param accountType          the account type
     * @param expectActivityLaunch whether an activity launch is expected
     */
    public void editProperties(IAccountManagerResponse response, String accountType, boolean expectActivityLaunch) {
        try {
            getService().editProperties(response, accountType, expectActivityLaunch, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Confirms credentials for the given account.
     *
     * @param response             the callback for the result
     * @param account              the account to confirm
     * @param options              additional options
     * @param expectActivityLaunch whether an activity launch is expected
     */
    public void confirmCredentialsAsUser(IAccountManagerResponse response, Account account, Bundle options, boolean expectActivityLaunch) {
        try {
            getService().confirmCredentialsAsUser(response, account, options, expectActivityLaunch, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Notifies the system that the given account has been authenticated.
     *
     * @param account the authenticated account
     */
    public void accountAuthenticated(Account account) {
        try {
            getService().accountAuthenticated(account, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the label for an auth token type.
     *
     * @param response      the callback for the result
     * @param accountType   the account type
     * @param authTokenType the auth token type
     */
    public void getAuthTokenLabel(IAccountManagerResponse response, String accountType, String authTokenType) {
        try {
            getService().getAuthTokenLabel(response, accountType, authTokenType, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a mapping from package names to visibility values for the given account.
     *
     * @param account the account to query
     * @return a Map of package name to Integer visibility, or {@code null} on error
     */
    public Map getPackagesAndVisibilityForAccount(Account account) {
        try {
            return getService().getPackagesAndVisibilityForAccount(account, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Explicitly adds an account with per-package visibility settings.
     *
     * @param account    the account to add
     * @param password   the account password
     * @param extras     additional data
     * @param visibility a Map of package name to Integer visibility
     * @return {@code true} if the account was added successfully
     */
    public boolean addAccountExplicitlyWithVisibility(Account account, String password, Bundle extras, Map visibility) {
        try {
            return getService().addAccountExplicitlyWithVisibility(account, password, extras, visibility, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Sets the visibility of an account for a specific package.
     *
     * @param account       the account
     * @param packageName   the package name
     * @param newVisibility the new visibility value
     * @return {@code true} if the visibility was set successfully
     */
    public boolean setAccountVisibility(Account account, String packageName, int newVisibility) {
        try {
            return getService().setAccountVisibility(account, packageName, newVisibility, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Gets the visibility of an account for a specific package.
     *
     * @param account     the account
     * @param packageName the package name
     * @return the visibility value, or 3 (VISIBILITY_NOT_VISIBLE) on error
     */
    public int getAccountVisibility(Account account, String packageName) {
        try {
            return getService().getAccountVisibility(account, packageName, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        // AccountManager.VISIBILITY_NOT_VISIBLE
        return 3;
    }

    /**
     * Returns accounts and their visibility for a specific package.
     *
     * @param packageName the package name
     * @param accountType the account type, or {@code null} for all types
     * @return a Map of Account to Integer visibility, or {@code null} on error
     */
    public Map getAccountsAndVisibilityForPackage(String packageName, String accountType) {
        try {
            return getService().getAccountsAndVisibilityForPackage(packageName, accountType, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Registers an account listener for the given account types.
     *
     * @param accountTypes  the account types to listen for
     * @param opPackageName the calling package name
     */
    public void registerAccountListener(String[] accountTypes, String opPackageName) {
        try {
            getService().registerAccountListener(accountTypes, opPackageName, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Unregisters an account listener for the given account types.
     *
     * @param accountTypes  the account types to stop listening for
     * @param opPackageName the calling package name
     */
    public void unregisterAccountListener(String[] accountTypes, String opPackageName) {
        try {
            getService().unregisterAccountListener(accountTypes, opPackageName, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
