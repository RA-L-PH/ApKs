package com.rc.apks.adb

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import java.net.NetworkInterface

@RequiresApi(Build.VERSION_CODES.R)
class AdbMdns(
    context: Context,
    private val serviceType: String,
    private val observer: Observer<Int>
) {

    private var registered = false
    private var running = false
    private var serviceName: String? = null
    private var isResolving = false
    private val pendingServices = ArrayDeque<NsdServiceInfo>()
    private val listener = DiscoveryListener()
    private val nsdManager: NsdManager = context.getSystemService(NsdManager::class.java)

    fun start() {
        if (running) return
        running = true
        try {
            if (!registered) {
                nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, listener)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "discoverServices failed", e)
        }
    }

    fun stop() {
        if (!running) return
        running = false
        synchronized(pendingServices) {
            pendingServices.clear()
            isResolving = false
        }
        try {
            if (registered) {
                nsdManager.stopServiceDiscovery(listener)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "stopServiceDiscovery failed", e)
        }
    }

    private fun resolveNext() {
        if (!running) return
        val nextService: NsdServiceInfo?
        synchronized(pendingServices) {
            if (isResolving || pendingServices.isEmpty()) return
            isResolving = true
            nextService = pendingServices.removeFirstOrNull()
        }
        if (nextService != null) {
            try {
                nsdManager.resolveService(nextService, ResolveListener())
            } catch (e: Throwable) {
                Log.e(TAG, "resolveService failed", e)
                synchronized(pendingServices) {
                    isResolving = false
                }
                resolveNext()
            }
        }
    }

    private fun onServiceFound(info: NsdServiceInfo) {
        synchronized(pendingServices) {
            pendingServices.addLast(info)
        }
        resolveNext()
    }

    private fun onServiceLost(info: NsdServiceInfo) {
        if (info.serviceName == serviceName) {
            observer.onChanged(-1)
        }
    }

    private fun onServiceResolved(resolvedService: NsdServiceInfo) {
        synchronized(pendingServices) {
            isResolving = false
        }
        if (running) {
            val host = resolvedService.host
            val isLocal = if (host == null) {
                false
            } else {
                try {
                    NetworkInterface.getNetworkInterfaces().asSequence().any { networkInterface ->
                        networkInterface.inetAddresses.asSequence().any { addr ->
                            addr.hostAddress == host.hostAddress ||
                            (addr.address != null && host.address != null && addr.address.contentEquals(host.address))
                        }
                    }
                } catch (e: Throwable) {
                    true
                }
            }
            if (isLocal && resolvedService.port in 1..65535) {
                serviceName = resolvedService.serviceName
                observer.onChanged(resolvedService.port)
            }
        }
        resolveNext()
    }

    private fun onResolveFailed(nsdServiceInfo: NsdServiceInfo, errorCode: Int) {
        Log.w(TAG, "onResolveFailed: ${nsdServiceInfo.serviceName}, code: $errorCode")
        synchronized(pendingServices) {
            isResolving = false
        }
        resolveNext()
    }

    internal inner class DiscoveryListener : NsdManager.DiscoveryListener {
        override fun onDiscoveryStarted(serviceType: String) {
            Log.v(TAG, "onDiscoveryStarted: $serviceType")
            registered = true
            if (!running) {
                try {
                    nsdManager.stopServiceDiscovery(this)
                } catch (_: Throwable) {}
            }
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.v(TAG, "onStartDiscoveryFailed: $serviceType, $errorCode")
            registered = false
            running = false
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.v(TAG, "onDiscoveryStopped: $serviceType")
            registered = false
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.v(TAG, "onStopDiscoveryFailed: $serviceType, $errorCode")
            registered = false
        }

        override fun onServiceFound(serviceInfo: NsdServiceInfo) {
            Log.v(TAG, "onServiceFound: ${serviceInfo.serviceName}")
            this@AdbMdns.onServiceFound(serviceInfo)
        }

        override fun onServiceLost(serviceInfo: NsdServiceInfo) {
            Log.v(TAG, "onServiceLost: ${serviceInfo.serviceName}")
            this@AdbMdns.onServiceLost(serviceInfo)
        }
    }

    internal inner class ResolveListener : NsdManager.ResolveListener {
        override fun onResolveFailed(nsdServiceInfo: NsdServiceInfo, errorCode: Int) {
            this@AdbMdns.onResolveFailed(nsdServiceInfo, errorCode)
        }

        override fun onServiceResolved(nsdServiceInfo: NsdServiceInfo) {
            this@AdbMdns.onServiceResolved(nsdServiceInfo)
        }
    }

    companion object {
        const val TLS_CONNECT = "_adb-tls-connect._tcp"
        const val TLS_PAIRING = "_adb-tls-pairing._tcp"
        const val TAG = "AdbMdns"
    }
}
