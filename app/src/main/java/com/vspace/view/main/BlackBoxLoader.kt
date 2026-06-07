package com.vspace.view.main

import android.app.Application
import android.content.Context
import com.vcore.BlackBoxCore
import com.vcore.app.BActivityThread.getUserId
import com.vcore.app.configuration.AppLifecycleCallback
import com.vcore.app.configuration.ClientConfiguration
import com.vcore.utils.Slog
import com.vspace.app.App
import com.vspace.biz.cache.AppSharedPreferenceDelegate
import java.io.File

/**
 * Loader responsible for initializing and configuring the [BlackBoxCore] engine.
 *
 * Manages persisted user preferences for root-hiding, Xposed-hiding, daemon service,
 * and shortcut-permission dialog visibility. Delegates base-context attachment and
 * lifecycle-event registration to the core library.
 */
class BlackBoxLoader {
    /** Whether root detection should be hidden from virtual apps. */
    private var mHideRoot by AppSharedPreferenceDelegate(App.getContext(), false)
    /** Whether Xposed framework detection should be hidden from virtual apps. */
    private var mHideXposed by AppSharedPreferenceDelegate(App.getContext(), false)
    /** Whether the background daemon service is enabled. */
    private var mDaemonEnable by AppSharedPreferenceDelegate(App.getContext(), false)
    /** Whether the shortcut-permission guidance dialog should be shown. */
    private var mShowShortcutPermissionDialog by AppSharedPreferenceDelegate(App.getContext(), true)

    /**
     * Returns the current root-hiding preference.
     *
     * @return true if root detection is hidden.
     */
    fun hideRoot(): Boolean {
        return mHideRoot
    }

    /**
     * Updates the root-hiding preference.
     *
     * @param hideRoot true to hide root detection from virtual apps.
     */
    fun invalidHideRoot(hideRoot: Boolean) {
        this.mHideRoot = hideRoot
    }

    /**
     * Returns the current Xposed-hiding preference.
     *
     * @return true if Xposed detection is hidden.
     */
    fun hideXposed(): Boolean {
        return mHideXposed
    }

    /**
     * Updates the Xposed-hiding preference.
     *
     * @param hideXposed true to hide Xposed detection from virtual apps.
     */
    fun invalidHideXposed(hideXposed: Boolean) {
        this.mHideXposed = hideXposed
    }

    /**
     * Returns the current daemon-service preference.
     *
     * @return true if the daemon service is enabled.
     */
    fun daemonEnable(): Boolean {
        return mDaemonEnable
    }

    /**
     * Updates the daemon-service preference.
     *
     * @param enable true to enable the background daemon service.
     */
    fun invalidDaemonEnable(enable: Boolean) {
        this.mDaemonEnable = enable
    }

    /**
     * Returns whether the shortcut-permission dialog should be shown.
     *
     * @return true if the dialog should be displayed.
     */
    fun showShortcutPermissionDialog(): Boolean {
        return mShowShortcutPermissionDialog
    }

    /**
     * Registers an [AppLifecycleCallback] with [BlackBoxCore] to log
     * application lifecycle events (before/after create) for debugging.
     */
    fun addLifecycleCallback() {
        BlackBoxCore.get().addAppLifecycleCallback(object : AppLifecycleCallback() {
            override fun beforeCreateApplication(packageName: String?, processName: String?, context: Context?, userId: Int) {
                Slog.d(TAG, "beforeCreateApplication: pkg $packageName, processName $processName, userID:${getUserId()}")
            }

            override fun beforeApplicationOnCreate(packageName: String?, processName: String?, application: Application?, userId: Int) {
                Slog.d(TAG, "beforeApplicationOnCreate: pkg $packageName, processName $processName")
            }

            override fun afterApplicationOnCreate(packageName: String?, processName: String?, application: Application?, userId: Int) {
                Slog.d(TAG, "afterApplicationOnCreate: pkg $packageName, processName $processName")
            }
        })
    }

    /**
     * Attaches the base context to [BlackBoxCore] with the current [ClientConfiguration],
     * applying persisted preferences for root/Xposed hiding and daemon mode.
     *
     * @param context the application [Context].
     */
    fun attachBaseContext(context: Context) {
        BlackBoxCore.get().doAttachBaseContext(context, object : ClientConfiguration() {
            override fun getHostPackageName(): String {
                return context.packageName
            }

            override fun isHideRoot(): Boolean {
                return mHideRoot
            }

            override fun isHideXposed(): Boolean {
                return mHideXposed
            }

            override fun isEnableDaemonService(): Boolean {
                return mDaemonEnable
            }

            override fun requestInstallPackage(file: File?, userId: Int): Boolean {
                return false
            }
        })
    }

    /**
     * Finalizes [BlackBoxCore] creation; must be called during [App.onCreate].
     */
    fun doOnCreate() {
        BlackBoxCore.get().doCreate()
    }

    companion object {
        val TAG: String = BlackBoxLoader::class.java.simpleName
    }
}
