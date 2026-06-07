package black.android.content;

import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.content.ContentProviderClient} class.
 * Provides access to the underlying IContentProvider binder interface that
 * ContentProviderClient delegates to.
 */
public class ContentProviderClient {
    public static final Reflector REF = Reflector.on("android.content.ContentProviderClient");

    /** The IContentProvider binder interface for the underlying content provider. */
    public static Reflector.FieldWrapper<IInterface> mContentProvider = REF.field("mContentProvider");
}
