package com.vspace.view.apps

import android.graphics.Point
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import cbfg.rvadapter.RVAdapter
import com.afollestad.materialdialogs.MaterialDialog
import com.vcore.BlackBoxCore
import com.vspace.R
import com.vspace.bean.AppInfo
import com.vspace.databinding.FragmentAppsBinding
import com.vspace.util.InjectionUtil
import com.vspace.util.ShortcutUtil
import com.vspace.util.ToastEx.toast
import com.vspace.util.ViewBindingEx.inflate
import com.vspace.view.base.LoadingActivity
import com.vspace.view.main.MainActivity
import java.util.*
import kotlin.math.abs

/**
 * Fragment that displays the apps installed inside a specific virtual user.
 *
 * Supports launching, uninstalling, clearing data, force-stopping, and creating
 * home-screen shortcuts via a long-press context menu. Also supports drag-and-drop
 * reordering of apps through [AppsTouchCallBack].
 *
 * Use [newInstance] to create an instance with the required user ID argument.
 */
class AppsFragment : Fragment() {
    /** The virtual user ID this fragment represents. */
    var userID: Int = 0
    private lateinit var viewModel: AppsViewModel
    private lateinit var mAdapter: RVAdapter<AppInfo>
    private val viewBinding: FragmentAppsBinding by inflate()
    /** Holds the pending popup menu for touch-intercept dismissal logic. */
    private var popupMenu: PopupMenu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, InjectionUtil.getAppsFactory())[AppsViewModel::class.java]
        userID = requireArguments().getInt("userID", 0)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding.stateView.showEmpty()
        mAdapter = RVAdapter<AppInfo>(requireContext(), AppsAdapter())
			.bind(viewBinding.recyclerView)

        viewBinding.recyclerView.adapter = mAdapter
        viewBinding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 4)

        val touchCallBack = AppsTouchCallBack { from, to ->
            onItemMove(from, to)
            viewModel.updateSortLiveData.postValue(true)
        }

        val itemTouchHelper = ItemTouchHelper(touchCallBack)
        itemTouchHelper.attachToRecyclerView(viewBinding.recyclerView)
        mAdapter.setItemClickListener { _, data, _ ->
            showLoading()
            viewModel.launchApk(data.packageName, userID)
        }

        interceptTouch()
        setOnLongClick()
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
    }

    override fun onStart() {
        super.onStart()
        viewModel.getInstalledApps(userID)
    }

    /**
     * Intercepts touch events on the RecyclerView to coordinate drag vs. tap behavior.
     * Distinguishes between a vertical scroll (which hides/shows the FAB) and a tap
     * (which triggers the pending popup menu).
     */
    private fun interceptTouch() {
        val point = Point()

        viewBinding.recyclerView.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_UP -> {
                    if (!isMove(point, e)) {
                        popupMenu?.show()
                    }

                    popupMenu = null
                    point.set(0, 0)
                }

                MotionEvent.ACTION_MOVE -> {
                    if (point.x == 0 && point.y == 0) {
                        point.x = e.rawX.toInt()
                        point.y = e.rawY.toInt()
                    }

                    isDownAndUp(point, e)
                    if (isMove(point, e)) {
                        popupMenu?.dismiss()
                    }
                }
            }
            return@setOnTouchListener false
        }
    }

    /**
     * Determines whether the touch has moved beyond a 40px threshold,
     * indicating a drag rather than a tap.
     *
     * @param point the initial touch-down coordinates.
     * @param e the current [MotionEvent].
     * @return true if the touch moved beyond the threshold.
     */
    private fun isMove(point: Point, e: MotionEvent): Boolean {
        val max = 40
        val x = point.x
        val y = point.y

        val xU = abs(x - e.rawX)
        val yU = abs(y - e.rawY)
        return xU > max || yU > max
    }

    /**
     * Detects vertical movement and notifies the [MainActivity] to show/hide
     * the floating action button accordingly.
     *
     * @param point the initial touch-down coordinates.
     * @param e the current [MotionEvent].
     */
    private fun isDownAndUp(point: Point, e: MotionEvent) {
        val min = 10
        val y = point.y
        val yU = y - e.rawY

        if (abs(yU) > min) {
            (requireActivity() as MainActivity).showFloatButton(yU < 0)
        }
    }

    /**
     * Swaps items in the adapter list to reflect a drag-and-drop move
     * from [fromPosition] to [toPosition].
     *
     * @param fromPosition the start position of the dragged item.
     * @param toPosition the target position for the dragged item.
     */
    private fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(mAdapter.getItems(), i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(mAdapter.getItems(), i, i - 1)
            }
        }
        mAdapter.notifyItemMoved(fromPosition, toPosition)
    }

    /**
     * Configures the long-press context menu on each app item, offering:
     * uninstall, open, clear data, force stop, and create shortcut.
     */
    private fun setOnLongClick() {
        mAdapter.setItemLongClickListener { view, data, _ ->
            popupMenu = PopupMenu(requireContext(), view).also {
                // mAdapter.setItemClickListener { view, data, _ ->
                // PopupMenu(requireContext(), view).also {
                it.inflate(R.menu.app_menu)
                it.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.app_remove -> {
                            if (data.isXpModule) {
                                toast(R.string.uninstall_module_toast)
                            } else {
                                unInstallApk(data)
                            }
                        }

                        R.id.app_open -> {
                            showLoading()
                            viewModel.launchApk(data.packageName, userID)
                        }

                        R.id.app_clear -> {
                            clearApk(data)
                        }

                        R.id.app_stop -> {
                            stopApk(data)
                        }

                        R.id.app_shortcut -> {
                            ShortcutUtil.createShortcut(requireContext(), userID, data)
                        }
                    }
                    return@setOnMenuItemClickListener true
                }
                it.show()
            }
        }
    }

    /**
     * Sets up LiveData observers for the installed-app list, operation results,
     * launch outcomes, and sort-order updates.
     */
    private fun initData() {
        viewBinding.stateView.showLoading()
        viewModel.getInstalledApps(userID)
        viewModel.appsLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                mAdapter.setItems(it)
                if (it.isEmpty()) {
                    viewBinding.stateView.showEmpty()
                } else {
                    viewBinding.stateView.showContent()
                }
            }
        }

        viewModel.resultLiveData.observe(viewLifecycleOwner) {
            if (!TextUtils.isEmpty(it)) {
                hideLoading()
                requireContext().toast(it)
                viewModel.getInstalledApps(userID)
                scanUser()
            }
        }

        viewModel.launchLiveData.observe(viewLifecycleOwner) {
            it?.run {
                hideLoading()
                if (!it) {
                    toast(R.string.start_fail)
                }
            }
        }

        viewModel.updateSortLiveData.observe(viewLifecycleOwner) {
            if (this::mAdapter.isInitialized) {
                viewModel.updateApkOrder(userID, mAdapter.getItems())
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.resultLiveData.value = null
        viewModel.launchLiveData.value = null
    }

    /**
     * Shows a confirmation dialog before uninstalling the specified app.
     *
     * @param info the [AppInfo] of the app to uninstall.
     */
    private fun unInstallApk(info: AppInfo) {
        MaterialDialog(requireContext()).show {
            title(R.string.uninstall_app)
            message(text = getString(R.string.uninstall_app_hint, info.name))
            positiveButton(R.string.done) {
                showLoading()
                viewModel.unInstall(info.packageName, userID)
            }
            negativeButton(R.string.cancel)
        }
    }

    /**
     * Shows a confirmation dialog before force-stopping the specified app.
     *
     * @param info the [AppInfo] of the app to stop.
     */
    private fun stopApk(info: AppInfo) {
        MaterialDialog(requireContext()).show {
            title(R.string.app_stop)
            message(text = getString(R.string.app_stop_hint, info.name))
            positiveButton(R.string.done) {
                BlackBoxCore.get().stopPackage(info.packageName, userID)
                toast(getString(R.string.is_stop, info.name))
            }
            negativeButton(R.string.cancel)
        }
    }

    /**
     * Shows a confirmation dialog before clearing all data for the specified app.
     *
     * @param info the [AppInfo] of the app whose data to clear.
     */
    private fun clearApk(info: AppInfo) {
        MaterialDialog(requireContext()).show {
            title(R.string.app_clear)
            message(text = getString(R.string.app_clear_hint, info.name))
            positiveButton(R.string.done) {
                showLoading()
                viewModel.clearApkData(info.packageName, userID)
            }
            negativeButton(R.string.cancel)
        }
    }

    /**
     * Triggers installation of an APK from the given [source] into this fragment's virtual user.
     *
     * @param source the APK file path or URL to install.
     */
    fun installApk(source: String) {
        showLoading()
        viewModel.install(source, userID)
    }

    /**
     * Delegates to [MainActivity.scanUser] to synchronize the ViewPager user tabs.
     */
    private fun scanUser() {
        (requireActivity() as MainActivity).scanUser()
    }

    /**
     * Shows the loading dialog if the host activity implements [LoadingActivity].
     */
    private fun showLoading() {
        if (requireActivity() is LoadingActivity) {
            (requireActivity() as LoadingActivity).showLoading()
        }
    }

    /**
     * Hides the loading dialog if the host activity implements [LoadingActivity].
     */
    private fun hideLoading() {
        if (requireActivity() is LoadingActivity) {
            (requireActivity() as LoadingActivity).hideLoading()
        }
    }

    companion object {
        /**
         * Creates a new [AppsFragment] bound to the specified virtual user.
         *
         * @param userID the virtual user ID to display apps for.
         * @return a new [AppsFragment] instance with the user ID argument set.
         */
        fun newInstance(userID: Int): AppsFragment {
            val fragment = AppsFragment()
            val bundle = bundleOf("userID" to userID)

            fragment.arguments = bundle
            return fragment
        }
    }
}
