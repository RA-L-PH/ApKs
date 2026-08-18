package com.rc.apks.settings

import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.rc.apks.R
import com.rc.apks.ShizukuSettings
import com.rc.apks.app.ThemeHelper
import com.rc.apks.ktx.isComponentEnabled
import com.rc.apks.ktx.setComponentEnabled
import com.rc.apks.receiver.BootCompleteReceiver
import com.rc.apks.utils.AppIconCache
import com.rc.apks.utils.CustomTabsHelper
import rikka.material.app.LocaleDelegate
import rikka.shizuku.manager.ShizukuLocales
import java.util.*

class SettingsFragment : Fragment() {

    private var _nightModeValue = ShizukuSettings.getNightMode()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAccordion(view, R.id.appearanceHeader, R.id.appearanceContent, R.id.appearanceArrow)
        setupAccordion(view, R.id.languageHeader, R.id.languageContent, R.id.languageArrow)
        setupAccordion(view, R.id.startupHeader, R.id.startupContent, R.id.startupArrow)
        setupAccordion(view, R.id.aboutHeader, R.id.aboutContent, R.id.aboutArrow)

        setupAppearance(view)
        setupLanguage(view)
        setupStartup(view)
        setupAbout(view)
    }

    private fun setupAccordion(view: View, headerId: Int, contentId: Int, arrowId: Int) {
        val header = view.findViewById<LinearLayout>(headerId) ?: return
        val content = view.findViewById<LinearLayout>(contentId) ?: return
        val arrow = view.findViewById<ImageView>(arrowId) ?: return

        header.setOnClickListener {
            if (content.visibility == View.VISIBLE) {
                content.visibility = View.GONE
                arrow.rotation = 0f
            } else {
                content.visibility = View.VISIBLE
                arrow.rotation = 180f
            }
        }
    }

    private fun setupAppearance(view: View) {
        val switchBlackTheme = view.findViewById<MaterialSwitch>(R.id.switchBlackTheme)
        val switchSystemColor = view.findViewById<MaterialSwitch>(R.id.switchSystemColor)
        val btnNightMode = view.findViewById<MaterialButton>(R.id.btnNightMode)

        val nightMode = ShizukuSettings.getNightMode()
        btnNightMode.text = when (nightMode) {
            AppCompatDelegate.MODE_NIGHT_YES -> "Dark"
            AppCompatDelegate.MODE_NIGHT_NO -> "Light"
            else -> "Follow system"
        }

        btnNightMode.setOnClickListener {
            val modes = arrayOf("Follow system", "Light", "Dark")
            val values = arrayOf(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                AppCompatDelegate.MODE_NIGHT_NO,
                AppCompatDelegate.MODE_NIGHT_YES
            )
            val current = values.indexOf(_nightModeValue)

            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Dark theme")
                .setItems(modes) { _, which ->
                    val newMode = values[which]
                    if (_nightModeValue != newMode) {
                        _nightModeValue = newMode
                        ShizukuSettings.getPreferences().edit()
                            .putInt(ShizukuSettings.NIGHT_MODE, newMode)
                            .apply()
                        AppCompatDelegate.setDefaultNightMode(newMode)
                        btnNightMode.text = modes[which]
                        activity?.recreate()
                    }
                }
                .show()
        }

        if (ShizukuSettings.getNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
            switchBlackTheme.isChecked = ThemeHelper.isBlackNightTheme(requireContext())
            switchBlackTheme.setOnCheckedChangeListener { _, isChecked ->
                ShizukuSettings.getPreferences().edit()
                    .putBoolean(ThemeHelper.KEY_BLACK_NIGHT_THEME, isChecked)
                    .apply()
                if (isNightMode()) {
                    activity?.recreate()
                }
            }
        } else {
            switchBlackTheme.visibility = View.GONE
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            switchSystemColor.isChecked = ThemeHelper.isUsingSystemColor()
            switchSystemColor.setOnCheckedChangeListener { _, isChecked ->
                ShizukuSettings.getPreferences().edit()
                    .putBoolean(ThemeHelper.KEY_USE_SYSTEM_COLOR, isChecked)
                    .apply()
                if (ThemeHelper.isUsingSystemColor() != isChecked) {
                    activity?.recreate()
                }
            }
        } else {
            switchSystemColor.visibility = View.GONE
        }
    }

    private fun setupLanguage(view: View) {
        val btnLanguage = view.findViewById<MaterialButton>(R.id.btnLanguage)
        val btnTranslation = view.findViewById<MaterialButton>(R.id.btnTranslation)

        val currentTag = ShizukuSettings.getPreferences().getString(ShizukuSettings.LANGUAGE, null)
        btnLanguage.text = if (currentTag.isNullOrEmpty() || "SYSTEM" == currentTag) {
            getString(R.string.follow_system)
        } else {
            val locale = Locale.forLanguageTag(currentTag)
            locale?.getDisplayName(locale) ?: currentTag
        }

        btnLanguage.setOnClickListener {
            val localeTags = ShizukuLocales.LOCALES
            val displayLocaleTags = ShizukuLocales.DISPLAY_LOCALES
            val names = mutableListOf<String>()
            names.add(getString(R.string.follow_system))
            for (tag in displayLocaleTags) {
                val locale = Locale.forLanguageTag(tag.toString())
                names.add(locale?.getDisplayName(locale) ?: tag.toString())
            }

            val currentIdx = localeTags.indexOf(currentTag).coerceAtLeast(0)

            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Language")
                .setSingleChoiceItems(names.toTypedArray(), currentIdx) { dialog, which ->
                    val newTag = if (which == 0) "SYSTEM" else localeTags[which]
                    val locale = if ("SYSTEM" == newTag) {
                        LocaleDelegate.systemLocale
                    } else {
                        Locale.forLanguageTag(newTag)
                    }
                    LocaleDelegate.defaultLocale = locale
                    ShizukuSettings.getPreferences().edit()
                        .putString(ShizukuSettings.LANGUAGE, newTag)
                        .apply()
                    dialog.dismiss()
                    activity?.recreate()
                }
                .show()
        }

        btnTranslation.setOnClickListener {
            CustomTabsHelper.launchUrlOrCopy(
                requireContext(),
                getString(R.string.translation_url)
            )
        }
    }

    private fun setupStartup(view: View) {
        val switchStartOnBoot = view.findViewById<MaterialSwitch>(R.id.switchStartOnBoot)
        val componentName = ComponentName(
            requireContext().packageName,
            BootCompleteReceiver::class.java.name
        )

        switchStartOnBoot.isChecked = requireContext().packageManager.isComponentEnabled(componentName)
        switchStartOnBoot.setOnCheckedChangeListener { _, isChecked ->
            requireContext().packageManager.setComponentEnabled(componentName, isChecked)
            if (requireContext().packageManager.isComponentEnabled(componentName) != isChecked) {
                switchStartOnBoot.isChecked = !isChecked
                Toast.makeText(requireContext(), "Failed to update setting", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupAbout(view: View) {
        val aboutIcon = view.findViewById<ImageView>(R.id.aboutIcon)
        val aboutVersion = view.findViewById<TextView>(R.id.aboutVersion)
        val btnSourceCode = view.findViewById<MaterialButton>(R.id.btnSourceCode)

        try {
            aboutIcon?.setImageBitmap(
                AppIconCache.getOrLoadBitmap(
                    requireContext(),
                    requireContext().applicationInfo,
                    android.os.Process.myUid() / 100000,
                    resources.getDimensionPixelOffset(R.dimen.default_app_icon_size)
                )
            )
        } catch (_: Exception) {}

        aboutVersion?.text = try {
            requireContext().packageManager
                .getPackageInfo(requireContext().packageName, 0).versionName
        } catch (_: Exception) { "" }

        val btnShizuku = view.findViewById<MaterialButton>(R.id.btnShizuku)
        btnShizuku?.setOnClickListener {
            CustomTabsHelper.launchUrlOrCopy(
                requireContext(),
                "https://github.com/rikkaapps/shizuku"
            )
        }

        btnSourceCode?.setOnClickListener {
            CustomTabsHelper.launchUrlOrCopy(
                requireContext(),
                "https://github.com/RA-L-PH"
            )
        }

        val btnPortfolio = view.findViewById<MaterialButton>(R.id.btnPortfolio)
        btnPortfolio?.setOnClickListener {
            CustomTabsHelper.launchUrlOrCopy(
                requireContext(),
                "https://ra-l-ph.pages.dev"
            )
        }
    }

    private fun isNightMode(): Boolean {
        return rikka.core.util.ResourceUtils.isNightMode(requireContext().resources.configuration)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
