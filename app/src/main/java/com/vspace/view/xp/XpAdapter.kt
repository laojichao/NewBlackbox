package com.vspace.view.xp

import android.view.View
import android.view.ViewGroup
import cbfg.rvadapter.RVHolder
import cbfg.rvadapter.RVHolderFactory
import com.vspace.R
import com.vspace.bean.XpModuleInfo
import com.vspace.databinding.ItemXpBinding

/**
 * RecyclerView adapter factory for displaying [XpModuleInfo] items
 * in the Xposed module management screen.
 *
 * Each item shows the module icon, name, description, and an enable/disable toggle.
 */
class XpAdapter : RVHolderFactory() {
    override fun createViewHolder(parent: ViewGroup?, viewType: Int, item: Any): RVHolder<out Any> {
        return XpVH(inflate(R.layout.item_xp, parent))
    }

    /**
     * ViewHolder that binds an [XpModuleInfo] to the item_xp layout.
     *
     * When the enable switch is pressed by the user (not programmatically),
     * it triggers the root view's click listener to toggle the module state.
     */
    class XpVH(itemView: View) : RVHolder<XpModuleInfo>(itemView) {
        private val binding = ItemXpBinding.bind(itemView)

        override fun setContent(item: XpModuleInfo, isSelected: Boolean, payload: Any?) {
            binding.icon.setImageDrawable(item.icon)
            binding.name.text = item.name
            binding.desc.text = item.desc
            binding.enable.isChecked = item.enable
            binding.enable.setOnCheckedChangeListener { buttonView, _ ->
                // Only forward user-initiated presses, not programmatic setChecked calls
                if (buttonView.isPressed) {
                    binding.root.performClick()
                }
            }
        }
    }
}
