package com.vspace.view.fake

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vspace.data.FakeLocationRepository

/**
 * [ViewModelProvider.Factory] that creates [FakeLocationViewModel] instances
 * with the injected [FakeLocationRepository].
 *
 * @property repo the repository to pass to [FakeLocationViewModel].
 */
class FakeLocationFactory(private val repo: FakeLocationRepository) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FakeLocationViewModel(repo) as T
    }
}
