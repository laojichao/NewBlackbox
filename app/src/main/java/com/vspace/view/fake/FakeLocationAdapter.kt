package com.vspace.view.fake

import android.view.View
import android.view.ViewGroup
import cbfg.rvadapter.RVHolder
import cbfg.rvadapter.RVHolderFactory
import com.vcore.fake.frameworks.BLocationManager
import com.vspace.R
import com.vspace.bean.FakeLocationBean
import com.vspace.databinding.ItemFakeBinding
import com.vspace.util.ResUtil.getString

/**
 * RecyclerView adapter factory for displaying [FakeLocationBean] items
 * in the fake location manager screen.
 *
 * Shows the app icon, name, and the currently configured fake coordinates
 * (or "real location" when no override is active).
 */
class FakeLocationAdapter : RVHolderFactory() {
    override fun createViewHolder(parent: ViewGroup?, viewType: Int, item: Any): RVHolder<out Any> {
        return FakeLocationVH(inflate(R.layout.item_fake, parent))
    }

    /**
     * ViewHolder that binds a [FakeLocationBean] to the item_fake layout.
     *
     * Displays either the overridden lat/lng or a "real location" label when
     * no override is configured or the mode is [BLocationManager.CLOSE_MODE].
     */
    class FakeLocationVH(itemView: View) : RVHolder<FakeLocationBean>(itemView) {
        private val binding = ItemFakeBinding.bind(itemView)

        override fun setContent(item: FakeLocationBean, isSelected: Boolean, payload: Any?) {
            binding.icon.setImageDrawable(item.icon)
            binding.name.text = item.name

            if (item.fakeLocation == null || item.fakeLocationPattern == BLocationManager.CLOSE_MODE) {
                binding.fakeLocation.text = getString(R.string.real_location)
            } else {
                binding.fakeLocation.text = String.format("%f, %f", item.fakeLocation!!.latitude, item.fakeLocation!!.longitude)
            }
            binding.cornerLabel.visibility = View.VISIBLE
        }
    }
}
