package com.vcore.utils.provider;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.vcore.BlackBoxCore;
import com.vcore.utils.compat.ContentProviderCompat;

/**
 * Utility class for making IPC calls to Android content providers. Provides both safe
 * (exception-swallowing) and checked variants for invoking content provider methods.
 * Delegates to {@link ContentProviderCompat} for version-compatible provider access
 * with retry logic.
 */
public class ProviderCall {
    /**
     * Safely calls a content provider method, returning {@code null} instead of throwing
     * if the provider cannot be reached. Uses the application context and a default retry
     * count of 5.
     *
     * @param authority  the content provider authority string
     * @param methodName the provider method to invoke
     * @param arg        an optional string argument; may be {@code null}
     * @param bundle     optional bundle of additional arguments; may be {@code null}
     * @return the {@link Bundle} result from the provider, or {@code null} if the call fails
     */
    public static Bundle callSafely(String authority, String methodName, String arg, Bundle bundle) {
        try {
            return call(authority, BlackBoxCore.getContext(), methodName, arg, bundle, 5);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Calls a content provider method with the specified retry count. Constructs the
     * provider URI from the authority and delegates to {@link ContentProviderCompat#call}.
     *
     * @param authority   the content provider authority string
     * @param context     the context used to access the content resolver
     * @param method      the provider method to invoke
     * @param arg         an optional string argument; may be {@code null}
     * @param bundle      optional bundle of additional arguments; may be {@code null}
     * @param retryCount  the maximum number of retry attempts when acquiring the provider client
     * @return the {@link Bundle} result from the provider
     * @throws IllegalAccessException if the content provider could not be reached after retries
     */
    public static Bundle call(String authority, Context context, String method, String arg, Bundle bundle, int retryCount) throws IllegalAccessException {
        Uri uri = Uri.parse("content://" + authority);
        return ContentProviderCompat.call(context, uri, method, arg, bundle, retryCount);
    }
}
