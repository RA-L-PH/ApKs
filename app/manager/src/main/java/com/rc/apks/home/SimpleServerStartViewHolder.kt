package com.rc.apks.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rc.apks.R
import com.rc.apks.adb.AdbPairingTutorialActivity
import com.rc.apks.databinding.HomeItemContainerBinding
import com.rc.apks.databinding.HomeServerStartSimpleBinding
import com.rc.apks.starter.StarterActivity
import com.rc.apks.utils.EnvironmentUtils
import rikka.recyclerview.BaseViewHolder
import rikka.recyclerview.BaseViewHolder.Creator
import rikka.shizuku.Shizuku

class SimpleServerStartViewHolder(private val binding: HomeServerStartSimpleBinding, root: View) :
    BaseViewHolder<Boolean>(root) {

    companion object {
        val CREATOR = Creator<Boolean> { inflater: LayoutInflater, parent: ViewGroup? ->
            val outer = HomeItemContainerBinding.inflate(inflater, parent, false)
            val inner = HomeServerStartSimpleBinding.inflate(inflater, outer.root, true)
            SimpleServerStartViewHolder(inner, outer.root)
        }
    }

    private val btnStartStop: MaterialButton get() = binding.btnStartStop
    private val progressBar: LinearProgressIndicator get() = binding.progressBar
    private val btnPairing: MaterialButton get() = binding.btnPairing

    init {
        btnStartStop.setOnClickListener { v -> onStartStopClicked() }
        btnPairing.setOnClickListener { v ->
            v.context.startActivity(Intent(v.context, AdbPairingTutorialActivity::class.java))
        }
    }

    private fun onStartStopClicked() {
        val context = itemView.context
        val isRunning = Shizuku.pingBinder()

        if (isRunning) {
            MaterialAlertDialogBuilder(context)
                .setMessage(R.string.dialog_stop_message)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    try {
                        Shizuku.exit()
                    } catch (e: Throwable) {
                        // ignore
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        } else {
            val isRoot = EnvironmentUtils.isRooted()
            val intent = Intent(context, StarterActivity::class.java).apply {
                putExtra(StarterActivity.EXTRA_IS_ROOT, isRoot)
            }
            context.startActivity(intent)
            progressBar.visibility = View.VISIBLE
        }
    }

    override fun onBind() {
        val isRunning = data ?: false
        val context = itemView.context

        if (isRunning) {
            btnStartStop.text = context.getString(R.string.server_stop_button)
            btnStartStop.setIconResource(R.drawable.ic_server_stop_24dp)
            progressBar.visibility = View.GONE
        } else {
            btnStartStop.text = context.getString(R.string.server_start_button)
            btnStartStop.setIconResource(R.drawable.ic_server_start_24dp)
            progressBar.visibility = View.GONE
        }

        btnPairing.visibility = if (isRunning) View.GONE else View.VISIBLE
    }
}
