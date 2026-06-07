package com.vspace.data

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.webkit.URLUtil
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import com.vcore.BlackBoxCore
import com.vcore.BlackBoxCore.getPackageManager
import com.vcore.core.GmsCore
import com.vcore.utils.AbiUtils
import com.vcore.utils.Slog
import com.vcore.utils.compat.BuildCompat
import com.vspace.R
import com.vspace.app.AppManager
import com.vspace.bean.AppInfo
import com.vspace.bean.InstalledAppBean
import com.vspace.util.ResUtil.getString
import java.io.File

/**
 * Repository responsible for managing application operations within the virtual environment.
 *
 * Provides host-device app scanning, per-user install/uninstall, launch, data clearing,
 * app sort ordering, and Xposed module detection. All operations are backed by [BlackBoxCore].
 */
class AppsRepository {
    private val TAG: String = "AppsRepository"
    private var mInstalledList = mutableListOf<AppInfo>()

    /**
     * Scans the host device for installed (non-system, ABI-compatible) applications
     * and caches the result in [mInstalledList]. This is typically called once before
     * displaying the install picker to avoid repeated PackageManager queries.
     */
    fun previewInstallList() {
        synchronized(mInstalledList) {
            val installedList = mutableListOf<AppInfo>()
            val installedApplications: List<ApplicationInfo> = if (BuildCompat.isT()) {
                getPackageManager().getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
            } else {
                getPackageManager().getInstalledApplications(0)
            }

            for (installedApplication in installedApplications) {
                val file = File(installedApplication.sourceDir)
                // Skip system apps -- they are generally not user-installable clones
                if (installedApplication.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                    continue
                }

                // Skip apps whose ABI is not supported by the virtual engine
                if (!AbiUtils.isSupport(file)) {
                    continue
                }

                val isXpModule = BlackBoxCore.get().isXposedModule(file)
                val info = AppInfo(
                    installedApplication.loadLabel(getPackageManager()).toString(),
                    installedApplication.loadIcon(getPackageManager()),
                    installedApplication.packageName,
                    installedApplication.sourceDir,
                    isXpModule
                )
                installedList.add(info)
            }

            installedList.sortWith { a, b ->
                if (a.name > b.name) {
                    1
                } else {
                    -1
                }
            }

            this.mInstalledList.clear()
            this.mInstalledList.addAll(installedList)
        }
    }

    /**
     * Builds a list of [InstalledAppBean] for the install picker, marking which host apps
     * are already installed inside the given virtual [userID].
     *
     * @param userID the virtual user ID to check against.
     * @param loadingLiveData posts true while loading, false when complete.
     * @param appsLiveData receives the resulting list of [InstalledAppBean].
     */
    fun getInstalledAppList(userID: Int, loadingLiveData: MutableLiveData<Boolean>, appsLiveData: MutableLiveData<List<InstalledAppBean>>) {
        loadingLiveData.postValue(true)

        synchronized(mInstalledList) {
            val blackBoxCore = BlackBoxCore.get()
            Slog.d(TAG, mInstalledList.joinToString(","))

            val newInstalledList = mInstalledList.map {
                InstalledAppBean(it.name, it.icon, it.packageName, it.sourceDir, blackBoxCore.isInstalled(it.packageName, userID))
            }
            appsLiveData.postValue(newInstalledList)
            loadingLiveData.postValue(false)
        }
    }

    /**
     * Filters the cached host app list to only Xposed modules and reports their
     * installation status within the virtual environment.
     *
     * @param loadingLiveData posts true while loading, false when complete.
     * @param appsLiveData receives the resulting list of Xposed-module [InstalledAppBean].
     */
    fun getInstalledModuleList(loadingLiveData: MutableLiveData<Boolean>, appsLiveData: MutableLiveData<List<InstalledAppBean>>) {
        loadingLiveData.postValue(true)

        synchronized(mInstalledList) {
            val blackBoxCore = BlackBoxCore.get()
            val moduleList = mInstalledList.filter {
                it.isXpModule
            }.map {
                InstalledAppBean(it.name, it.icon, it.packageName, it.sourceDir, blackBoxCore.isInstalledXposedModule(it.packageName))
            }
            appsLiveData.postValue(moduleList)
            loadingLiveData.postValue(false)
        }
    }

    /**
     * Retrieves the list of apps installed inside a virtual user, sorted by the user's
     * custom ordering stored in SharedPreferences.
     *
     * @param userId the virtual user ID whose installed apps to retrieve.
     * @param appsLiveData receives the resulting sorted list of [AppInfo].
     */
    fun getVmInstallList(userId: Int, appsLiveData: MutableLiveData<List<AppInfo>>) {
        val sortListData = AppManager.mRemarkSharedPreferences.getString("AppList$userId", "")
        val sortList = sortListData?.split(",")

        val applicationList = BlackBoxCore.get().getInstalledApplications(0, userId)
        val appInfoList = mutableListOf<AppInfo>()
        applicationList.also {
            if (sortList.isNullOrEmpty()) {
                return@also
            }
            it.sortWith(AppsSortComparator(sortList))
        }.forEach {
            val info = AppInfo(it.loadLabel(getPackageManager()).toString(), it.loadIcon(getPackageManager()), it.packageName, it.sourceDir,
                isInstalledXpModule(it.packageName))
            appInfoList.add(info)
        }
        appsLiveData.postValue(appInfoList)
    }

    /**
     * Checks whether the given [packageName] is registered as an Xposed module
     * inside the virtual environment.
     *
     * @param packageName the package name to check.
     * @return true if the package is an installed Xposed module.
     */
    private fun isInstalledXpModule(packageName: String): Boolean {
        BlackBoxCore.get().installedXPModules.forEach {
            if (packageName == it.packageName) {
                return@isInstalledXpModule true
            }
        }
        return false
    }

    /**
     * Installs an APK into a virtual user. The [source] can be either a file path
     * or a URL; the latter is resolved via [Uri].
     *
     * @param source the APK file path or download URL.
     * @param userId the target virtual user ID.
     * @param resultLiveData receives a user-facing success/failure message string.
     */
    fun installApk(source: String, userId: Int, resultLiveData: MutableLiveData<String>) {
        val blackBoxCore = BlackBoxCore.get()
        val installResult = if (URLUtil.isValidUrl(source)) {
            val uri = Uri.parse(source)
            blackBoxCore.installPackageAsUser(uri, userId)
        } else {
            blackBoxCore.installPackageAsUser(source, userId)
        }

        if (installResult.success) {
            updateAppSortList(userId, installResult.packageName, true)
            resultLiveData.postValue(getString(R.string.install_success))
        } else {
            resultLiveData.postValue(getString(R.string.install_fail, installResult.msg))
        }
        scanUser()
    }

    /**
     * Uninstalls an application from a virtual user and updates the sort order.
     *
     * @param packageName the package to uninstall.
     * @param userID the virtual user ID.
     * @param resultLiveData receives a user-facing success message.
     */
    fun unInstall(packageName: String, userID: Int, resultLiveData: MutableLiveData<String>) {
        BlackBoxCore.get().uninstallPackageAsUser(packageName, userID)
        updateAppSortList(userID, packageName, false)
        scanUser()
        resultLiveData.postValue(getString(R.string.uninstall_success))
    }

    /**
     * Launches an application inside a virtual user environment.
     *
     * @param packageName the package to launch.
     * @param userId the virtual user ID.
     * @param launchLiveData receives true on success, false on failure.
     */
    fun launchApk(packageName: String, userId: Int, launchLiveData: MutableLiveData<Boolean>) {
        val result = BlackBoxCore.get().launchApk(packageName, userId)
        launchLiveData.postValue(result)
    }

    /**
     * Clears all data for an application in a virtual user.
     *
     * @param packageName the package whose data to clear.
     * @param userID the virtual user ID.
     * @param resultLiveData receives a user-facing success message.
     */
    fun clearApkData(packageName: String, userID: Int, resultLiveData: MutableLiveData<String>) {
        BlackBoxCore.get().clearPackage(packageName, userID)
        resultLiveData.postValue(getString(R.string.clear_success))
    }

    /**
     * Recursively scans virtual users in reverse order and removes any that have no
     * installed applications. Cleans up associated remark and sort-order preferences.
     */
    private fun scanUser() {
        val blackBoxCore = BlackBoxCore.get()
        val userList = blackBoxCore.users

        if (userList.isEmpty()) {
            return
        }

        val id = userList.last().id
        if (blackBoxCore.getInstalledApplications(0, id).isEmpty()) {
            blackBoxCore.deleteUser(id)
            AppManager.mRemarkSharedPreferences.edit {
                remove("Remark$id")
                remove("AppList$id")
            }
            scanUser()
        }
    }

    /**
     * Updates the persisted app sort list for a given user by adding or removing [pkg].
     *
     * @param userID the virtual user ID.
     * @param pkg the package name to add or remove.
     * @param isAdd true to add, false to remove.
     */
    private fun updateAppSortList(userID: Int, pkg: String, isAdd: Boolean) {
        val savedSortList = AppManager.mRemarkSharedPreferences.getString("AppList$userID", "")
        val sortList = linkedSetOf<String>()
        if (savedSortList != null) {
            sortList.addAll(savedSortList.split(","))
        }

        if (isAdd) {
            sortList.add(pkg)
        } else {
            sortList.remove(pkg)
        }

        AppManager.mRemarkSharedPreferences.edit {
            putString("AppList$userID", sortList.joinToString(","))
        }
    }

    /**
     * Persists the current app ordering after a drag-and-drop re-sort.
     *
     * @param userID the virtual user ID.
     * @param dataList the reordered list of [AppInfo] reflecting the new display order.
     */
    fun updateApkOrder(userID: Int, dataList: List<AppInfo>) {
        AppManager.mRemarkSharedPreferences.edit {
            putString("AppList$userID", dataList.joinToString(",") { it.packageName })
        }
    }
}
