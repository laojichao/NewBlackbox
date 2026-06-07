package com.vspace.data

import android.content.pm.ApplicationInfo
import androidx.lifecycle.MutableLiveData
import com.vcore.BlackBoxCore
import com.vcore.entity.location.BLocation
import com.vcore.fake.frameworks.BLocationManager
import com.vcore.utils.Slog
import com.vspace.bean.FakeLocationBean

/**
 * Repository that manages per-app fake location configurations within the virtual environment.
 *
 * Delegates read/write operations to [BLocationManager] and converts installed app metadata
 * into [FakeLocationBean] representations for the UI layer.
 */
class FakeLocationRepository {
    private val TAG: String = "FakeLocationRepository"

    /**
     * Sets the fake location mode for a given app within a virtual user.
     *
     * @param userId the virtual user ID.
     * @param pkg the target application package name.
     * @param pattern the location mode constant (e.g., [BLocationManager.CLOSE_MODE]).
     */
    fun setPattern(userId: Int, pkg: String, pattern: Int) {
        BLocationManager.get().setPattern(userId, pkg, pattern)
    }

    /**
     * Retrieves the current fake location mode for a given app.
     *
     * @param userId the virtual user ID.
     * @param pkg the target application package name.
     * @return the location mode constant.
     */
    private fun getPattern(userId: Int, pkg: String): Int {
        return BLocationManager.get().getPattern(userId, pkg)
    }

    /**
     * Retrieves the overridden [BLocation] for a given app, or null if none is set.
     *
     * @param userId the virtual user ID.
     * @param pkg the target application package name.
     * @return the [BLocation] override, or null.
     */
    private fun getLocation(userId: Int, pkg: String): BLocation? {
        return BLocationManager.get().getLocation(userId, pkg)
    }

    /**
     * Sets the overridden location coordinates for a given app within a virtual user.
     *
     * @param userId the virtual user ID.
     * @param pkg the target application package name.
     * @param location the [BLocation] coordinates to apply.
     */
    fun setLocation(userId: Int, pkg: String, location: BLocation) {
        BLocationManager.get().setLocation(userId, pkg, location)
    }

    /**
     * Loads all applications installed in the specified virtual user and maps them to
     * [FakeLocationBean] with their current fake-location configuration attached.
     *
     * @param userID the virtual user ID.
     * @param appsFakeLiveData receives the resulting list of [FakeLocationBean].
     */
    fun getInstalledAppList(userID: Int, appsFakeLiveData: MutableLiveData<List<FakeLocationBean>>) {
        val installedList = mutableListOf<FakeLocationBean>()
        val installedApplications: List<ApplicationInfo> = BlackBoxCore.get().getInstalledApplications(0, userID)
        // List<ApplicationInfo> -> List<FakeLocationBean>
        for (installedApplication in installedApplications) {
            /*val file = File(installedApplication.sourceDir)
            if ((installedApplication.flags and ApplicationInfo.FLAG_SYSTEM) != 0) {
                continue
            }

            if (!AbiUtils.isSupport(file)) {
                continue
            }

            val isXpModule = BlackBoxCore.get().isXposedModule(file)*/
            val info = FakeLocationBean(
                userID,
                installedApplication.loadLabel(BlackBoxCore.getPackageManager()).toString(),
                installedApplication.loadIcon(BlackBoxCore.getPackageManager()),
                installedApplication.packageName,
                getPattern(userID, installedApplication.packageName),
                getLocation(userID, installedApplication.packageName)
            )

            installedList.add(info)
        }

        Slog.d(TAG, installedList.joinToString(","))
        appsFakeLiveData.postValue(installedList)
    }
}
