package com.rc.apks.sideloader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rc.apks.R
import com.rc.apks.databinding.ItemSideloadBinding

class SideloaderAdapter(
    private val items: List<SideloadFile>,
    private val onInstallClicked: (SideloadFile) -> Unit,
    private val onUninstallClicked: (SideloadFile) -> Unit
) : RecyclerView.Adapter<SideloaderAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemSideloadBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSideloadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.binding.appName.text = item.name
        holder.binding.fileInfo.text = item.file.name
        holder.binding.appSize.text = item.sizeText

        if (item.icon != null) {
            holder.binding.appIcon.setImageDrawable(item.icon)
        } else {
            holder.binding.appIcon.setImageResource(R.drawable.ic_sideload)
        }

        // Content descriptions for accessibility
        holder.binding.appIcon.contentDescription = context.getString(R.string.app_icon_desc)

        if (item.isInstalled) {
            holder.binding.btnInstall.text = "Installed"
            holder.binding.btnInstall.isEnabled = false
            holder.binding.btnInstall.contentDescription = context.getString(R.string.installed_desc)
            holder.binding.btnUninstall.visibility = View.VISIBLE
            holder.binding.btnUninstall.contentDescription = context.getString(R.string.uninstall_app_desc)
        } else {
            holder.binding.btnInstall.text = "Install"
            holder.binding.btnInstall.isEnabled = true
            holder.binding.btnInstall.contentDescription = context.getString(R.string.install_app_desc, item.name)
            holder.binding.btnUninstall.visibility = View.GONE
        }

        // Combined content description for the entire item
        val statusText = if (item.isInstalled) "installed" else "not installed"
        holder.itemView.contentDescription = "${item.name}, ${item.sizeText}, $statusText"

        holder.binding.btnInstall.setOnClickListener {
            onInstallClicked(item)
        }

        holder.binding.btnUninstall.setOnClickListener {
            onUninstallClicked(item)
        }
    }

    override fun getItemCount() = items.size
}
