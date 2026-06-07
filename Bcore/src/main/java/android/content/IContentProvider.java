package android.content;

import android.os.IInterface;

/**
 * Stub implementation of Android's {@code IContentProvider} Binder interface.
 *
 * <p>This is the system-level interface through which applications communicate with
 * content providers across process boundaries. It extends {@link IInterface} to support
 * Binder IPC. The actual implementation in the Android framework provides methods for
 * querying, inserting, updating, and deleting content provider data.</p>
 *
 * @see android.app.ContentProviderHolder
 */
public interface IContentProvider extends IInterface { }