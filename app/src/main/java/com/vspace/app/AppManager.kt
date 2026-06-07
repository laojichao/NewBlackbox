package com.vspace.app

import android.content.Context
import android.content.SharedPreferences
import com.vspace.view.main.BlackBoxLoader

/**
 * Singleton application manager that coordinates BlackBox loader initialization
 * and provides shared resources such as user remark preferences.
 *
 * All lifecycle delegation from [App] flows through this object.
 */
object AppManager {
    /** Lazily-initialized [BlackBoxLoader] responsible for core engine setup. */
    @JvmStatic
    val mBlackBoxLoader by lazy {
        BlackBoxLoader()
    }

    /** Lazily-initialized [SharedPreferences] for storing user remark/alias data. */
    @JvmStatic
    val mRemarkSharedPreferences: SharedPreferences by lazy {
        App.getContext().getSharedPreferences("UserRemark", Context.MODE_PRIVATE)
    }

    /**
     * Called during [App.attachBaseContext] to initialize the BlackBox core engine.
     *
     * Attaches the base context to [BlackBoxLoader] and registers lifecycle callbacks.
     *
     * @param context the application [Context] provided during attach.
     */
    fun doAttachBaseContext(context: Context) {
        try {
            mBlackBoxLoader.attachBaseContext(context)
            mBlackBoxLoader.addLifecycleCallback()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Called during [App.onCreate] to finalize core creation and third-party service init.
     */
    fun doOnCreate() {
        mBlackBoxLoader.doOnCreate()
        initThirdService()
    }

    /** Placeholder for future third-party service initialization. */
    private fun initThirdService() { }
}
