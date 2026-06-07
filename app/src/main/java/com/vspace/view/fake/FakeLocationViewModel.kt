package com.vspace.view.fake

import androidx.lifecycle.MutableLiveData
import com.vcore.entity.location.BLocation
import com.vspace.bean.FakeLocationBean
import com.vspace.data.FakeLocationRepository
import com.vspace.view.base.BaseViewModel

/**
 * ViewModel for the fake location manager screen.
 *
 * Delegates app-list loading and location configuration changes to [FakeLocationRepository],
 * exposing results via [MutableLiveData] for UI observation.
 *
 * @property mRepo the [FakeLocationRepository] backing all operations.
 */
class FakeLocationViewModel(private val mRepo: FakeLocationRepository) : BaseViewModel() {
    /** LiveData holding the list of apps with their fake-location configuration. */
    val appsLiveData = MutableLiveData<List<FakeLocationBean>>()

    /**
     * Loads the installed-app list for the specified virtual user,
     * including each app's current fake-location settings.
     *
     * @param userID the virtual user ID to query.
     */
    fun getInstallAppList(userID: Int) {
        launchOnUI {
            mRepo.getInstalledAppList(userID, appsLiveData)
        }
    }

    /**
     * Updates the fake location mode for a specific app.
     *
     * @param userId the virtual user ID.
     * @param pkg the target application package name.
     * @param pattern the new location mode constant.
     */
    fun setPattern(userId: Int, pkg: String, pattern: Int) {
        launchOnUI {
            mRepo.setPattern(userId, pkg, pattern)
        }
    }

    /**
     * Sets the overridden location coordinates for a specific app.
     *
     * @param userId the virtual user ID.
     * @param pkg the target application package name.
     * @param location the [BLocation] coordinates to apply.
     */
    fun setLocation(userId: Int, pkg: String, location: BLocation) {
        launchOnUI {
            mRepo.setLocation(userId, pkg, location)
        }
    }
}
