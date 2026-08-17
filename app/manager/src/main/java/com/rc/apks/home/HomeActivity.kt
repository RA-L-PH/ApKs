package com.rc.apks.home

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rc.apks.R
import com.rc.apks.databinding.AboutDialogBinding
import com.rc.apks.databinding.HomeActivityBinding
import com.rc.apks.app.AppBarActivity
import com.rc.apks.howto.HowToActivity
import com.rc.apks.ktx.toHtml
import com.rc.apks.settings.SettingsActivity
import com.rc.apks.ShizukuSettings
import com.rc.apks.sideloader.SideloaderFragment
import com.rc.apks.utils.AppIconCache
import rikka.shizuku.Shizuku

abstract class HomeActivity : AppBarActivity() {

    private lateinit var binding: HomeActivityBinding

    private val howToLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = HomeActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check first launch
        if (savedInstanceState == null && !ShizukuSettings.getPreferences()
                .getBoolean(ShizukuSettings.FIRST_LAUNCH_DONE, false)
        ) {
            howToLauncher.launch(Intent(this, HowToActivity::class.java))
        }

        // Setup bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_apks -> {
                    switchFragment(SideloaderFragment())
                    supportActionBar?.setTitle(R.string.nav_apks)
                    true
                }
                R.id.nav_server -> {
                    switchFragment(HomeFragment())
                    supportActionBar?.setTitle(R.string.nav_server)
                    true
                }
                else -> false
            }
        }

        // Load default fragment (APKs tab)
        if (savedInstanceState == null) {
            switchFragment(SideloaderFragment())
            supportActionBar?.setTitle(R.string.nav_apks)
        }
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_about -> {
                val dialogBinding = AboutDialogBinding.inflate(LayoutInflater.from(this), null, false)
                dialogBinding.sourceCode.movementMethod = LinkMovementMethod.getInstance()
                dialogBinding.sourceCode.text = getString(
                    R.string.about_view_source_code,
                    "<b><a href=\"https://github.com/RikkaApps/Shizuku\">GitHub</a></b>"
                ).toHtml()
                dialogBinding.icon.setImageBitmap(
                    AppIconCache.getOrLoadBitmap(
                        this,
                        applicationInfo,
                        Process.myUid() / 100000,
                        resources.getDimensionPixelOffset(R.dimen.default_app_icon_size)
                    )
                )
                dialogBinding.versionName.text = packageManager.getPackageInfo(packageName, 0).versionName
                MaterialAlertDialogBuilder(this)
                    .setView(dialogBinding.root)
                    .show()
                true
            }
            R.id.action_stop -> {
                if (!Shizuku.pingBinder()) {
                    return true
                }
                MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.dialog_stop_message)
                    .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                        try {
                            Shizuku.exit()
                        } catch (e: Throwable) {
                        }
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
