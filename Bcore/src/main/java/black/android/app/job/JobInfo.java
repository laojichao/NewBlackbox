package black.android.app.job;

import android.content.ComponentName;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.job.JobInfo} class.
 * Provides access to the service ComponentName field of a scheduled job.
 */
public class JobInfo {
    public static final Reflector REF = Reflector.on("android.app.job.JobInfo");

    /** The ComponentName of the service that will execute this job. */
    public static Reflector.FieldWrapper<ComponentName> service = REF.field("service");
}
