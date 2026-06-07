package com.vspace.view.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vspace.data.AppsRepository

/**
 * [ViewModelProvider.Factory] that creates [AppsViewModel] instances
 * with the injected [AppsRepository].
 *
 * @property appsRepository the repository to pass to [AppsViewModel].
 */
@Suppress("UNCHECKED_CAST")
class AppsFactory(private val appsRepository: AppsRepository) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AppsViewModel(appsRepository) as T
    }
}
