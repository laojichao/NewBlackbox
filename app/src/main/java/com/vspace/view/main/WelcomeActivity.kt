package com.vspace.view.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.vspace.util.InjectionUtil
import com.vspace.view.list.ListViewModel

/**
 * Splash/welcome activity that pre-scans the host-device installed app list
 * before immediately navigating to [MainActivity].
 *
 * Also handles re-entry via [onNewIntent] to avoid duplicate launches.
 */
class WelcomeActivity : AppCompatActivity() {
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        jump()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        previewInstalledAppList()
        jump()
    }

    /**
     * Navigates to [MainActivity] and finishes this activity.
     */
    private fun jump() {
        MainActivity.start(this)
        finish()
    }

    /**
     * Kicks off a background scan of host-device installed apps via [ListViewModel]
     * so the app picker is pre-populated when the user first opens it.
     */
    private fun previewInstalledAppList() {
        val viewModel = ViewModelProvider(this, InjectionUtil.getListFactory())[ListViewModel::class.java]
        viewModel.previewInstalledList()
    }
}
