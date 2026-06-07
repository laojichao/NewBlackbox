package com.vcore.fake.provider;

import android.content.Context;
import android.content.pm.ProviderInfo;
import android.net.Uri;

import java.io.File;
import java.util.List;

import com.vcore.BlackBoxCore;
import com.vcore.app.BActivityThread;
import com.vcore.utils.compat.BuildCompat;

/**
 * Utility class for handling file URI conversions within the virtual environment.
 *
 * <p>Provides methods to convert between content URIs issued by {@link FileProvider} and
 * their corresponding file paths. On Android N (API 24) and above, content URIs are
 * converted to file paths and then re-issued through the virtual storage manager's
 * {@code getUriForFile} to ensure proper file sharing within the virtual space.</p>
 *
 * @see FileProvider
 */
public class FileProviderHandler {

    /**
     * Converts a content URI from a {@link FileProvider} into a virtual storage URI.
     *
     * <p>On Android N and above, this resolves the URI to a {@link File} and then generates
     * a new URI through the virtual storage manager. On earlier API levels, the original
     * URI is returned unchanged.</p>
     *
     * @param context the current context
     * @param uri     the content URI to convert
     * @return the converted URI, or {@code null} if the URI could not be resolved to a file
     */
    public static Uri convertFileUri(Context context, Uri uri) {
        if (BuildCompat.isN()) {
            File file = convertFile(context, uri);
            if (file == null) {
                return null;
            }
            return BlackBoxCore.getBStorageManager().getUriForFile(file.getAbsolutePath());
        }
        return uri;
    }

    /**
     * Converts a content URI to a {@link File} by checking all registered FileProvider
     * authorities in the current virtual process.
     *
     * @param context the current context
     * @param uri     the content URI to convert
     * @return the corresponding File if found and exists, or {@code null}
     */
    public static File convertFile(Context context, Uri uri) {
        List<ProviderInfo> providers = BActivityThread.getProviders();
        for (ProviderInfo provider : providers) {
            try {
                File fileForUri = FileProvider.getFileForUri(context, provider.authority, uri);
                if (fileForUri != null && fileForUri.exists()) {
                    return fileForUri;
                }
            } catch (Exception ignored) { }
        }
        return null;
    }
}
