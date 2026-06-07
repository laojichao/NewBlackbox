package com.vcore.core.system.am

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Binder
import android.os.IBinder
import com.vcore.core.system.ProcessRecord
import java.util.UUID

/**
 * Represents an activity record within the virtual core framework.
 *
 * Tracks the state and metadata of a running activity instance in a sandboxed
 * environment. Each [ActivityRecord] extends [Binder] to serve as a unique
 * token identifying the activity in the activity manager system.
 *
 * This is analogous to Android's internal `ActivityRecord` used by the
 * `ActivityManagerService` to manage activity lifecycle and task stacks.
 *
 * @see TaskRecord
 * @see ProcessRecord
 */
class ActivityRecord : Binder() {
    /** The [TaskRecord] this activity belongs to, or `null` if not yet assigned. */
    @JvmField
    var task: TaskRecord? = null

    /** The [IBinder] token uniquely identifying this activity instance. */
    @JvmField
    var token: IBinder? = null

    /** The token of the activity that started this one for a result, or `null`. */
    @JvmField
    var resultTo: IBinder? = null

    /** The [ActivityInfo] metadata parsed from the manifest for this activity. */
    @JvmField
    var info: ActivityInfo? = null

    /** The [ComponentName] of this activity (package name + class name). */
    @JvmField
    var component: ComponentName? = null

    /** The [Intent] used to launch this activity. */
    @JvmField
    var intent: Intent? = null

    /** The Android user ID under which this activity is running. */
    @JvmField
    var userId = 0

    /** Whether this activity has finished execution. */
    @JvmField
    var finished = false

    /** The [ProcessRecord] hosting this activity, or `null` if not yet assigned. */
    @JvmField
    var processRecord: ProcessRecord? = null

    /** A unique string token generated at creation time for internal identification. */
    @JvmField
    var mBToken: String? = null

    companion object {
        /**
         * Factory method that creates a new [ActivityRecord] populated with the
         * provided activity metadata.
         *
         * @param intent    the [Intent] used to launch the activity, or `null`
         * @param info      the [ActivityInfo] describing the activity from the manifest
         * @param resultTo  the [IBinder] token of the calling activity expecting a result, or `null`
         * @param userId    the Android user ID for the activity
         * @return a new [ActivityRecord] instance with a generated unique token
         */
        @JvmStatic
        fun create(intent: Intent?, info: ActivityInfo, resultTo: IBinder?, userId: Int): ActivityRecord {
            val record = ActivityRecord()
            record.intent = intent
            record.info = info
            record.component = ComponentName(info.packageName, info.name)
            record.resultTo = resultTo
            record.userId = userId
            record.mBToken = UUID.randomUUID().toString()
            return record
        }
    }
}