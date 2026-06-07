package android.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ProviderInfo;
import android.os.Handler;
import android.os.IBinder;
import android.util.ArrayMap;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;

/**
 * Stub implementation of Android's {@code ActivityThread} class.
 *
 * <p>{@code ActivityThread} is the main thread managed by the Android framework for each
 * application process. It is responsible for managing the lifecycle of activities, services,
 * content providers, and broadcast receivers within the process.</p>
 *
 * <p>This stub class provides field and method signatures that mirror the hidden
 * {@code android.app.ActivityThread} API, allowing reflective access and hook-based
 * manipulation of the application's main thread state in sandboxed or virtualized
 * environments.</p>
 *
 * @see Handler
 * @see Instrumentation
 */
public class ActivityThread {
    /** The main {@link Handler} for dispatching messages on the application's main thread. */
    public H mH = null;

    /** Data about the application that is currently bound to this thread. */
    public AppBindData mBoundApplication;

    /** The initial {@link Application} object created for this thread. */
    public Application mInitialApplication;

    /** The {@link Instrumentation} instance used to manage activity lifecycle callbacks. */
    public Instrumentation mInstrumentation;

    /** Map of package names to weak references of their associated package objects. */
    public Map<String, WeakReference<?>> mPackages;

    /** Map of activity tokens to their corresponding {@link ActivityClientRecord} objects. */
    public Map<IBinder, ActivityClientRecord> mActivities;

    /** Map of provider keys to installed content provider objects. */
    public ArrayMap<ProviderKey, Object> mProviderMap;

    /**
     * Handler subclass used by {@link ActivityThread} for dispatching lifecycle and
     * framework messages on the application's main thread.
     */
    static class H extends Handler { }

    /**
     * Returns the current {@link ActivityThread} instance for the calling process.
     *
     * @return the current {@code ActivityThread} associated with this application process
     * @throws RuntimeException always, as this is a stub implementation
     */
    public static ActivityThread currentActivityThread() {
        throw new RuntimeException();
    }

    /**
     * Returns the process name of the current application.
     *
     * @return the name of the process running this {@code ActivityThread}
     * @throws RuntimeException always, as this is a stub implementation
     */
    public String getProcessName() {
        throw new RuntimeException();
    }

    /**
     * Returns the main {@link Handler} associated with this {@code ActivityThread}.
     *
     * @return the {@link Handler} used for dispatching messages on the main thread
     * @throws RuntimeException always, as this is a stub implementation
     */
    public Handler getHandler() {
        throw new RuntimeException();
    }

    /**
     * Installs a content provider into the application process.
     *
     * @param context            the {@link Context} of the application
     * @param holder             a {@link ContentProviderHolder} containing the provider instance and metadata
     * @param info               the {@link ProviderInfo} describing the provider's manifest declaration
     * @param noisy              if {@code true}, log diagnostic information during installation
     * @param noReleaseNeeded    if {@code true}, the provider does not need to be released when all clients disconnect
     * @param stable             if {@code true}, this is a stable reference that prevents the provider from being killed
     * @return the installed {@link ContentProviderHolder}
     * @throws RuntimeException always, as this is a stub implementation
     */
    public ContentProviderHolder installProvider(Context context, ContentProviderHolder holder, ProviderInfo info, boolean noisy,
                                                 boolean noReleaseNeeded, boolean stable) {
        throw new RuntimeException();
    }

    /**
     * Data structure holding binding information for the application, including the
     * loaded package and process configuration.
     */
    static final class AppBindData { }

    /**
     * Record holding information about an activity that is currently running in this
     * application process. Contains the activity instance, its token, metadata, and
     * launch intent.
     */
    public static final class ActivityClientRecord {
        /** The actual {@link Activity} instance. */
        public Activity activity;

        /** The {@link IBinder} token identifying this activity in the system. */
        public IBinder token;

        /** The {@link ActivityInfo} metadata parsed from the manifest. */
        public ActivityInfo activityInfo;

        /** The {@link Intent} used to launch this activity. */
        public Intent intent;
    }

    /**
     * Composite key used to look up content providers by their authority string and
     * the user ID under which they are running.
     */
    public static final class ProviderKey {
        /** The authority string declared in the provider's manifest. */
        public final String authority;

        /** The Android user ID for which this provider instance is running. */
        public final int userId;

        /**
         * Constructs a new {@code ProviderKey} with the given authority and user ID.
         *
         * @param authority the authority string of the content provider
         * @param userId    the Android user ID
         */
        public ProviderKey(String authority, int userId) {
            this.authority = authority;
            this.userId = userId;
        }

        /**
         * Compares this key to another object for equality. Two keys are equal if they
         * have the same authority string and user ID.
         *
         * @param o the object to compare with
         * @return {@code true} if the other object is a {@code ProviderKey} with matching
         *         authority and userId; {@code false} otherwise
         */
        @Override
        public boolean equals(Object o) {
            if (o instanceof ProviderKey) {
                final ProviderKey other = (ProviderKey) o;
                return Objects.equals(authority, other.authority) && userId == other.userId;
            }
            return false;
        }

        /**
         * Returns a hash code for this key, computed from the authority and user ID.
         *
         * @return the hash code value
         */
        @Override
        public int hashCode() {
            return ((authority != null) ? authority.hashCode() : 0) ^ userId;
        }
    }
}
