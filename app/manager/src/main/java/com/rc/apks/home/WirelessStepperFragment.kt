package com.rc.apks.home

import android.animation.ObjectAnimator
import android.content.Intent
import android.content.SharedPreferences
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rc.apks.R
import com.rc.apks.ShizukuSettings
import com.rc.apks.adb.AdbPairingService

class WirelessStepperFragment : Fragment() {

    private var onStep1Click: (() -> Unit)? = null
    private var onBatteryClick: (() -> Unit)? = null
    private var onStartServer: (() -> Unit)? = null
    private var onStopServer: (() -> Unit)? = null

    private var isServiceRunning = false
    private var isServiceStarting = false
    private var isStep1Done = false
    private var isStep2Done = false

    private var step1Expanded = false
    private var step2Expanded = false

    private val handler = Handler(Looper.getMainLooper())
    private var wirelessDebuggingObserver: ContentObserver? = null
    private var prefsListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    companion object {
        private const val ARG_SERVICE_RUNNING = "service_running"
        private const val ARG_STEP1_DONE = "step1_done"
        private const val ARG_STEP2_DONE = "step2_done"

        fun newInstance(
            onStep1Click: () -> Unit,
            onBatteryClick: () -> Unit,
            onStartServer: () -> Unit,
            onStopServer: () -> Unit,
            serviceRunning: Boolean = false,
            step1Done: Boolean = false,
            step2Done: Boolean = false
        ): WirelessStepperFragment {
            return WirelessStepperFragment().apply {
                this.onStep1Click = onStep1Click
                this.onBatteryClick = onBatteryClick
                this.onStartServer = onStartServer
                this.onStopServer = onStopServer
                this.isServiceRunning = serviceRunning
                this.isStep1Done = step1Done
                this.isStep2Done = step2Done
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home_stepper_wireless, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            isServiceRunning = savedInstanceState.getBoolean(ARG_SERVICE_RUNNING, false)
            isStep1Done = savedInstanceState.getBoolean(ARG_STEP1_DONE, false)
            isStep2Done = savedInstanceState.getBoolean(ARG_STEP2_DONE, false)
            step1Expanded = false
            step2Expanded = false
        }

        setupStep1Accordion(view)
        setupStep2Accordion(view)
        setupTroubleshootingAccordion(view)
        setupPlayButton(view)
        updateUI()
    }

    override fun onStart() {
        super.onStart()
        startWirelessDebuggingObserver()
        startPairingListener()
    }

    override fun onResume() {
        super.onResume()
        checkWirelessDebuggingStatus()
        checkPairingStatus()
    }

    override fun onStop() {
        super.onStop()
        stopWirelessDebuggingObserver()
        stopPairingListener()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(ARG_SERVICE_RUNNING, isServiceRunning)
        outState.putBoolean(ARG_STEP1_DONE, isStep1Done)
        outState.putBoolean(ARG_STEP2_DONE, isStep2Done)
    }

    fun setServiceRunning(running: Boolean) {
        isServiceRunning = running
        isServiceStarting = false
        view?.let { updateUI() }
    }

    fun setServiceStarting(starting: Boolean) {
        isServiceStarting = starting
        view?.let { updateUI() }
    }

    fun setStep1Done(done: Boolean) {
        isStep1Done = done
        view?.let { updateUI() }
    }

    fun setStep2Done(done: Boolean) {
        isStep2Done = done
        view?.let { updateUI() }
    }

    private fun isWirelessDebuggingEnabled(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return false
        return try {
            Settings.Global.getInt(requireContext().contentResolver, "adb_wifi_enabled", 0) == 1
        } catch (_: Exception) {
            false
        }
    }

    private fun checkWirelessDebuggingStatus() {
        if (isWirelessDebuggingEnabled() && !isStep1Done) {
            isStep1Done = true
            step1Expanded = false
            updateUI()
        }
    }

    private fun startWirelessDebuggingObserver() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        val uri = Settings.Global.getUriFor("adb_wifi_enabled")
        wirelessDebuggingObserver = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean) {
                checkWirelessDebuggingStatus()
            }
        }
        requireContext().contentResolver.registerContentObserver(uri, false, wirelessDebuggingObserver!!)
    }

    private fun stopWirelessDebuggingObserver() {
        wirelessDebuggingObserver?.let {
            try { requireContext().contentResolver.unregisterContentObserver(it) } catch (_: Exception) {}
        }
        wirelessDebuggingObserver = null
    }

    private fun checkPairingStatus() {
        val paired = ShizukuSettings.getPreferences().getBoolean("pairing_success", false)
        if (paired && !isStep2Done) {
            isStep2Done = true
            step2Expanded = false
            updateUI()
        }
    }

    private fun startPairingListener() {
        prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "pairing_success") {
                checkPairingStatus()
            }
        }
        ShizukuSettings.getPreferences().registerOnSharedPreferenceChangeListener(prefsListener)
    }

    private fun stopPairingListener() {
        prefsListener?.let {
            ShizukuSettings.getPreferences().unregisterOnSharedPreferenceChangeListener(it)
        }
        prefsListener = null
    }

    private fun setupStep1Accordion(view: View) {
        val header = view.findViewById<LinearLayout>(R.id.step1Header)
        val content = view.findViewById<LinearLayout>(R.id.step1Content)
        val arrow = view.findViewById<ImageView>(R.id.step1Arrow)

        header?.setOnClickListener {
            if (isStep1Done) return@setOnClickListener
            step1Expanded = !step1Expanded
            animateAccordion(content, arrow, step1Expanded)
        }

        view.findViewById<MaterialButton>(R.id.btnStep1)?.setOnClickListener {
            onStep1Click?.invoke()
            step1Expanded = false
            animateAccordion(content, arrow, false)
        }
    }

    private fun setupStep2Accordion(view: View) {
        val header = view.findViewById<LinearLayout>(R.id.step2Header)
        val content = view.findViewById<LinearLayout>(R.id.step2Content)
        val arrow = view.findViewById<ImageView>(R.id.step2Arrow)

        header?.setOnClickListener {
            if (isStep2Done) return@setOnClickListener
            step2Expanded = !step2Expanded
            animateAccordion(content, arrow, step2Expanded)
        }

        view.findViewById<MaterialButton>(R.id.btnStep2)?.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            try { startActivity(intent) } catch (_: Exception) {}

            try {
                ContextCompat.startForegroundService(
                    requireContext(),
                    AdbPairingService.startIntent(requireContext())
                )
            } catch (_: Exception) {}
        }
    }

    private fun setupTroubleshootingAccordion(view: View) {
        val header = view.findViewById<LinearLayout>(R.id.accordionHeader)
        val content = view.findViewById<LinearLayout>(R.id.accordionContent)
        val arrow = view.findViewById<ImageView>(R.id.accordionArrow)
        var expanded = false

        header?.setOnClickListener {
            expanded = !expanded
            animateAccordion(content, arrow, expanded)
        }

        view.findViewById<MaterialButton>(R.id.btnBattery)?.setOnClickListener {
            onBatteryClick?.invoke()
        }
    }

    private fun setupPlayButton(view: View) {
        val btnToggle = view.findViewById<MaterialButton>(R.id.btnToggleServer)
        btnToggle?.setOnClickListener {
            if (isServiceRunning) {
                MaterialAlertDialogBuilder(requireContext())
                    .setMessage(R.string.dialog_stop_message)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        onStopServer?.invoke()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            } else {
                onStartServer?.invoke()
            }
        }
    }

    private fun animateAccordion(content: LinearLayout?, arrow: ImageView?, expand: Boolean) {
        if (content == null || arrow == null) return
        if (expand) {
            content.visibility = View.VISIBLE
            ObjectAnimator.ofFloat(arrow, "rotation", 180f).start()
        } else {
            content.visibility = View.GONE
            ObjectAnimator.ofFloat(arrow, "rotation", 0f).start()
        }
    }

    private fun updateUI() {
        val view = view ?: return

        val step1Number = view.findViewById<TextView>(R.id.step1Number)
        val step1Title = view.findViewById<TextView>(R.id.step1Title)
        val step1Content = view.findViewById<LinearLayout>(R.id.step1Content)
        val step1Arrow = view.findViewById<ImageView>(R.id.step1Arrow)

        val step2Number = view.findViewById<TextView>(R.id.step2Number)
        val step2Title = view.findViewById<TextView>(R.id.step2Title)
        val step2Content = view.findViewById<LinearLayout>(R.id.step2Content)
        val step2Arrow = view.findViewById<ImageView>(R.id.step2Arrow)

        val playContainer = view.findViewById<LinearLayout>(R.id.serverButtonContainer)
        val playLabel = view.findViewById<TextView>(R.id.serverLabel)
        val btnPlay = view.findViewById<MaterialButton>(R.id.btnToggleServer)
        val ring = view.findViewById<View>(R.id.serverButtonRing)
        val progressRing = view.findViewById<View>(R.id.serverProgressRing)

        if (isStep1Done) {
            step1Number?.text = "\u2713"
            step1Number?.setBackgroundResource(R.drawable.step_circle_done_bg)
            step1Title?.setTextColor(ContextCompat.getColor(requireContext(), R.color.ok))
            step1Content?.visibility = View.GONE
            step1Arrow?.visibility = View.GONE
            step1Expanded = false
        } else {
            step1Number?.text = "1"
            step1Number?.setBackgroundResource(R.drawable.step_circle_bg)
            step1Title?.setTextColor(0xFFFFFFFF.toInt())
            step1Arrow?.visibility = View.VISIBLE
        }

        if (isStep2Done) {
            step2Number?.text = "\u2713"
            step2Number?.setBackgroundResource(R.drawable.step_circle_done_bg)
            step2Title?.setTextColor(ContextCompat.getColor(requireContext(), R.color.ok))
            step2Title?.text = "Paired"
            step2Content?.visibility = View.GONE
            step2Arrow?.visibility = View.GONE
            step2Expanded = false
        } else {
            step2Number?.text = "2"
            step2Number?.setBackgroundResource(R.drawable.step_circle_bg)
            step2Title?.setTextColor(0xFFFFFFFF.toInt())
            step2Title?.text = "Pair with Pairing Code"
            step2Arrow?.visibility = View.VISIBLE
        }

        if (isStep1Done && isStep2Done) {
            playContainer?.visibility = View.VISIBLE
            if (isServiceStarting) {
                ring?.setBackgroundResource(R.drawable.bg_server_button_stopped)
                progressRing?.visibility = View.VISIBLE
                btnPlay?.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_server_start_24dp)
                btnPlay?.backgroundTintList = android.content.res.ColorStateList.valueOf(0x40166534.toInt())
                btnPlay?.iconTint = android.content.res.ColorStateList.valueOf(0xFF4ADE80.toInt())
                btnPlay?.isEnabled = false
                playLabel?.text = "Starting..."
                playLabel?.setTextColor(0xFF4ADE80.toInt())
            } else if (isServiceRunning) {
                ring?.setBackgroundResource(R.drawable.bg_server_button_running)
                progressRing?.visibility = View.GONE
                btnPlay?.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_server_stop_24dp)
                btnPlay?.backgroundTintList = android.content.res.ColorStateList.valueOf(0x40DC2626.toInt())
                btnPlay?.iconTint = android.content.res.ColorStateList.valueOf(0xFFDC2626.toInt())
                btnPlay?.isEnabled = true
                playLabel?.text = "Stop Server"
                playLabel?.setTextColor(0xFFDC2626.toInt())
            } else {
                ring?.setBackgroundResource(R.drawable.bg_server_button_stopped)
                progressRing?.visibility = View.GONE
                btnPlay?.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_server_start_24dp)
                btnPlay?.backgroundTintList = android.content.res.ColorStateList.valueOf(0x40166534.toInt())
                btnPlay?.iconTint = android.content.res.ColorStateList.valueOf(0xFF4ADE80.toInt())
                btnPlay?.isEnabled = true
                playLabel?.text = "Start Server"
                playLabel?.setTextColor(0xFF4ADE80.toInt())
            }
        } else {
            playContainer?.visibility = View.GONE
        }
    }
}
