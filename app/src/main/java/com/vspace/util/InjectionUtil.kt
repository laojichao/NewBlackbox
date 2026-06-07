package com.vspace.util

import com.vspace.data.AppsRepository
import com.vspace.data.FakeLocationRepository
import com.vspace.data.GmsRepository
import com.vspace.data.XpRepository
import com.vspace.view.apps.AppsFactory
import com.vspace.view.fake.FakeLocationFactory
import com.vspace.view.gms.GmsFactory
import com.vspace.view.list.ListFactory
import com.vspace.view.xp.XpFactory

/**
 * Simple manual dependency-injection utility that acts as a service locator.
 *
 * Holds singleton instances of all repositories and provides factory methods
 * for creating [ViewModelProvider.Factory] instances with the appropriate repository.
 */
object InjectionUtil {
    private val appsRepository = AppsRepository()
    private val xpRepository = XpRepository()
    private val gmsRepository = GmsRepository()
    private val fakeLocationRepository = FakeLocationRepository()

    /**
     * Creates an [AppsFactory] wired with the shared [AppsRepository].
     *
     * @return a new [AppsFactory] instance.
     */
    fun getAppsFactory() : AppsFactory {
        return AppsFactory(appsRepository)
    }

    /**
     * Creates a [ListFactory] wired with the shared [AppsRepository].
     *
     * @return a new [ListFactory] instance.
     */
    fun getListFactory(): ListFactory {
        return ListFactory(appsRepository)
    }

    /**
     * Creates an [XpFactory] wired with the shared [XpRepository].
     *
     * @return a new [XpFactory] instance.
     */
    fun getXpFactory():XpFactory{
        return XpFactory(xpRepository)
    }

    /**
     * Creates a [GmsFactory] wired with the shared [GmsRepository].
     *
     * @return a new [GmsFactory] instance.
     */
    fun getGmsFactory():GmsFactory{
        return GmsFactory(gmsRepository)
    }

    /**
     * Creates a [FakeLocationFactory] wired with the shared [FakeLocationRepository].
     *
     * @return a new [FakeLocationFactory] instance.
     */
    fun getFakeLocationFactory():FakeLocationFactory{
        return FakeLocationFactory(fakeLocationRepository)
    }
}
