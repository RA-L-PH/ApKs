package com.rc.apks.home

import android.animation.ObjectAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.rc.apks.R

class PcStepperFragment : Fragment() {

    private var onDownloadToolsClick: (() -> Unit)? = null
    private var onBatteryClick: (() -> Unit)? = null

    companion object {
        fun newInstance(
            onDownloadToolsClick: () -> Unit,
            onBatteryClick: () -> Unit
        ): PcStepperFragment {
            return PcStepperFragment().apply {
                this.onDownloadToolsClick = onDownloadToolsClick
                this.onBatteryClick = onBatteryClick
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home_stepper_pc, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialButton>(R.id.btnStep1)?.setOnClickListener {
            onDownloadToolsClick?.invoke()
        }

        view.findViewById<View>(R.id.btnCopy)?.setOnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val command = "adb shell sh /sdcard/Android/data/com.rc.apks/files/starter.sh"
            clipboard.setPrimaryClip(ClipData.newPlainText("adb command", command))
            Toast.makeText(requireContext(), "Command copied to clipboard", Toast.LENGTH_SHORT).show()
        }

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
