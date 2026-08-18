package com.rc.apks.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class HomePagerAdapter(
    activity: FragmentActivity,
    private val onStep1Click: () -> Unit,
    private val onStartRootClick: () -> Unit,
    private val onRestartRootClick: () -> Unit,
    private val onDownloadToolsClick: () -> Unit,
    private val onBatteryClick: () -> Unit,
    private val onStartServer: () -> Unit,
    private val onStopServer: () -> Unit
) : FragmentStateAdapter(activity) {

    private var serviceRunning = false
    private var step1Done = false
    private var step2Done = false
    private var wirelessFragment: WirelessStepperFragment? = null

    fun setServiceRunning(running: Boolean) {
        serviceRunning = running
        wirelessFragment?.setServiceRunning(running)
    }

    fun setServiceStarting(starting: Boolean) {
        wirelessFragment?.setServiceStarting(starting)
    }

    fun setStep1Done(done: Boolean) {
        step1Done = done
        wirelessFragment?.setStep1Done(done)
    }

    fun setStep2Done(done: Boolean) {
        step2Done = done
        wirelessFragment?.setStep2Done(done)
    }

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> WirelessStepperFragment.newInstance(
                onStep1Click, onBatteryClick, onStartServer, onStopServer,
                serviceRunning, step1Done, step2Done
            ).also { wirelessFragment = it }
            1 -> RootStepperFragment.newInstance(onStartRootClick, onRestartRootClick, onBatteryClick)
            2 -> PcStepperFragment.newInstance(onDownloadToolsClick, onBatteryClick)
            else -> throw IllegalStateException("Invalid position $position")
        }
    }
}
