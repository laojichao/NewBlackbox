package com.vcore.fake.frameworks;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

import com.vcore.app.BActivityThread;
import com.vcore.core.system.ServiceManager;
import com.vcore.core.system.notification.IBNotificationManagerService;

/**
 * Virtual environment manager for notification operations.
 *
 * <p>Wraps {@link IBNotificationManagerService} to provide notification management
 * functionality scoped to the virtual environment. Handles notification channels,
 * channel groups, and notification posting/cancellation.</p>
 *
 * @see BlackManager
 * @see IBNotificationManagerService
 */
public class BNotificationManager extends BlackManager<IBNotificationManagerService> {
    private static final BNotificationManager sNotificationManager = new BNotificationManager();

    /**
     * Returns the singleton instance of {@link BNotificationManager}.
     *
     * @return the global BNotificationManager instance
     */
    public static BNotificationManager get() {
        return sNotificationManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getServiceName() {
        return ServiceManager.NOTIFICATION_MANAGER;
    }

    /**
     * Retrieves a notification channel by its ID.
     *
     * @param channelId the notification channel ID
     * @return the notification channel, or {@code null} on error
     */
    public NotificationChannel getNotificationChannel(String channelId) {
        try {
            return getService().getNotificationChannel(channelId, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves all notification channel groups for the given package.
     *
     * @param packageName the package name
     * @return a list of channel groups, or {@code null} on error
     */
    public List<NotificationChannelGroup> getNotificationChannelGroups(String packageName) {
        try {
            return getService().getNotificationChannelGroups(packageName, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates a notification channel in the virtual environment.
     *
     * @param notificationChannel the channel to create
     */
    public void createNotificationChannel(NotificationChannel notificationChannel) {
        try {
            getService().createNotificationChannel(notificationChannel, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes a notification channel by its ID.
     *
     * @param channelId the channel ID to delete
     */
    public void deleteNotificationChannel(String channelId) {
        try {
            getService().deleteNotificationChannel(channelId, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a notification channel group in the virtual environment.
     *
     * @param notificationChannelGroup the group to create
     */
    public void createNotificationChannelGroup(NotificationChannelGroup notificationChannelGroup) {
        try {
            getService().createNotificationChannelGroup(notificationChannelGroup, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes a notification channel group by its ID.
     *
     * @param groupId the group ID to delete
     */
    public void deleteNotificationChannelGroup(String groupId) {
        try {
            getService().deleteNotificationChannelGroup(groupId, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Posts a notification with a tag in the virtual environment.
     *
     * @param id           the notification ID
     * @param tag          the notification tag
     * @param notification the notification to post
     */
    public void enqueueNotificationWithTag(int id, String tag, Notification notification) {
        try {
            getService().enqueueNotificationWithTag(id, tag, notification, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cancels a notification by tag in the virtual environment.
     *
     * @param id  the notification ID
     * @param tag the notification tag
     */
    public void cancelNotificationWithTag(int id, String tag) {
        try {
            getService().cancelNotificationWithTag(id, tag, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves all notification channels for the given package.
     *
     * @param packageName the package name
     * @return a list of notification channels, or an empty list on error
     */
    public List<NotificationChannel> getNotificationChannels(String packageName) {
        try {
            return getService().getNotificationChannels(packageName, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
