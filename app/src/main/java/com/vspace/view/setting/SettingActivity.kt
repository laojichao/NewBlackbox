package com.vspace.view.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.vspace.R
import com.vspace.databinding.ActivitySettingBinding
import com.vspace.util.ViewBindingEx.inflate
import com.vspace.view.base.BaseActivity

/**
 * Host activity for the settings screen. Embeds [SettingFragment] in its layout.
 */
class SettingActivity : BaseActivity() {
    private val viewBinding: ActivitySettingBinding by inflate()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        initToolbar(viewBinding.toolbarLayout.toolbar, R.string.setting, true)
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, SettingFragment())
                .commit()
    }

    companion object {
        /**
         * Convenience method to start this activity from any [Context].
         *
         * @param context the launching [Context].
         */
        fun start(context: Context) {
            val intent = Intent(context, SettingActivity::class.java)
            intent.action = Intent.ACTION_OPEN_DOCUMENT
            context.startActivity(intent)
        }
    }
}
