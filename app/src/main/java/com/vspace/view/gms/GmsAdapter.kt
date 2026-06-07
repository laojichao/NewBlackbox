package com.vspace.view.gms

import android.view.View
import android.view.ViewGroup
import cbfg.rvadapter.RVHolder
import cbfg.rvadapter.RVHolderFactory
import com.vspace.R
import com.vspace.bean.GmsBean
import com.vspace.databinding.ItemGmsBinding

/**
 * RecyclerView adapter factory for displaying [GmsBean] items
 * in the GMS manager screen.
 *
 * Each item shows the user name and a toggle switch indicating
 * whether GMS is installed for that user.
 */
class GmsAdapter : RVHolderFactory() {
    override fun createViewHolder(parent: ViewGroup?, viewType: Int, item: Any): RVHolder<out Any> {
        return GmsVH(inflate(R.layout.item_gms, parent))
    }

    /**
     * ViewHolder that binds a [GmsBean] to the item_gms layout.
     *
     * When the checkbox is pressed by the user (not programmatically),
     * it triggers the root view's click listener to handle install/uninstall flow.
     */
    class GmsVH(itemView: View) : RVHolder<GmsBean>(itemView) {
        private val binding = ItemGmsBinding.bind(itemView)

        override fun setContent(item: GmsBean, isSelected: Boolean, payload: Any?) {
            binding.tvTitle.text = item.userName
            binding.checkbox.isChecked = item.isInstalledGms
            binding.checkbox.setOnCheckedChangeListener  { buttonView, _ ->
                // Only forward user-initiated presses, not programmatic setChecked calls
                if (buttonView.isPressed) {
                    binding.root.performClick()
                }
            }
        }
    }
}
