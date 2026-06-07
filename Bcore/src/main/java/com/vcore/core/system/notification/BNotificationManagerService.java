package com.vcore.core.system.notification;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Binder;
import android.os.Build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import black.android.app.NotificationO;
import com.vcore.BlackBoxCore;
import com.vcore.core.system.BProcessManagerService;
import com.vcore.core.system.ISystemService;
import com.vcore.core.system.ProcessRecord;
import com.vcore.utils.compat.BuildCompat;

/**
 * Virtual notification manager service for the BlackBox environment.
 * <p>
 * Intercepts and manages notification channels, channel groups, and notification
 * posting/cancellation on behalf of virtual applications. Ensures notification
 * isolation by prefixing channel and group IDs with unique suffixes per user,
 * preventing conflicts between virtual apps and the host system. Tracks all
 * active notifications for cleanup when a virtual process dies.
 */
public class BNotificationManagerService extends IBNotificationManagerService.Stub implements ISystemService {
    /** Singleton instance. */
    private final static BNotificationManagerService sService = new BNotificationManagerService();

    /** Suffix prefix used to namespace virtual notification channels. */
    public static final String CHANNEL_BLACK = "@black-";

    /** Suffix prefix used to namespace virtual notification channel groups. */
    public static final String GROUP_BLACK = "@black-group-";

    /** Map from "packageName-userId" key to NotificationRecord. */
    private final Map<String, NotificationRecord> mNotificationRecords = new HashMap<>();

    /** The real system NotificationManager for posting actual notifications. */
    private final NotificationManager mRealNotificationManager = (NotificationManager) BlackBoxCore.getContext().getSystemService(Context.NOTIFICATION_SERVICE);

    /**
     * Returns the singleton instance of BNotificationManagerService.
     *
     * @return the singleton service instance
     */
    public static BNotificationManagerService get() {
        return sService;
    }

    /** Called when the system is ready. No initialization needed. */
    @Override
    public void systemReady() { }

    /**
     * Returns the NotificationRecord for the given package and user, creating one if absent.
     *
     * @param packageName the package name
     * @param userId      the virtual user ID
     * @return the NotificationRecord
     */
    private NotificationRecord getNotificationRecord(String packageName, int userId) {
        String key = packageName + "-" + userId;
        synchronized (mNotificationRecords) {
            NotificationRecord notificationRecord = mNotificationRecords.get(key);
            if (notificationRecord == null) {
                notificationRecord = new NotificationRecord();
                mNotificationRecords.put(key, notificationRecord);
            }
            return notificationRecord;
        }
    }

    /**
     * Removes the NotificationRecord for the given package and user.
     *
     * @param packageName the package name
     * @param userId      the virtual user ID
     */
    private void removeNotificationRecord(String packageName, int userId) {
        String key = packageName + "-" + userId;
        synchronized (mNotificationRecords) {
            mNotificationRecords.remove(key);
        }
    }

    /**
     * Returns the notification channel for the calling process with the given channel ID.
     *
     * @param channelId the channel ID to look up
     * @param userId    the virtual user ID
     * @return the NotificationChannel, or null if the calling process is not found
     */
    @Override
    @TargetApi(Build.VERSION_CODES.O)
    public NotificationChannel getNotificationChannel(String channelId, int userId) {
        int callingPid = getCallingPid();
        ProcessRecord processByPid = BProcessManagerService.get().findProcessByPid(callingPid);
        if (processByPid == null) {
            return null;
        }

        NotificationRecord notificationRecord = getNotificationRecord(processByPid.getPackageName(), userId);
        synchronized (notificationRecord.mNotificationChannels) {
            return notificationRecord.mNotificationChannels.get(channelId);
        }
    }

    /**
     * Returns all notification channels for the given package and user.
     *
     * @param packageName the package name
     * @param userId      the virtual user ID
     * @return a list of NotificationChannel objects
     */
    @Override
    public List<NotificationChannel> getNotificationChannels(String packageName, int userId) {
        NotificationRecord notificationRecord = getNotificationRecord(packageName, userId);
        synchronized (notificationRecord.mNotificationChannels) {
            return new ArrayList<>(notificationRecord.mNotificationChannels.values());
        }
    }

    /**
     * Returns all notification channel groups for the given package and user.
     *
     * @param packageName the package name
     * @param userId      the virtual user ID
     * @return a list of NotificationChannelGroup objects
     */
    @Override
    public List<NotificationChannelGroup> getNotificationChannelGroups(String packageName, int userId) {
        NotificationRecord notificationRecord = getNotificationRecord(packageName, userId);
        synchronized (notificationRecord.mNotificationChannelGroups) {
            return new ArrayList<>(notificationRecord.mNotificationChannelGroups.values());
        }
    }

    /**
     * Creates a notification channel for the calling virtual application.
     * The channel ID is namespaced with a user suffix before being registered
     * with the real NotificationManager.
     *
     * @param notificationChannel the channel to create
     * @param userId              the virtual user ID
     */
    @Override
    @TargetApi(Build.VERSION_CODES.O)
    public void createNotificationChannel(NotificationChannel notificationChannel, int userId) {
        int callingPid = getCallingPid();
        ProcessRecord processByPid = BProcessManagerService.get().findProcessByPid(callingPid);
        if (processByPid == null) {
            return;
        }

        handleNotificationChannel(notificationChannel, userId);
        mRealNotificationManager.createNotificationChannel(notificationChannel);

        resetNotificationChannel(notificationChannel);
        NotificationRecord notificationRecord = getNotificationRecord(processByPid.getPackageName(), userId);
        synchronized (notificationRecord.mNotificationChannels) {
            notificationRecord.mNotificationChannels.put(notificationChannel.getId(), notificationChannel);
        }
    }

    /**
     * Deletes a notification channel for the calling virtual application.
     *
     * @param channelId the channel ID to delete
     * @param userId    the virtual user ID
     */
    @Override
    @TargetApi(Build.VERSION_CODES.O)
    public void deleteNotificationChannel(String channelId, int userId) {
        int callingPid = getCallingPid();
        ProcessRecord processByPid = BProcessManagerService.get().findProcessByPid(callingPid);
        if (processByPid == null) {
            return;
        }

        NotificationRecord notificationRecord = getNotificationRecord(processByPid.getPackageName(), userId);
        synchronized (notificationRecord.mNotificationChannels) {
            NotificationChannel remove = notificationRecord.mNotificationChannels.remove(channelId);
            if (remove != null) {
                String blackChannelId = getBlackChannelId(remove.getId(), userId);
                mRealNotificationManager.deleteNotificationChannel(blackChannelId);
            }
        }
    }

    /**
     * Creates a notification channel group for the calling virtual application.
     *
     * @param notificationChannelGroup the channel group to create
     * @param userId                   the virtual user ID
     */
    @Override
    @TargetApi(Build.VERSION_CODES.O)
    public void createNotificationChannelGroup(NotificationChannelGroup notificationChannelGroup, int userId) {
        int callingPid = getCallingPid();
        ProcessRecord processByPid = BProcessManagerService.get().findProcessByPid(callingPid);
        if (processByPid == null) {
            return;
        }

        handleNotificationGroup(notificationChannelGroup, userId);
        mRealNotificationManager.createNotificationChannelGroup(notificationChannelGroup);

        resetNotificationGroup(notificationChannelGroup);
        NotificationRecord notificationRecord = getNotificationRecord(processByPid.getPackageName(), userId);
        synchronized (notificationRecord.mNotificationChannelGroups) {
            notificationRecord.mNotificationChannelGroups.put(notificationChannelGroup.getId(), notificationChannelGroup);
        }
    }

    /**
     * Deletes a notification channel group for the calling virtual application.
     *
     * @param groupId the group ID to delete
     * @param userId  the virtual user ID
     */
    @Override
    @TargetApi(Build.VERSION_CODES.O)
    public void deleteNotificationChannelGroup(String groupId, int userId) {
        int callingPid = getCallingPid();
        ProcessRecord processByPid = BProcessManagerService.get().findProcessByPid(callingPid);
        if (processByPid == null) {
            return;
        }

        NotificationRecord notificationRecord = getNotificationRecord(processByPid.getPackageName(), userId);
        synchronized (notificationRecord.mNotificationChannelGroups) {
            NotificationChannelGroup remove = notificationRecord.mNotificationChannelGroups.remove(groupId);
            if (remove != null) {
                String blackGroupId = getBlackGroupId(remove.getId(), userId);
                mRealNotificationManager.deleteNotificationChannelGroup(blackGroupId);
            }
        }
    }

    /**
     * Posts a notification on behalf of the calling virtual application.
     * The notification ID and channel/group IDs are namespaced to prevent conflicts.
     *
     * @param id           the notification ID
     * @param tag          the notification tag (nullable)
     * @param notification the Notification to post
     * @param userId       the virtual user ID
     */
    @Override
    public void enqueueNotificationWithTag(int id, String tag, Notification notification, int userId) {
        ProcessRecord processByPid = BProcessManagerService.get().findProcessByPid(Binder.getCallingPid());
        if (processByPid == null) {
            return;
        }

        int notificationId = getNotificationId(userId, id, processByPid.getPackageName());
        if (BuildCompat.isOreo()) {
            if (NotificationO.mChannelId != null) {
                String blackChannelId = getBlackChannelId(NotificationO.mChannelId.get(), userId);
                NotificationO.mChannelId.set(blackChannelId);
            }

            if (NotificationO.mGroupKey != null) {
                String blackGroupId = getBlackGroupId(NotificationO.mGroupKey.get(), userId);
                NotificationO.mGroupKey.set(blackGroupId);
            }
        }

        NotificationRecord notificationRecord = getNotificationRecord(processByPid.getPackageName(), userId);
        synchronized (notificationRecord.mIds) {
            notificationRecord.mIds.add(notificationId);
        }
        mRealNotificationManager.notify(notificationId, notification);
    }

    /**
     * Cancels a notification posted by the calling virtual application.
     *
     * @param id     the notification ID
     * @param tag    the notification tag (nullable)
     * @param userId the virtual user ID
     */
    @Override
    public void cancelNotificationWithTag(int id, String tag, int userId) {
        ProcessRecord processByPid = BProcessManagerService.get().findProcessByPid(Binder.getCallingPid());
        if (processByPid == null) {
            return;
        }

        int notificationId = getNotificationId(userId, id, processByPid.getPackageName());
        mRealNotificationManager.cancel(notificationId);

        NotificationRecord notificationRecord = getNotificationRecord(processByPid.getPackageName(), userId);
        synchronized (notificationRecord.mIds) {
            notificationRecord.mIds.remove(notificationId);
        }
    }

    /**
     * Prefixes the notification channel ID with the user suffix for namespacing.
     *
     * @param notificationChannel the channel to modify
     * @param userId              the virtual user ID
     */
    @TargetApi(Build.VERSION_CODES.O)
    private void handleNotificationChannel(NotificationChannel notificationChannel, int userId) {
        String channelId = black.android.app.NotificationChannel.mId.get(notificationChannel);
        String blackChannelId = getBlackChannelId(channelId, userId);

        black.android.app.NotificationChannel.mId.set(notificationChannel, blackChannelId);
        notificationChannel.setGroup(getBlackGroupId(notificationChannel.getGroup(), userId));
    }

    /**
     * Restores the original channel ID by stripping the user suffix.
     *
     * @param notificationChannel the channel to reset
     */
    private void resetNotificationChannel(NotificationChannel notificationChannel) {
        String channelId = black.android.app.NotificationChannel.mId.get(notificationChannel);
        String realChannelId = getRealChannelId(channelId);
        black.android.app.NotificationChannel.mId.set(notificationChannel, realChannelId);
    }

    /**
     * Prefixes the notification channel group ID with the user suffix and
     * creates any child channels.
     *
     * @param notificationChannelGroup the group to modify
     * @param userId                   the virtual user ID
     */
    @TargetApi(Build.VERSION_CODES.O)
    private void handleNotificationGroup(NotificationChannelGroup notificationChannelGroup, int userId) {
        String groupId = black.android.app.NotificationChannelGroup.mId.get(notificationChannelGroup);
        String blackGroupId = getBlackGroupId(groupId, userId);
        black.android.app.NotificationChannelGroup.mId.set(notificationChannelGroup, blackGroupId);

        List<NotificationChannel> notificationChannels = black.android.app.NotificationChannelGroup.mChannels.get(notificationChannelGroup);
        if (notificationChannels != null) {
            for (NotificationChannel notificationChannel : notificationChannels) {
                createNotificationChannel(notificationChannel, userId);
            }
        }
    }

    /**
     * Restores the original group ID by stripping the user suffix.
     *
     * @param notificationChannelGroup the group to reset
     */
    private void resetNotificationGroup(NotificationChannelGroup notificationChannelGroup) {
        String groupId = black.android.app.NotificationChannelGroup.mId.get(notificationChannelGroup);
        String realGroupId = getRealGroupId(groupId);
        black.android.app.NotificationChannelGroup.mId.set(notificationChannelGroup, realGroupId);

        List<NotificationChannel> notificationChannels = black.android.app.NotificationChannelGroup.mChannels.get(notificationChannelGroup);
        if (notificationChannels != null) {
            for (NotificationChannel notificationChannel : notificationChannels) {
                resetNotificationChannel(notificationChannel);
            }
        }
    }

    /**
     * Deletes all notification channels, groups, and active notifications for the
     * given package and user. Called when a virtual process dies or a package is uninstalled.
     *
     * @param packageName the package name
     * @param userId      the virtual user ID
     */
    @SuppressLint("NewApi")
    public void deletePackageNotification(String packageName, int userId) {
        NotificationRecord notificationRecord = getNotificationRecord(packageName, userId);
        if (BuildCompat.isOreo()) {
            for (NotificationChannelGroup value : notificationRecord.mNotificationChannelGroups.values()) {
                String blackGroupId = getBlackGroupId(value.getId(), userId);
                mRealNotificationManager.deleteNotificationChannelGroup(blackGroupId);
            }

            for (NotificationChannel value : notificationRecord.mNotificationChannels.values()) {
                String blackChannelId = getBlackChannelId(value.getId(), userId);
                mRealNotificationManager.deleteNotificationChannel(blackChannelId);
            }
        }

        for (Integer id : notificationRecord.mIds) {
            mRealNotificationManager.cancel(id);
        }
        removeNotificationRecord(packageName, userId);
    }

    /**
     * Returns the namespaced channel ID by appending the user suffix.
     *
     * @param channelId the original channel ID
     * @param userId    the virtual user ID
     * @return the namespaced channel ID
     */
    private String getBlackChannelId(String channelId, int userId) {
        if (channelId == null || channelId.contains(CHANNEL_BLACK)) {
            return channelId;
        }
        return channelId + CHANNEL_BLACK + userId;
    }

    /**
     * Strips the user suffix from a namespaced channel ID.
     *
     * @param channelId the namespaced channel ID
     * @return the original channel ID
     */
    private String getRealChannelId(String channelId) {
        if (channelId == null || !channelId.contains(CHANNEL_BLACK)) {
            return channelId;
        }
        return channelId.split(CHANNEL_BLACK)[0];
    }

    /**
     * Returns the namespaced group ID by appending the user suffix.
     *
     * @param groupId the original group ID
     * @param userId  the virtual user ID
     * @return the namespaced group ID
     */
    private String getBlackGroupId(String groupId, int userId) {
        if (groupId == null || groupId.contains(GROUP_BLACK)) {
            return groupId;
        }
        return groupId + GROUP_BLACK + userId;
    }

    /**
     * Strips the user suffix from a namespaced group ID.
     *
     * @param groupId the namespaced group ID
     * @return the original group ID
     */
    private String getRealGroupId(String groupId) {
        if (groupId == null || !groupId.contains(GROUP_BLACK)) {
            return groupId;
        }
        return groupId.split(GROUP_BLACK)[0];
    }

    /**
     * Generates a unique notification ID by hashing the package name, user ID,
     * and notification ID together.
     *
     * @param userId         the virtual user ID
     * @param notificationId the original notification ID
     * @param packageName    the package name
     * @return a unique hash-based notification ID
     */
    public static int getNotificationId(int userId, int notificationId, String packageName) {
        return (packageName + userId + notificationId).hashCode();
    }
}
