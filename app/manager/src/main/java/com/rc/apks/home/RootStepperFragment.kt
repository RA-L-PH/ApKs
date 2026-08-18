package com.rc.apks.home

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.rc.apks.R
import com.rc.apks.utils.EnvironmentUtils
import rikka.shizuku.Shizuku

class RootStepperFragment : Fragment() {

    private var onStartRootClick: (() -> Unit)? = null
    private var onRestartRootClick: (() -> Unit)? = null
    private var onBatteryClick: (() -> Unit)? = null

    companion object {
        fun newInstance(
            onStartRootClick: () -> Unit,
            onRestartRootClick: () -> Unit,
            onBatteryClick: () -> Unit
        ): RootStepperFragment {
            return RootStepperFragment().apply {
                this.onStartRootClick = onStartRootClick
                this.onRestartRootClick = onRestartRootClick
                this.onBatteryClick = onBatteryClick
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home_stepper_root, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isRooted = EnvironmentUtils.isRooted()
        val isRunning = Shizuku.pingBinder()
        val noRootCard = view.findViewById<View>(R.id.noRootCard)
        val rootStatusText = view.findViewById<TextView>(R.id.rootStatusText)
        val btnStartRoot = view.findViewById<MaterialButton>(R.id.btnStartRoot)
        val btnRestartRoot = view.findViewById<MaterialButton>(R.id.btnRestartRoot)

        if (isRooted) {
            noRootCard?.visibility = View.GONE
            rootStatusText?.text = "Root access detected. Start the service with full permissions."
            btnStartRoot?.isEnabled = true
            btnRestartRoot?.visibility = if (isRunning) View.VISIBLE else View.GONE
        } else {
            noRootCard?.visibility = View.VISIBLE
            rootStatusText?.text = "Root access not detected on this device."
            btnStartRoot?.isEnabled = false
            btnRestartRoot?.visibility = View.GONE
        }

        btnStartRoot?.setOnClickListener {
            onStartRootClick?.invoke()
        }

        btnRestartRoot?.setOnClickListener {
            onRestartRootClick?.invoke()
        }

        // Accordion toggle
        val header = view.findViewById<LinearLayout>(R.id.accordionHeader)
        val content = view.findViewById<LinearLayout>(R.id.accordionContent)
        val arrow = view.findViewById<ImageView>(R.id.accordionArrow)

        header?.setOnClickListener {
            if (content?.visibility == View.VISIBLE) {
                ObjectAnimator.ofFloat(arrow, "rotation", 0f).start()
                content?.visibility = View.GONE
            } else {
                ObjectAnimator.ofFloat(arrow, "rotation", 180f).start()
                content?.visibility = View.VISIBLE
            }
        }

        view.findViewById<MaterialButton>(R.id.btnBattery)?.setOnClickListener {
            onBatteryClick?.invoke()
        }
    }
}
