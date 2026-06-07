package black.android.content;

import android.os.Bundle;
import android.os.IBinder;

import black.Reflector;

/**
 * Reflection wrapper for hidden methods and inner classes of {@code android.content.BroadcastReceiver}.
 * Provides access to the PendingResult mechanism that allows receivers to propagate
 * results and manage ordered broadcast state.
 */
public class BroadcastReceiver {
    public static final Reflector REF = Reflector.on("android.content.BroadcastReceiver");

    /**
     * Returns the PendingResult for this broadcast receiver, if any.
     *
     * @return the PendingResult, or null if no pending result exists
     */
    public static Reflector.MethodWrapper<android.content.BroadcastReceiver.PendingResult> getPendingResult = REF.method("getPendingResult");

    /**
     * Sets the PendingResult for this broadcast receiver.
     *
     * @param pendingResult the PendingResult to set
     */
    public static Reflector.MethodWrapper<Void> setPendingResult = REF.method("setPendingResult", Reflector.findClass("android.content.BroadcastReceiver$PendingResult"));

    /**
     * Reflection wrapper for {@code android.content.BroadcastReceiver$PendingResult} on Android M+ (API 23+).
     * Contains additional mFlags field compared to older versions.
     */
    public static class PendingResultM {
        public static final Reflector REF = Reflector.on("android.content.BroadcastReceiver$PendingResult");

        /**
         * Creates a new PendingResult instance (M+ variant with mFlags).
         *
         * @param resultCode        the result code
         * @param resultData        the result data string
         * @param resultExtras      the result extras Bundle
         * @param type              the pending result type
         * @param orderedHint       whether this is an ordered broadcast
         * @param stickyHint        whether this is a sticky broadcast
         * @param token             the IBinder token
         * @param sendingUser       the sending user ID
         * @param flags             the pending result flags
         */
        public static Reflector.ConstructorWrapper<android.content.BroadcastReceiver.PendingResult> _new = REF.constructor(int.class, String.class, Bundle.class, int.class, boolean.class, boolean.class, IBinder.class, int.class, int.class);

        /** Whether the broadcast has been aborted. */
        public static Reflector.FieldWrapper<Boolean> mAbortBroadcast = REF.field("mAbortBroadcast");

        /** Whether this pending result has finished. */
        public static Reflector.FieldWrapper<Boolean> mFinished = REF.field("mFinished");

        /** The flags associated with this pending result. */
        public static Reflector.FieldWrapper<Integer> mFlags = REF.field("mFlags");

        /** Whether this was an initial sticky broadcast hint. */
        public static Reflector.FieldWrapper<Boolean> mInitialStickyHint = REF.field("mInitialStickyHint");

        /** Whether this is an ordered broadcast hint. */
        public static Reflector.FieldWrapper<Boolean> mOrderedHint = REF.field("mOrderedHint");

        /** The result data string. */
        public static Reflector.FieldWrapper<String> mResultData = REF.field("mResultData");

        /** The result extras Bundle. */
        public static Reflector.FieldWrapper<Bundle> mResultExtras = REF.field("mResultExtras");

        /** The user ID that sent the broadcast. */
        public static Reflector.FieldWrapper<Integer> mSendingUser = REF.field("mSendingUser");

        /** The IBinder token for this pending result. */
        public static Reflector.FieldWrapper<IBinder> mToken = REF.field("mToken");

        /** The pending result type constant. */
        public static Reflector.FieldWrapper<Integer> mType = REF.field("mType");
    }

    /**
     * Reflection wrapper for {@code android.content.BroadcastReceiver$PendingResult} on pre-M versions.
     * Standard pending result without the mFlags field.
     */
    public static class PendingResult {
        public static final Reflector REF = Reflector.on("android.content.BroadcastReceiver$PendingResult");

        /**
         * Creates a new PendingResult instance (pre-M variant without mFlags).
         *
         * @param resultCode        the result code
         * @param resultData        the result data string
         * @param resultExtras      the result extras Bundle
         * @param type              the pending result type
         * @param orderedHint       whether this is an ordered broadcast
         * @param stickyHint        whether this is a sticky broadcast
         * @param token             the IBinder token
         * @param sendingUser       the sending user ID
         */
        public static Reflector.ConstructorWrapper<android.content.BroadcastReceiver.PendingResult> _new = REF.constructor(int.class, String.class, Bundle.class, int.class, boolean.class, boolean.class, IBinder.class, int.class);

        /** Whether the broadcast has been aborted. */
        public static Reflector.FieldWrapper<Boolean> mAbortBroadcast = REF.field("mAbortBroadcast");

        /** Whether this pending result has finished. */
        public static Reflector.FieldWrapper<Boolean> mFinished = REF.field("mFinished");

        /** Whether this was an initial sticky broadcast hint. */
        public static Reflector.FieldWrapper<Boolean> mInitialStickyHint = REF.field("mInitialStickyHint");

        /** Whether this is an ordered broadcast hint. */
        public static Reflector.FieldWrapper<Boolean> mOrderedHint = REF.field("mOrderedHint");

        /** The result data string. */
        public static Reflector.FieldWrapper<String> mResultData = REF.field("mResultData");

        /** The result extras Bundle. */
        public static Reflector.FieldWrapper<Bundle> mResultExtras = REF.field("mResultExtras");

        /** The user ID that sent the broadcast. */
        public static Reflector.FieldWrapper<Integer> mSendingUser = REF.field("mSendingUser");

        /** The IBinder token for this pending result. */
        public static Reflector.FieldWrapper<IBinder> mToken = REF.field("mToken");

        /** The pending result type constant. */
        public static Reflector.FieldWrapper<Integer> mType = REF.field("mType");
    }
}
