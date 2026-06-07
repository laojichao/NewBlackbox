package com.vspace.util

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.vspace.R
import com.vspace.app.App
import com.vspace.app.AppManager
import com.vspace.bean.AppInfo
import com.vspace.util.ContextUtil.openAppSystemSettings
import com.vspace.util.ToastEx.toast
import com.vspace.view.main.ShortcutActivity

/**
 * Utility object for creating Android home-screen shortcuts that launch apps
 * inside the virtual environment via [ShortcutActivity].
 */
object ShortcutUtil {
    /**
     * Creates a pinned home-screen shortcut for the specified app inside a virtual user.
     *
     * Displays a dialog allowing the user to customize the shortcut label. If pinning
     * is not supported by the launcher, a toast is shown instead.
     *
     * @param context the [Context] used to show dialogs and register the shortcut.
     * @param userID the virtual user ID the shortcut targets.
     * @param info the [AppInfo] for the application to create a shortcut for.
     */
    fun createShortcut(context: Context,userID: Int, info: AppInfo) {
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            val labelName = info.name + userID
            val intent = Intent(context, ShortcutActivity::class.java)
                .setAction(Intent.ACTION_MAIN)
                .putExtra("pkg", info.packageName)
                .putExtra("userId", userID)

            MaterialDialog(context).show {
                title(res = R.string.app_shortcut)
                input(
                    hintRes = R.string.shortcut_name,
                    prefill = labelName
                ) { _, input ->
                    val shortcutInfo: ShortcutInfoCompat =
                        ShortcutInfoCompat.Builder(context, info.packageName + userID)
                            .setIntent(intent)
                            .setShortLabel(input)
                            .setLongLabel(input)
                            .setIcon(IconCompat.createWithBitmap(info.icon.toBitmap()))
                            .build()

                    ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
                    showAllowPermissionDialog(context)
                }
                positiveButton(R.string.done)
                negativeButton(R.string.cancel)
            }
        } else {
            toast(R.string.cannot_create_shortcut)
        }
    }

    /**
     * Shows a dialog prompting the user to grant the "Add shortcut" permission
     * if the first attempt was denied. Offers a button to open app settings directly.
     *
     * @param context the [Context] used to show the dialog.
     */
    private fun showAllowPermissionDialog(context: Context) {
        if (!AppManager.mBlackBoxLoader.showShortcutPermissionDialog()) {
            return
        }

        MaterialDialog(context).show {
            title(R.string.try_add_shortcut)
            message(R.string.add_shortcut_fail_msg)
            positiveButton(R.string.done)
            negativeButton(R.string.permission_setting) {
                App.getContext().openAppSystemSettings()
            }
        }
    }
}
