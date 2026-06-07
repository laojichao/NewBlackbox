package black.com.android.internal;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code com.android.internal.R$styleable} class.
 * Provides access to internal framework styleable resource arrays that are not
 * part of the public SDK, such as AccountAuthenticator and Window attributes.
 */
public class R {
    /**
     * Reflection wrapper for {@code com.android.internal.R$styleable}.
     * Contains integer array indices for referencing internal framework styleable attributes.
     */
    public static class styleable {
        public static final Reflector REF = Reflector.on("com.android.internal.R$styleable");

        /** The AccountAuthenticator styleable attribute array. */
        public static Reflector.FieldWrapper<Integer[]> AccountAuthenticator = REF.field("AccountAuthenticator");

        /** Index for the accountPreferences attribute. */
        public static Reflector.FieldWrapper<Integer> AccountAuthenticator_accountPreferences = REF.field("AccountAuthenticator_accountPreferences");

        /** Index for the accountType attribute. */
        public static Reflector.FieldWrapper<Integer> AccountAuthenticator_accountType = REF.field("AccountAuthenticator_accountType");

        /** Index for the customTokens attribute. */
        public static Reflector.FieldWrapper<Integer> AccountAuthenticator_customTokens = REF.field("AccountAuthenticator_customTokens");

        /** Index for the icon attribute. */
        public static Reflector.FieldWrapper<Integer> AccountAuthenticator_icon = REF.field("AccountAuthenticator_icon");

        /** Index for the label attribute. */
        public static Reflector.FieldWrapper<Integer> AccountAuthenticator_label = REF.field("AccountAuthenticator_label");

        /** Index for the smallIcon attribute. */
        public static Reflector.FieldWrapper<Integer> AccountAuthenticator_smallIcon = REF.field("AccountAuthenticator_smallIcon");

        /** The Window styleable attribute array. */
        public static Reflector.FieldWrapper<Integer[]> Window = REF.field("Window");

        /** Index for the windowFullscreen attribute. */
        public static Reflector.FieldWrapper<Integer> Window_windowFullscreen = REF.field("Window_windowFullscreen");

        /** Index for the windowIsTranslucent attribute. */
        public static Reflector.FieldWrapper<Integer> Window_windowIsTranslucent = REF.field("Window_windowIsTranslucent");

        /** Index for the windowShowWallpaper attribute. */
        public static Reflector.FieldWrapper<Integer> Window_windowShowWallpaper = REF.field("Window_windowShowWallpaper");
    }
}
