package com.vspace.view.list

import android.view.View
import android.view.ViewGroup
import cbfg.rvadapter.RVHolder
import cbfg.rvadapter.RVHolderFactory
import com.vspace.R
import com.vspace.bean.InstalledAppBean
import com.vspace.databinding.ItemPackageBinding

/**
 * RecyclerView adapter factory for displaying [InstalledAppBean] items
 * in the app/module picker list.
 *
 * Each item shows the app icon, name, package name, and a corner label
 * indicating whether the app is already installed in the virtual environment.
 */
class ListAdapter : RVHolderFactory() {
    override fun createViewHolder(parent: ViewGroup?, viewType: Int, item: Any): RVHolder<out Any> {
        return ListVH(inflate(R.layout.item_package, parent))
    }

    /**
     * ViewHolder that binds an [InstalledAppBean] to the item_package layout.
     *
     * The corner label is visible when [InstalledAppBean.isInstall] is true.
     */
    class ListVH(itemView: View) : RVHolder<InstalledAppBean>(itemView) {
        private val binding = ItemPackageBinding.bind(itemView)

        override fun setContent(item: InstalledAppBean, isSelected: Boolean, payload: Any?) {
            binding.icon.setImageDrawable(item.icon)
            binding.name.text = item.name
            binding.packageName.text = item.packageName
            binding.cornerLabel.visibility = if (item.isInstall) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }
}
