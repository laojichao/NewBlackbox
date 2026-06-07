package com.vspace.util

import android.app.Activity
import android.app.Dialog
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * Utility object providing lazy [ViewBinding] inflation via reified generics
 * for [Activity], [Fragment], and [Dialog] contexts.
 *
 * Uses reflection to locate the generated `inflate(LayoutInflater)` method on
 * the concrete [ViewBinding] class.
 */
object ViewBindingEx {
    /**
     * Lazily inflates a [ViewBinding] for an [Activity].
     *
     * @param T the concrete [ViewBinding] type.
     * @return a [Lazy] delegate that produces the inflated binding on first access.
     */
    inline fun <reified T : ViewBinding> Activity.inflate(): Lazy<T> = lazy {
        inflateBinding(layoutInflater)
    }

    /**
     * Lazily inflates a [ViewBinding] for a [Fragment].
     *
     * @param T the concrete [ViewBinding] type.
     * @return a [Lazy] delegate that produces the inflated binding on first access.
     */
    inline fun <reified T : ViewBinding> Fragment.inflate(): Lazy<T> = lazy {
        inflateBinding(layoutInflater)
    }

    /**
     * Lazily inflates a [ViewBinding] for a [Dialog].
     *
     * @param T the concrete [ViewBinding] type.
     * @return a [Lazy] delegate that produces the inflated binding on first access.
     */
    inline fun <reified T : ViewBinding> Dialog.inflate(): Lazy<T> = lazy {
        inflateBinding(layoutInflater)
    }

    /**
     * Reflectively invokes the static `inflate(LayoutInflater)` method on the
     * reified [ViewBinding] type.
     *
     * @param T the concrete [ViewBinding] type.
     * @param layoutInflater the [LayoutInflater] to pass to the inflate method.
     * @return the inflated [T] instance.
     */
    inline fun <reified T : ViewBinding> inflateBinding(layoutInflater: LayoutInflater): T {
        val method = T::class.java.getMethod("inflate", LayoutInflater::class.java)
        return method.invoke(null, layoutInflater) as T
    }
}
