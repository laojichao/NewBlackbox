package com.vcore.core.system.am;

import android.os.IBinder;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a per-user space in the virtual activity manager system.
 *
 * <p>Each user space encapsulates the activity manager components that are scoped
 * to a specific virtual user: active services, the activity stack, and pending
 * intent sender records. This provides isolation between different virtual users.</p>
 */
public class UserSpace {

    /** The active services manager for this user space (singleton per user). */
    public final ActiveServices mActiveServices = new ActiveServices();

    /** The activity stack manager for this user space (singleton per user). */
    public final ActivityStack mStack = new ActivityStack();

    /** Maps IBinder tokens to their PendingIntentRecord for this user space. */
    public final Map<IBinder, PendingIntentRecord> mIntentSenderRecords = new HashMap<>();
}
