package com.vspace.view.gms

import androidx.lifecycle.MutableLiveData
import com.vspace.bean.GmsBean
import com.vspace.bean.GmsInstallBean
import com.vspace.data.GmsRepository
import com.vspace.view.base.BaseViewModel

/**
 * ViewModel for the GMS manager screen.
 *
 * Exposes the GMS installation status per virtual user and delegates
 * install/uninstall operations to [GmsRepository].
 *
 * @property mRepo the [GmsRepository] backing all GMS operations.
 */
class GmsViewModel(private val mRepo: GmsRepository) : BaseViewModel() {
    /** LiveData holding the GMS installation status for all virtual users. */
    val mInstalledLiveData = MutableLiveData<List<GmsBean>>()
    /** LiveData holding the result of the most recent install/uninstall operation. */
    val mUpdateInstalledLiveData = MutableLiveData<GmsInstallBean>()

    /**
     * Loads the GMS installation status for every virtual user.
     */
    fun getInstalledUser() {
        launchOnUI {
            mRepo.getGmsInstalledList(mInstalledLiveData)
        }
    }

    /**
     * Installs Google Mobile Services into the specified virtual user.
     *
     * @param userID the virtual user ID to install GMS into.
     */
    fun installGms(userID: Int) {
        launchOnUI {
            mRepo.installGms(userID, mUpdateInstalledLiveData)
        }
    }

    /**
     * Uninstalls Google Mobile Services from the specified virtual user.
     *
     * @param userID the virtual user ID to uninstall GMS from.
     */
    fun uninstallGms(userID: Int) {
        launchOnUI {
            mRepo.uninstallGms(userID, mUpdateInstalledLiveData)
        }
    }
}
