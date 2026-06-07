package com.vspace.view.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vspace.data.AppsRepository

/**
 * [ViewModelProvider.Factory] that creates [ListViewModel] instances
 * with the injected [AppsRepository].
 *
 * @property appsRepository the repository to pass to [ListViewModel].
 */
@Suppress("UNCHECKED_CAST")
class ListFactory(private val appsRepository: AppsRepository) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ListViewModel(appsRepository) as T
    }
}
