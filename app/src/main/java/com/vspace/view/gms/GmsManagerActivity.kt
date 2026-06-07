package com.vspace.view.gms

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Switch
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cbfg.rvadapter.RVAdapter
import com.afollestad.materialdialogs.MaterialDialog
import com.vspace.R
import com.vspace.bean.GmsBean
import com.vspace.databinding.ActivityGmsBinding
import com.vspace.util.InjectionUtil
import com.vspace.util.ToastEx.toast
import com.vspace.util.ViewBindingEx.inflate
import com.vspace.view.base.LoadingActivity

/**
 * Activity that displays a list of all virtual users with the ability to
 * install or uninstall Google Mobile Services (GMS) per user.
 *
 * Each user row shows a toggle switch that triggers a confirmation dialog
 * before performing the install/uninstall operation.
 */
class GmsManagerActivity : LoadingActivity() {
    private lateinit var viewModel: GmsViewModel
    private lateinit var mAdapter: RVAdapter<GmsBean>
    private val viewBinding: ActivityGmsBinding by inflate()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        initToolbar(viewBinding.toolbarLayout.toolbar, R.string.gms_manager, true)
        initViewModel()
        initRecyclerView()
    }

    /**
     * Initializes the [GmsViewModel] and wires up LiveData observers for
     * the user list and install/uninstall operation results.
     */
    private fun initViewModel() {
        viewModel = ViewModelProvider(this, InjectionUtil.getGmsFactory())[GmsViewModel::class.java]
        showLoading()

        viewModel.mInstalledLiveData.observe(this) {
            hideLoading()
            mAdapter.setItems(it)
        }

        viewModel.mUpdateInstalledLiveData.observe(this) { result ->
            if (result == null) {
                return@observe
            }

            val items = mAdapter.getItems()
            for (index in items.indices) {
                val bean = items[index]
                if (bean.userID == result.userID) {
                    if (result.success) {
                        bean.isInstalledGms = !bean.isInstalledGms
                    }
                    mAdapter.replaceAt(index, bean)
                    break
                }
            }

            hideLoading()
            if (result.success) {
                toast(result.msg)
            } else {
                MaterialDialog(this).show {
                    title(R.string.gms_manager)
                    message(text = result.msg)
                    positiveButton(R.string.done)
                }
            }
        }
        viewModel.getInstalledUser()
    }

    /**
     * Sets up the RecyclerView with the [GmsAdapter] and click listeners
     * that toggle GMS install/uninstall based on current state.
     */
    private fun initRecyclerView() {
        mAdapter = RVAdapter<GmsBean>(this, GmsAdapter())
			.bind(viewBinding.recyclerView)
            .setItemClickListener { view, item, _ ->
                val checkbox = view.findViewById<Switch>(R.id.checkbox)
                if (item.isInstalledGms) {
                    uninstallGms(item.userID, checkbox)
                } else {
                    installGms(item.userID, checkbox)
                }
            }
        viewBinding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    /**
     * Shows a confirmation dialog before installing GMS for the specified user.
     * Reverts the checkbox state if the user cancels.
     *
     * @param userID the virtual user ID to install GMS into.
     * @param checkbox the [Switch] widget to revert on cancel.
     */
    private fun installGms(userID: Int, checkbox: Switch) {
        MaterialDialog(this).show {
            title(R.string.enable_gms)
            message(R.string.enable_gms_hint)
            positiveButton(R.string.done) {
                showLoading()
                viewModel.installGms(userID)
            }

            negativeButton(R.string.cancel) {
                checkbox.isChecked = !checkbox.isChecked
            }
        }
    }

    /**
     * Shows a confirmation dialog before uninstalling GMS from the specified user.
     * Reverts the checkbox state if the user cancels.
     *
     * @param userID the virtual user ID to uninstall GMS from.
     * @param checkbox the [Switch] widget to revert on cancel.
     */
    private fun uninstallGms(userID: Int, checkbox: Switch) {
        MaterialDialog(this).show {
            title(R.string.disable_gms)
            message(R.string.disable_gms_hint)
            positiveButton(R.string.done) {
                showLoading()
                viewModel.uninstallGms(userID)
            }

            negativeButton(R.string.cancel) {
                checkbox.isChecked = !checkbox.isChecked
            }
        }
    }

    companion object{
        /**
         * Convenience method to start this activity from any [Context].
         *
         * @param context the launching [Context].
         */
        fun start(context: Context) {
            val intent = Intent(context, GmsManagerActivity::class.java)
            context.startActivity(intent)
        }
    }
}
