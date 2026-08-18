package com.rc.apks.starter

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topjohnwu.superuser.CallbackList
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.rc.apks.AppConstants.EXTRA
import com.rc.apks.R
import com.rc.apks.ShizukuSettings
import com.rc.apks.adb.AdbClient
import com.rc.apks.adb.AdbKey
import com.rc.apks.adb.AdbKeyException
import com.rc.apks.adb.AdbMdns
import com.rc.apks.adb.PreferenceAdbKeyStore
import com.rc.apks.app.AppBarActivity
import com.rc.apks.databinding.StarterActivityBinding
import com.rc.apks.utils.EnvironmentUtils
import rikka.lifecycle.Resource
import rikka.lifecycle.Status
import rikka.lifecycle.viewModels
import rikka.shizuku.Shizuku
import java.net.ConnectException
import javax.net.ssl.SSLProtocolException

private class NotRootedException : Exception()

class StarterActivity : AppBarActivity() {

    private val viewModel by viewModels {
        ViewModel(
            this,
            intent.getBooleanExtra(EXTRA_IS_ROOT, false),
            intent.getStringExtra(EXTRA_HOST),
            intent.getIntExtra(EXTRA_PORT, 0)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_24)

        val binding = StarterActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.output.observe(this) {
            val output = it.data?.toString()?.trim() ?: ""
            if (output.endsWith("info: shizuku_starter exit with 0")) {
                viewModel.appendOutput("")
                viewModel.appendOutput("Waiting for service...")

                Shizuku.addBinderReceivedListener(object : Shizuku.OnBinderReceivedListener {
                    override fun onBinderReceived() {
                        Shizuku.removeBinderReceivedListener(this)
                        viewModel.appendOutput("Service started successfully! Closing in 2 seconds...")

                        window?.decorView?.postDelayed({
                            if (!isFinishing) finish()
                        }, 2000)
                    }
                })
            } else if (it.status == Status.ERROR) {
                var message = 0
                when (it.error) {
                    is AdbKeyException -> {
                        message = R.string.adb_error_key_store
                    }
                    is NotRootedException -> {
                        message = R.string.start_with_root_failed
                    }
                    is ConnectException -> {
                        message = R.string.cannot_connect_port
                    }
                    is SSLProtocolException -> {
                        message = R.string.adb_pair_required
                    }
                }

                if (message != 0) {
                    MaterialAlertDialogBuilder(this)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                }
            }
            binding.text1.text = output
        }
    }

    companion object {

        const val EXTRA_IS_ROOT = "$EXTRA.IS_ROOT"
        const val EXTRA_HOST = "$EXTRA.HOST"
        const val EXTRA_PORT = "$EXTRA.PORT"
    }
}

private class ViewModel(private val context: Context, root: Boolean, host: String?, port: Int) : androidx.lifecycle.ViewModel() {

    private val sb = StringBuilder()
    private val _output = MutableLiveData<Resource<StringBuilder>>()
    private var adbMdns: AdbMdns? = null

    val output = _output as LiveData<Resource<StringBuilder>>

    init {
        try {
            if (root) {
                startRoot()
            } else {
                val effectiveHost = if (host.isNullOrEmpty()) "127.0.0.1" else host
                var effectivePort = port
                if (effectivePort <= 0) {
                    effectivePort = EnvironmentUtils.getAdbTcpPort()
                }

                if (effectivePort > 0) {
                    startAdb(effectiveHost, effectivePort)
                } else {
                    sb.append("Searching for Wireless Debugging connection...").append('\n')
                    postResult()

                    adbMdns = AdbMdns(context, AdbMdns.TLS_CONNECT) { discoveredPort ->
                        if (discoveredPort > 0 && discoveredPort <= 65535) {
                            adbMdns?.stop()
                            startAdb(effectiveHost, discoveredPort)
                        }
                    }.apply { start() }
                }
            }
        } catch (e: Throwable) {
            postResult(e)
        }
    }

    fun appendOutput(line: String) {
        sb.appendLine(line)
        postResult()
    }

    private fun postResult(throwable: Throwable? = null) {
        if (throwable == null)
            _output.postValue(Resource.success(sb))
        else
            _output.postValue(Resource.error(throwable, sb))
    }

    private fun startRoot() {
        sb.append("Starting with root...").append('\n').append('\n')
        postResult()

        GlobalScope.launch(Dispatchers.IO) {
            if (!Shell.getShell().isRoot) {
                Shell.getCachedShell()?.close()
                sb.append('\n').append("Can't open root shell, try again...").append('\n')

                postResult()
                if (!Shell.getShell().isRoot) {
                    sb.append('\n').append("Root access not granted.").append('\n')
                    postResult(NotRootedException())
                    return@launch
                }
            }

            Shell.cmd(Starter.internalCommand).to(object : CallbackList<String?>() {
                override fun onAddElement(s: String?) {
                    sb.append(s).append('\n')
                    postResult()
                }
            }).submit {
                if (it.code != 0) {
                    sb.append('\n').append("Failed to execute root command.")
                    postResult()
                }
            }
        }
    }

    private fun startAdb(host: String, port: Int) {
        sb.append("Connecting to Wireless ADB on port $port...").append('\n').append('\n')
        postResult()

        GlobalScope.launch(Dispatchers.IO) {
            val key = try {
                AdbKey(PreferenceAdbKeyStore(ShizukuSettings.getPreferences()), "shizuku")
            } catch (e: Throwable) {
                e.printStackTrace()
                sb.append('\n').append(Log.getStackTraceString(e))

                postResult(AdbKeyException(e))
                return@launch
            }

            AdbClient(host, port, key).runCatching {
                connect()
                sb.append("Connected! Starting ApKs server...").append('\n')
                postResult()

                shellCommand(Starter.internalCommand) {
                    sb.append(String(it))
                    postResult()
                }
                close()
            }.onFailure {
                it.printStackTrace()

                sb.append('\n').append(Log.getStackTraceString(it))
                postResult(it)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        adbMdns?.stop()
    }
}
