package black.android.content;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.content.AttributionSource} class (Android S+).
 * AttributionSource tracks which app/component is performing an action for
 * privacy and permission attribution purposes.
 */
public class AttributionSource {
    public static final Reflector REF = Reflector.on("android.content.AttributionSource");

    /** The underlying AttributionSourceState parcelable. */
    public static Reflector.FieldWrapper<Object> mAttributionSourceState = REF.field("mAttributionSourceState");

    /**
     * Returns the next AttributionSource in the chain (for delegated calls).
     *
     * @return the next AttributionSource, or null
     */
    public static Reflector.MethodWrapper<Object> getNext = REF.method("getNext");
}
