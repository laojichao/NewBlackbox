package black.android.os.storage;

import java.io.File;

import black.Reflector;

/**
 * Reflection wrapper for hidden fields in {@code android.os.storage.StorageVolume}.
 * Provides access to the internal and public path fields of a storage volume
 * which are not exposed through the public API.
 */
public class StorageVolume {
    public static final Reflector REF = Reflector.on("android.os.storage.StorageVolume");

    /** The internal (system-level) path of this storage volume. */
    public static Reflector.FieldWrapper<File> mInternalPath = REF.field("mInternalPath");

    /** The public (user-visible) path of this storage volume. */
    public static Reflector.FieldWrapper<File> mPath = REF.field("mPath");
}
