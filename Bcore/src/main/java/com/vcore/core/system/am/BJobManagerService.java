package com.vcore.core.system.am;

import android.app.job.JobInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.RemoteException;

import java.util.HashMap;
import java.util.Map;

import com.vcore.BlackBoxCore;
import com.vcore.core.system.BProcessManagerService;
import com.vcore.core.system.ISystemService;
import com.vcore.core.system.ProcessRecord;
import com.vcore.core.system.pm.BPackageManagerService;
import com.vcore.entity.JobRecord;
import com.vcore.proxy.ProxyManifest;



/**
 * Virtual Job Scheduler Service that manages scheduled jobs within the virtual environment.
 *
 * <p>This service intercepts job scheduling requests and redirects them to proxy job services
 * running in the host process. It maintains a mapping of job records keyed by process name
 * and job ID to enable query and cancellation operations.</p>
 */
public class BJobManagerService extends IBJobManagerService.Stub implements ISystemService {

    /** Singleton instance of the service. */
    private static final BJobManagerService sService = new BJobManagerService();

    /** Maps composite keys (processName_jobId) to their JobRecord entries. */
    private final Map<String, JobRecord> mJobRecords = new HashMap<>();

    /**
     * Returns the singleton instance of the service.
     *
     * @return the global {@link BJobManagerService} instance
     */
    public static BJobManagerService get() {
        return sService;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Schedules a job by resolving the target service, ensuring its process is running,
     * and replacing the service component with a proxy job service.</p>
     */
    @Override
    public JobInfo schedule(JobInfo info, int userId) {
        ComponentName componentName = info.getService();
        Intent intent = new Intent();
        intent.setComponent(componentName);
        ResolveInfo resolveInfo = BPackageManagerService.get().resolveService(intent, PackageManager.GET_META_DATA, null, userId);
        if (resolveInfo == null) {
            return info;
        }

        ServiceInfo serviceInfo = resolveInfo.serviceInfo;
        ProcessRecord processRecord = BProcessManagerService.get().findProcessRecord(serviceInfo.packageName, serviceInfo.processName, userId);
        if (processRecord == null) {
            processRecord = BProcessManagerService.get().startProcessLocked(serviceInfo.packageName, serviceInfo.processName, userId,
                    -1, Binder.getCallingPid());
            if (processRecord == null) {
                throw new RuntimeException("Unable to create Process " + serviceInfo.processName);
            }
        }
        return scheduleJob(processRecord, info, serviceInfo);
    }

    /**
     * Queries a job record by process name and job ID.
     *
     * @param processName the name of the process hosting the job
     * @param jobId       the unique job identifier
     * @param userId      the virtual user ID
     * @return the matching {@link JobRecord}, or null if not found
     */
    @Override
    public JobRecord queryJobRecord(String processName, int jobId, int userId) {
        return mJobRecords.get(formatKey(processName, jobId));
    }

    /**
     * Schedules a job by creating a JobRecord, storing it, and replacing the service
     * component in the JobInfo with a proxy job service.
     *
     * @param processRecord the process record of the target app
     * @param info          the original JobInfo to schedule
     * @param serviceInfo   the resolved ServiceInfo for the job service
     * @return the modified JobInfo with the proxy service component
     */
    public JobInfo scheduleJob(ProcessRecord processRecord, JobInfo info, ServiceInfo serviceInfo) {
        JobRecord jobRecord = new JobRecord();
        jobRecord.mJobInfo = info;
        jobRecord.mServiceInfo = serviceInfo;

        mJobRecords.put(formatKey(processRecord.processName, info.getId()), jobRecord);
        black.android.app.job.JobInfo.service.set(info, new ComponentName(BlackBoxCore.getHostPkg(), ProxyManifest.getProxyJobService(processRecord.bPID)));
        return info;
    }

    @Override
    public void cancelAll(String processName, int userId) { }

    @Override
    public int cancel(String processName, int jobId, int userId) throws RemoteException {
        return jobId;
    }

    /**
     * Formats a composite key from process name and job ID for map lookups.
     *
     * @param processName the process name
     * @param jobId       the job ID
     * @return a string in the format "processName_jobId"
     */
    private String formatKey(String processName, int jobId) {
        return processName + "_" + jobId;
    }

    @Override
    public void systemReady() { }
}
