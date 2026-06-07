package black.android.providers;

import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for hidden inner classes of {@code android.provider.Settings}.
 * Provides access to the NameValueCache and ContentProviderHolder mechanisms
 * used internally by Settings.System, Settings.Secure, and Settings.Global
 * for reading and writing system settings.
 */
public class Settings {
    /**
     * Reflection wrapper for {@code android.provider.Settings$System}.
     * Provides access to the NameValueCache for system settings.
     */
    public static class System {
        public static final Reflector REF = Reflector.on("android.provider.Settings$System");

        /** The NameValueCache instance for system settings. */
        public static Reflector.FieldWrapper<Object> sNameValueCache = REF.field("sNameValueCache");
    }

    /**
     * Reflection wrapper for {@code android.provider.Settings$Secure}.
     * Provides access to the NameValueCache for secure settings.
     */
    public static class Secure {
        public static final Reflector REF = Reflector.on("android.provider.Settings$Secure");

        /** The NameValueCache instance for secure settings. */
        public static Reflector.FieldWrapper<Object> sNameValueCache = REF.field("sNameValueCache");
    }

    /**
     * Reflection wrapper for {@code android.provider.Settings$ContentProviderHolder}.
     * Holds the IContentProvider interface for accessing the settings provider.
     */
    public static class ContentProviderHolder {
        public static final Reflector REF = Reflector.on("android.provider.Settings$ContentProviderHolder");

        /** The IContentProvider binder for the settings content provider. */
        public static Reflector.FieldWrapper<IInterface> mContentProvider = REF.field("mContentProvider");
    }

    /**
     * Reflection wrapper for {@code android.provider.Settings$NameValueCache} on Android O+.
     * Uses a providerHolder field instead of a direct content provider reference.
     */
    public static class NameValueCacheOreo {
        public static final Reflector REF = Reflector.on("android.provider.Settings$NameValueCache");

        /** The ContentProviderHolder containing the settings content provider (O+ variant). */
        public static Reflector.FieldWrapper<Object> mProviderHolder = REF.field("mProviderHolder");
    }

    /**
     * Reflection wrapper for {@code android.provider.Settings$NameValueCache} on pre-O versions.
     * Uses a direct content provider reference.
     */
    public static class NameValueCache {
        public static final Reflector REF = Reflector.on("android.provider.Settings$NameValueCache");

        /** The IContentProvider binder for the settings content provider (pre-O variant). */
        public static Reflector.FieldWrapper<Object> mContentProvider = REF.field("mContentProvider");
    }

    /**
     * Reflection wrapper for {@code android.provider.Settings$Global}.
     * Provides access to the NameValueCache for global settings.
     */
    public static class Global {
        public static final Reflector REF = Reflector.on("android.provider.Settings$Global");

        /** The NameValueCache instance for global settings. */
        public static Reflector.FieldWrapper<Object> sNameValueCache = REF.field("sNameValueCache");
    }
}
