package com.vcore.core.system.am;

import android.content.Intent;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a task (task stack) in the virtual activity manager.
 *
 * <p>A task groups related activities together and manages their lifecycle within
 * the virtual environment. Each task has an ID, associated user ID, task affinity
 * string, and an ordered list of activity records representing the activity stack.</p>
 */
public class TaskRecord {

    /** The unique task ID assigned by the system. */
    public final int id;

    /** The virtual user ID that owns this task. */
    public final int userId;

    /** The task affinity string used for task matching and reparenting. */
    public final String taskAffinity;

    /** The original intent that created this task. */
    public Intent rootIntent;

    /** The ordered list of activity records in this task, bottom to top. */
    public final List<ActivityRecord> activities = new LinkedList<>();

    /**
     * Creates a new task record.
     *
     * @param id          the unique task ID
     * @param userId      the virtual user ID
     * @param taskAffinity the task affinity string
     */
    public TaskRecord(int id, int userId, String taskAffinity) {
        this.id = id;
        this.userId = userId;
        this.taskAffinity = taskAffinity;
    }

    /**
     * Checks whether a new task should be created instead of reusing this one.
     *
     * <p>Returns true if all activities in this task are finished, meaning the task
     * is effectively empty and a new one should be started.</p>
     *
     * @return true if all activities are finished, false if any activity is still active
     */
    public boolean needNewTask() {
        for (ActivityRecord activity : activities) {
            if (!activity.finished) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds an activity record to the top of this task's stack.
     *
     * @param record the activity record to add
     */
    public void addTopActivity(ActivityRecord record) {
        activities.add(record);
    }

    /**
     * Removes an activity record from this task's stack.
     *
     * @param record the activity record to remove
     */
    public void removeActivity(ActivityRecord record) {
        activities.remove(record);
    }

    /**
     * Returns the topmost (most recently started) non-finished activity in this task.
     *
     * @return the top active {@link ActivityRecord}, or null if all activities are finished
     */
    public ActivityRecord getTopActivityRecord() {
        for (int i = activities.size() - 1; i >= 0; i--) {
            ActivityRecord activityRecord = activities.get(i);
            if (!activityRecord.finished) {
                return activityRecord;
            }
        }
        return null;
    }
}
