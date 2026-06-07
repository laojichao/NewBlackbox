package com.vspace.view.list

import androidx.lifecycle.MutableLiveData
import com.vspace.bean.InstalledAppBean
import com.vspace.data.AppsRepository
import com.vspace.view.base.BaseViewModel

/**
 * ViewModel for the installed-app/module picker screen.
 *
 * Provides operations to load host-device installed apps (with virtual-user
 * install status) or Xposed modules, and to trigger a host-app preview scan.
 *
 * @property repo the [AppsRepository] backing all operations.
 */
class ListViewModel(private val repo: AppsRepository) : BaseViewModel() {
    /** LiveData holding the list of [InstalledAppBean] for display. */
    val appsLiveData = MutableLiveData<List<InstalledAppBean>>()
    /** LiveData indicating whether a loading operation is in progress. */
    val loadingLiveData = MutableLiveData<Boolean>()

    /**
     * Triggers a scan and cache of the host-device installed app list.
     * Typically called once after the list is first displayed.
     */
    fun previewInstalledList() {
        launchOnUI{
            repo.previewInstallList()
        }
    }

    /**
     * Loads the host-device installed app list and marks which are installed
     * inside the specified virtual user.
     *
     * @param userID the virtual user ID to check install status against.
     */
    fun getInstallAppList(userID: Int) {
        launchOnUI {
            repo.getInstalledAppList(userID, loadingLiveData, appsLiveData)
        }
    }

    /**
     * Loads only Xposed modules from the cached host-app list.
     */
    fun getInstalledModules() {
        launchOnUI {
            repo.getInstalledModuleList(loadingLiveData, appsLiveData)
        }
    }
}
