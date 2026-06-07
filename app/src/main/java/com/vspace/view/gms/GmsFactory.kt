package com.vspace.view.gms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vspace.data.GmsRepository

/**
 * [ViewModelProvider.Factory] that creates [GmsViewModel] instances
 * with the injected [GmsRepository].
 *
 * @property repo the repository to pass to [GmsViewModel].
 */
class GmsFactory(private val repo: GmsRepository) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GmsViewModel(repo) as T
    }
}
