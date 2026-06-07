package black.android.content.pm;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.content.pm.UserInfo} class.
 * Represents information about a device user, including user ID, name, and flags.
 */
public class UserInfo {
    public static final Reflector REF = Reflector.on("android.content.pm.UserInfo");

    /**
     * Creates a new UserInfo instance.
     *
     * @param id   the user ID
     * @param name the user name
     * @param flags the user flags (e.g., FLAG_PRIMARY)
     */
    public static Reflector.ConstructorWrapper<Object> _new = REF.constructor(int.class, String.class, int.class);

    /** Flag indicating this is the primary user. */
    public static Reflector.FieldWrapper<Integer> FLAG_PRIMARY = REF.field("FLAG_PRIMARY");
}
