package com.rc.apks.howto

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.rc.apks.R
import com.rc.apks.ShizukuSettings

class HowToActivity : AppCompatActivity() {

    private sealed class HowToPage {
        data class InfoPage(
            val iconRes: Int,
            val titleRes: Int,
            val descriptionRes: Int,
            val iconDescRes: Int
        ) : HowToPage()

        object PermissionsPage : HowToPage()
    }

    private val pages = listOf(
        HowToPage.InfoPage(R.drawable.ic_sideload, R.string.howto_page1_title, R.string.howto_page1_desc, R.string.howto_page1_icon_desc),
        HowToPage.InfoPage(R.drawable.ic_sideload, R.string.howto_page2_title, R.string.howto_page2_desc, R.string.howto_page2_icon_desc),
        HowToPage.InfoPage(R.drawable.ic_server_start_24dp, R.string.howto_page3_title, R.string.howto_page3_desc, R.string.howto_page3_icon_desc),
        HowToPage.InfoPage(R.drawable.ic_baseline_link_24, R.string.howto_page4_title, R.string.howto_page4_desc, R.string.howto_page4_icon_desc),
        HowToPage.PermissionsPage
    )

    private var notificationGranted = false
    private var storageGranted = false

    private val notificationPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationGranted = granted
        updatePermissionUI()
        if (granted) {
            Toast.makeText(this, R.string.howto_permissions_granted, Toast.LENGTH_SHORT).show()
        }
    }

    private val storagePermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        storageGranted = granted
        updatePermissionUI()
        if (granted) {
            Toast.makeText(this, R.string.howto_permissions_granted, Toast.LENGTH_SHORT).show()
        }
    }

    private var viewPager: ViewPager2? = null

    private fun updatePermissionUI() {
        val holder = viewPager?.let { vp ->
            val recyclerView = vp.getChildAt(0) as? RecyclerView
            recyclerView?.findViewHolderForAdapterPosition(pages.size - 1) as? PermissionsViewHolder
        } ?: return

        holder.btnNotification.isEnabled = !notificationGranted
        holder.btnStorage.isEnabled = !storageGranted
        holder.btnNotification.text = if (notificationGranted) {
            getString(R.string.howto_permissions_granted)
        } else {
            getString(R.string.howto_permissions_notification)
        }
        holder.btnStorage.text = if (storageGranted) {
            getString(R.string.howto_permissions_granted)
        } else {
            getString(R.string.howto_permissions_storage)
        }
        holder.permissionStatus.text = buildString {
            if (notificationGranted) append("✓ Notification  ")
            if (storageGranted) append("✓ Storage")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_howto)

        viewPager = findViewById(R.id.view_pager)
        val btnSkip = findViewById<MaterialButton>(R.id.btn_skip)
        val btnNext = findViewById<MaterialButton>(R.id.btn_next)

        // Check existing permissions
        notificationGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        storageGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }

        viewPager!!.adapter = HowToPagerAdapter(pages)

        viewPager!!.contentDescription = getString(R.string.howto_content_desc)

        viewPager!!.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val isLast = position == pages.size - 1
                btnNext.text = if (isLast) getString(R.string.howto_get_started) else getString(R.string.howto_next)
                btnSkip.visibility = if (isLast) View.GONE else View.VISIBLE
                btnNext.contentDescription = btnNext.text
                viewPager!!.announceForAccessibility(
                    getString(R.string.howto_page_announce, position + 1, pages.size)
                )
                if (isLast) {
                    updatePermissionUI()
                }
            }
        })

        val finishAction = View.OnClickListener {
            ShizukuSettings.getPreferences().edit()
                .putBoolean(ShizukuSettings.FIRST_LAUNCH_DONE, true)
                .apply()
            setResult(RESULT_OK)
            finish()
        }

        btnSkip.setOnClickListener(finishAction)
        btnNext.setOnClickListener {
            if (viewPager!!.currentItem < pages.size - 1) {
                viewPager!!.currentItem = viewPager!!.currentItem + 1
            } else {
                finishAction.onClick(it)
            }
        }
    }

    private fun onNotificationPermissionClicked() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationGranted = true
                updatePermissionUI()
            } else {
                notificationPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            notificationGranted = true
            updatePermissionUI()
        }
    }

    private fun onStoragePermissionClicked() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                storageGranted = true
                updatePermissionUI()
            } else {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:${packageName}")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivity(intent)
                }
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                storageGranted = true
                updatePermissionUI()
            } else {
                storagePermLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewPager?.currentItem == pages.size - 1) {
            notificationGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            storageGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            }
            updatePermissionUI()
        }
    }

    inner class PermissionsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.page_icon)
        val title: TextView = view.findViewById(R.id.page_title)
        val description: TextView = view.findViewById(R.id.page_description)
        val btnNotification: MaterialButton = view.findViewById(R.id.btn_notification_permission)
        val btnStorage: MaterialButton = view.findViewById(R.id.btn_storage_permission)
        val permissionStatus: TextView = view.findViewById(R.id.permission_status)

        init {
            btnNotification.setOnClickListener { onNotificationPermissionClicked() }
            btnStorage.setOnClickListener { onStoragePermissionClicked() }
        }
    }

    private class HowToPagerAdapter(
        private val pages: List<HowToPage>
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private companion object {
            const val TYPE_INFO = 0
            const val TYPE_PERMISSIONS = 1
        }

        class InfoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val icon: ImageView = view.findViewById(R.id.page_icon)
            val title: TextView = view.findViewById(R.id.page_title)
            val description: TextView = view.findViewById(R.id.page_description)
        }

        override fun getItemViewType(position: Int): Int {
            return when (pages[position]) {
                is HowToPage.InfoPage -> TYPE_INFO
                is HowToPage.PermissionsPage -> TYPE_PERMISSIONS
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return when (viewType) {
                TYPE_PERMISSIONS -> {
                    val view = inflater.inflate(R.layout.item_howto_permissions, parent, false)
                    (parent.context as HowToActivity).PermissionsViewHolder(view)
                }
                else -> {
                    val view = inflater.inflate(R.layout.item_howto_page, parent, false)
                    InfoViewHolder(view)
                }
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (val page = pages[position]) {
                is HowToPage.InfoPage -> {
                    val h = holder as InfoViewHolder
                    h.icon.setImageResource(R.mipmap.ic_launcher)
                    h.icon.clipToOutline = true
                    h.icon.contentDescription = holder.itemView.context.getString(page.iconDescRes)
                    h.title.setText(page.titleRes)
                    h.description.setText(page.descriptionRes)

                    ViewCompat.setAccessibilityDelegate(holder.itemView, object : AccessibilityDelegateCompat() {
                        override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
                            super.onInitializeAccessibilityNodeInfo(host, info)
                            info.contentDescription = buildString {
                                append(holder.itemView.context.getString(page.titleRes))
                                append(". ")
                                append(holder.itemView.context.getString(page.descriptionRes))
                            }
                        }
                    })
                }
                is HowToPage.PermissionsPage -> {
                    val h = holder as PermissionsViewHolder
                    h.icon.setImageResource(R.mipmap.ic_launcher)
                    h.icon.clipToOutline = true
                    h.title.setText(R.string.howto_page5_title)
                    h.description.setText(R.string.howto_page5_desc)
                    (holder.itemView.context as HowToActivity).updatePermissionUI()
                }
            }
        }

        override fun getItemCount() = pages.size
    }
}
