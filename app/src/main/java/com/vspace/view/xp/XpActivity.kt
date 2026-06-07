package com.vspace.view.xp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cbfg.rvadapter.RVAdapter
import com.afollestad.materialdialogs.MaterialDialog
import com.vcore.BlackBoxCore
import com.vspace.R
import com.vspace.bean.XpModuleInfo
import com.vspace.databinding.ActivityXpBinding
import com.vspace.util.InjectionUtil
import com.vspace.util.ToastEx.toast
import com.vspace.util.ViewBindingEx.inflate
import com.vspace.view.base.LoadingActivity
import com.vspace.view.list.ListActivity

/**
 * Activity that displays all installed Xposed modules in the virtual environment
 * and allows enabling/disabling or uninstalling them.
 *
 * The FAB opens [ListActivity] in Xposed-module-only mode to install new modules.
 */
class XpActivity : LoadingActivity() {
    private val viewBinding: ActivityXpBinding by inflate()
    private lateinit var viewModel: XpViewModel
    private lateinit var mAdapter: RVAdapter<XpModuleInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        initToolbar(viewBinding.toolbarLayout.toolbar, R.string.xp_setting, true)

        viewModel = ViewModelProvider(this, InjectionUtil.getXpFactory())[XpViewModel::class.java]
        initRecyclerView()
        initFab()
    }

    /**
     * Starts observing the module list and operation-result LiveData,
     * then triggers a module refresh from the repository.
     */
    private fun observeLiveData() {
        viewBinding.stateView.showLoading()
        viewModel.getInstalledModule()
        viewModel.appsLiveData.observe(this) {
            if (it.isNullOrEmpty()) {
                viewBinding.stateView.showEmpty()
            } else {
                mAdapter.setItems(it)
                viewBinding.stateView.showContent()
            }
        }

        viewModel.resultLiveData.observe(this) {
            if (!TextUtils.isEmpty(it)) {
                hideLoading()
                toast(it)
                viewModel.getInstalledModule()
            }
        }
    }

    /**
     * Sets up the RecyclerView with the [XpAdapter]. Clicking a module toggles
     * its enable state; long-pressing triggers an uninstall confirmation dialog.
     */
    private fun initRecyclerView() {
        mAdapter = RVAdapter<XpModuleInfo>(this, XpAdapter())
			.bind(viewBinding.recyclerView)
            .setItemClickListener { _, item, position ->
                item.enable = !item.enable
                BlackBoxCore.get().setModuleEnable(item.packageName, item.enable)
                mAdapter.replaceAt(position, item)
                toast(R.string.restart_module)
            }
            .setItemLongClickListener { _, item, _ ->
                unInstallModule(item.packageName)
            }

        viewBinding.recyclerView.layoutManager = LinearLayoutManager(this)
        viewBinding.stateView.showEmpty()
    }

    /**
     * Configures the FAB to open [ListActivity] showing only Xposed modules
     * for installation into the virtual environment.
     */
    private fun initFab() {
        viewBinding.fab.setOnClickListener {
            val intent = Intent(this, ListActivity::class.java)
            intent.putExtra("onlyShowXp", true)
            apkPathResult.launch(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        observeLiveData()
    }

    override fun onStop() {
        super.onStop()
        viewModel.appsLiveData.value = null
        viewModel.appsLiveData.removeObservers(this)
        viewModel.resultLiveData.value = null
        viewModel.resultLiveData.removeObservers(this)
    }

    /**
     * Shows a confirmation dialog before uninstalling the specified Xposed module.
     *
     * @param packageName the package name of the module to uninstall.
     */
    private fun unInstallModule(packageName: String) {
        MaterialDialog(this).show {
            title(R.string.uninstall_module)
            message(R.string.uninstall_module_hint)
            positiveButton(R.string.done) {
                showLoading()
                viewModel.unInstallModule(packageName)
            }
            negativeButton(R.string.cancel)
        }
    }

    /**
     * Triggers installation of a module from the given [source].
     *
     * @param source the module APK path or URL.
     */
    private fun installModule(source: String) {
        showLoading()
        viewModel.installModule(source)
    }

    /**
     * Activity result launcher that receives the selected module source from [ListActivity].
     */
    private val apkPathResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            it.data?.let { data ->
                val source = data.getStringExtra("source")
                if (source != null) {
                    installModule(source)
                }
            }
        }
    }

    companion object {
        /**
         * Convenience method to start this activity from any [Context].
         *
         * @param context the launching [Context].
         */
        fun start(context: Context) {
            val intent = Intent(context, XpActivity::class.java)
            context.startActivity(intent)
        }
    }
}
