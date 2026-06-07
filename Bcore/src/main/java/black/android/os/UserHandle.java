package black.android.os;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.os.UserHandle#myUserId} method.
 * Provides access to the current user ID, which identifies the Android user
 * (e.g., owner, work profile, guest) running the calling process.
 */
public class UserHandle {
    public static final Reflector REF = Reflector.on("android.os.UserHandle");

    /**
     * Returns the user ID of the current process.
     *
     * @return the current user ID (e.g., 0 for the primary user)
     */
    public static Reflector.StaticMethodWrapper<Integer> myUserId = REF.staticMethod("myUserId");
}
