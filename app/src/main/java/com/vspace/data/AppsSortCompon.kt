package com.vspace.data

import android.content.pm.ApplicationInfo

/**
 * A [Comparator] that sorts [ApplicationInfo] objects according to a user-defined
 * package-name ordering persisted in SharedPreferences.
 *
 * Applications whose package names appear earlier in [sortedList] are ranked first.
 * Entries not found in the list are treated as equal (returning 0).
 *
 * @property sortedList the ordered list of package names defining the desired sort order.
 */
class AppsSortComparator(private val sortedList: List<String>) : Comparator<ApplicationInfo> {
    override fun compare(o1: ApplicationInfo?, o2: ApplicationInfo?): Int {
        if (o1 == null || o2 == null) {
            return 0
        }

        val first = sortedList.indexOf(o1.packageName)
        val second = sortedList.indexOf(o2.packageName)
        return first - second
    }
}
