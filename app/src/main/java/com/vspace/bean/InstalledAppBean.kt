package com.vspace.bean

import android.graphics.drawable.Drawable

/**
 * Data class representing an installed application with its virtual-space installation status.
 *
 * Used in the app list picker to indicate whether the app is already installed in the current
 * virtual user environment.
 *
 * @property name the display name of the application.
 * @property icon the application icon as a [Drawable].
 * @property packageName the unique Android package name.
 * @property sourceDir the absolute path to the APK file on disk.
 * @property isInstall whether the app is installed inside the virtual environment.
 */
data class InstalledAppBean(
    val name: String,
    val icon: Drawable,
    val packageName: String,
    val sourceDir: String,
    val isInstall: Boolean
)
