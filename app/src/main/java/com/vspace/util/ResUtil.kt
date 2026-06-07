package com.vspace.util

import androidx.annotation.StringRes
import com.vspace.app.App

/**
 * Utility object for resolving string resources without requiring a direct [Context] reference.
 *
 * Uses the global [App.getContext] to access resources, making it safe to call from
 * non-Activity/Fragment code such as repositories.
 */
object ResUtil {
    /**
     * Returns the localized string for the given resource ID, optionally formatted
     * with the supplied arguments.
     *
     * @param id the string resource ID.
     * @param arg optional format arguments to substitute into the string.
     * @return the resolved string value.
     */
    fun getString(@StringRes id: Int, vararg arg: String): String {
        if (arg.isEmpty()) {
            return App.getContext().getString(id)
        }
        return App.getContext().getString(id, *arg)
    }
}
