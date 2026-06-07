package com.vcore.core.system.am;

import java.util.Objects;

/**
 * Represents a pending intent sender record within the virtual environment.
 *
 * <p>Tracks the UID and package name associated with a pending intent, enabling
 * the system to resolve the originator of pending intents for permission checks
 * and package attribution.</p>
 */
public class PendingIntentRecord {

    /** The UID of the application that created the pending intent. */
    public int uid;

    /** The package name of the application that created the pending intent. */
    public String packageName;

    /**
     * Compares this record to another object for equality based on UID and package name.
     *
     * @param o the object to compare with
     * @return true if the object is a {@link PendingIntentRecord} with the same uid and packageName
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof PendingIntentRecord)) {
            return false;
        }

        PendingIntentRecord that = (PendingIntentRecord) o;
        return uid == that.uid && Objects.equals(packageName, that.packageName);
    }

    /**
     * Returns a hash code based on uid and packageName.
     *
     * @return the hash code for this record
     */
    @Override
    public int hashCode() {
        return Objects.hash(uid, packageName);
    }
}
