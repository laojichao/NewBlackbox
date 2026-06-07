package com.vspace.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/**
 * Utility object providing Context extension functions.
 */
object ContextUtil {
    /**
     * Opens the system application details settings screen for the current application.
     *
     * Uses [Settings.ACTION_APPLICATION_DETAILS_SETTINGS] with the current package URI.
     */
    fun Context.openAppSystemSettings() {
        startActivity(Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            data = Uri.fromParts("package", packageName, null)
        })
    }
}
