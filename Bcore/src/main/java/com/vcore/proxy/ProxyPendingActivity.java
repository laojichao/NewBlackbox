package com.vcore.proxy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.vcore.app.BActivityThread;
import com.vcore.proxy.record.ProxyPendingRecord;
import com.vcore.utils.Slog;

/**
 * Proxy activity used for pending intent resolution in the virtual framework.
 * <p>
 * When launched (typically via a {@link android.app.PendingIntent}), this activity
 * immediately finishes itself and re-launches the real target activity from the
 * {@link ProxyPendingRecord} extracted from the intent. Adds
 * {@link Intent#FLAG_ACTIVITY_NEW_TASK} to ensure proper task stack behavior.
 * </p>
 */
public class ProxyPendingActivity extends Activity {
    /** Tag for logging. */
    public static final String TAG = "ProxyPendingActivity";

    /**
     * Called when the proxy pending activity is created. Extracts the
     * {@link ProxyPendingRecord} from the intent, finishes itself, and starts the
     * real target activity with the {@link Intent#FLAG_ACTIVITY_NEW_TASK} flag.
     *
     * @param savedInstanceState the saved instance state bundle, or {@code null} if none
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();

        ProxyPendingRecord pendingActivityRecord = ProxyPendingRecord.create(getIntent());
        Slog.d(TAG, "ProxyPendingActivity: " + pendingActivityRecord);
        if (pendingActivityRecord.mTarget == null) {
            return;
        }

        pendingActivityRecord.mTarget.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        pendingActivityRecord.mTarget.setExtrasClassLoader(BActivityThread.getApplication().getClassLoader());
        startActivity(pendingActivityRecord.mTarget);
    }

    /** Proxy stub subclass P0 registered in the host manifest. */
    public static class P0 extends ProxyPendingActivity { }

    /** Proxy stub subclass P1 registered in the host manifest. */
    public static class P1 extends ProxyPendingActivity { }

    /** Proxy stub subclass P2 registered in the host manifest. */
    public static class P2 extends ProxyPendingActivity { }

    /** Proxy stub subclass P3 registered in the host manifest. */
    public static class P3 extends ProxyPendingActivity { }

    /** Proxy stub subclass P4 registered in the host manifest. */
    public static class P4 extends ProxyPendingActivity { }

    /** Proxy stub subclass P5 registered in the host manifest. */
    public static class P5 extends ProxyPendingActivity { }

    /** Proxy stub subclass P6 registered in the host manifest. */
    public static class P6 extends ProxyPendingActivity { }

    /** Proxy stub subclass P7 registered in the host manifest. */
    public static class P7 extends ProxyPendingActivity { }

    /** Proxy stub subclass P8 registered in the host manifest. */
    public static class P8 extends ProxyPendingActivity { }

    /** Proxy stub subclass P9 registered in the host manifest. */
    public static class P9 extends ProxyPendingActivity { }

    /** Proxy stub subclass P10 registered in the host manifest. */
    public static class P10 extends ProxyPendingActivity { }

    /** Proxy stub subclass P11 registered in the host manifest. */
    public static class P11 extends ProxyPendingActivity { }

    /** Proxy stub subclass P12 registered in the host manifest. */
    public static class P12 extends ProxyPendingActivity { }

    /** Proxy stub subclass P13 registered in the host manifest. */
    public static class P13 extends ProxyPendingActivity { }

    /** Proxy stub subclass P14 registered in the host manifest. */
    public static class P14 extends ProxyPendingActivity { }

    /** Proxy stub subclass P15 registered in the host manifest. */
    public static class P15 extends ProxyPendingActivity { }

    /** Proxy stub subclass P16 registered in the host manifest. */
    public static class P16 extends ProxyPendingActivity { }

    /** Proxy stub subclass P17 registered in the host manifest. */
    public static class P17 extends ProxyPendingActivity { }

    /** Proxy stub subclass P18 registered in the host manifest. */
    public static class P18 extends ProxyPendingActivity { }

    /** Proxy stub subclass P19 registered in the host manifest. */
    public static class P19 extends ProxyPendingActivity { }

    /** Proxy stub subclass P20 registered in the host manifest. */
    public static class P20 extends ProxyPendingActivity { }

    /** Proxy stub subclass P21 registered in the host manifest. */
    public static class P21 extends ProxyPendingActivity { }

    /** Proxy stub subclass P22 registered in the host manifest. */
    public static class P22 extends ProxyPendingActivity { }

    /** Proxy stub subclass P23 registered in the host manifest. */
    public static class P23 extends ProxyPendingActivity { }

    /** Proxy stub subclass P24 registered in the host manifest. */
    public static class P24 extends ProxyPendingActivity { }

    /** Proxy stub subclass P25 registered in the host manifest. */
    public static class P25 extends ProxyPendingActivity { }

    /** Proxy stub subclass P26 registered in the host manifest. */
    public static class P26 extends ProxyPendingActivity { }

    /** Proxy stub subclass P27 registered in the host manifest. */
    public static class P27 extends ProxyPendingActivity { }

    /** Proxy stub subclass P28 registered in the host manifest. */
    public static class P28 extends ProxyPendingActivity { }

    /** Proxy stub subclass P29 registered in the host manifest. */
    public static class P29 extends ProxyPendingActivity { }

    /** Proxy stub subclass P30 registered in the host manifest. */
    public static class P30 extends ProxyPendingActivity { }

    /** Proxy stub subclass P31 registered in the host manifest. */
    public static class P31 extends ProxyPendingActivity { }

    /** Proxy stub subclass P32 registered in the host manifest. */
    public static class P32 extends ProxyPendingActivity { }

    /** Proxy stub subclass P33 registered in the host manifest. */
    public static class P33 extends ProxyPendingActivity { }

    /** Proxy stub subclass P34 registered in the host manifest. */
    public static class P34 extends ProxyPendingActivity { }

    /** Proxy stub subclass P35 registered in the host manifest. */
    public static class P35 extends ProxyPendingActivity { }

    /** Proxy stub subclass P36 registered in the host manifest. */
    public static class P36 extends ProxyPendingActivity { }

    /** Proxy stub subclass P37 registered in the host manifest. */
    public static class P37 extends ProxyPendingActivity { }

    /** Proxy stub subclass P38 registered in the host manifest. */
    public static class P38 extends ProxyPendingActivity { }

    /** Proxy stub subclass P39 registered in the host manifest. */
    public static class P39 extends ProxyPendingActivity { }

    /** Proxy stub subclass P40 registered in the host manifest. */
    public static class P40 extends ProxyPendingActivity { }

    /** Proxy stub subclass P41 registered in the host manifest. */
    public static class P41 extends ProxyPendingActivity { }

    /** Proxy stub subclass P42 registered in the host manifest. */
    public static class P42 extends ProxyPendingActivity { }

    /** Proxy stub subclass P43 registered in the host manifest. */
    public static class P43 extends ProxyPendingActivity { }

    /** Proxy stub subclass P44 registered in the host manifest. */
    public static class P44 extends ProxyPendingActivity { }

    /** Proxy stub subclass P45 registered in the host manifest. */
    public static class P45 extends ProxyPendingActivity { }

    /** Proxy stub subclass P46 registered in the host manifest. */
    public static class P46 extends ProxyPendingActivity { }

    /** Proxy stub subclass P47 registered in the host manifest. */
    public static class P47 extends ProxyPendingActivity { }

    /** Proxy stub subclass P48 registered in the host manifest. */
    public static class P48 extends ProxyPendingActivity { }

    /** Proxy stub subclass P49 registered in the host manifest. */
    public static class P49 extends ProxyPendingActivity { }
}
