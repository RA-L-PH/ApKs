package com.rc.apks.home

import android.os.Build
import com.rc.apks.management.AppsViewModel
import com.rc.apks.utils.EnvironmentUtils
import rikka.recyclerview.IdBasedRecyclerViewAdapter
import rikka.recyclerview.IndexCreatorPool
import rikka.shizuku.Shizuku

class HomeAdapter(private val homeModel: HomeViewModel, private val appsModel: AppsViewModel) :
    IdBasedRecyclerViewAdapter(ArrayList()) {

    init {
        updateData()
        setHasStableIds(true)
    }

    companion object {
        private const val ID_STATUS = 0L
        private const val ID_START_WADB = 4L
        private const val ID_START_ROOT = 3L
        private const val ID_APPS = 1L
        private const val ID_TERMINAL = 2L
        private const val ID_ADB_PERMISSION_LIMITED = 7L
        private const val ID_START_ADB = 5L
        private const val ID_LEARN_MORE = 6L
    }

    override fun onCreateCreatorPool(): IndexCreatorPool {
        return IndexCreatorPool()
    }

    fun updateData() {
        val status = homeModel.serviceStatus.value?.data ?: return
        val grantedCount = appsModel.grantedCount.value?.data ?: 0
        val adbPermission = status.permission
        val running = status.isRunning

        clear()

        // 1. Server status — always first
        addItem(ServerStatusViewHolder.CREATOR, status, ID_STATUS)

        // 2. Wireless ADB setup — the main action card
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R || EnvironmentUtils.getAdbTcpPort() > 0) {
            addItem(StartWirelessAdbViewHolder.CREATOR, null, ID_START_WADB)
        }

        // 3. Root option — alternative to wireless
        val root = EnvironmentUtils.isRooted()
        val rootRestart = running && status.uid == 0
        addItem(StartRootViewHolder.CREATOR, rootRestart, ID_START_ROOT)

        // 4. App management & terminal — only when running with full permission
        if (adbPermission) {
            addItem(ManageAppsViewHolder.CREATOR, status to grantedCount, ID_APPS)
            addItem(TerminalViewHolder.CREATOR, status, ID_TERMINAL)
        }

        // 5. Limited permission warning
        if (running && !adbPermission) {
            addItem(AdbPermissionLimitedViewHolder.CREATOR, status, ID_ADB_PERMISSION_LIMITED)
        }

        // 6. ADB command — alternative method
        addItem(StartAdbViewHolder.CREATOR, null, ID_START_ADB)

        // 7. Learn more
        addItem(LearnMoreViewHolder.CREATOR, null, ID_LEARN_MORE)
        notifyDataSetChanged()
    }
}
