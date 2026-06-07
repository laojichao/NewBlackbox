package com.vspace.bean

import android.graphics.drawable.Drawable

/**
 * Data class representing an Xposed module's metadata and current enable state.
 *
 * @property name the display name of the module.
 * @property desc the module's description text.
 * @property packageName the unique Android package name of the module.
 * @property version the version string of the installed module.
 * @property enable whether the module is currently enabled in the Xposed framework.
 * @property icon the module's icon as a [Drawable].
 */
data class XpModuleInfo(
        val name: String,
        val desc: String,
        val packageName: String,
        val version: String,
        var enable:Boolean,
        val icon: Drawable
)
