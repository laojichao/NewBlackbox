package com.vspace.data

import androidx.lifecycle.MutableLiveData
import com.vcore.BlackBoxCore
import com.vspace.R
import com.vspace.app.AppManager
import com.vspace.bean.GmsBean
import com.vspace.bean.GmsInstallBean
import com.vspace.util.ResUtil.getString

/**
 * Repository that manages Google Mobile Services (GMS) installation status
 * across all virtual users.
 *
 * Provides operations to list GMS state per user, install GMS into a virtual user,
 * and uninstall GMS from a virtual user.
 */
class GmsRepository {
    /**
     * Retrieves the GMS installation status for every virtual user and posts
     * the result as a list of [GmsBean].
     *
     * @param mInstalledLiveData receives the list of [GmsBean] for all users.
     */
    fun getGmsInstalledList(mInstalledLiveData: MutableLiveData<List<GmsBean>>) {
        val userList = arrayListOf<GmsBean>()

        BlackBoxCore.get().users.forEach {
            val userId = it.id
            val userName = AppManager.mRemarkSharedPreferences.getString("Remark$userId", "User $userId") ?: ""
            val isInstalled = BlackBoxCore.get().isInstallGms(userId)
            val bean = GmsBean(userId, userName, isInstalled)
            userList.add(bean)
        }
        mInstalledLiveData.postValue(userList)
    }

    /**
     * Installs Google Mobile Services into the specified virtual user.
     *
     * @param userID the virtual user ID to install GMS into.
     * @param mUpdateInstalledLiveData receives a [GmsInstallBean] with the operation result.
     */
    fun installGms(userID: Int, mUpdateInstalledLiveData: MutableLiveData<GmsInstallBean>) {
        val installResult = BlackBoxCore.get().installGms(userID)
        val result = if (installResult.success) {
            getString(R.string.install_success)
        } else {
            getString(R.string.install_fail, installResult.msg)
        }

        val bean = GmsInstallBean(userID, installResult.success, result)
        mUpdateInstalledLiveData.postValue(bean)
    }

    /**
     * Uninstalls Google Mobile Services from the specified virtual user.
     * If GMS is not installed, the operation reports failure.
     *
     * @param userID the virtual user ID to uninstall GMS from.
     * @param mUpdateInstalledLiveData receives a [GmsInstallBean] with the operation result.
     */
    fun uninstallGms(userID: Int, mUpdateInstalledLiveData: MutableLiveData<GmsInstallBean>) {
        var isSuccess = false
        if (BlackBoxCore.get().isInstallGms(userID)) {
            isSuccess = BlackBoxCore.get().uninstallGms(userID)
        }

        val result = if (isSuccess) {
            getString(R.string.uninstall_success)
        } else {
            getString(R.string.uninstall_fail)
        }

        val bean = GmsInstallBean(userID, isSuccess, result)
        mUpdateInstalledLiveData.postValue(bean)
    }
}
