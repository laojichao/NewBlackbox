package android.content.pm;

import android.util.ArraySet;

/**
 * Stub implementation of Android's {@code PackageUserState} class.
 *
 * <p>Represents the per-user state of an installed package. This includes information
 * such as whether the package is stopped, installed, hidden, or enabled, along with
 * lists of individually enabled or disabled components and domain verification status.</p>
 *
 * <p>Used internally by the package manager to track per-user package configuration.</p>
 *
 * @see android.content.pm.PackageManager
 */
public class PackageUserState {
    /**
     * Whether the package has been force-stopped by the user or system.
     * A stopped package cannot receive implicit broadcasts.
     */
    public boolean stopped;

    /** Whether the package has not yet been launched by this user. */
    public boolean notLaunched;

    /** Whether the package is installed for this user. */
    public boolean installed;

    /**
     * Whether the package is hidden or restricted by the device owner or admin policy.
     */
    public boolean hidden; // Is the app restricted by owner/admin

    /**
     * The enabled state of the package. One of
     * {@link android.content.pm.PackageManager#COMPONENT_ENABLED_STATE_DEFAULT},
     * {@link android.content.pm.PackageManager#COMPONENT_ENABLED_STATE_ENABLED},
     * {@link android.content.pm.PackageManager#COMPONENT_ENABLED_STATE_DISABLED},
     * {@link android.content.pm.PackageManager#COMPONENT_ENABLED_STATE_DISABLED_USER},
     * {@link android.content.pm.PackageManager#COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED},
     * or {@link android.content.pm.PackageManager#COMPONENT_ENABLED_STATE_DISABLED_COMPONENTS}.
     */
    public int enabled;

    /** Whether uninstall of this package is blocked. */
    public boolean blockUninstall;

    /** The package name of the caller that most recently disabled this package. */
    public String lastDisableAppCaller;

    /**
     * Set of component class names that have been individually disabled for this user.
     */
    public ArraySet<String> disabledComponents;

    /**
     * Set of component class names that have been individually enabled for this user.
     */
    public ArraySet<String> enabledComponents;

    /**
     * The domain verification status for this package. Indicates whether the package
     * has verified its declared app links.
     */
    public int domainVerificationStatus;

    /** The current app link generation counter for this package. */
    public int appLinkGeneration;

    /**
     * Constructs a default {@link PackageUserState} with all fields initialized to
     * their default values.
     */
    public PackageUserState() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Copy constructor that creates a {@link PackageUserState} from an existing instance.
     *
     * @param o the {@link PackageUserState} instance to copy from
     */
    public PackageUserState(final PackageUserState o) {
        throw new RuntimeException("Stub!");
    }
}
