package com.vcore.fake.frameworks;

import android.app.job.JobInfo;
import android.os.RemoteException;

import com.vcore.app.BActivityThread;
import com.vcore.core.system.ServiceManager;
import com.vcore.core.system.am.IBJobManagerService;
import com.vcore.entity.JobRecord;

/**
 * Virtual environment manager for job scheduling operations.
 *
 * <p>Wraps {@link IBJobManagerService} to provide JobScheduler functionality
 * scoped to the virtual environment. Handles scheduling, querying, and
 * cancellation of jobs within the virtual user space.</p>
 *
 * @see BlackManager
 * @see IBJobManagerService
 */
public class BJobManager extends BlackManager<IBJobManagerService> {
    private static final BJobManager sJobManager = new BJobManager();

    /**
     * Returns the singleton instance of {@link BJobManager}.
     *
     * @return the global BJobManager instance
     */
    public static BJobManager get() {
        return sJobManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getServiceName() {
        return ServiceManager.JOB_MANAGER;
    }

    /**
     * Schedules a job within the virtual environment.
     *
     * @param info the job info to schedule
     * @return the scheduled job info, or {@code null} on error
     */
    public JobInfo schedule(JobInfo info) {
        try {
            return getService().schedule(info, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Queries a job record for the given process and job ID.
     *
     * @param processName the process name
     * @param jobId       the job ID
     * @return the job record, or {@code null} if not found or on error
     */
    public JobRecord queryJobRecord(String processName, int jobId) {
        try {
            return getService().queryJobRecord(processName, jobId, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Cancels all jobs for the given process.
     *
     * @param processName the process name
     */
    public void cancelAll(String processName) {
        try {
            getService().cancelAll(processName, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cancels a specific job for the given process.
     *
     * @param processName the process name
     * @param jobId       the job ID to cancel
     * @return the result code, or -1 on error
     */
    public int cancel(String processName, int jobId) {
        try {
            return getService().cancel(processName, jobId, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
