package com.vcore.core.system.am;

import android.content.BroadcastReceiver;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vcore.BlackBoxCore;
import com.vcore.core.system.pm.BPackage;
import com.vcore.core.system.pm.BPackageManagerService;
import com.vcore.core.system.pm.BPackageSettings;
import com.vcore.core.system.pm.PackageMonitor;
import com.vcore.entity.am.PendingResultData;
import com.vcore.proxy.ProxyBroadcastReceiver;
import com.vcore.utils.Slog;

/**
 * Manages broadcast receivers within the virtual environment.
 *
 * <p>This class registers proxy broadcast receivers for all declared receivers in installed
 * virtual packages. It listens for package install/uninstall events to keep receiver
 * registrations in sync. It also manages broadcast timeouts to prevent broadcasts from
 * hanging indefinitely if receivers do not complete in time.</p>
 *
 * <p>Implements {@link PackageMonitor} to receive package lifecycle callbacks.</p>
 */
public class BroadcastManager implements PackageMonitor {
    public static final String TAG = "BroadcastManager";

    /** Timeout in milliseconds before a broadcast is considered timed out. */
    public static final int TIMEOUT = 9000;

    /** Message ID for the broadcast timeout handler. */
    public static final int MSG_TIME_OUT = 1;

    /** Volatile singleton instance for thread-safe lazy initialization. */
    private static volatile BroadcastManager sBroadcastManager;

    /** The package manager service used for querying package settings. */
    private final BPackageManagerService mPms;

    /** Maps package names to their registered proxy broadcast receivers. */
    private final Map<String, List<BroadcastReceiver>> mReceivers = new HashMap<>();

    /** Maps broadcast tokens to their pending result data for timeout tracking. */
    private final Map<String, PendingResultData> mReceiversData = new HashMap<>();

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_TIME_OUT) {
                try {
                    PendingResultData data = (PendingResultData) msg.obj;
                    data.build().finish();
                    Slog.d(TAG, "Timeout Receiver: " + data);
                } catch (Throwable ignore) { }
            }
        }
    };

    /**
     * Returns or creates the singleton BroadcastManager instance.
     *
     * @param pms the package manager service
     * @return the singleton {@link BroadcastManager} instance
     */
    public static BroadcastManager startSystem(BPackageManagerService pms) {
        if (sBroadcastManager == null) {
            synchronized (BroadcastManager.class) {
                if (sBroadcastManager == null) {
                    sBroadcastManager = new BroadcastManager(pms);
                }
            }
        }
        return sBroadcastManager;
    }

    /**
     * Constructs a BroadcastManager with the given package manager service.
     *
     * @param pms the package manager service for querying package settings
     */
    public BroadcastManager(BPackageManagerService pms) {
        this.mPms = pms;
    }

    /**
     * Starts the broadcast manager by registering as a package monitor and
     * registering broadcast receivers for all currently installed packages.
     */
    public void startup() {
        mPms.addPackageMonitor(this);
        List<BPackageSettings> bPackageSettings = mPms.getBPackageSettings();
        for (BPackageSettings bPackageSetting : bPackageSettings) {
            BPackage bPackage = bPackageSetting.pkg;
            registerPackage(bPackage);
        }
    }

    /**
     * Registers proxy broadcast receivers for all declared receivers in the given package.
     *
     * @param bPackage the package whose receivers should be registered
     */
    private void registerPackage(BPackage bPackage) {
        synchronized (mReceivers) {
            Slog.d(TAG, "register: " + bPackage.packageName + ", size: " + bPackage.receivers.size());
            for (BPackage.Activity receiver : bPackage.receivers) {
                List<BPackage.ActivityIntentInfo> intents = receiver.intents;
                for (BPackage.ActivityIntentInfo intent : intents) {
                    ProxyBroadcastReceiver proxyBroadcastReceiver = new ProxyBroadcastReceiver();
                    BlackBoxCore.getContext().registerReceiver(proxyBroadcastReceiver, intent.intentFilter);
                    addReceiver(bPackage.packageName, proxyBroadcastReceiver);
                }
            }
        }
    }

    /**
     * Adds a broadcast receiver to the receiver map for the given package.
     *
     * @param packageName the package name to associate the receiver with
     * @param receiver    the broadcast receiver to add
     */
    private void addReceiver(String packageName, BroadcastReceiver receiver) {
        List<BroadcastReceiver> broadcastReceivers = mReceivers.get(packageName);
        if (broadcastReceivers == null) {
            broadcastReceivers = new ArrayList<>();
            mReceivers.put(packageName, broadcastReceivers);
        }
        broadcastReceivers.add(receiver);
    }

    /**
     * Sends a broadcast by storing the pending result data and setting a timeout.
     * If the broadcast is not finished within {@link #TIMEOUT}, it will be auto-finished.
     *
     * @param pendingResultData the pending result data for the broadcast
     */
    public void sendBroadcast(PendingResultData pendingResultData) {
        synchronized (mReceiversData) {
            mReceiversData.put(pendingResultData.mBToken, pendingResultData);
            Message obtain = Message.obtain(mHandler, MSG_TIME_OUT, pendingResultData);
            mHandler.sendMessageDelayed(obtain, TIMEOUT);
        }
    }

    /**
     * Finishes a broadcast by removing its timeout message.
     *
     * @param data the pending result data of the completed broadcast
     */
    public void finishBroadcast(PendingResultData data) {
        synchronized (mReceiversData) {
            mHandler.removeMessages(MSG_TIME_OUT, mReceiversData.get(data.mBToken));
        }
    }

    @Override
    public void onPackageUninstalled(String packageName, boolean removeApp, int userId) {
        if (removeApp) {
            synchronized (mReceivers) {
                List<BroadcastReceiver> broadcastReceivers = mReceivers.get(packageName);
                if (broadcastReceivers != null) {
                    Slog.d(TAG, "unregisterReceiver Package: " + packageName + ", size: " + broadcastReceivers.size());
                    for (BroadcastReceiver broadcastReceiver : broadcastReceivers) {
                        try {
                            BlackBoxCore.getContext().unregisterReceiver(broadcastReceiver);
                        } catch (Throwable ignored) { }
                    }
                }
                mReceivers.remove(packageName);
            }
        }
    }

    @Override
    public void onPackageInstalled(String packageName, int userId) {
        synchronized (mReceivers) {
            mReceivers.remove(packageName);
            BPackageSettings bPackageSetting = mPms.getBPackageSetting(packageName);
            if (bPackageSetting != null) {
                registerPackage(bPackageSetting.pkg);
            }
        }
    }
}
