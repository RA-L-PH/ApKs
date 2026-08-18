package com.rc.apks.starter

import android.util.Log
import com.topjohnwu.superuser.CallbackList
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.rc.apks.ShizukuSettings
import com.rc.apks.adb.AdbClient
import com.rc.apks.adb.AdbKey
import com.rc.apks.adb.AdbMdns
import com.rc.apks.adb.PreferenceAdbKeyStore
import com.rc.apks.utils.EnvironmentUtils
import rikka.shizuku.Shizuku

object BackgroundServerStarter {

    fun start(context: android.content.Context, isRoot: Boolean, host: String?, port: Int, onComplete: () -> Unit, onError: ((String) -> Unit)? = null) {
        if (isRoot) {
            startRoot(onComplete, onError)
        } else {
            val effectiveHost = if (host.isNullOrEmpty()) "127.0.0.1" else host
            var effectivePort = port
            if (effectivePort <= 0) {
                effectivePort = EnvironmentUtils.getAdbTcpPort()
            }

            if (effectivePort > 0) {
                startAdb(effectiveHost, effectivePort, onComplete, onError)
            } else {
                var mdns: AdbMdns? = null
                mdns = AdbMdns(context, AdbMdns.TLS_CONNECT) { discoveredPort ->
                    if (discoveredPort > 0 && discoveredPort <= 65535) {
                        mdns?.stop()
                        startAdb(effectiveHost, discoveredPort, onComplete, onError)
                    }
                }
                mdns.start()
            }
        }
    }

    private fun startRoot(onComplete: () -> Unit, onError: ((String) -> Unit)?) {
        GlobalScope.launch(Dispatchers.IO) {
            if (!Shell.getShell().isRoot) {
                Shell.getCachedShell()?.close()
                if (!Shell.getShell().isRoot) {
                    onError?.invoke("Root access not granted")
                    return@launch
                }
            }

            Shell.cmd(Starter.internalCommand).to(object : CallbackList<String?>() {
                override fun onAddElement(s: String?) {}
            }).submit {
                if (it.code == 0) {
                    GlobalScope.launch(Dispatchers.Main) { onComplete() }
                } else {
                    onError?.invoke("Failed to execute root command")
                }
            }
        }
    }

    private fun startAdb(host: String, port: Int, onComplete: () -> Unit, onError: ((String) -> Unit)?) {
        GlobalScope.launch(Dispatchers.IO) {
            val key = try {
                AdbKey(PreferenceAdbKeyStore(ShizukuSettings.getPreferences()), "shizuku")
            } catch (e: Throwable) {
                e.printStackTrace()
                onError?.invoke("ADB key error: ${e.message}")
                return@launch
            }

            AdbClient(host, port, key).runCatching {
                connect()
                shellCommand(Starter.internalCommand) {}
                close()
                GlobalScope.launch(Dispatchers.Main) { onComplete() }
            }.onFailure {
                it.printStackTrace()
                onError?.invoke("Connection failed: ${it.message}")
            }
        }
    }
}
