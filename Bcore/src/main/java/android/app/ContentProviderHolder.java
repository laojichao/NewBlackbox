package android.app;

import android.content.IContentProvider;
import android.content.pm.ProviderInfo;
import android.os.IBinder;

/**
 * Stub implementation of Android's {@code ContentProviderHolder} class.
 *
 * <p>A holder class used internally by the Android framework to transport a
 * {@link IContentProvider} reference along with its {@link ProviderInfo} metadata
 * across Binder calls. This is commonly used during content provider installation
 * and lookup in {@link ActivityThread}.</p>
 *
 * @see IContentProvider
 * @see ActivityThread#installProvider
 */
public class ContentProviderHolder {
    /**
     * The {@link ProviderInfo} describing this content provider's manifest declaration,
     * including its authority, permissions, and other metadata.
     */
    public final ProviderInfo info = null;

    /**
     * The {@link IContentProvider} Binder interface proxy for communicating with
     * the content provider.
     */
    public IContentProvider provider;

    /**
     * The {@link IBinder} connection token used to manage the lifecycle and reference
     * counting of the content provider.
     */
    public IBinder connection;
}
