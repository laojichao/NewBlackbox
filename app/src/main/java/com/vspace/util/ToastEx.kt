package com.vspace.util

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import com.vspace.app.App
import com.vspace.util.ResUtil.getString

/**
 * Utility object providing convenient Toast display functions.
 *
 * Maintains a single [Toast] instance to prevent overlapping toasts from
 * queuing up on rapid invocations.
 */
object ToastEx {
    /** The currently showing toast instance; cancelled before showing a new one. */
    private var toastImpl:Toast? = null

    /**
     * Extension function on [Context] that shows a short-duration [Toast].
     * Cancels any previously showing toast first.
     *
     * @param msg the message string to display.
     */
    fun Context.toast(msg:String) {
        toastImpl?.cancel()
        toastImpl = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
        toastImpl?.show()
    }

    /**
     * Shows a toast using the global application context.
     *
     * @param msg the message string to display.
     */
    fun toast(msg: String) {
        App.getContext().toast(msg)
    }

    /**
     * Shows a toast by resolving a string resource ID.
     *
     * @param msgID the string resource ID to display.
     */
    fun toast(@StringRes msgID:Int) {
        toast(getString(msgID))
    }
}
