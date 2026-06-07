package com.vspace.view.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*

/**
 * Base [ViewModel] providing a coroutine-launch helper that executes blocks
 * on the IO dispatcher with built-in exception handling.
 *
 * Subclasses should use [launchOnUI] for all repository/background operations.
 */
open class BaseViewModel : ViewModel() {
    /**
     * Launches [block] on the IO dispatcher within the ViewModel's [viewModelScope].
     * Any thrown [Throwable] is caught and printed to avoid crashing the scope.
     *
     * @param block the suspend lambda to execute on the IO thread.
     */
    fun launchOnUI(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    block()
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}
