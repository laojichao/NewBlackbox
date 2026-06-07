package com.vspace.view.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.vcore.BlackBoxCore
import com.vspace.R
import com.vspace.app.App
import com.vspace.app.AppManager
import com.vspace.databinding.ActivityMainBinding
import com.vspace.util.Resolution
import com.vspace.util.ViewBindingEx.inflate
import com.vspace.view.apps.AppsFragment
import com.vspace.view.base.LoadingActivity
import com.vspace.view.fake.FakeManagerActivity
import com.vspace.view.list.ListActivity
import com.vspace.view.setting.SettingActivity

/**
 * Main launcher activity that displays a [ViewPager2] of [AppsFragment] pages,
 * one per virtual user. Provides floating-action-button for installing new apps,
 * toolbar user-remark editing, and navigation to settings, GMS manager, fake location,
 * and external links.
 */
class MainActivity : LoadingActivity() {
    private val viewBinding: ActivityMainBinding by inflate()
    private lateinit var mViewPagerAdapter: ViewPagerAdapter
    /** List of [AppsFragment] instances, one per virtual user plus one "new user" placeholder. */
    private val fragmentList = mutableListOf<AppsFragment>()
    /** The user ID of the currently visible ViewPager page. */
    private var currentUser = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        initToolbar(viewBinding.toolbarLayout.toolbar, R.string.app_name)
        initViewPager()
        initFab()
        initToolbarSubTitle()
    }

    /**
     * Sets up the toolbar subtitle to display the current virtual user's remark/alias.
     * Tapping the subtitle opens a dialog to edit the remark.
     */
    private fun initToolbarSubTitle() {
        updateUserRemark(0)

        viewBinding.toolbarLayout.toolbar.getChildAt(1).setOnClickListener {
            MaterialDialog(this).show {
                title(res = R.string.userRemark)
                input(
                    hintRes = R.string.userRemark,
                    prefill = viewBinding.toolbarLayout.toolbar.subtitle
                ) { _, input ->
                    AppManager.mRemarkSharedPreferences.edit {
                        putString("Remark$currentUser", input.toString())
                        viewBinding.toolbarLayout.toolbar.subtitle = input
                    }
                }
                positiveButton(res = R.string.done)
                negativeButton(res = R.string.cancel)
            }
        }
    }

    /**
     * Initializes the [ViewPager2] with one [AppsFragment] per existing virtual user
     * plus an empty trailing slot for creating a new user. Attaches the dots indicator
     * and page-change callback to track the active user.
     */
    private fun initViewPager() {
        val userList = BlackBoxCore.get().users
        userList.forEach {
            fragmentList.add(AppsFragment.newInstance(it.id))
        }

        currentUser = userList.firstOrNull()?.id ?: 0
        fragmentList.add(AppsFragment.newInstance(userList.size))

        mViewPagerAdapter = ViewPagerAdapter(this)
        mViewPagerAdapter.replaceData(fragmentList)
        viewBinding.viewPager.adapter = mViewPagerAdapter

        viewBinding.dotsIndicator.attachTo(viewBinding.viewPager)
        viewBinding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentUser = fragmentList[position].userID

                updateUserRemark(currentUser)
                showFloatButton(true)
            }
        })
    }

    /**
     * Configures the FAB to launch [ListActivity] for installing a new app
     * into the currently selected virtual user.
     */
    private fun initFab() {
        viewBinding.fab.setOnClickListener {
            val userId = viewBinding.viewPager.currentItem
            val intent = Intent(this, ListActivity::class.java)

            intent.putExtra("userID", userId)
            apkPathResult.launch(intent)
        }
    }

    /**
     * Animates the floating action button in or out of view.
     *
     * @param show true to fade in and slide up; false to fade out and slide down.
     */
    fun showFloatButton(show: Boolean) {
        val tranY: Float = Resolution.convertDpToPixel(120F, App.getContext())
        val time = 200L

        if (show) {
            viewBinding.fab.animate().translationY(0f).alpha(1f).setDuration(time)
                .start()
        } else {
            viewBinding.fab.animate().translationY(tranY).alpha(0f).setDuration(time)
                .start()
        }
    }

    /**
     * Synchronizes the ViewPager page count with the actual number of virtual users.
     * Adds or removes the trailing "new user" placeholder fragment as needed.
     */
    fun scanUser() {
        val userList = BlackBoxCore.get().users

        if (fragmentList.size == userList.size) {
            fragmentList.add(AppsFragment.newInstance(fragmentList.size))
        } else if (fragmentList.size > userList.size + 1) {
            fragmentList.removeLast()
        }
        mViewPagerAdapter.notifyDataSetChanged()
    }

    /**
     * Updates the toolbar subtitle with the user's remark/alias for the given [userId].
     *
     * @param userId the virtual user ID whose remark to display.
     */
    private fun updateUserRemark(userId: Int) {
        var remark = AppManager.mRemarkSharedPreferences.getString("Remark$userId", "User $userId")
        if (remark.isNullOrEmpty()) {
            remark = "User $userId"
        }
        viewBinding.toolbarLayout.toolbar.subtitle = remark
    }

    /**
     * Activity result launcher that receives the selected APK source from [ListActivity]
     * and triggers installation into the appropriate user's [AppsFragment].
     */
    private val apkPathResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                it.data?.let { data ->
                    val userId = data.getIntExtra("userID", 0)
                    val source = data.getStringExtra("source")

                    if (source != null) {
                        fragmentList[userId].installApk(source)
                    }
                }
            }
        }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.main_git -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/alex5402/NewBlackbox"))
                startActivity(intent)
            }

            R.id.main_setting -> {
                SettingActivity.start(this)
            }

            R.id.main_tg -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/"))
                startActivity(intent)
            }

            R.id.fake_location -> {
                val intent = Intent(this, FakeManagerActivity::class.java)
                intent.putExtra("userID", currentUser)
                startActivity(intent)
            }
        }
        return true
    }

    companion object {
        /**
         * Convenience method to start this activity from any [Context].
         *
         * @param context the launching [Context].
         */
        fun start(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }
}
