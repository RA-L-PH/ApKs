package com.rc.apks.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rc.apks.Helps
import com.rc.apks.databinding.HomeExtraStepRequiredBinding
import com.rc.apks.databinding.HomeItemContainerBinding
import com.rc.apks.utils.CustomTabsHelper
import rikka.recyclerview.BaseViewHolder
import rikka.recyclerview.BaseViewHolder.Creator

class AdbPermissionLimitedViewHolder(binding: HomeExtraStepRequiredBinding, root: View) : BaseViewHolder<Any?>(root) {

    companion object {
        val CREATOR = Creator<Any> { inflater: LayoutInflater, parent: ViewGroup? ->
            val outer = HomeItemContainerBinding.inflate(inflater, parent, false)
            val inner = HomeExtraStepRequiredBinding.inflate(inflater, outer.root, true)
            AdbPermissionLimitedViewHolder(inner, outer.root)
        }
    }

    init {
        binding.button1.setOnClickListener { v: View -> CustomTabsHelper.launchUrlOrCopy(v.context, Helps.ADB_PERMISSION.get()) }
    }
}
