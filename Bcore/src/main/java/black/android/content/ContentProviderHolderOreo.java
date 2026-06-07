package black.android.content;

import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.ContentProviderHolder} class on Android O (Oreo, API 26+).
 * Provides access to the IContentProvider binder interface stored in a ContentProviderHolder.
 */
public class ContentProviderHolderOreo {
    public static final Reflector REF = Reflector.on("android.app.ContentProviderHolder");

    /** The IContentProvider binder interface stored in this holder. */
    public static Reflector.FieldWrapper<IInterface> provider = REF.field("provider");
}
