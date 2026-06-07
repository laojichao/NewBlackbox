package com.vspace.util

import android.content.Context

/**
 * Utility object for screen density conversions.
 */
object Resolution {
    /**
     * Converts a dp (density-independent pixel) value to the equivalent pixel value
     * based on the current device's screen density.
     *
     * @param dp the value in dp to convert.
     * @param context the [Context] used to access display metrics.
     * @return the equivalent pixel value as a float.
     */
    fun convertDpToPixel(dp: Float, context: Context): Float {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return dp * (metrics.densityDpi / 160f)
    }
}
