package com.vspace.view.base

import android.view.KeyEvent
import com.roger.catloadinglibrary.CatLoadingView
import com.vspace.R

/**
 * Abstract [BaseActivity] subclass that provides a reusable loading dialog
 * powered by [CatLoadingView].
 *
 * Subclasses can call [showLoading] and [hideLoading] to display or dismiss
 * a non-cancelable loading overlay during asynchronous operations.
 */
abstract class LoadingActivity : BaseActivity() {
    private lateinit var loadingView: CatLoadingView

    /**
     * Shows the loading dialog if it is not already displayed.
     * The dialog blocks back-key presses and prevents tap-to-dismiss.
     */
    fun showLoading() {
        if (!this::loadingView.isInitialized) {
            loadingView = CatLoadingView()
        }

        if (!loadingView.isAdded) {
            loadingView.setBackgroundColor(R.color.primary)
            loadingView.show(supportFragmentManager, "")

            supportFragmentManager.executePendingTransactions()
            loadingView.setClickCancelAble(false)
            loadingView.dialog?.setOnKeyListener { _, keyCode, _ ->
                if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
                    return@setOnKeyListener true
                }
                false
            }
        }
    }

    /**
     * Dismisses the loading dialog if it has been initialized and is currently showing.
     */
    fun hideLoading() {
        if (this::loadingView.isInitialized) {
            loadingView.dismiss()
        }
    }
}
