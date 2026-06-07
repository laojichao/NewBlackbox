package black.android.content.pm;

import java.util.List;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.content.pm.ParceledListSlice} class.
 * ParceledListSlice is used to transfer large lists of Parcelable objects across Binder
 * transactions by splitting them into manageable slices.
 */
public class ParceledListSlice {
    public static final Reflector REF = Reflector.on("android.content.pm.ParceledListSlice");

    /**
     * Creates a new empty ParceledListSlice.
     */
    public static Reflector.ConstructorWrapper<Object> _new0 = REF.constructor();

    /**
     * Creates a new ParceledListSlice with the given list.
     */
    public static Reflector.ConstructorWrapper<Object> _new1 = REF.constructor(List.class);

    /**
     * Appends an object to this list slice.
     *
     * @param item the object to append
     * @return true if the item was appended successfully
     */
    public static Reflector.MethodWrapper<Boolean> append = REF.method("append", Object.class);

    /**
     * Returns the underlying list of objects.
     *
     * @return the list of objects in this slice
     */
    public static Reflector.MethodWrapper<List<?>> getList = REF.method("getList");

    /**
     * Sets whether this is the last slice in the transfer sequence.
     *
     * @param lastSlice true if this is the final slice
     */
    public static Reflector.MethodWrapper<Void> setLastSlice = REF.method("setLastSlice", boolean.class);
}
