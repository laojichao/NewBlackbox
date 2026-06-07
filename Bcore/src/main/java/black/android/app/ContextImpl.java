package black.android.app;

import android.content.Context;
import android.content.pm.PackageManager;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.ContextImpl} class.
 * ContextImpl is the concrete implementation of Context in Android.
 * This wrapper provides access to internal fields for package name,
 * package info, and the package manager.
 */
public class ContextImpl {
    public static final Reflector REF = Reflector.on("android.app.ContextImpl");

    /** The base package name for this context. */
    public static Reflector.FieldWrapper<String> mBasePackageName = REF.field("mBasePackageName");

    /** The LoadedApk package info associated with this context. */
    public static Reflector.FieldWrapper<Object> mPackageInfo = REF.field("mPackageInfo");

    /** The PackageManager instance for this context. */
    public static Reflector.FieldWrapper<PackageManager> mPackageManager = REF.field("mPackageManager");

    /**
     * Sets the outer Context (e.g., Activity) that wraps this ContextImpl.
     *
     * @param context the outer Context to set
     */
    public static Reflector.MethodWrapper<Void> setOuterContext = REF.method("setOuterContext", Context.class);

    /**
     * Returns the AttributionSource for this context (Android S+).
     *
     * @return the AttributionSource object
     */
    public static Reflector.MethodWrapper<Object> getAttributionSource = REF.method("getAttributionSource");
}
