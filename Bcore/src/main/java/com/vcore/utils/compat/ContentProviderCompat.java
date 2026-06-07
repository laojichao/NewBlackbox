package com.vcore.utils.compat;

import android.content.ContentProviderClient;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemClock;

/**
 * Compatibility wrapper for Android's {@code ContentProvider} call operations.
 * Provides retry-capable content provider access with proper client lifecycle management.
 * On API 17+ (Jelly Bean MR1), uses {@link ContentProviderClient#call(String, String, Bundle)}
 * for more reliable provider interactions; on earlier versions, falls back to the
 * {@code ContentResolver.call()} method.
 */
public class ContentProviderCompat {
    /**
     * Calls a content provider method with automatic retry logic for client acquisition.
     * On API 17+, uses an acquired {@link ContentProviderClient}; on older versions,
     * delegates directly to {@code ContentResolver.call()}.
     *
     * @param context    the context used to access the content resolver
     * @param uri        the URI of the content provider to call
     * @param method     the provider method name to invoke
     * @param arg        an optional string argument; may be {@code null}
     * @param extras     optional bundle of additional arguments; may be {@code null}
     * @param retryCount the maximum number of retry attempts when acquiring the provider client
     * @return the {@link Bundle} result returned by the content provider
     * @throws IllegalAccessException if the content provider client could not be acquired
     *                                after all retries, or if a {@link RemoteException} occurs
     */
    public static Bundle call(Context context, Uri uri, String method, String arg, Bundle extras, int retryCount) throws IllegalAccessException {
        if (VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return context.getContentResolver().call(uri, method, arg, extras);
        }

        ContentProviderClient client = acquireContentProviderClientRetry(context, uri, retryCount);
        try {
            if (client == null) {
                throw new IllegalAccessException();
            }
            return client.call(method, arg, extras);
        } catch (RemoteException e) {
            throw new IllegalAccessException(e.getMessage());
        } finally {
            releaseQuietly(client);
        }
    }

    /**
     * Attempts to acquire a content provider client for the given URI. On Jelly Bean (API 16+),
     * uses the unstable acquisition method which is more resilient to provider crashes.
     *
     * @param context the context used to access the content resolver
     * @param uri     the URI of the content provider
     * @return the acquired {@link ContentProviderClient}, or {@code null} if acquisition failed
     *         (e.g., due to a {@link SecurityException})
     */
    private static ContentProviderClient acquireContentProviderClient(Context context, Uri uri) {
        try {
            if (VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                return context.getContentResolver().acquireUnstableContentProviderClient(uri);
            }
            return context.getContentResolver().acquireContentProviderClient(uri);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Attempts to acquire a content provider client with retry logic. If the initial
     * acquisition fails, retries up to {@code retryCount} times with a 400ms delay
     * between attempts to allow the provider to become available.
     *
     * @param context    the context used to access the content resolver
     * @param uri        the URI of the content provider
     * @param retryCount the maximum number of retry attempts
     * @return the acquired {@link ContentProviderClient}, or {@code null} if all retries
     *         are exhausted without success
     */
    public static ContentProviderClient acquireContentProviderClientRetry(Context context, Uri uri, int retryCount) {
        ContentProviderClient client = acquireContentProviderClient(context, uri);
        if (client == null) {
            int retry = 0;
            while (retry < retryCount && client == null) {
                SystemClock.sleep(400);
                retry++;
                client = acquireContentProviderClient(context, uri);
            }
        }
        return client;
    }

    /**
     * Releases a {@link ContentProviderClient} silently, using the appropriate API method
     * based on the Android version. On Nougat (API 24+) uses {@code close()}, on earlier
     * versions uses {@code release()}. Any exception during release is silently ignored.
     *
     * @param client the content provider client to release; may be {@code null}
     */
    private static void releaseQuietly(ContentProviderClient client) {
        if (client != null) {
            try {
                if (VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    client.close();
                } else {
                    client.release();
                }
            } catch (Exception ignored) { }
        }
    }
}
