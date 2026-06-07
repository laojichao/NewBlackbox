package com.vspace.view.list

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cbfg.rvadapter.RVAdapter
import com.ferfalk.simplesearchview.SimpleSearchView
import com.vspace.R
import com.vspace.bean.InstalledAppBean
import com.vspace.databinding.ActivityListBinding
import com.vspace.util.InjectionUtil
import com.vspace.util.ViewBindingEx.inflate
import com.vspace.view.base.BaseActivity

/**
 * Activity that displays a searchable list of host-installed applications (or Xposed modules)
 * and returns the user's selection as the activity result.
 *
 * When launched with "onlyShowXp" = true, only Xposed modules are shown.
 * The selected package name (or APK URI from the file picker) is returned via RESULT_OK
 * in the "source" intent extra.
 */
class ListActivity : BaseActivity() {
    private val viewBinding: ActivityListBinding by inflate()
    private lateinit var mAdapter: RVAdapter<InstalledAppBean>
    private lateinit var viewModel: ListViewModel
    /** Full unfiltered app list, used as the source for text-based filtering. */
    private var appList: List<InstalledAppBean> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        initToolbar(viewBinding.toolbarLayout.toolbar, R.string.installed_app, true)
        mAdapter = RVAdapter<InstalledAppBean>(this,ListAdapter()).bind(viewBinding.recyclerView).setItemClickListener { _, item, _ ->
            finishWithResult(item.packageName)
        }

        viewBinding.recyclerView.layoutManager = LinearLayoutManager(this)

        initSearchView()
        initViewModel()
        onBackPressedDispatcher
            .addCallback(onBackPressedCallback)
    }

    /**
     * Wires up the [SimpleSearchView] to filter the displayed list as the user types.
     */
    private fun initSearchView() {
        viewBinding.searchView.setOnQueryTextListener(object : SimpleSearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                filterApp(newText)
                return true
            }

            override fun onQueryTextCleared(): Boolean {
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                return true
            }
        })
    }

    /**
     * Initializes the [ListViewModel] and determines whether to show all installed apps
     * or only Xposed modules based on the "onlyShowXp" intent extra.
     */
    private fun initViewModel() {
        viewModel = ViewModelProvider(this, InjectionUtil.getListFactory())[ListViewModel::class.java]
        val onlyShowXp = intent.getBooleanExtra("onlyShowXp", false)
        val userID = intent.getIntExtra("userID", 0)

        if (onlyShowXp) {
            viewModel.getInstalledModules()
            viewBinding.toolbarLayout.toolbar.setTitle(R.string.installed_module)
        } else {
            viewModel.getInstallAppList(userID)
            viewBinding.toolbarLayout.toolbar.setTitle(R.string.installed_app)
        }

        viewModel.loadingLiveData.observe(this) {
            if (it) {
                viewBinding.stateView.showLoading()
            } else {
                viewBinding.stateView.showContent()
            }
        }

        viewModel.appsLiveData.observe(this) {
            if (it != null) {
                this.appList = it
                viewBinding.searchView.setQuery("", false)
                filterApp("")

                if (it.isNotEmpty()) {
                    viewBinding.stateView.showContent()
                    viewModel.previewInstalledList()
                } else {
                    viewBinding.stateView.showEmpty()
                }
            }
        }
    }

    /**
     * Filters the adapter to items whose name or package name contains [newText].
     *
     * @param newText the search query string.
     */
    private fun filterApp(newText: String) {
        val newList = this.appList.filter {
            it.name.contains(newText, true) or it.packageName.contains(newText, true)
        }
        mAdapter.setItems(newList)
    }

    /**
     * Activity result launcher for the system file picker (APK selection).
     */
    private val openDocumentedResult = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it?.run {
            finishWithResult(it.toString())
        }
    }

    private val onBackPressedCallback =
        object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewBinding.searchView.isSearchOpen) {
                    viewBinding.searchView.closeSearch()
                }
                finish()
            }
        }

    /**
     * Sets the selected [source] as the activity result and finishes,
     * also hiding the soft keyboard.
     *
     * @param source the package name or APK URI to return.
     */
    private fun finishWithResult(source: String) {
        intent.putExtra("source", source)
        setResult(Activity.RESULT_OK, intent)

        val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        window.peekDecorView()?.run {
            imm.hideSoftInputFromWindow(windowToken, 0)
        }
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.list_choose) {
            openDocumentedResult.launch("application/vnd.android.package-archive")
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_list, menu)

        val item = menu.findItem(R.id.list_search)
        viewBinding.searchView.setMenuItem(item)
        return true
    }

    override fun onStop() {
        super.onStop()
        viewModel.loadingLiveData.postValue(true)
        viewModel.loadingLiveData.removeObservers(this)
        viewModel.appsLiveData.postValue(null)
        viewModel.appsLiveData.removeObservers(this)
    }

    companion object {
        /**
         * Convenience method to start this activity.
         *
         * @param context the launching [Context].
         * @param onlyShowXp if true, only Xposed modules are displayed; otherwise all apps.
         */
        fun start(context: Context, onlyShowXp: Boolean) {
            val intent = Intent(context, ListActivity::class.java)
            intent.putExtra("onlyShowXp", onlyShowXp)
            context.startActivity(intent)
        }
    }
}
