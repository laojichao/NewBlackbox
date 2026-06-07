package black.android.content;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.content.ContentProviderNative} class.
 * Provides access to the static asInterface method for obtaining an IContentProvider
 * proxy from an IBinder.
 */
public class ContentProviderNative {
    public static final Reflector REF = Reflector.on("android.content.ContentProviderNative");

    /**
     * Converts an {@link IBinder} into an {@link IInterface} proxy for a content provider.
     */
    public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
}
