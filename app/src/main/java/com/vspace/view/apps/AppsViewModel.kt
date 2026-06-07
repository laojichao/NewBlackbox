package com.vspace.view.apps

import androidx.lifecycle.MutableLiveData
import com.vspace.bean.AppInfo
import com.vspace.data.AppsRepository
import com.vspace.view.base.BaseViewModel

/**
 * ViewModel for the apps screen that exposes installed-app list data and
 * orchestrates install/uninstall/launch/clear operations through [AppsRepository].
 *
 * All repository calls are dispatched on the IO thread via [BaseViewModel.launchOnUI].
 *
 * @property repo the [AppsRepository] used for all app management operations.
 */
class AppsViewModel(private val repo: AppsRepository) : BaseViewModel() {
    /** LiveData holding the list of apps installed in the current virtual user. */
    val appsLiveData = MutableLiveData<List<AppInfo>>()
    /** LiveData holding user-facing result message strings after operations. */
    val resultLiveData = MutableLiveData<String>()
    /** LiveData indicating whether a launch succeeded (true) or failed (false). */
    val launchLiveData = MutableLiveData<Boolean>()
    // 利用LiveData只更新最后一次的特性，用来保存app顺序
    /** LiveData used as a trigger to persist the latest app sort order after drag-and-drop. */
    val updateSortLiveData = MutableLiveData<Boolean>()

    /**
     * Loads the installed apps for the given virtual user into [appsLiveData].
     *
     * @param userId the virtual user ID whose installed apps to retrieve.
     */
    fun getInstalledApps(userId: Int) {
        launchOnUI {
            repo.getVmInstallList(userId, appsLiveData)
        }
    }

    /**
     * Installs an APK from [source] into the specified virtual user.
     *
     * @param source the APK file path or download URL.
     * @param userID the target virtual user ID.
     */
    fun install(source: String, userID: Int) {
        launchOnUI {
            repo.installApk(source, userID, resultLiveData)
        }
    }

    /**
     * Uninstalls an application from the specified virtual user.
     *
     * @param packageName the package name to uninstall.
     * @param userID the virtual user ID.
     */
    fun unInstall(packageName: String, userID: Int) {
        launchOnUI {
            repo.unInstall(packageName, userID, resultLiveData)
        }
    }

    /**
     * Clears all data for the specified application in the virtual user.
     *
     * @param packageName the package name whose data to clear.
     * @param userID the virtual user ID.
     */
    fun clearApkData(packageName: String,userID: Int) {
        launchOnUI {
            repo.clearApkData(packageName,userID,resultLiveData)
        }
    }

    /**
     * Launches the specified application in the virtual user environment.
     *
     * @param packageName the package name to launch.
     * @param userID the virtual user ID.
     */
    fun launchApk(packageName: String, userID: Int) {
        launchOnUI {
            repo.launchApk(packageName, userID, launchLiveData)
        }
    }

    /**
     * Persists the new app display order after a drag-and-drop re-sort.
     *
     * @param userID the virtual user ID.
     * @param dataList the reordered list of [AppInfo].
     */
    fun updateApkOrder(userID: Int, dataList:List<AppInfo>) {
        launchOnUI {
            repo.updateApkOrder(userID, dataList)
        }
    }
}
