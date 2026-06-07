package com.vspace.bean

import android.graphics.drawable.Drawable
import com.vcore.entity.location.BLocation

/**
 * Data class representing an application's fake location configuration within a virtual user.
 *
 * @property userID the virtual user ID this app belongs to.
 * @property name the display name of the application.
 * @property icon the application icon as a [Drawable].
 * @property packageName the unique Android package name.
 * @property fakeLocationPattern the current fake location mode (e.g., close, global, per-app).
 * @property fakeLocation the overridden [BLocation] coordinates, or null if no override is set.
 */
data class FakeLocationBean(
    val userID: Int,
    val name: String,
    val icon: Drawable,
    val packageName: String,
    var fakeLocationPattern: Int,
    var fakeLocation: BLocation?
)
