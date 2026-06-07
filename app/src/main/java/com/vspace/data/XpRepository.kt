package com.vspace.data

import android.net.Uri
import android.webkit.URLUtil
import androidx.lifecycle.MutableLiveData
import com.vcore.BlackBoxCore
import com.vcore.BlackBoxCore.getPackageManager
import com.vspace.R
import com.vspace.bean.XpModuleInfo
import com.vspace.util.ResUtil.getString

/**
 * Repository responsible for managing Xposed module lifecycle operations
 * within the virtual environment.
 *
 * Provides listing, installation, and removal of Xposed modules via [BlackBoxCore].
 */
class XpRepository {
    /**
     * Retrieves all currently installed Xposed modules and posts them to [modulesLiveData].
     *
     * @param modulesLiveData receives the list of [XpModuleInfo].
     */
    fun getInstallModules(modulesLiveData: MutableLiveData<List<XpModuleInfo>>) {
        val moduleList = BlackBoxCore.get().installedXPModules
        val result = mutableListOf<XpModuleInfo>()

        moduleList.forEach {
            val info = XpModuleInfo(it.name, it.desc, it.packageName, it.packageInfo.versionName, it.enable, it.application.loadIcon(getPackageManager()))
            result.add(info)
        }
        modulesLiveData.postValue(result)
    }

    /**
     * Installs an Xposed module from the given [source], which can be a file path or a URL.
     *
     * @param source the APK path or download URL for the module.
     * @param resultLiveData receives a user-facing success/failure message.
     */
    fun installModule(source: String, resultLiveData: MutableLiveData<String>) {
        val blackBoxCore = BlackBoxCore.get()
        val installResult = if (URLUtil.isValidUrl(source)) {
            val uri = Uri.parse(source)
            blackBoxCore.installXPModule(uri)
        } else {
            // source == packageName
            blackBoxCore.installXPModule(source)
        }

        if (installResult.success) {
            resultLiveData.postValue(getString(R.string.install_success))
        } else {
            resultLiveData.postValue(getString(R.string.install_fail, installResult.msg))
        }
    }

    /**
     * Uninstalls an Xposed module by its package name.
     *
     * @param packageName the package name of the module to remove.
     * @param resultLiveData receives a user-facing success message.
     */
    fun unInstallModule(packageName: String, resultLiveData: MutableLiveData<String>) {
        BlackBoxCore.get().uninstallXPModule(packageName)
        resultLiveData.postValue(getString(R.string.remove_success))
    }
}
