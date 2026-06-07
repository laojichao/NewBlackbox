package com.vcore.proxy;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.vcore.app.BActivityThread;
import com.vcore.fake.hook.HookManager;
import com.vcore.fake.service.HCallbackProxy;
import com.vcore.proxy.record.ProxyActivityRecord;

/**
 * Proxy activity that acts as a stub entry point in the host application manifest.
 * <p>
 * When launched, it immediately finishes itself and delegates to the real target activity
 * by reading the {@link ProxyActivityRecord} from the incoming intent. This is the core
 * mechanism that allows the virtual framework to launch activities of guest applications
 * through proxy stubs registered in the host manifest.
 * </p>
 */
public class ProxyActivity extends Activity {
    /** Tag for logging. */
    public static final String TAG = "ProxyActivity";

    /**
     * Called when the proxy activity is created. Extracts the {@link ProxyActivityRecord}
     * from the intent, ensures the hook environment is initialized, sets the correct
     * class loader on the target intent extras, and starts the real target activity.
     * The proxy activity finishes itself immediately after delegation.
     *
     * @param savedInstanceState the saved instance state bundle, or {@code null} if none
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        finish();

        HookManager.get().checkEnv(HCallbackProxy.class);
        ProxyActivityRecord record = ProxyActivityRecord.create(getIntent());
        if (record.mTarget != null) {
            record.mTarget.setExtrasClassLoader(BActivityThread.getApplication().getClassLoader());
            startActivity(record.mTarget);
        }
    }

    /** Proxy stub subclass P0 registered in the host manifest. */
    public static class P0 extends ProxyActivity { }

    /** Proxy stub subclass P1 registered in the host manifest. */
    public static class P1 extends ProxyActivity { }

    /** Proxy stub subclass P2 registered in the host manifest. */
    public static class P2 extends ProxyActivity { }

    /** Proxy stub subclass P3 registered in the host manifest. */
    public static class P3 extends ProxyActivity { }

    /** Proxy stub subclass P4 registered in the host manifest. */
    public static class P4 extends ProxyActivity { }

    /** Proxy stub subclass P5 registered in the host manifest. */
    public static class P5 extends ProxyActivity { }

    /** Proxy stub subclass P6 registered in the host manifest. */
    public static class P6 extends ProxyActivity { }

    /** Proxy stub subclass P7 registered in the host manifest. */
    public static class P7 extends ProxyActivity { }

    /** Proxy stub subclass P8 registered in the host manifest. */
    public static class P8 extends ProxyActivity { }

    /** Proxy stub subclass P9 registered in the host manifest. */
    public static class P9 extends ProxyActivity { }

    /** Proxy stub subclass P10 registered in the host manifest. */
    public static class P10 extends ProxyActivity { }

    /** Proxy stub subclass P11 registered in the host manifest. */
    public static class P11 extends ProxyActivity { }

    /** Proxy stub subclass P12 registered in the host manifest. */
    public static class P12 extends ProxyActivity { }

    /** Proxy stub subclass P13 registered in the host manifest. */
    public static class P13 extends ProxyActivity { }

    /** Proxy stub subclass P14 registered in the host manifest. */
    public static class P14 extends ProxyActivity { }

    /** Proxy stub subclass P15 registered in the host manifest. */
    public static class P15 extends ProxyActivity { }

    /** Proxy stub subclass P16 registered in the host manifest. */
    public static class P16 extends ProxyActivity { }

    /** Proxy stub subclass P17 registered in the host manifest. */
    public static class P17 extends ProxyActivity { }

    /** Proxy stub subclass P18 registered in the host manifest. */
    public static class P18 extends ProxyActivity { }

    /** Proxy stub subclass P19 registered in the host manifest. */
    public static class P19 extends ProxyActivity { }

    /** Proxy stub subclass P20 registered in the host manifest. */
    public static class P20 extends ProxyActivity { }

    /** Proxy stub subclass P21 registered in the host manifest. */
    public static class P21 extends ProxyActivity { }

    /** Proxy stub subclass P22 registered in the host manifest. */
    public static class P22 extends ProxyActivity { }

    /** Proxy stub subclass P23 registered in the host manifest. */
    public static class P23 extends ProxyActivity { }

    /** Proxy stub subclass P24 registered in the host manifest. */
    public static class P24 extends ProxyActivity { }

    /** Proxy stub subclass P25 registered in the host manifest. */
    public static class P25 extends ProxyActivity { }

    /** Proxy stub subclass P26 registered in the host manifest. */
    public static class P26 extends ProxyActivity { }

    /** Proxy stub subclass P27 registered in the host manifest. */
    public static class P27 extends ProxyActivity { }

    /** Proxy stub subclass P28 registered in the host manifest. */
    public static class P28 extends ProxyActivity { }

    /** Proxy stub subclass P29 registered in the host manifest. */
    public static class P29 extends ProxyActivity { }

    /** Proxy stub subclass P30 registered in the host manifest. */
    public static class P30 extends ProxyActivity { }

    /** Proxy stub subclass P31 registered in the host manifest. */
    public static class P31 extends ProxyActivity { }

    /** Proxy stub subclass P32 registered in the host manifest. */
    public static class P32 extends ProxyActivity { }

    /** Proxy stub subclass P33 registered in the host manifest. */
    public static class P33 extends ProxyActivity { }

    /** Proxy stub subclass P34 registered in the host manifest. */
    public static class P34 extends ProxyActivity { }

    /** Proxy stub subclass P35 registered in the host manifest. */
    public static class P35 extends ProxyActivity { }

    /** Proxy stub subclass P36 registered in the host manifest. */
    public static class P36 extends ProxyActivity { }

    /** Proxy stub subclass P37 registered in the host manifest. */
    public static class P37 extends ProxyActivity { }

    /** Proxy stub subclass P38 registered in the host manifest. */
    public static class P38 extends ProxyActivity { }

    /** Proxy stub subclass P39 registered in the host manifest. */
    public static class P39 extends ProxyActivity { }

    /** Proxy stub subclass P40 registered in the host manifest. */
    public static class P40 extends ProxyActivity { }

    /** Proxy stub subclass P41 registered in the host manifest. */
    public static class P41 extends ProxyActivity { }

    /** Proxy stub subclass P42 registered in the host manifest. */
    public static class P42 extends ProxyActivity { }

    /** Proxy stub subclass P43 registered in the host manifest. */
    public static class P43 extends ProxyActivity { }

    /** Proxy stub subclass P44 registered in the host manifest. */
    public static class P44 extends ProxyActivity { }

    /** Proxy stub subclass P45 registered in the host manifest. */
    public static class P45 extends ProxyActivity { }

    /** Proxy stub subclass P46 registered in the host manifest. */
    public static class P46 extends ProxyActivity { }

    /** Proxy stub subclass P47 registered in the host manifest. */
    public static class P47 extends ProxyActivity { }

    /** Proxy stub subclass P48 registered in the host manifest. */
    public static class P48 extends ProxyActivity { }

    /** Proxy stub subclass P49 registered in the host manifest. */
    public static class P49 extends ProxyActivity { }
}
