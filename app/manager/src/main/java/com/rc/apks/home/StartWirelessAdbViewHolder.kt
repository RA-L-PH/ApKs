package com.rc.apks.home

import android.content.Intent
import android.os.Build
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.rc.apks.Helps
import com.rc.apks.R

import com.rc.apks.databinding.HomeItemContainerBinding
import com.rc.apks.databinding.HomeStartWirelessAdbBinding
import com.rc.apks.ktx.toHtml
import com.rc.apks.starter.StarterActivity
import com.rc.apks.utils.CustomTabsHelper
import com.rc.apks.utils.EnvironmentUtils
import rikka.core.content.asActivity
import rikka.html.text.HtmlCompat
import rikka.recyclerview.BaseViewHolder
import rikka.recyclerview.BaseViewHolder.Creator

class StartWirelessAdbViewHolder(binding: HomeStartWirelessAdbBinding, root: View) :
    BaseViewHolder<Any?>(root) {

    companion object {
        val CREATOR = Creator<Any> { inflater: LayoutInflater, parent: ViewGroup? ->
            val outer = HomeItemContainerBinding.inflate(inflater, parent, false)
            val inner = HomeStartWirelessAdbBinding.inflate(inflater, outer.root, true)
            StartWirelessAdbViewHolder(inner, outer.root)
        }
    }

    init {
        binding.button1.setOnClickListener { v: View ->
            onAdbClicked(v.context)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            binding.button3.setOnClickListener { v: View ->
                CustomTabsHelper.launchUrlOrCopy(v.context, Helps.ADB_ANDROID11.get())
            }
            binding.button2.setOnClickListener { v: View ->
                onPairClicked(v.context)
            }
            binding.text1.movementMethod = LinkMovementMethod.getInstance()
            binding.text1.text = context.getString(R.string.home_wireless_adb_description)
                .toHtml(HtmlCompat.FROM_HTML_OPTION_TRIM_WHITESPACE)
        } else {
            binding.text1.text = context.getString(R.string.home_wireless_adb_description_pre_11)
                .toHtml(HtmlCompat.FROM_HTML_OPTION_TRIM_WHITESPACE)
            binding.button2.isVisible = false
            binding.button3.isVisible = false
        }
    }

    override fun onBind(payloads: MutableList<Any>) {
        super.onBind(payloads)
    }

    private fun onAdbClicked(context: android.content.Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            AdbDialogFragment().show(context.asActivity<FragmentActivity>().supportFragmentManager)
            return
        }

        val port = EnvironmentUtils.getAdbTcpPort()
        if (port > 0) {
            val host = "127.0.0.1"
            val intent = Intent(context, StarterActivity::class.java).apply {
                putExtra(StarterActivity.EXTRA_IS_ROOT, false)
                putExtra(StarterActivity.EXTRA_HOST, host)
                putExtra(StarterActivity.EXTRA_PORT, port)
            }
            context.startActivity(intent)
        } else {
            WadbNotEnabledDialogFragment().show(context.asActivity<FragmentActivity>().supportFragmentManager)
        }
    }

    private fun onPairClicked(context: android.content.Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            AdbPairDialogFragment().show(context.asActivity<FragmentActivity>().supportFragmentManager)
        }
    }
}
