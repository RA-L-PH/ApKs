package com.rc.apks.home

import com.rc.apks.management.AppsViewModel
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
        private const val ID_START_SIMPLE = 1L
    }

    override fun onCreateCreatorPool(): IndexCreatorPool {
        return IndexCreatorPool()
    }

    fun updateData() {
        val status = homeModel.serviceStatus.value?.data ?: return
        val running = status.isRunning

        clear()

        // Server status always first
        addItem(ServerStatusViewHolder.CREATOR, status, ID_STATUS)

        // Simple start/stop button
        addItem(SimpleServerStartViewHolder.CREATOR, running, ID_START_SIMPLE)

        notifyDataSetChanged()
    }
}
