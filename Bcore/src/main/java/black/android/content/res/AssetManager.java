package black.android.content.res;

import black.Reflector;

/**
 * Reflection wrapper for hidden methods in {@code android.content.res.AssetManager}.
 * Provides access to the constructor and addAssetPath method for loading
 * resources from arbitrary APK files.
 */
public class AssetManager {
    public static final Reflector REF = Reflector.on("android.content.res.AssetManager");

    /**
     * Creates a new AssetManager instance (deprecated public constructor was hidden in later APIs).
     */
    public static Reflector.ConstructorWrapper<android.content.res.AssetManager> _new = REF.constructor();

    /**
     * Adds an asset path (APK file) to this AssetManager for resource loading.
     *
     * @param path the path to the APK or asset directory
     * @return the cookie identifier for the added path
     */
    public static Reflector.MethodWrapper<Integer> addAssetPath = REF.method("addAssetPath", String.class);
}
