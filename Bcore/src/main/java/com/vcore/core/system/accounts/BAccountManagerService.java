package com.vcore.core.system.accounts;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.accounts.IAccountAuthenticator;
import android.accounts.IAccountAuthenticatorResponse;
import android.accounts.IAccountManagerResponse;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import androidx.annotation.NonNull;
import androidx.core.util.AtomicFile;
import androidx.core.util.Preconditions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.vcore.BlackBoxCore;
import com.vcore.core.env.BEnvironment;
import com.vcore.core.system.BProcessManagerService;
import com.vcore.core.system.ISystemService;
import com.vcore.core.system.ProcessRecord;
import com.vcore.core.system.pm.BPackageManagerService;
import com.vcore.core.system.pm.PackageMonitor;
import com.vcore.core.system.user.BUserHandle;
import com.vcore.utils.ArrayUtils;
import com.vcore.utils.CloseUtils;
import com.vcore.utils.FileUtils;
import com.vcore.utils.Slog;
import com.vcore.utils.compat.AccountManagerCompat;


/**
 * Virtual account manager service for the BlackBox environment.
 * <p>
 * Provides a complete account management implementation that mirrors the Android
 * AccountManager API. Handles account CRUD operations, authentication token management,
 * authenticator service binding, account visibility per package, and persistent
 * storage of account data. All account data is isolated per virtual user.
 */
@SuppressLint("InlinedApi")
public class BAccountManagerService extends IBAccountManagerService.Stub implements ISystemService, PackageMonitor {
    private static final String TAG = "AccountManagerService";

    /** Singleton instance. */
    private static final BAccountManagerService sService = new BAccountManagerService();

    /** Shared empty Account array for zero-result returns. */
    private static final Account[] EMPTY_ACCOUNT_ARRAY = new Account[]{};

    /** Handler message code for session timeouts. */
    private static final int MESSAGE_TIMED_OUT = 3;

    /** Reference to the package manager service for resolving intents and authenticators. */
    private final BPackageManagerService mPms;

    /** Map from virtual user ID to their account data. */
    private final Map<Integer, BUserAccounts> mUserAccountsMap = new HashMap<>();

    /** Cache of discovered account authenticators indexed by account type. */
    private final AuthenticatorCache mAuthenticatorCache = new AuthenticatorCache();

    /** LRU cache of authentication tokens with expiry. */
    private final LinkedList<TokenCache> mTokenCaches = new LinkedList<>();

    /** Active sessions bound to authenticator services. */
    private final LinkedHashMap<String, Session> mSessions = new LinkedHashMap<>();

    /** Handler for posting messages on the main looper. */
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    /** Application context. */
    private final Context mContext;

    /**
     * Returns the singleton instance of BAccountManagerService.
     *
     * @return the singleton service instance
     */
    public static BAccountManagerService get() {
        return sService;
    }

    /**
     * Constructs the account manager service, obtaining the application context
     * and package manager reference.
     */
    public BAccountManagerService() {
        this.mContext = BlackBoxCore.getContext();
        this.mPms = BPackageManagerService.get();
    }

    /**
     * Called when the system is ready. Loads persisted accounts, refreshes the
     * authenticator cache, and registers for package change notifications.
     */
    @Override
    public void systemReady() {
        loadAccounts();
        loadAuthenticatorCache(null);
        mPms.addPackageMonitor(this);
    }

    /**
     * Called when a package is uninstalled. Refreshes the authenticator cache.
     *
     * @param packageName the uninstalled package
     * @param isRemove    whether the package was fully removed
     * @param userId      the user ID
     */
    @Override
    public void onPackageUninstalled(String packageName, boolean isRemove, int userId) {
        loadAuthenticatorCache(null);
    }

    /**
     * Called when a package is installed. Refreshes the authenticator cache for that package.
     *
     * @param packageName the installed package
     * @param userId      the user ID
     */
    @Override
    public void onPackageInstalled(String packageName, int userId) {
        loadAuthenticatorCache(packageName);
    }

    /**
     * Loads all account data from the persisted configuration file into memory.
     */
    private void loadAccounts() {
        Parcel parcel = Parcel.obtain();
        InputStream is = null;
        try {
            File userInfoConf = BEnvironment.getAccountsConf();
            if (!userInfoConf.exists()) {
                return;
            }

            is = new FileInputStream(BEnvironment.getAccountsConf());
            byte[] bytes = FileUtils.toByteArray(is);
            parcel.unmarshall(bytes, 0, bytes.length);
            parcel.setDataPosition(0);

            HashMap<Integer, BUserAccounts> accountsMap = parcel.readHashMap(BUserAccounts.class.getClassLoader());
            if (accountsMap == null) {
                return;
            }

            synchronized (mUserAccountsMap) {
                mUserAccountsMap.clear();
                for (Integer key : accountsMap.keySet()) {
                    mUserAccountsMap.put(key, accountsMap.get(key));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            parcel.recycle();
            CloseUtils.close(is);
        }
    }

    /**
     * Persists all current account data to the configuration file using atomic writes.
     */
    private void saveAllAccounts() {
        synchronized (mUserAccountsMap) {
            Parcel parcel = Parcel.obtain();
            AtomicFile atomicFile = new AtomicFile(BEnvironment.getAccountsConf());
            FileOutputStream fileOutputStream = null;

            try {
                parcel.writeMap(mUserAccountsMap);
                try {
                    fileOutputStream = atomicFile.startWrite();
                    FileUtils.writeParcelToOutput(parcel, fileOutputStream);
                    atomicFile.finishWrite(fileOutputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                    atomicFile.failWrite(fileOutputStream);
                } finally {
                    CloseUtils.close(fileOutputStream);
                }
            } finally {
                parcel.recycle();
            }
        }
    }

    /**
     * Returns the password for the given account.
     *
     * @param account the account to query
     * @param userId  the virtual user ID
     * @return the password string, or null if the account does not exist
     * @throws IllegalArgumentException if account is null
     */
    @Override
    public String getPassword(Account account, int userId) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "getPassword: " + account + ", caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid());
        }

        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        BUserAccounts accounts = getUserAccounts(userId);
        return readPasswordInternal(accounts, account);
    }

    /**
     * Returns a specific user data value for the given account and key.
     *
     * @param account the account to query
     * @param key     the user data key
     * @param userId  the virtual user ID
     * @return the user data value, or null if not found
     * @throws NullPointerException if account or key is null
     */
    @Override
    public String getUserData(Account account, String key, int userId) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            String msg = String.format("getUserData( account: %s, key: %s, callerUid: %s, pid: %s", account, key, Binder.getCallingUid(),
                    Binder.getCallingPid());
            Log.v(TAG, msg);
        }

        Objects.requireNonNull(account, "account cannot be null");
        Objects.requireNonNull(key, "key cannot be null");
        BUserAccounts accounts = getUserAccounts(userId);
        return readUserDataInternal(accounts, account, key);
    }

    /**
     * Returns the authenticator descriptions for all account types present
     * in the given user's accounts.
     *
     * @param userId the virtual user ID
     * @return an array of AuthenticatorDescription for registered account types
     */
    @Override
    public AuthenticatorDescription[] getAuthenticatorTypes(int userId) {
        BUserAccounts userAccounts = getUserAccounts(userId);
        List<AuthenticatorDescription> authenticatorDescriptions = new ArrayList<>();
        synchronized (userAccounts.lock) {
            for (BAccount account : userAccounts.accounts) {
                AuthenticatorInfo authenticatorInfo = mAuthenticatorCache.authenticators.get(account.account.type);
                if (authenticatorInfo != null) {
                    authenticatorDescriptions.add(authenticatorInfo.desc);
                }
            }
        }
        return authenticatorDescriptions.toArray(new AuthenticatorDescription[]{});
    }

    /**
     * Returns accounts visible to the specified package for a given user.
     *
     * @param packageName the package name to check visibility for
     * @param uid         the caller's UID (currently unused)
     * @param userId      the virtual user ID
     * @return an array of visible Account objects
     */
    @Override
    public Account[] getAccountsForPackage(String packageName, int uid, int userId) {
        BUserAccounts userAccounts = getUserAccounts(userId);
        List<Account> accounts = new ArrayList<>();
        synchronized (userAccounts.lock) {
            for (BAccount account : userAccounts.accounts) {
                Integer visibility = account.visibility.get(packageName);
                if (visibility != null && visibility == AccountManager.VISIBILITY_VISIBLE) {
                    accounts.add(account.account);
                }
            }
        }
        return accounts.toArray(new Account[]{});
    }

    /**
     * Returns accounts of a specific type visible to the given package.
     *
     * @param type        the account type to filter by
     * @param packageName the package name to check visibility for
     * @param userId      the virtual user ID
     * @return an array of matching Account objects
     */
    @Override
    public Account[] getAccountsByTypeForPackage(String type, String packageName, int userId) {
        BUserAccounts userAccounts = getUserAccounts(userId);
        List<Account> accounts = new ArrayList<>();
        synchronized (userAccounts.lock) {
            for (BAccount account : userAccounts.accounts) {
                if (account.account.type.equals(type)) {
                    Integer visibility = account.visibility.get(packageName);
                    if (visibility != null && visibility == AccountManager.VISIBILITY_VISIBLE) {
                        accounts.add(account.account);
                    }
                }
            }
        }
        return accounts.toArray(new Account[]{});
    }

    /**
     * Returns all accounts of the given type for the specified user.
     *
     * @param accountType the account type to filter by
     * @param userId      the virtual user ID
     * @return an array of Account objects matching the type
     */
    @Override
    public Account[] getAccountsAsUser(String accountType, int userId) {
        BUserAccounts userAccounts = getUserAccounts(userId);
        List<Account> accounts = new ArrayList<>();
        synchronized (userAccounts.lock) {
            for (BAccount account : userAccounts.accounts) {
                if (account.account.type.equals(accountType)) {
                    accounts.add(account.account);
                }
            }
        }
        return accounts.toArray(new Account[]{});
    }

    /**
     * Asynchronously retrieves accounts of a given type that have the specified features.
     * Results are delivered to the response callback.
     *
     * @param response    the callback to deliver results to
     * @param accountType the account type to search for
     * @param features    required features, or null/empty for any
     * @param userId      the virtual user ID
     * @throws IllegalArgumentException if response or accountType is null
     */
    @Override
    public void getAccountByTypeAndFeatures(IAccountManagerResponse response, String accountType, String[] features, int userId) {
        if (response == null) {
            throw new IllegalArgumentException("response is null");
        }

        if (accountType == null) {
            throw new IllegalArgumentException("accountType is null");
        }

        String opPackageName = getCallingPackageName();
        BUserAccounts userAccounts = getUserAccounts(userId);

        if (ArrayUtils.isEmpty(features)) {
            Account[] accountsWithManagedNotVisible = getAccountsFromCache(userAccounts, accountType, opPackageName,
                    true /* include managed not visible */);
            handleGetAccountsResult(response, accountsWithManagedNotVisible, opPackageName, userId);
            return;
        }

        IAccountManagerResponse retrieveAccountsResponse =
                new IAccountManagerResponse.Stub() {
                    @Override
                    public void onResult(Bundle value) {
                        Parcelable[] parcelables = value.getParcelableArray(AccountManager.KEY_ACCOUNTS);

                        Account[] accounts = new Account[parcelables.length];
                        for (int i = 0; i < parcelables.length; i++) {
                            accounts[i] = (Account) parcelables[i];
                        }
                        handleGetAccountsResult(response, accounts, opPackageName, userId);
                    }

                    @Override
                    public void onError(int errorCode, String errorMessage) {
                        // Will not be called in this case.
                    }
                };
        new GetAccountsByTypeAndFeatureSession(userAccounts, retrieveAccountsResponse, accountType, features, userId, opPackageName,
                true /* include managed not visible */)
                .bind();
    }

    /**
     * Asynchronously retrieves accounts of a given type, filtered by features.
     * Unlike {@link #getAccountByTypeAndFeatures}, this returns all matching accounts
     * without including managed-not-visible ones.
     *
     * @param response the callback to deliver results to
     * @param type     the account type to search for
     * @param features required features, or null/empty to skip filtering
     * @param userId   the virtual user ID
     * @throws IllegalArgumentException if response or type is null
     */
    @Override
    public void getAccountsByFeatures(IAccountManagerResponse response, String type, String[] features, int userId) {
        if (response == null) {
            throw new IllegalArgumentException("response is null");
        }

        if (type == null) {
            throw new IllegalArgumentException("accountType is null");
        }

        String opPackageName = getCallingPackageName();
        BUserAccounts userAccounts = getUserAccounts(userId);
        if (features == null || features.length == 0) {
            Account[] accounts = getAccountsFromCache(userAccounts, type, opPackageName, false);

            Bundle result = new Bundle();
            result.putParcelableArray(AccountManager.KEY_ACCOUNTS, accounts);

            onResult(response, result);
            return;
        }
        new GetAccountsByTypeAndFeatureSession(userAccounts, response, type, features, userId, opPackageName,
                false /* include managed not visible */)
                .bind();
    }

    /**
     * Adds a new account explicitly with the given password and extras.
     *
     * @param account  the account to add
     * @param password the password for the account
     * @param extras   additional data to store with the account
     * @param userId   the virtual user ID
     * @return true if the account was added successfully, false if it already exists
     */
    @Override
    public boolean addAccountExplicitly(Account account, String password, Bundle extras, int userId) {
        return addAccountExplicitlyWithVisibility(account, password, extras, null, userId);
    }

    /**
     * Initiates an asynchronous account removal through the authenticator.
     *
     * @param response            the callback to deliver the result
     * @param account             the account to remove
     * @param expectActivityLaunch whether an activity launch is expected
     * @param userId              the virtual user ID
     */
    @Override
    public void removeAccountAsUser(IAccountManagerResponse response, Account account, boolean expectActivityLaunch, int userId) {
        Preconditions.checkArgument(account != null, "Account cannot be null");
        Preconditions.checkArgument(response != null, "Response cannot be null");
        BUserAccounts accounts = getUserAccounts(userId);
        new RemoveAccountSession(accounts, response, account, expectActivityLaunch)
                .bind();
    }

    /**
     * Removes an account explicitly without going through the authenticator.
     *
     * @param account the account to remove
     * @param userId  the virtual user ID
     * @return true if the account was found and removed, false otherwise
     */
    @Override
    public boolean removeAccountExplicitly(Account account, int userId) {
        final int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "removeAccountExplicitly: " + account + ", caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }

        if (account == null) {
            Log.e(TAG, "account is null");
            return false;
        }
        BUserAccounts accounts = getUserAccounts(userId);
        return removeAccountInternal(accounts, account);
    }

    /**
     * Copies an account from one user to another by invoking the authenticator's
     * credential cloning mechanism.
     *
     * @param response the callback for the copy result
     * @param account  the account to copy
     * @param userFrom the source user ID
     * @param userTo   the target user ID
     */
    @Override
    public void copyAccountToUser(IAccountManagerResponse response, Account account, int userFrom, int userTo) {
        final BUserAccounts fromAccounts = getUserAccounts(userFrom);
        final BUserAccounts toAccounts = getUserAccounts(userTo);
        if (fromAccounts == null || toAccounts == null) {
            if (response != null) {
                Bundle result = new Bundle();
                result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
                try {
                    response.onResult(result);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Failed to report error back to the client." + e);
                }
            }
            return;
        }

        Slog.d(TAG, "Copying account " + account.toString() + " from user " + userFrom + " to user " + userTo);
        new Session(fromAccounts, response, account.type, false, false /* stripAuthTokenFromResult */, account.name,
                false /* authDetailsRequired */) {
            @Override
            protected String toDebugString(long now) {
                return super.toDebugString(now) + ", getAccountCredentialsForClone" + ", " + account.type;
            }

            @Override
            public void run() throws RemoteException {
                mAuthenticator.getAccountCredentialsForCloning(this, account);
            }

            @Override
            public void onResult(Bundle result) {
                if (result != null && result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT, false)) {
                    completeCloningAccount(response, result, account, toAccounts, userFrom);
                }
                super.onResult(result);
            }
        }
        .bind();
    }

    /**
     * Invalidates all cached authentication tokens of the specified type for all
     * accounts of the given type.
     *
     * @param accountType  the account type whose tokens should be invalidated
     * @param authToken    the specific auth token to invalidate
     * @param userId       the virtual user ID
     */
    @Override
    public void invalidateAuthToken(String accountType, String authToken, int userId) {
        BUserAccounts accounts = getUserAccounts(userId);
        synchronized (accounts.lock) {
            boolean changed = false;
            for (BAccount account : accounts.accounts) {
                if (account.account.type.equals(accountType)) {
                    account.accountUserData.values().remove(authToken);
                    changed = true;
                }
            }

            if (changed) {
                saveAllAccounts();
            }
        }

        synchronized (mTokenCaches) {
            Iterator<TokenCache> iterator = mTokenCaches.iterator();
            while (iterator.hasNext()) {
                TokenCache next = iterator.next();
                if (next.account.type.equals(accountType) && next.userId == userId && next.authToken.equals(authToken)) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Returns a cached auth token for the given account and token type without
     * invoking the authenticator.
     *
     * @param account        the account to peek at
     * @param authTokenType  the token type to look up
     * @param userId         the virtual user ID
     * @return the cached auth token, or null if not found
     * @throws NullPointerException if account or authTokenType is null
     */
    @Override
    public String peekAuthToken(Account account, String authTokenType, int userId) {
        Objects.requireNonNull(account, "Account cannot be null");
        Objects.requireNonNull(authTokenType, "AuthTokenType cannot be null");

        BUserAccounts accounts = getUserAccounts(userId);
        if (accounts == null) {
            return null;
        }

        synchronized (accounts.lock) {
            return accounts.getAuthToken(account).get(authTokenType);
        }
    }

    /**
     * Sets an authentication token for the given account and token type.
     *
     * @param account        the account to update
     * @param authTokenType  the token type key
     * @param authToken      the token value to store
     * @param userId         the virtual user ID
     * @throws NullPointerException if account or authTokenType is null
     */
    @Override
    public void setAuthToken(Account account, String authTokenType, String authToken, int userId) {
        Objects.requireNonNull(account, "Account cannot be null");
        Objects.requireNonNull(authTokenType, "AuthTokenType cannot be null");

        BUserAccounts accounts = getUserAccounts(userId);
        if (accounts == null) {
            return;
        }

        synchronized (accounts.lock) {
            accounts.getAuthToken(account).put(authTokenType, authToken);
            saveAllAccounts();
        }
    }

    /**
     * Sets the password for the given account and clears all cached auth tokens.
     *
     * @param account  the account to update
     * @param password the new password (null to clear)
     * @param userId   the virtual user ID
     * @throws NullPointerException if account is null
     */
    @Override
    public void setPassword(Account account, String password, int userId) {
        Objects.requireNonNull(account, "Account cannot be null");
        BUserAccounts accounts = getUserAccounts(userId);
        if (accounts == null) {
            return;
        }

        synchronized (accounts.lock) {
            BAccount bAccount = accounts.getAccount(account);
            bAccount.password = password;
            bAccount.authTokens.clear();
            saveAllAccounts();
        }

        synchronized (mTokenCaches) {
            Iterator<TokenCache> iterator = mTokenCaches.iterator();
            while (iterator.hasNext()) {
                TokenCache next = iterator.next();
                if (next.account.equals(account) && next.userId == userId) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Clears the password for the given account (sets it to null).
     *
     * @param account the account whose password should be cleared
     * @param userId  the virtual user ID
     */
    @Override
    public void clearPassword(Account account, int userId) {
        setPassword(account, null, userId);
    }

    /**
     * Sets a user data key-value pair for the given account.
     *
     * @param account the account to update
     * @param key     the user data key
     * @param value   the user data value
     * @param userId  the virtual user ID
     * @throws IllegalArgumentException if key or account is null
     */
    @Override
    public void setUserData(Account account, String key, String value, int userId) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }

        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }

        BUserAccounts accounts = getUserAccounts(userId);
        if (accounts == null) {
            return;
        }

        synchronized (accounts.lock) {
            accounts.getAccountUserData(account).put(key, value);
            saveAllAccounts();
        }
    }

    /**
     * Updates app-level permission for an account authenticator. Currently a no-op.
     *
     * @param account        the account
     * @param authTokenType  the auth token type
     * @param uid            the UID to update
     * @param value          the permission value
     */
    @Override
    public void updateAppPermission(Account account, String authTokenType, int uid, boolean value) {
        // System
    }

    /**
     * Asynchronously retrieves an authentication token for the given account.
     * Checks the internal cache first, then falls back to the authenticator service.
     *
     * @param response           the callback to deliver the token
     * @param account            the account to get a token for
     * @param authTokenType      the type of token to retrieve
     * @param notifyOnAuthFailure whether to notify on authentication failure
     * @param expectActivityLaunch whether an activity launch is expected
     * @param loginOptions       additional options for the authenticator
     * @param userId             the virtual user ID
     */
    @Override
    public void getAuthToken(IAccountManagerResponse response, Account account, String authTokenType, boolean notifyOnAuthFailure, boolean expectActivityLaunch, Bundle loginOptions, int userId) {
        Preconditions.checkArgument(response != null, "response cannot be null");
        try {
            if (account == null) {
                Slog.w(TAG, "getAuthToken called with null account");
                response.onError(AccountManager.ERROR_CODE_BAD_ARGUMENTS, "account is null");
                return;
            }

            if (authTokenType == null) {
                Slog.w(TAG, "getAuthToken called with null authTokenType");
                response.onError(AccountManager.ERROR_CODE_BAD_ARGUMENTS, "authTokenType is null");
                return;
            }
        } catch (RemoteException e) {
            Slog.w(TAG, "Failed to report error back to the client." + e);
            return;
        }

        final BUserAccounts accounts = getUserAccounts(userId);
        AuthenticatorInfo authenticatorInfo = mAuthenticatorCache.authenticators.get(account.type);

        final boolean customTokens = authenticatorInfo != null && authenticatorInfo.desc.customTokens;

        final String callerPkg = loginOptions.getString(AccountManager.KEY_ANDROID_PACKAGE_NAME);

        loginOptions.putInt(AccountManager.KEY_CALLER_UID, Binder.getCallingUid());
        loginOptions.putInt(AccountManager.KEY_CALLER_PID, Binder.getCallingPid());

        if (notifyOnAuthFailure) {
            loginOptions.putBoolean(AccountManagerCompat.KEY_NOTIFY_ON_FAILURE, true);
        }

        if (!customTokens) {
            String authToken = readAuthTokenInternal(accounts, account, authTokenType);
            if (authToken != null) {
                Bundle result = new Bundle();
                result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);

                onResult(response, result);
                return;
            }
        }

        if (customTokens) {
            String token = readCachedTokenInternal(accounts, account, authTokenType, callerPkg);
            if (token != null) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "getAuthToken: cache hit ofr custom token authenticator.");
                }

                Bundle result = new Bundle();
                result.putString(AccountManager.KEY_AUTHTOKEN, token);
                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);

                onResult(response, result);
                return;
            }
        }
        new Session(accounts, response, account.type, expectActivityLaunch, false /* stripAuthTokenFromResult */,
                account.name, false /* authDetailsRequired */) {
            @Override
            protected String toDebugString(long now) {
                loginOptions.keySet();
                return super.toDebugString(now) + ", getAuthToken" + ", " + account + ", authTokenType " + authTokenType + ", loginOptions " + loginOptions
                        + ", notifyOnAuthFailure " + notifyOnAuthFailure;
            }

            @Override
            public void run() throws RemoteException {
                mAuthenticator.getAuthToken(this, account, authTokenType, loginOptions);
            }

            @Override
            public void onResult(Bundle result) {
                if (result != null) {
                    String authToken = result.getString(AccountManager.KEY_AUTHTOKEN);
                    if (authToken != null) {
                        String name = result.getString(AccountManager.KEY_ACCOUNT_NAME);
                        String type = result.getString(AccountManager.KEY_ACCOUNT_TYPE);
                        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(name)) {
                            onError(AccountManager.ERROR_CODE_INVALID_RESPONSE, "the type and name should not be empty");
                            return;
                        }

                        Account resultAccount = new Account(name, type);
                        if (!customTokens) {
                            saveAuthTokenToDatabase(mAccounts, resultAccount, authTokenType, authToken);
                        }

                        long expiryMillis = result.getLong(AbstractAccountAuthenticator.KEY_CUSTOM_TOKEN_EXPIRY, 0L);
                        if (customTokens && expiryMillis > System.currentTimeMillis()) {
                            saveCachedToken(mAccounts, account, callerPkg, authTokenType, authToken, expiryMillis);
                        }
                    }
                }
                super.onResult(result);
            }
        }
        .bind();
    }

    /**
     * Initiates adding a new account through the authenticator service.
     *
     * @param response         the callback for the result
     * @param accountType      the type of account to add
     * @param authTokenType    the auth token type (optional)
     * @param requiredFeatures required features (optional)
     * @param expectActivityLaunch whether an activity launch is expected
     * @param optionsIn        additional options (null-safe)
     * @param userId           the virtual user ID
     * @throws IllegalArgumentException if response or accountType is null
     */
    @Override
    public void addAccount(IAccountManagerResponse response, String accountType, String authTokenType, String[] requiredFeatures, boolean expectActivityLaunch, Bundle optionsIn, int userId) {
        if (response == null) {
            throw new IllegalArgumentException("response is null");
        }

        if (accountType == null) {
            throw new IllegalArgumentException("accountType is null");
        }

        final Bundle options = (optionsIn == null) ? new Bundle() : optionsIn;
        BUserAccounts accounts = getUserAccounts(userId);

        new Session(accounts, response, accountType, expectActivityLaunch,
                true /* stripAuthTokenFromResult */, null /* accountName */,
                false /* authDetailsRequired */, true /* updateLastAuthenticationTime */) {
            @Override
            public void run() throws RemoteException {
                mAuthenticator.addAccount(this, mAccountType, authTokenType, requiredFeatures, options);
            }

            @Override
            protected String toDebugString(long now) {
                return super.toDebugString(now) + ", addAccount" + ", accountType " + accountType + ", requiredFeatures "
                        + Arrays.toString(requiredFeatures);
            }
        }
        .bind();
    }

    /**
     * Adds an account as a specific user. Currently ignored.
     */
    @Override
    public void addAccountAsUser(IAccountManagerResponse response, String accountType, String authTokenType, String[] requiredFeatures, boolean expectActivityLaunch, Bundle options, int userId) {
        // Ignore
    }

    /**
     * Initiates credential update for an existing account through the authenticator.
     *
     * @param response            the callback for the result
     * @param account             the account to update
     * @param authTokenType       the auth token type
     * @param expectActivityLaunch whether an activity launch is expected
     * @param loginOptions        additional options
     * @param userId              the virtual user ID
     * @throws IllegalArgumentException if response or account is null
     */
    @Override
    public void updateCredentials(IAccountManagerResponse response, Account account, String authTokenType, boolean expectActivityLaunch, Bundle loginOptions, int userId) {
        if (response == null) {
            throw new IllegalArgumentException("response is null");
        }

        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }

        BUserAccounts accounts = getUserAccounts(userId);
        new Session(accounts, response, account.type, expectActivityLaunch,
                true /* stripAuthTokenFromResult */, account.name,
                false /* authDetailsRequired */, true /* updateLastCredentialTime */) {
            @Override
            public void run() throws RemoteException {
                mAuthenticator.updateCredentials(this, account, authTokenType, loginOptions);
            }
            @Override
            protected String toDebugString(long now) {
                if (loginOptions != null) {
                    loginOptions.keySet();
                }
                return super.toDebugString(now) + ", updateCredentials" + ", " + account + ", authTokenType " + authTokenType
                        + ", loginOptions " + loginOptions;
            }
        }
        .bind();
    }

    /**
     * Initiates editing properties for an account type through the authenticator.
     *
     * @param response            the callback for the result
     * @param accountType         the account type to edit
     * @param expectActivityLaunch whether an activity launch is expected
     * @param userId              the virtual user ID
     * @throws IllegalArgumentException if response or accountType is null
     */
    @Override
    public void editProperties(IAccountManagerResponse response, String accountType, boolean expectActivityLaunch, int userId) {
        if (response == null) {
            throw new IllegalArgumentException("response is null");
        }

        if (accountType == null) {
            throw new IllegalArgumentException("accountType is null");
        }

        BUserAccounts accounts = getUserAccounts(userId);
        new Session(accounts, response, accountType, expectActivityLaunch,
                true /* stripAuthTokenFromResult */, null /* accountName */,
                false /* authDetailsRequired */) {
            @Override
            public void run() throws RemoteException {
                mAuthenticator.editProperties(this, mAccountType);
            }
            @Override
            protected String toDebugString(long now) {
                return super.toDebugString(now) + ", editProperties" + ", accountType " + accountType;
            }
        }
        .bind();
    }

    /**
     * Confirms credentials for an account as a specific user. Currently ignored.
     */
    @Override
    public void confirmCredentialsAsUser(IAccountManagerResponse response, Account account, Bundle options, boolean expectActivityLaunch, int userId) {
        // Ignore
    }

    /**
     * Records a successful authentication for the given account.
     *
     * @param account the authenticated account
     * @param userId  the virtual user ID
     * @return true if the last authenticated time was updated, false if the user
     *         accounts do not exist
     * @throws NullPointerException if account is null
     */
    @Override
    public boolean accountAuthenticated(Account account, int userId) {
        Objects.requireNonNull(account, "account cannot be null");
        BUserAccounts userAccounts = getUserAccounts(userId);

        if (userAccounts == null) {
            return false;
        }
        return updateLastAuthenticatedTime(userAccounts, account);
    }

    /**
     * Retrieves the human-readable label for a specific auth token type.
     *
     * @param response       the callback for the result
     * @param accountType    the account type
     * @param authTokenType  the auth token type
     * @param userId         the virtual user ID
     */
    @Override
    public void getAuthTokenLabel(IAccountManagerResponse response, String accountType, String authTokenType, int userId) {
        Preconditions.checkArgument(accountType != null, "accountType cannot be null");
        Preconditions.checkArgument(authTokenType != null, "authTokenType cannot be null");

        BUserAccounts accounts = getUserAccounts(userId);
        new Session(accounts, response, accountType, false /* expectActivityLaunch */,
                false /* stripAuthTokenFromResult */,  null /* accountName */,
                false /* authDetailsRequired */) {
            @Override
            protected String toDebugString(long now) {
                return super.toDebugString(now) + ", getAuthTokenLabel" + ", " + accountType + ", authTokenType " + authTokenType;
            }

            @Override
            public void run() throws RemoteException {
                mAuthenticator.getAuthTokenLabel(this, authTokenType);
            }

            @Override
            public void onResult(Bundle result) {
                if (result != null) {
                    String label = result.getString(AccountManager.KEY_AUTH_TOKEN_LABEL);

                    Bundle bundle = new Bundle();
                    bundle.putString(AccountManager.KEY_AUTH_TOKEN_LABEL, label);
                    super.onResult(bundle);
                }
                super.onResult(null);
            }
        }
        .bind();
    }

    /**
     * Returns packages and their visibility settings for the given account.
     *
     * @param account the account to query
     * @param userId  the virtual user ID
     * @return an empty HashMap (visibility is managed per-account in BAccount)
     */
    @Override
    public HashMap<?, ?> getPackagesAndVisibilityForAccount(Account account, int userId) {
        return new HashMap<>();
    }

    /**
     * Saves a cached token with an expiry time to the token cache.
     *
     * @param accounts     the user accounts context
     * @param account      the account the token belongs to
     * @param callerPkg    the requesting package name
     * @param tokenType    the token type
     * @param token        the token value
     * @param expiryMillis the token expiry time in epoch millis
     */
    protected void saveCachedToken(BUserAccounts accounts, Account account, String callerPkg, String tokenType, String token, long expiryMillis) {
        if (account == null || tokenType == null || callerPkg == null) {
            return;
        }

        TokenCache cache = new TokenCache(accounts.userId, account, callerPkg, tokenType, token, expiryMillis);
        synchronized (mTokenCaches) {
            mTokenCaches.add(cache);
        }
    }

    /**
     * Persists an auth token to the account database and saves to disk.
     *
     * @param accounts       the user accounts to update
     * @param account        the account the token belongs to
     * @param authTokenType  the token type key
     * @param authToken      the token value
     */
    protected void saveAuthTokenToDatabase(BUserAccounts accounts, Account account, String authTokenType, String authToken) {
        if (accounts == null) {
            return;
        }

        synchronized (accounts.lock) {
            accounts.getAuthToken(account).put(authTokenType, authToken);
            saveAllAccounts();
        }
    }

    /**
     * Reads a cached token from the in-memory token cache, checking expiry.
     *
     * @param accounts       the user accounts context
     * @param account        the account to look up
     * @param tokenType      the token type to find
     * @param callingPackage the requesting package name
     * @return the cached token string, or null if not found or expired
     */
    protected String readCachedTokenInternal(BUserAccounts accounts, Account account, String tokenType, String callingPackage) {
        long nowTime = System.currentTimeMillis();
        synchronized (mTokenCaches) {
            Iterator<TokenCache> iterator = mTokenCaches.iterator();
            while (iterator.hasNext()) {
                TokenCache next = iterator.next();

                if (next.userId == accounts.userId && next.account.equals(account) && next.authTokenType.equals(tokenType) && next.packageName.equals(callingPackage)) {
                    if (next.expiryEpochMillis > nowTime) {
                        return next.authToken;
                    }
                    iterator.remove();
                }
            }
            return null;
        }
    }

    /**
     * Reads an auth token from the persisted account data.
     *
     * @param accounts       the user accounts to search
     * @param account        the account to look up
     * @param authTokenType  the token type to find
     * @return the auth token string, or null if not found
     */
    protected String readAuthTokenInternal(BUserAccounts accounts, Account account, String authTokenType) {
        if (accounts == null) {
            return null;
        }
        synchronized (accounts.lock) {
            Map<String, String> authToken = accounts.getAuthToken(account);
            return authToken.get(authTokenType);
        }
    }

    /**
     * Completes account cloning by calling the authenticator's addAccountFromCredentials
     * on the target user.
     *
     * @param response          the callback for the result
     * @param accountCredentials the credentials bundle from the source account
     * @param account           the account being cloned
     * @param targetUser        the target user accounts
     * @param parentUserId      the source user ID
     */
    private void completeCloningAccount(IAccountManagerResponse response, final Bundle accountCredentials, final Account account,
                                        final BUserAccounts targetUser, final int parentUserId) {
        new Session(targetUser, response, account.type, false,
                false /* stripAuthTokenFromResult */, account.name,
                false /* authDetailsRequired */) {
            @Override
            protected String toDebugString(long now) {
                return super.toDebugString(now) + ", getAccountCredentialsForClone" + ", " + account.type;
            }

            @Override
            public void run() throws RemoteException {
                for (Account acc : getAccounts(parentUserId, mContext.getPackageName())) {
                    if (acc.equals(account)) {
                        mAuthenticator.addAccountFromCredentials(this, account, accountCredentials);
                        break;
                    }
                }
            }

            @Override
            public void onResult(Bundle result) {
                super.onResult(result);
            }

            @Override
            public void onError(int errorCode, String errorMessage) {
                super.onError(errorCode,  errorMessage);
            }
        }
        .bind();
    }

    /**
     * Returns all accounts for the specified user, filtered by the calling package.
     *
     * @param userId        the virtual user ID
     * @param opPackageName the calling package name for visibility filtering
     * @return an array of Account objects
     */
    public Account[] getAccounts(int userId, String opPackageName) {
        BUserAccounts userAccounts = getUserAccounts(userId);
        return userAccounts.accounts.toArray(new Account[]{});
    }

    /**
     * Adds an account explicitly with optional per-package visibility settings.
     *
     * @param account             the account to add
     * @param password            the account password
     * @param extras              additional user data
     * @param packageToVisibility map of package names to visibility levels
     * @param userId              the virtual user ID
     * @return true if the account was added, false if it already exists
     */
    @Override
    public boolean addAccountExplicitlyWithVisibility(Account account, String password, Bundle extras, Map packageToVisibility, int userId) {
        BUserAccounts accounts = getUserAccounts(userId);
        return addAccountInternal(accounts, account, password, extras, (Map<String, Integer>) packageToVisibility);
    }

    /**
     * Sets the visibility of an account for a specific package.
     *
     * @param account       the account to configure
     * @param packageName   the package name to set visibility for
     * @param newVisibility the new visibility level
     * @param userId        the virtual user ID
     * @return true if the visibility was set, false if the account was not found
     * @throws NullPointerException if account or packageName is null
     */
    @Override
    public boolean setAccountVisibility(Account account, String packageName, int newVisibility, int userId) {
        Objects.requireNonNull(account, "account cannot be null");
        Objects.requireNonNull(packageName, "packageName cannot be null");

        BUserAccounts userAccounts = getUserAccounts(userId);
        if (userAccounts == null) {
            return false;
        }
        return setAccountVisibility(account, packageName, newVisibility, userAccounts);
    }

    /**
     * Returns the visibility level of an account for a specific package.
     *
     * @param account     the account to query
     * @param packageName the package name to check
     * @param userId      the virtual user ID
     * @return the visibility level constant
     * @throws NullPointerException if account or packageName is null
     */
    @Override
    public int getAccountVisibility(Account account, String packageName, int userId) {
        Objects.requireNonNull(account, "account cannot be null");
        Objects.requireNonNull(packageName, "packageName cannot be null");

        BUserAccounts accounts = getUserAccounts(userId);
        if (AccountManager.PACKAGE_NAME_KEY_LEGACY_VISIBLE.equals(packageName)) {
            int visibility = getAccountVisibilityFromCache(account, packageName, accounts);

            if (AccountManager.VISIBILITY_UNDEFINED != visibility) {
                return visibility;
            } else {
                return AccountManager.VISIBILITY_USER_MANAGED_VISIBLE;
            }
        }

        if (AccountManager.PACKAGE_NAME_KEY_LEGACY_NOT_VISIBLE.equals(packageName)) {
            int visibility = getAccountVisibilityFromCache(account, packageName, accounts);
            if (AccountManager.VISIBILITY_UNDEFINED != visibility) {
                return visibility;
            } else {
                return AccountManager.VISIBILITY_USER_MANAGED_NOT_VISIBLE;
            }
        }
        return resolveAccountVisibility(account, packageName, accounts);
    }

    /**
     * Returns accounts and their visibility for a given package and account type.
     *
     * @param packageName the package name to filter by
     * @param accountType the account type to filter by
     * @param userId      the virtual user ID
     * @return a map of Account to visibility level
     */
    @Override
    public Map getAccountsAndVisibilityForPackage(String packageName, String accountType, int userId) {
        Map<Account, Integer> hashMap = new HashMap<>();
        BUserAccounts userAccounts = getUserAccounts(userId);

        synchronized (userAccounts.lock) {
            for (BAccount account : userAccounts.accounts) {
                if (account.account.type.equals(accountType)) {
                    Integer integer = userAccounts.getVisibility(account.account).get(packageName);
                    if (integer != null) {
                        hashMap.put(account.account, integer);
                    }
                }
            }
        }
        return hashMap;
    }

    /** No-op implementation for account listener registration. */
    @Override
    public void registerAccountListener(String[] accountTypes, String opPackageName, int userId) throws RemoteException { }

    /** No-op implementation for account listener unregistration. */
    @Override
    public void unregisterAccountListener(String[] accountTypes, String opPackageName, int userId) { }

    /**
     * Internal method to add an account with password, extras, and visibility settings.
     *
     * @param accounts            the user accounts to add to
     * @param account             the account to add
     * @param password            the account password
     * @param extras              additional user data bundle
     * @param packageToVisibility per-package visibility settings
     * @return true if added successfully, false if the account already exists
     */
    private boolean addAccountInternal(BUserAccounts accounts, Account account, String password, Bundle extras, Map<String, Integer> packageToVisibility) {
        if (accounts == null) {
            accounts = new BUserAccounts();
        }

        synchronized (accounts.lock) {
            BAccount bAccount = accounts.getAccount(account);
            if (bAccount != null) {
                Slog.d(TAG, "skipping since insertExtra failed for key " + account);
                return false;
            }

            bAccount = accounts.addAccount(account);
            bAccount.password = password;
            if (extras != null) {
                for (String key : extras.keySet()) {
                    final String value = extras.getString(key);
                    bAccount.insertExtra(key, value);
                }
            }

            if (packageToVisibility != null) {
                for (Map.Entry<String, Integer> entry : packageToVisibility.entrySet()) {
                    setAccountVisibility(account, entry.getKey() /* package */, entry.getValue() /* visibility */, accounts);
                }
            }
        }

        saveAllAccounts();
        return true;
    }

    /**
     * Sets account visibility for a package within the given user accounts.
     *
     * @param account       the account to configure
     * @param packageName   the target package
     * @param newVisibility the new visibility level
     * @param accounts      the user accounts context
     * @return true if the visibility was set, false if the account was not found
     */
    private boolean setAccountVisibility(Account account, String packageName, int newVisibility, BUserAccounts accounts) {
        synchronized (accounts.lock) {
            BAccount bAccount = accounts.getAccount(account);
            if (bAccount == null) {
                return false;
            }

            bAccount.visibility.put(packageName, newVisibility);
            return true;
        }
    }

    /**
     * Retrieves accounts from the in-memory cache, optionally filtered by type and visibility.
     *
     * @param userAccounts            the user accounts to search
     * @param accountType             the account type, or null for all types
     * @param callingPackage          the calling package for visibility filtering
     * @param includeManagedNotVisible whether to include user-managed-not-visible accounts
     * @return an array of matching Account objects
     */
    protected Account[] getAccountsFromCache(BUserAccounts userAccounts, String accountType, String callingPackage, boolean includeManagedNotVisible) {
        if (accountType != null) {
            Account[] accounts;
            synchronized (userAccounts.lock) {
                accounts = userAccounts.getAccountsByType(accountType);
            }

            if (accounts == null) {
                return EMPTY_ACCOUNT_ARRAY;
            }
            return filterAccounts(userAccounts, Arrays.copyOf(accounts, accounts.length), callingPackage, includeManagedNotVisible);
        } else {
            int totalLength = 0;
            Account[] accountsArray;

            synchronized (mUserAccountsMap) {
                for (BUserAccounts bUserAccounts : mUserAccountsMap.values()) {
                    totalLength += bUserAccounts.toAccounts().length;
                }

                if (totalLength == 0) {
                    return EMPTY_ACCOUNT_ARRAY;
                }

                accountsArray = new Account[totalLength];
                totalLength = 0;
                for (BUserAccounts bUserAccounts : mUserAccountsMap.values()) {
                    Account[] accountsOfType = bUserAccounts.toAccounts();

                    System.arraycopy(accountsOfType, 0, accountsArray, totalLength, accountsOfType.length);
                    totalLength += accountsOfType.length;
                }
            }
            return filterAccounts(userAccounts, accountsArray, callingPackage, includeManagedNotVisible);
        }
    }

    /**
     * Filters an array of accounts based on their visibility for the calling package.
     *
     * @param accounts                 the user accounts context
     * @param unfiltered               the unfiltered account array
     * @param callingPackage           the calling package for visibility checks
     * @param includeManagedNotVisible whether to include user-managed-not-visible accounts
     * @return the filtered array of Account objects
     */
    @NonNull
    private Account[] filterAccounts(BUserAccounts accounts, Account[] unfiltered, String callingPackage, boolean includeManagedNotVisible) {
        Map<Account, Integer> firstPass = new LinkedHashMap<>();
        for (Account account : unfiltered) {
            int visibility = resolveAccountVisibility(account, callingPackage, accounts);
            if ((visibility == AccountManager.VISIBILITY_VISIBLE || visibility == AccountManager.VISIBILITY_USER_MANAGED_VISIBLE)
                    || (includeManagedNotVisible && (visibility == AccountManager.VISIBILITY_USER_MANAGED_NOT_VISIBLE))) {
                firstPass.put(account, visibility);
            }
        }
        return firstPass.keySet().toArray(new Account[]{});
    }

    /**
     * Method which handles default values for Account visibility.
     *
     * @param account     The account to check visibility.
     * @param packageName Package name to check visibility
     * @param accounts    UserAccount that currently hosts the account and application
     * @return Visibility value, the method never returns AccountManager.VISIBILITY_UNDEFINED
     */
    private Integer resolveAccountVisibility(Account account, @NonNull String packageName, BUserAccounts accounts) {
        if (accounts == null) {
            return AccountManager.VISIBILITY_NOT_VISIBLE;
        }

        BAccount bAccount = accounts.getAccount(account);
        if (bAccount == null) {
            return AccountManager.VISIBILITY_NOT_VISIBLE;
        }

        // Return stored value if it was set.
        int visibility = getAccountVisibilityFromCache(account, packageName, accounts);
        if (AccountManager.VISIBILITY_UNDEFINED != visibility) {
            return visibility;
        }
        return AccountManager.VISIBILITY_NOT_VISIBLE;
    }

    /**
     * Method returns visibility for given account and package name.
     *
     * @param account     The account to check visibility.
     * @param packageName Package name to check visibility.
     * @param accounts    UserAccount that currently hosts the account and application
     * @return Visibility value, AccountManager.VISIBILITY_UNDEFINED if no value was stored.
     */
    private int getAccountVisibilityFromCache(Account account, String packageName, BUserAccounts accounts) {
        synchronized (accounts.lock) {
            Map<String, Integer> accountVisibility = getPackagesAndVisibilityForAccountLocked(account, accounts);
            Integer visibility = accountVisibility.get(packageName);
            return visibility != null ? visibility : AccountManager.VISIBILITY_UNDEFINED;
        }
    }

    /**
     * Returns the package-to-visibility map for the given account within locked accounts.
     *
     * @param account  the account to query
     * @param accounts the user accounts context
     * @return the visibility map for the account
     */
    private @NonNull Map<String, Integer> getPackagesAndVisibilityForAccountLocked(Account account, BUserAccounts accounts) {
        return accounts.getVisibility(account);
    }

    /**
     * Handles the result of a get-accounts query, determining whether to launch
     * a choose-account activity or return the result directly.
     *
     * @param response      the callback for the result
     * @param accounts      the matching accounts
     * @param callingPackage the calling package name
     * @param userId        the virtual user ID
     */
    private void handleGetAccountsResult(IAccountManagerResponse response, Account[] accounts, String callingPackage, int userId) {
        if (needToStartChooseAccountActivity(accounts, callingPackage, userId)) {
            return;
        }

        if (accounts.length == 1) {
            Bundle bundle = new Bundle();
            bundle.putString(AccountManager.KEY_ACCOUNT_NAME, accounts[0].name);
            bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, accounts[0].type);

            onResult(response, bundle);
            return;
        }
        onResult(response, new Bundle());
    }

    /**
     * Determines whether a choose-account activity should be launched based on
     * the number of accounts and their visibility.
     *
     * @param accounts      the array of matching accounts
     * @param callingPackage the calling package
     * @param userId        the virtual user ID
     * @return true if a chooser activity should be started
     */
    private boolean needToStartChooseAccountActivity(Account[] accounts, String callingPackage, int userId) {
        if (accounts.length < 1) {
            return false;
        }

        if (accounts.length > 1) {
            return true;
        }

        Account account = accounts[0];
        BUserAccounts userAccounts = getUserAccounts(userId);
        int visibility = resolveAccountVisibility(account, callingPackage, userAccounts);
        return visibility == AccountManager.VISIBILITY_USER_MANAGED_NOT_VISIBLE;
    }

    /**
     * Reads user data from the persisted accounts for the given key.
     *
     * @param accounts the user accounts context
     * @param account  the account to query
     * @param key      the user data key
     * @return the value, or null if not found
     */
    private String readUserDataInternal(BUserAccounts accounts, Account account, String key) {
        if (accounts == null) {
            return null;
        }

        synchronized (accounts.lock) {
            Map<String, String> accountUserData = accounts.getAccountUserData(account);
            return accountUserData.get(key);
        }
    }

    /**
     * Reads the password for an account from the persisted data.
     *
     * @param accounts the user accounts context
     * @param account  the account to query
     * @return the password string, or null if not found
     */
    public String readPasswordInternal(BUserAccounts accounts, Account account) {
        if (accounts == null) {
            return null;
        }

        synchronized (accounts.lock) {
            BAccount bAccount = accounts.getAccount(account);
            if (bAccount == null) {
                return null;
            }
            return bAccount.password;
        }
    }

    /**
     * Returns the BUserAccounts for the specified user ID, creating one if it does
     * not yet exist.
     *
     * @param userId the virtual user ID
     * @return the BUserAccounts instance for the user
     */
    public BUserAccounts getUserAccounts(int userId) {
        synchronized (mUserAccountsMap) {
            BUserAccounts bUserAccounts = mUserAccountsMap.get(userId);
            if (bUserAccounts == null) {
                bUserAccounts = new BUserAccounts();
                mUserAccountsMap.put(userId, bUserAccounts);
            }
            return mUserAccountsMap.get(userId);
        }
    }

    /**
     * Checks whether an account with the given name and type exists for the caller's user.
     *
     * @param accountName the account name
     * @param accountType the account type
     * @param userId      the virtual user ID
     * @return true if the account exists
     */
    private boolean isAccountPresentForCaller(String accountName, String accountType, int userId) {
        BUserAccounts userAccounts = getUserAccounts(userId);
        if (userAccounts != null) {
            BAccount account = userAccounts.getAccount(new Account(accountName, accountType));
            return account != null;
        }
        return false;
    }

    /**
     * Removes an account from the given user accounts and persists the change.
     *
     * @param accounts the user accounts to modify
     * @param account  the account to remove
     * @return true if the account was found and removed
     */
    private boolean removeAccountInternal(BUserAccounts accounts, Account account) {
        synchronized (accounts.lock) {
            boolean del = accounts.delAccount(account);
            if (del) {
                saveAllAccounts();
            }
            return del;
        }
    }

    /**
     * Session implementation for removing an account through the authenticator.
     */
    private class RemoveAccountSession extends Session {
        final Account mAccount;

        /**
         * Constructs a remove-account session.
         *
         * @param accounts            the user accounts context
         * @param response            the callback for the result
         * @param account             the account to remove
         * @param expectActivityLaunch whether an activity launch is expected
         */
        public RemoveAccountSession(BUserAccounts accounts, IAccountManagerResponse response, Account account, boolean expectActivityLaunch) {
            super(accounts, response, account.type, expectActivityLaunch, true /* stripAuthTokenFromResult */, account.name,
                    false /* authDetailsRequired */);
            this.mAccount = account;
        }

        @Override
        protected String toDebugString(long now) {
            return super.toDebugString(now) + ", removeAccount" + ", account " + mAccount;
        }

        @Override
        public void run() throws RemoteException {
            mAuthenticator.getAccountRemovalAllowed(this, mAccount);
        }

        @Override
        public void onResult(Bundle result) {
            if (result != null && result.containsKey(AccountManager.KEY_BOOLEAN_RESULT) && !result.containsKey(AccountManager.KEY_INTENT)) {
                final boolean removalAllowed = result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT);
                if (removalAllowed) {
                    removeAccountInternal(mAccounts, mAccount);
                }

                IAccountManagerResponse response = getResponseAndClose();
                if (response != null) {
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log.v(TAG, getClass().getSimpleName() + " calling onResult() on response " + response);
                    }

                    try {
                        response.onResult(result);
                    } catch (RemoteException e) {
                        Slog.e(TAG, "Error calling onResult()", e);
                    }
                }
            }
            super.onResult(result);
        }
    }

    /**
     * Session implementation for querying accounts by type and features.
     * Iterates through accounts of the given type, checking each for required
     * features via the authenticator.
     */
    private class GetAccountsByTypeAndFeatureSession extends Session {
        private final String[] mFeatures;
        private volatile Account[] mAccountsOfType = null;
        private volatile ArrayList<Account> mAccountsWithFeatures = null;
        private volatile int mCurrentAccount = 0;
        private final String mPackageName;
        private final boolean mIncludeManagedNotVisible;

        /**
         * Constructs a get-accounts-by-type-and-feature session.
         *
         * @param accounts                the user accounts context
         * @param response                the callback for results
         * @param type                    the account type to query
         * @param features                required features to check
         * @param userId                  the virtual user ID
         * @param packageName             the calling package for visibility filtering
         * @param includeManagedNotVisible whether to include managed-not-visible accounts
         */
        public GetAccountsByTypeAndFeatureSession(BUserAccounts accounts, IAccountManagerResponse response, String type, String[] features, int userId,
                                                  String packageName, boolean includeManagedNotVisible) {
            super(accounts, response, type, false /* expectActivityLaunch */, true /* stripAuthTokenFromResult */,
                    null /* accountName */, false /* authDetailsRequired */);
            this.mFeatures = features;
            this.mPackageName = packageName;
            this.mIncludeManagedNotVisible = includeManagedNotVisible;
        }

        @Override
        public void run() throws RemoteException {
            mAccountsOfType = getAccountsFromCache(mAccounts, mAccountType, mPackageName, mIncludeManagedNotVisible);
            mAccountsWithFeatures = new ArrayList<>(mAccountsOfType.length);
            mCurrentAccount = 0;
            checkAccount();
        }

        /**
         * Checks the current account for required features and advances to the next.
         */
        public void checkAccount() {
            if (mCurrentAccount >= mAccountsOfType.length) {
                sendResult();
                return;
            }

            final IAccountAuthenticator accountAuthenticator = mAuthenticator;
            if (accountAuthenticator == null) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "checkAccount: aborting session since we are no longer" + " connected to the authenticator, " + toDebugString());
                }
                return;
            }

            try {
                accountAuthenticator.hasFeatures(this, mAccountsOfType[mCurrentAccount], mFeatures);
            } catch (RemoteException e) {
                onError(AccountManager.ERROR_CODE_REMOTE_EXCEPTION, "remote exception");
            }
        }

        @Override
        public void onResult(Bundle result) {
            mNumResults++;
            if (result == null) {
                onError(AccountManager.ERROR_CODE_INVALID_RESPONSE, "null bundle");
                return;
            }

            if (result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT, false)) {
                mAccountsWithFeatures.add(mAccountsOfType[mCurrentAccount]);
            }

            mCurrentAccount++;
            checkAccount();
        }

        /**
         * Sends the final result containing all accounts that matched the requested features.
         */
        public void sendResult() {
            IAccountManagerResponse response = getResponseAndClose();
            if (response != null) {
                try {
                    Account[] accounts = new Account[mAccountsWithFeatures.size()];
                    for (int i = 0; i < accounts.length; i++) {
                        accounts[i] = mAccountsWithFeatures.get(i);
                    }

                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log.v(TAG, getClass().getSimpleName() + " calling onResult() on response " + response);
                    }

                    Bundle result = new Bundle();
                    result.putParcelableArray(AccountManager.KEY_ACCOUNTS, accounts);
                    response.onResult(result);
                } catch (RemoteException e) {
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log.v(TAG, "failure while notifying response", e);
                    }
                }
            }
        }

        @Override
        protected String toDebugString(long now) {
            return super.toDebugString(now) + ", getAccountsByTypeAndFeatures" + ", " + (mFeatures != null ? TextUtils.join(",", mFeatures) : null);
        }
    }

    /**
     * Holds information about a registered account authenticator, including
     * its description and the service info of the declaring service.
     */
    private static final class AuthenticatorInfo {
        final AuthenticatorDescription desc;
        final ServiceInfo serviceInfo;

        /**
         * Constructs an AuthenticatorInfo.
         *
         * @param desc the authenticator description
         * @param info the service info of the authenticator service
         */
        AuthenticatorInfo(AuthenticatorDescription desc, ServiceInfo info) {
            this.desc = desc;
            this.serviceInfo = info;
        }
    }

    /**
     * Simple cache holding discovered account authenticators keyed by account type.
     */
    private static final class AuthenticatorCache {
        final Map<String, AuthenticatorInfo> authenticators = new HashMap<>();
    }

    /**
     * Parses an AuthenticatorDescription from an XML resource attribute set.
     *
     * @param resources    the resources to obtain attributes from
     * @param packageName  the declaring package name
     * @param attributeSet the XML attribute set
     * @return the parsed AuthenticatorDescription, or null if accountType is empty
     */
    private static AuthenticatorDescription parseAuthenticatorDescription(Resources resources, String packageName, AttributeSet attributeSet) {
        TypedArray array = resources.obtainAttributes(attributeSet, ArrayUtils.toInt(black.com.android.internal.R.styleable.AccountAuthenticator.get()));
        try {
            String accountType = array.getString(black.com.android.internal.R.styleable.AccountAuthenticator_accountType.get());
            int label = array.getResourceId(black.com.android.internal.R.styleable.AccountAuthenticator_label.get(), 0);
            int icon = array.getResourceId(black.com.android.internal.R.styleable.AccountAuthenticator_icon.get(), 0);
            int smallIcon = array.getResourceId(black.com.android.internal.R.styleable.AccountAuthenticator_smallIcon.get(), 0);
            int accountPreferences = array.getResourceId(black.com.android.internal.R.styleable.AccountAuthenticator_accountPreferences.get(), 0);
            boolean customTokens = array.getBoolean(black.com.android.internal.R.styleable.AccountAuthenticator_customTokens.get(), false);

            if (TextUtils.isEmpty(accountType)) {
                return null;
            }
            return new AuthenticatorDescription(accountType, packageName, label, icon, smallIcon, accountPreferences, customTokens);
        } finally {
            array.recycle();
        }
    }

    /**
     * Loads or reloads the authenticator cache by scanning for authenticator services.
     *
     * @param packageName optional package name to limit the scan to, or null for all
     */
    public void loadAuthenticatorCache(String packageName) {
        mAuthenticatorCache.authenticators.clear();
        Intent intent = new Intent(AccountManager.ACTION_AUTHENTICATOR_INTENT);
        if (packageName != null) {
            intent.setPackage(packageName);
        }

        generateServicesMap(mPms.queryIntentServices(intent, PackageManager.GET_META_DATA, BUserHandle.USER_ALL), mAuthenticatorCache.authenticators,
                new RegisteredServicesParser());
    }

    /**
     * Generates the authenticator map from a list of resolved service info entries.
     *
     * @param services    the list of ResolveInfo for authenticator services
     * @param map         the map to populate (type -> AuthenticatorInfo)
     * @param accountParser the parser for reading authenticator metadata
     */
    private void generateServicesMap(List<ResolveInfo> services, Map<String, AuthenticatorInfo> map, RegisteredServicesParser accountParser) {
        for (ResolveInfo info : services) {
            XmlResourceParser parser = accountParser.getParser(mContext, info.serviceInfo, AccountManager.AUTHENTICATOR_META_DATA_NAME);
            if (parser != null) {
                try {
                    AttributeSet attributeSet = Xml.asAttributeSet(parser);
                    if (AccountManager.AUTHENTICATOR_ATTRIBUTES_NAME.equals(parser.getName())) {
                        AuthenticatorDescription desc = parseAuthenticatorDescription(accountParser.getResources(mContext, info.serviceInfo.applicationInfo),
                                info.serviceInfo.packageName, attributeSet);
                        if (desc != null) {
                            map.put(desc.type, new AuthenticatorInfo(desc, info.serviceInfo));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Abstract base class for authenticator sessions. Manages the lifecycle of
     * binding to an authenticator service, handling responses, timeouts, and cleanup.
     * Extends IAccountAuthenticatorResponse.Stub to serve as the authenticator callback.
     */
    private abstract class Session extends IAccountAuthenticatorResponse.Stub implements IBinder.DeathRecipient, ServiceConnection {
        IAccountManagerResponse mResponse;
        final String mAccountType;
        final boolean mExpectActivityLaunch;
        final long mCreationTime;
        final String mAccountName;
        final boolean mAuthDetailsRequired;
        final boolean mUpdateLastAuthenticatedTime;

        public int mNumResults = 0;
        private int mNumRequestContinued = 0;
        private int mNumErrors = 0;

        IAccountAuthenticator mAuthenticator = null;

        private final boolean mStripAuthTokenFromResult;
        protected final BUserAccounts mAccounts;

        /**
         * Constructs a session without updating last authenticated time.
         */
        public Session(BUserAccounts accounts, IAccountManagerResponse response, String accountType, boolean expectActivityLaunch,
                       boolean stripAuthTokenFromResult, String accountName, boolean authDetailsRequired) {
            this(accounts, response, accountType, expectActivityLaunch, stripAuthTokenFromResult, accountName,
                    authDetailsRequired, false /* updateLastAuthenticatedTime */);
        }

        /**
         * Constructs a full session with all parameters.
         */
        public Session(BUserAccounts accounts, IAccountManagerResponse response, String accountType, boolean expectActivityLaunch,
                       boolean stripAuthTokenFromResult, String accountName, boolean authDetailsRequired, boolean updateLastAuthenticatedTime) {
            super();
            if (accountType == null) {
                throw new IllegalArgumentException("accountType is null");
            }

            mAccounts = accounts;
            mStripAuthTokenFromResult = stripAuthTokenFromResult;
            mResponse = response;
            mAccountType = accountType;
            mExpectActivityLaunch = expectActivityLaunch;
            mCreationTime = SystemClock.elapsedRealtime();
            mAccountName = accountName;
            mAuthDetailsRequired = authDetailsRequired;
            mUpdateLastAuthenticatedTime = updateLastAuthenticatedTime;

            synchronized (mSessions) {
                mSessions.put(toString(), this);
            }

            if (response != null) {
                try {
                    response.asBinder().linkToDeath(this, 0 /* flags */);
                } catch (RemoteException e) {
                    mResponse = null;
                    binderDied();
                }
            }
        }

        /**
         * Returns the response and closes the session. Returns null if already closed.
         *
         * @return the IAccountManagerResponse, or null
         */
        IAccountManagerResponse getResponseAndClose() {
            if (mResponse == null) {
                return null;
            }
            IAccountManagerResponse response = mResponse;
            close();
            return response;
        }

        /**
         * Checks Intents, supplied via KEY_INTENT, to make sure that they don't violate our
         * security policy.
         *
         * @param authUid the UID of the authenticator
         * @param intent  the intent to validate
         * @return true if the intent resolves to a valid activity
         */
        protected boolean checkKeyIntent(int authUid, Intent intent) {
            if (intent.getClipData() == null) {
                intent.setClipData(ClipData.newPlainText(null, null));
            }
            intent.setFlags(intent.getFlags() & ~(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION));
            long bid = Binder.clearCallingIdentity();
            try {
                ResolveInfo resolveInfo = mPms.resolveActivity(intent, 0, null, mAccounts.userId);
                return resolveInfo != null;
            } finally {
                Binder.restoreCallingIdentity(bid);
            }
        }

        /**
         * Closes this session, unbinding from the authenticator and cleaning up resources.
         */
        private void close() {
            synchronized (mSessions) {
                if (mSessions.remove(toString()) == null) {
                    return;
                }
            }
            if (mResponse != null) {
                mResponse.asBinder().unlinkToDeath(this, 0 /* flags */);
                mResponse = null;
            }
            cancelTimeout();
            unbind();
        }

        /** Called when the response binder dies. Closes the session. */
        @Override
        public void binderDied() {
            mResponse = null;
            close();
        }

        /**
         * Returns a debug string representation of this session.
         *
         * @return debug string
         */
        protected String toDebugString() {
            return toDebugString(SystemClock.elapsedRealtime());
        }

        /**
         * Returns a debug string with the given current time.
         *
         * @param now the current elapsed realtime in millis
         * @return debug string with session stats and lifetime
         */
        protected String toDebugString(long now) {
            return "Session: expectLaunch " + mExpectActivityLaunch + ", connected " + (mAuthenticator != null)
                    + ", stats (" + mNumResults + "/" + mNumRequestContinued + "/" + mNumErrors + ")" + ", lifetime " + ((now - mCreationTime) / 1000.0);
        }

        /**
         * Initiates binding to the authenticator service for this session's account type.
         */
        void bind() {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "initiating bind to authenticator type " + mAccountType);
            }

            if (!bindToAuthenticator(mAccountType)) {
                Log.d(TAG, "bind attempt failed for " + toDebugString());
                onError(AccountManager.ERROR_CODE_REMOTE_EXCEPTION, "bind failure");
            }
        }

        /**
         * Unbinds from the authenticator service if currently bound.
         */
        private void unbind() {
            if (mAuthenticator != null) {
                mAuthenticator = null;
                mContext.unbindService(this);
            }
        }

        /**
         * Cancels any pending timeout messages for this session.
         */
        public void cancelTimeout() {
            mHandler.removeMessages(MESSAGE_TIMED_OUT, this);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mAuthenticator = IAccountAuthenticator.Stub.asInterface(service);
            try {
                run();
            } catch (RemoteException e) {
                onError(AccountManager.ERROR_CODE_REMOTE_EXCEPTION, "remote exception");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mAuthenticator = null;
            IAccountManagerResponse response = getResponseAndClose();

            if (response != null) {
                try {
                    response.onError(AccountManager.ERROR_CODE_REMOTE_EXCEPTION, "disconnected");
                } catch (RemoteException e) {
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log.v(TAG, "Session.onServiceDisconnected: " + "caught RemoteException while responding", e);
                    }
                }
            }
        }

        /**
         * Subclasses implement this to invoke the appropriate authenticator method.
         *
         * @throws RemoteException if the authenticator is unavailable
         */
        public abstract void run() throws RemoteException;

        /**
         * Handles the result bundle from the authenticator, performing security checks
         * on intents, updating authentication times, and forwarding to the caller.
         *
         * @param result the result bundle from the authenticator
         */
        @Override
        public void onResult(Bundle result) {
            mNumResults++;
            Intent intent = null;

            if (result != null) {
                boolean isSuccessfulConfirmCreds = result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
                boolean isSuccessfulUpdateCredsOrAddAccount = result.containsKey(AccountManager.KEY_ACCOUNT_NAME)
                        && result.containsKey(AccountManager.KEY_ACCOUNT_TYPE);
                boolean needUpdate = mUpdateLastAuthenticatedTime && (isSuccessfulConfirmCreds || isSuccessfulUpdateCredsOrAddAccount);
                if (needUpdate || mAuthDetailsRequired) {
                    boolean accountPresent = isAccountPresentForCaller(mAccountName, mAccountType, mAccounts.userId);
                    if (needUpdate && accountPresent) {
                        updateLastAuthenticatedTime(mAccounts, new Account(mAccountName, mAccountType));
                    }

                    if (mAuthDetailsRequired) {
                        long lastAuthenticatedTime = -1;
                        if (accountPresent) {
                            lastAuthenticatedTime = mAccounts.findAccountLastAuthenticatedTime(new Account(mAccountName, mAccountType));
                        }

                        result.putLong(AccountManager.KEY_LAST_AUTHENTICATED_TIME, lastAuthenticatedTime);
                    }
                }
            }

            if (result != null && (intent = result.getParcelable(AccountManager.KEY_INTENT)) != null) {
                if (!checkKeyIntent(Binder.getCallingUid(), intent)) {
                    onError(AccountManager.ERROR_CODE_INVALID_RESPONSE, "invalid intent in bundle returned");
                    return;
                }
            }

            IAccountManagerResponse response;
            if (mExpectActivityLaunch && result != null && result.containsKey(AccountManager.KEY_INTENT)) {
                response = mResponse;
            } else {
                response = getResponseAndClose();
            }

            if (response != null) {
                try {
                    if (result == null) {
                        if (Log.isLoggable(TAG, Log.VERBOSE)) {
                            Log.v(TAG, getClass().getSimpleName() + " calling onError() on response " + response);
                        }

                        response.onError(AccountManager.ERROR_CODE_INVALID_RESPONSE, "null bundle returned");
                    } else {
                        if (mStripAuthTokenFromResult) {
                            result.remove(AccountManager.KEY_AUTHTOKEN);
                        }

                        if (Log.isLoggable(TAG, Log.VERBOSE)) {
                            Log.v(TAG, getClass().getSimpleName() + " calling onResult() on response " + response);
                        }

                        if ((result.getInt(AccountManager.KEY_ERROR_CODE, -1) > 0) && (intent == null)) {
                            response.onError(result.getInt(AccountManager.KEY_ERROR_CODE), result.getString(AccountManager.KEY_ERROR_MESSAGE));
                        } else {
                            response.onResult(result);
                        }
                    }
                } catch (RemoteException e) {
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log.v(TAG, "failure while notifying response", e);
                    }
                }
            }
        }

        @Override
        public void onRequestContinued() {
            mNumRequestContinued++;
        }

        @Override
        public void onError(int errorCode, String errorMessage) {
            mNumErrors++;
            IAccountManagerResponse response = getResponseAndClose();

            if (response != null) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, getClass().getSimpleName() + " calling onError() on response " + response);
                }

                try {
                    response.onError(errorCode, errorMessage);
                } catch (RemoteException e) {
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log.v(TAG, "Session.onError: caught RemoteException while responding", e);
                    }
                }
            } else {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "Session.onError: already closed");
                }
            }
        }

        /**
         * Finds the component name for the authenticator and initiates a bind.
         *
         * @param authenticatorType the account type of the authenticator
         * @return true if binding was initiated successfully, false otherwise
         */
        private boolean bindToAuthenticator(String authenticatorType) {
            AuthenticatorInfo authenticatorInfo = mAuthenticatorCache.authenticators.get(authenticatorType);
            if (authenticatorInfo == null) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "there is no authenticator for " + authenticatorType + ", bailing out");
                }
                return false;
            }

            Intent intent = new Intent();
            intent.setAction(AccountManager.ACTION_AUTHENTICATOR_INTENT);

            ComponentName componentName = new ComponentName(authenticatorInfo.serviceInfo.packageName, authenticatorInfo.serviceInfo.name);
            intent.setComponent(componentName);
            intent.putExtra("_B_|_UserId", mAccounts.userId);

            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "performing bindService to " + componentName);
            }

            int flags = Context.BIND_AUTO_CREATE;
            if (!mContext.bindService(intent, this, flags)) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "bindService to " + componentName + " failed");
                }
                return false;
            }
            return true;
        }
    }

    /**
     * Sends a result bundle to the given response callback.
     *
     * @param response the response callback
     * @param result   the result bundle
     */
    private void onResult(IAccountManagerResponse response, Bundle result) {
        if (result == null) {
            Log.e(TAG, "the result is unexpectedly null", new Exception());
        }

        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, getClass().getSimpleName() + " calling onResult() on response " + response);
        }

        try {
            response.onResult(result);
        } catch (RemoteException e) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "failure while notifying response", e);
            }
        }
    }

    /**
     * Updates the last authenticated time for the given account.
     *
     * @param userAccounts the user accounts context
     * @param account      the account to update
     * @return always returns true
     */
    private boolean updateLastAuthenticatedTime(BUserAccounts userAccounts, Account account) {
        userAccounts.updateLastAuthenticatedTime(account);
        return true;
    }

    /**
     * Returns the package name of the calling process by looking up its ProcessRecord.
     *
     * @return the caller's package name
     * @throws IllegalArgumentException if no ProcessRecord is found for the calling PID
     */
    private String getCallingPackageName() {
        int callingPid = Binder.getCallingPid();
        ProcessRecord processByPid = BProcessManagerService.get().findProcessByPid(callingPid);

        if (processByPid == null) {
            throw new IllegalArgumentException("ProcessRecord is null, PID: " + callingPid);
        }
        return processByPid.getPackageName();
    }
}
