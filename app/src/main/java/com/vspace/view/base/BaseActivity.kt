package com.vspace.view.base

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

/**
 * Base [AppCompatActivity] providing common toolbar initialization utilities
 * and intent-based user-ID extraction for all screens in the app.
 */
open class BaseActivity : AppCompatActivity() {
    /**
     * Configures the given [Toolbar] as the activity's action bar, optionally
     * displaying a back-navigation arrow.
     *
     * @param toolbar the [Toolbar] widget to configure.
     * @param title the string resource ID for the toolbar title.
     * @param showBack whether to show the back-navigation arrow (default false).
     * @param onBack optional callback invoked before [finish] when the back arrow is pressed.
     */
    protected fun initToolbar(toolbar: Toolbar, title:Int, showBack: Boolean = false, onBack: (() -> Unit)? = null) {
        setSupportActionBar(toolbar)
        toolbar.setTitle(title)

        if (showBack) {
            supportActionBar?.let {
                it.setDisplayHomeAsUpEnabled(true)
                toolbar.setNavigationOnClickListener {
                    if (onBack != null) {
                        onBack()
                    }
                    finish()
                }
            }
        }
    }

    /**
     * Extracts the virtual user ID from the launching intent's extras.
     *
     * @return the virtual user ID, defaulting to 0 if absent.
     */
    protected fun currentUserID(): Int {
        return intent.getIntExtra("userID", 0)
    }
}
