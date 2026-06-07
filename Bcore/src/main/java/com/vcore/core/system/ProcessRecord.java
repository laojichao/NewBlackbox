package com.vcore.core.system;

import android.content.pm.ApplicationInfo;
import android.os.Binder;
import android.os.ConditionVariable;
import android.os.IInterface;

import java.util.Arrays;

import com.vcore.core.IBActivityThread;
import com.vcore.entity.AppConfig;
import com.vcore.proxy.ProxyManifest;

/**
 * Represents a running virtual application process within the BlackBox environment.
 * <p>
 * Holds all metadata about a virtual process including its real and virtual PIDs/UIDs,
 * the associated {@link ApplicationInfo}, the client-side activity thread reference,
 * and a condition variable used to synchronize process initialization.
 */
public class ProcessRecord extends Binder {
    /** The application info for the package running in this process. */
    public final ApplicationInfo info;

    /** The virtual process name within the BlackBox environment. */
    final public String processName;

    /** Reference to the client-side activity thread interface. */
    public IBActivityThread bActivityThread;

    /** Reference to the application thread interface (IApplicationThread). */
    public IInterface appThread;

    /** The real host UID of the process. */
    public int uid;

    /** The real host PID of the process. */
    public int pid;

    /** The BlackBox UID (virtual UID) assigned to this process. */
    public int bUID;

    /** The BlackBox PID (virtual PID) assigned to this process. */
    public int bPID;

    /** The bUID of the process that initiated the creation of this process. */
    public int callingBUid;

    /** The virtual user ID under which this process runs. */
    public int userId;

    /**
     * Condition variable used to block callers until the process has been
     * fully initialized (i.e., the client thread has been attached).
     */
    public final ConditionVariable initLock = new ConditionVariable();

    /**
     * Constructs a new ProcessRecord for the given application and process name.
     *
     * @param info        the application info of the package
     * @param processName the virtual process name
     */
    public ProcessRecord(ApplicationInfo info, String processName) {
        this.info = info;
        this.processName = processName;
    }

    /**
     * Returns a hash code based on all identifying fields of this process record.
     *
     * @return the hash code value
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[]{processName, pid, bUID, bPID, uid, pid, userId});
    }

    /**
     * Returns the content provider authority for the proxy process associated
     * with this record's bPID.
     *
     * @return the provider authority string
     */
    public String getProviderAuthority() {
        return ProxyManifest.getProxyAuthorities(bPID);
    }

    /**
     * Creates and returns an {@link AppConfig} populated with this process record's
     * metadata. The config is used to communicate process configuration to the
     * client-side initialization.
     *
     * @return a new AppConfig instance with the current process state
     */
    public AppConfig getClientConfig() {
        AppConfig config = new AppConfig();
        config.packageName = info.packageName;
        config.processName = processName;
        config.bPID = bPID;
        config.bUID = bUID;
        config.uid = uid;
        config.callingBUid = callingBUid;
        config.userId = userId;
        config.token = this;
        return config;
    }

    /**
     * Returns the package name of the application running in this process.
     *
     * @return the package name
     */
    public String getPackageName() {
        return info.packageName;
    }
}
