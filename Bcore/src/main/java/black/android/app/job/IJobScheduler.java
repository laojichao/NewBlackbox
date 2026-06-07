package black.android.app.job;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.job.IJobScheduler$Stub} class.
 * Provides access to the job scheduler system service used for scheduling
 * deferred background work.
 */
public class IJobScheduler {
    /**
     * Reflection wrapper for {@code android.app.job.IJobScheduler$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.app.job.IJobScheduler$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the job scheduler service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
