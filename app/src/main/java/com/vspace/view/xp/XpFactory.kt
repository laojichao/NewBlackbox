package com.vspace.view.xp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vspace.data.XpRepository

/**
 * [ViewModelProvider.Factory] that creates [XpViewModel] instances
 * with the injected [XpRepository].
 *
 * @property repo the repository to pass to [XpViewModel].
 */
@Suppress("UNCHECKED_CAST")
class XpFactory(private val repo: XpRepository) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return XpViewModel(repo) as T
    }
}
