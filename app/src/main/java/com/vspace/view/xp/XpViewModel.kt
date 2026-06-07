package com.vspace.view.xp

import androidx.lifecycle.MutableLiveData
import com.vspace.bean.XpModuleInfo
import com.vspace.data.XpRepository
import com.vspace.view.base.BaseViewModel

/**
 * ViewModel for the Xposed module management screen.
 *
 * Delegates module listing, installation, and removal to [XpRepository]
 * and exposes results via [MutableLiveData] for UI observation.
 *
 * @property repo the [XpRepository] backing all Xposed module operations.
 */
class XpViewModel(private val repo: XpRepository) : BaseViewModel() {
    /** LiveData holding the list of installed Xposed modules. */
    val appsLiveData = MutableLiveData<List<XpModuleInfo>>()
    /** LiveData holding user-facing result message strings after operations. */
    val resultLiveData = MutableLiveData<String>()

    /**
     * Loads all installed Xposed modules into [appsLiveData].
     */
    fun getInstalledModule() {
        launchOnUI {
            repo.getInstallModules(appsLiveData)
        }
    }

    /**
     * Installs an Xposed module from the given source path or URL.
     *
     * @param source the module APK path or download URL.
     */
    fun installModule(source:String) {
        launchOnUI {
            repo.installModule(source, resultLiveData)
        }
    }

    /**
     * Uninstalls an Xposed module by its package name.
     *
     * @param packageName the package name of the module to remove.
     */
    fun unInstallModule(packageName: String) {
        launchOnUI {
            repo.unInstallModule(packageName, resultLiveData)
        }
    }
}
