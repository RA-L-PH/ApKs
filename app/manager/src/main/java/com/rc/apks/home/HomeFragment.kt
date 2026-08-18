package com.rc.apks.home

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import androidx.viewpager2.widget.ViewPager2
import com.rc.apks.R
import com.rc.apks.ShizukuSettings
import com.rc.apks.databinding.FragmentHomeRedesignBinding
import com.rc.apks.management.appsViewModel
import com.rc.apks.utils.EnvironmentUtils
import rikka.core.ktx.unsafeLazy
import rikka.lifecycle.Status
import rikka.lifecycle.viewModels
import rikka.shizuku.Shizuku

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeRedesignBinding? = null
    private val binding get() = _binding!!

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        checkServerStatus()
        appsModel.load()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        checkServerStatus()
    }

    private val homeModel by viewModels { HomeViewModel() }
    private val appsModel by appsViewModel()

    private val tabTitles = arrayOf("Wireless", "Root", "PC")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeRedesignBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupStatusBanner()
        setupTabs()

        homeModel.serviceStatus.observe(viewLifecycleOwner) {
            if (it.status == Status.SUCCESS) {
                val status = homeModel.serviceStatus.value?.data ?: return@observe
                updateStatusBanner(status)
                ShizukuSettings.setLastLaunchMode(if (status.uid == 0) ShizukuSettings.LaunchMethod.ROOT else ShizukuSettings.LaunchMethod.ADB)
            }
        }

        appsModel.grantedCount.observe(viewLifecycleOwner) {
            if (it.status == Status.SUCCESS) {
                // Data updated
            }
        }

        Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
    }

    private fun setupStatusBanner() {
        // Status banner will be updated when service status changes
    }

    private fun updateStatusBanner(status: com.rc.apks.model.ServiceStatus) {
        val pill = binding.statusPill
        val dot = binding.statusDot
        val text = binding.statusText

                val adapter = binding.viewPager.adapter as? HomePagerAdapter
                adapter?.setServiceStarting(false)
                adapter?.setServiceRunning(status.isRunning)

        if (status.isRunning) {
            dot.setBackgroundResource(R.drawable.shape_status_dot_running)
            text.text = "Running"
            text.setTextColor(android.graphics.Color.parseColor("#4ADE80"))
            pill.setCardBackgroundColor(android.graphics.Color.parseColor("#0F2317"))
            pill.strokeColor = android.graphics.Color.parseColor("#1B432C")
            pill.setOnClickListener(null)
        } else {
            dot.setBackgroundResource(R.drawable.shape_status_dot_stopped)
            text.text = "Stopped"
            text.setTextColor(android.graphics.Color.parseColor("#A1A1AA"))
            pill.setCardBackgroundColor(android.graphics.Color.parseColor("#161618"))
            pill.strokeColor = android.graphics.Color.parseColor("#27272A")
            pill.setOnClickListener(null)
        }
    }

    private fun setupTabs() {
        val toggleGroup = binding.modeToggleGroup
        val viewPager = binding.viewPager

        val adapter = HomePagerAdapter(
            requireActivity(),
            onStep1Click = {
                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                try { startActivity(intent) } catch (_: Exception) {}
            },
            onStartRootClick = {
                val intent = android.content.Intent(requireContext(), com.rc.apks.starter.StarterActivity::class.java).apply {
                    putExtra(com.rc.apks.starter.StarterActivity.EXTRA_IS_ROOT, true)
                }
                startActivity(intent)
            },
            onRestartRootClick = {
                val intent = android.content.Intent(requireContext(), com.rc.apks.starter.StarterActivity::class.java).apply {
                    putExtra(com.rc.apks.starter.StarterActivity.EXTRA_IS_ROOT, true)
                }
                startActivity(intent)
            },
            onDownloadToolsClick = {
                com.rc.apks.utils.CustomTabsHelper.launchUrlOrCopy(requireContext(), "https://developer.android.com/tools/releases/platform-tools")
            },
            onBatteryClick = {
                try {
                    val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.data = android.net.Uri.parse("package:${requireContext().packageName}")
                    startActivity(intent)
                } catch (_: Exception) {}
            },
            onStartServer = {
                val adapter = binding.viewPager.adapter as? HomePagerAdapter
                adapter?.setServiceStarting(true)
                val isRoot = EnvironmentUtils.isRooted()
                com.rc.apks.starter.BackgroundServerStarter.start(
                    context = requireContext(),
                    isRoot = isRoot,
                    host = if (isRoot) null else "127.0.0.1",
                    port = if (isRoot) 0 else EnvironmentUtils.getAdbTcpPort(),
                    onComplete = {},
                    onError = { }
                )
            },
            onStopServer = {
                try { Shizuku.exit() } catch (_: Throwable) {}
            }
        )

        viewPager.adapter = adapter

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val targetPage = when (checkedId) {
                    R.id.tabWireless -> 0
                    R.id.tabRoot -> 1
                    R.id.tabPc -> 2
                    else -> 0
                }
                if (viewPager.currentItem != targetPage) {
                    viewPager.currentItem = targetPage
                }
            }
        }

        viewPager.registerOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val targetId = when (position) {
                    0 -> R.id.tabWireless
                    1 -> R.id.tabRoot
                    2 -> R.id.tabPc
                    else -> R.id.tabWireless
                }
                if (toggleGroup.checkedButtonId != targetId) {
                    toggleGroup.check(targetId)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        checkServerStatus()
    }

    private fun checkServerStatus() {
        homeModel.reload()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
        _binding = null
    }
}
