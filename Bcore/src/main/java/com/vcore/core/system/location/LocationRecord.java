package com.vcore.core.system.location;

/**
 * Holds the package name and user ID associated with a location update listener.
 * <p>
 * Used by {@link BLocationManagerService} to track which package and user each
 * registered location listener belongs to, enabling per-package location configuration.
 */
public class LocationRecord {
    /** The package name that registered the location listener. */
    public final String packageName;

    /** The virtual user ID of the listener. */
    public final int userId;

    /**
     * Constructs a LocationRecord with the given package and user.
     *
     * @param packageName the package name of the listener
     * @param userId      the virtual user ID
     */
    public LocationRecord(String packageName, int userId) {
        this.packageName = packageName;
        this.userId = userId;
    }
}
