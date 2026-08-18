package com.rc.apks.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.rc.apks.R
import com.rc.apks.databinding.HomeActivityBinding
import com.rc.apks.app.AppBarActivity
import com.rc.apks.howto.HowToActivity
import com.rc.apks.settings.SettingsFragment
import com.rc.apks.ShizukuSettings
import com.rc.apks.sideloader.SideloaderFragment

abstract class HomeActivity : AppBarActivity() {

    private lateinit var binding: HomeActivityBinding

    private val howToLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = HomeActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        findViewById<android.view.View>(R.id.toolbar_container)?.visibility = android.view.View.GONE

        if (savedInstanceState == null && !ShizukuSettings.getPreferences()
                .getBoolean(ShizukuSettings.FIRST_LAUNCH_DONE, false)
        ) {
            howToLauncher.launch(Intent(this, HowToActivity::class.java))
        }

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
                R.id.nav_settings -> {
                    switchFragment(SettingsFragment())
                    supportActionBar?.setTitle(R.string.settings_title)
                    true
                }
                else -> false
            }
        }

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
}
