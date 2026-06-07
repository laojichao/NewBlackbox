package com.vspace.bean

import android.graphics.drawable.Drawable

/**
 * Data class representing basic information about an installed application.
 *
 * @property name the display name of the application.
 * @property icon the application icon as a [Drawable].
 * @property packageName the unique Android package name.
 * @property sourceDir the absolute path to the APK file on disk.
 * @property isXpModule whether this application is also an Xposed module.
 */
data class AppInfo(
	val name: String,
	val icon: Drawable,
	val packageName: String,
	val sourceDir: String,
	val isXpModule: Boolean
)
