package com.vcore.app.dispatcher;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.res.Configuration;

import java.util.HashMap;
import java.util.Map;

import com.vcore.BlackBoxCore;
import com.vcore.app.BActivityThread;
import com.vcore.entity.JobRecord;

/**
 * Dispatcher that routes Android {@link JobService} lifecycle events to the appropriate virtual
 * job service instances within the BlackBox environment.
 * <p>
 * This class acts as a proxy between the system's job scheduler and the virtual app's job services.
 * When the system dispatches a job event, this dispatcher looks up (or lazily creates) the corresponding
 * virtual {@link JobService} and forwards the event to it. Job records are cached by job ID.
 */
public class AppJobServiceDispatcher {
    /** Singleton instance. */
    private static final AppJobServiceDispatcher sServiceDispatcher = new AppJobServiceDispatcher();
    /** Cache of active job records, keyed by system job ID. */
    private final Map<Integer, JobRecord> mJobRecords = new HashMap<>();

    /**
     * Returns the singleton instance of {@link AppJobServiceDispatcher}.
     *
     * @return the dispatcher instance
     */
    public static AppJobServiceDispatcher get() {
        return sServiceDispatcher;
    }

    /**
     * Called when the system has determined that the job should start executing.
     * <p>
     * Retrieves or creates the virtual {@link JobService} for the given job ID and delegates
     * the {@code onStartJob} call to it.
     *
     * @param params the job parameters provided by the system
     * @return {@code true} if the job is still running and the system should keep the wakelock,
     *         {@code false} if the job is finished
     */
    public boolean onStartJob(JobParameters params) {
        try {
            JobService jobService = getJobService(params.getJobId());
            if (jobService == null) {
                return false;
            }
            return jobService.onStartJob(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Called when the system has determined that the job should stop executing.
     * <p>
     * Delegates the {@code onStopJob} call to the virtual {@link JobService}, destroys the
     * service, and removes the job record from the cache.
     *
     * @param params the job parameters provided by the system
     * @return {@code true} if the job should be rescheduled, {@code false} otherwise
     */
    public boolean onStopJob(JobParameters params) {
        JobService jobService = getJobService(params.getJobId());
        if (jobService == null) {
            return false;
        }

        boolean isStopJob = jobService.onStopJob(params);
        jobService.onDestroy();

        synchronized (mJobRecords) {
            mJobRecords.remove(params.getJobId());
        }
        return isStopJob;
    }

    /**
     * Forwards a configuration change event to all active virtual job services.
     *
     * @param newConfig the new device configuration
     */
    public void onConfigurationChanged(Configuration newConfig) {
        for (JobRecord jobRecord : mJobRecords.values()) {
            if (jobRecord.mJobService != null) {
                jobRecord.mJobService.onConfigurationChanged(newConfig);
            }
        }
    }

    /**
     * Destroys all active virtual job services and cleans up resources.
     * Called when the host process is shutting down.
     */
    public void onDestroy() {
        for (JobRecord jobRecord : mJobRecords.values()) {
            if (jobRecord.mJobService != null) {
                jobRecord.mJobService.onDestroy();
            }
        }
    }

    /**
     * Forwards a low-memory event to all active virtual job services.
     */
    public void onLowMemory() {
        for (JobRecord jobRecord : mJobRecords.values()) {
            if (jobRecord.mJobService != null) {
                jobRecord.mJobService.onLowMemory();
            }
        }
    }

    /**
     * Forwards a trim-memory event to all active virtual job services.
     *
     * @param level the memory trim level from the system
     */
    public void onTrimMemory(int level) {
        for (JobRecord jobRecord : mJobRecords.values()) {
            if (jobRecord.mJobService != null) {
                jobRecord.mJobService.onTrimMemory(level);
            }
        }
    }

    /**
     * Retrieves or lazily creates the virtual {@link JobService} for the given job ID.
     * <p>
     * If a cached service exists, it is returned immediately. Otherwise, the job record is queried
     * from {@link com.vcore.fake.frameworks.BJobManager} and a new service instance is created via
     * {@link BActivityThread#createJobService}.
     *
     * @param jobId the system-assigned job ID
     * @return the virtual {@link JobService}, or {@code null} if not found or creation failed
     */
    JobService getJobService(int jobId) {
        synchronized (mJobRecords) {
            JobRecord jobRecord = mJobRecords.get(jobId);
            if (jobRecord != null && jobRecord.mJobService != null) {
                return jobRecord.mJobService;
            }

            try {
                JobRecord record = BlackBoxCore.getBJobManager().queryJobRecord(BActivityThread.getAppProcessName(), jobId);
                if (record == null) {
                    return null;
                }

                record.mJobService = BActivityThread.currentActivityThread().createJobService(record.mServiceInfo);
                if (record.mJobService == null) {
                    return null;
                }

                mJobRecords.put(jobId, record);
                return record.mJobService;
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return null;
        }
    }
}
