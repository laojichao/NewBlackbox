package com.vcore.utils.compat;

import android.content.Context;
import android.content.ContextWrapper;

import black.android.app.ContextImpl;
import black.android.app.ContextImplKitkat;
import black.android.content.AttributionSource;
import black.android.content.AttributionSourceState;
import black.android.content.ContentResolver;
import com.vcore.BlackBoxCore;
import com.vcore.app.BActivityThread;

/**
 * Compatibility utility for patching Android {@link Context} internals to use the host
 * application's identity instead of the virtual application's. This ensures that system
 * services, package manager queries, and permission checks see the host app's package name
 * and UID rather than the virtualized app's.
 * <p>
 * On Android 12 S (API 31+), also fixes the {@code AttributionSourceState} chain which
 * is used for permission and privacy tracking.
 * </p>
 */
public class ContextCompat {
    /** Logging tag for this class. */
    public static final String TAG = "ContextCompat";

    /**
     * Recursively fixes the {@code AttributionSourceState} chain by replacing the package name
     * and UID with the host application's values. This ensures that permission checks and
     * privacy attributions reference the host app rather than the virtualized app.
     *
     * @param obj the {@code AttributionSource} object to fix; may be {@code null}
     * @param uid the host UID to set in the attribution source state
     */
    public static void fixAttributionSourceState(Object obj, int uid) {
        Object mAttributionSourceState;
        if (obj != null && AttributionSource.mAttributionSourceState != null) {
            mAttributionSourceState = AttributionSource.mAttributionSourceState.get(obj);

            AttributionSourceState.packageName.set(mAttributionSourceState, BlackBoxCore.getHostPkg());
            AttributionSourceState.uid.set(mAttributionSourceState, uid);
            fixAttributionSourceState(AttributionSource.getNext.call(obj), uid);
        }
    }

    /**
     * Patches the base context of a {@link ContextWrapper} chain to use the host application's
     * package name and related metadata. Unwraps nested {@link ContextWrapper}s up to 10 levels
     * deep, then modifies the underlying {@code ContextImpl} fields:
     * <ul>
     *   <li>Resets the cached PackageManager to force re-initialization</li>
     *   <li>Sets the base package name to the host package</li>
     *   <li>Patches the operation package name (KitKat+)</li>
     *   <li>Patches the ContentResolver's package name</li>
     *   <li>On Android 12+, fixes the AttributionSource state chain</li>
     * </ul>
     *
     * @param context the context to fix; expected to be a {@link ContextWrapper} whose
     *                ultimate base is a {@code ContextImpl}
     */
    public static void fix(Context context) {
        try {
            int deep = 0;
            while (context instanceof ContextWrapper) {
                context = ((ContextWrapper) context).getBaseContext();
                deep++;
                if (deep >= 10) {
                    return;
                }
            }

            ContextImpl.mPackageManager.set(context, null);
            try {
                context.getPackageManager();
            } catch (Throwable e) {
                e.printStackTrace();
            }

            ContextImpl.mBasePackageName.set(context, BlackBoxCore.getHostPkg());
            ContextImplKitkat.mOpPackageName.set(context, BlackBoxCore.getHostPkg());
            ContentResolver.mPackageName.set(context.getContentResolver(), BlackBoxCore.getHostPkg());

            if (BuildCompat.isS()) {
                fixAttributionSourceState(ContextImpl.getAttributionSource.call(context), BActivityThread.getBUid());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
