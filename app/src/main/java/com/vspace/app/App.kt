package com.vspace.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/**
 * Custom [Application] subclass that serves as the entry point of the app.
 *
 * Responsible for initializing the global application [Context] and delegating
 * lifecycle events to [AppManager] for BlackBox core setup.
 */
class App : Application() {
    companion object {
        /** Holds the application-level context, set during [attachBaseContext]. */
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private lateinit var mContext: Context

        /**
         * Returns the global application [Context].
         *
         * @return the [Context] established during [attachBaseContext].
         */
        @JvmStatic
        fun getContext(): Context {
            return mContext
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        mContext = base!!
        AppManager.doAttachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        AppManager.doOnCreate()
    }
}
