package com.vspace.view.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.vcore.BlackBoxCore

/**
 * Transparent trampoline activity that launches an app inside the virtual environment
 * when invoked from a home-screen shortcut.
 *
 * Reads the target package name ("pkg") and virtual user ID ("userId") from the
 * launching intent, launches the app via [BlackBoxCore], and finishes immediately.
 */
class ShortcutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pkg = intent.getStringExtra("pkg")
        val userID = intent.getIntExtra("userId", 0)

        lifecycleScope.launch {
            BlackBoxCore.get().launchApk(pkg, userID)
            finish()
        }
    }
}
