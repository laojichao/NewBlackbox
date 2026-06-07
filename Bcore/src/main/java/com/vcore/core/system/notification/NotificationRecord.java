package com.vcore.core.system.notification;

import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Holds notification state for a single package within a virtual user.
 * <p>
 * Tracks notification channels, channel groups, and active notification IDs
 * created by a virtual application, enabling proper isolation and cleanup
 * when the package is uninstalled or its process dies.
 */
public class NotificationRecord {
    /** Map of channel ID to NotificationChannel for this package. */
    public final Map<String, NotificationChannel> mNotificationChannels = new HashMap<>();

    /** Map of group ID to NotificationChannelGroup for this package. */
    public final Map<String, NotificationChannelGroup> mNotificationChannelGroups = new HashMap<>();

    /** Set of active notification IDs posted by this package. */
    public final Set<Integer> mIds = new HashSet<>();
}
