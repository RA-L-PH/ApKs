package com.rc.apks.home

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.rc.apks.R
import com.rc.apks.ShizukuSettings
import com.rc.apks.adb.*
import com.rc.apks.databinding.AdbPairDialogBinding
import rikka.lifecycle.viewModels
import java.net.ConnectException

@RequiresApi(VERSION_CODES.R)
class AdbPairDialogFragment : DialogFragment() {

    private lateinit var binding: AdbPairDialogBinding

    private val viewModel by viewModels { ViewModel(requireContext()) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        binding = AdbPairDialogBinding.inflate(LayoutInflater.from(context))

        val builder = MaterialAlertDialogBuilder(context).apply {
            setTitle("Pair with Wireless Debugging")
            setView(binding.root)
            setNegativeButton(android.R.string.cancel, null)
            setPositiveButton("Pair", null)
        }
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnShowListener { onDialogShow(dialog) }
        return dialog
    }

    private fun onDialogShow(dialog: AlertDialog) {
        binding.pairingCode.editText?.doAfterTextChanged {
            binding.pairingCode.error = null
        }
        binding.port.editText?.doAfterTextChanged {
            binding.port.error = null
        }

        binding.btnOpenDevOptions.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra(":settings:fragment_args_key", "toggle_adb_wireless")
            try {
                it.context.startActivity(intent)
            } catch (_: ActivityNotFoundException) {}
        }

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val context = it.context
            val code = binding.pairingCode.editText?.text?.toString()?.trim() ?: ""
            if (code.length < 6) {
                binding.pairingCode.error = "Enter 6-digit code"
                return@setOnClickListener
            }

            val port = try {
                binding.port.editText?.text?.toString()?.trim()?.toInt() ?: -1
            } catch (_: Exception) {
                -1
            }

            if (port > 65535 || port < 1) {
                binding.port.error = context.getString(R.string.dialog_adb_invalid_port)
                return@setOnClickListener
            }

            binding.statusHint.text = "Pairing with port $port..."
            binding.statusHint.setTextColor(android.graphics.Color.parseColor("#38BDF8"))
            viewModel.run(port, code)
        }

        viewModel.port.observe(this) { detectedPort ->
            if (detectedPort != null && detectedPort > 0) {
                if (binding.port.editText?.text.isNullOrEmpty()) {
                    binding.port.editText?.setText(detectedPort.toString())
                }
                binding.statusHint.text = "Discovered pairing port: $detectedPort"
                binding.statusHint.setTextColor(android.graphics.Color.parseColor("#4ADE80"))
            }
        }

        viewModel.result.observe(this) { state ->
            when (state) {
                is PairState.InProgress -> {
                    binding.pairingCode.isEnabled = false
                    binding.port.isEnabled = false
                    binding.btnOpenDevOptions.isEnabled = false
                    dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = false
                }
                is PairState.Success -> {
                    binding.pairingCode.isEnabled = false
                    binding.port.isEnabled = false
                    binding.btnOpenDevOptions.isEnabled = false
                    dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = false
                    binding.statusHint.text = "Paired successfully!"
                    binding.statusHint.setTextColor(android.graphics.Color.parseColor("#4ADE80"))
                    binding.pairingCode.error = null
                    binding.port.error = null

                    dialog?.window?.decorView?.postDelayed({
                        if (isAdded && !isStateSaved) {
                            dismissAllowingStateLoss()
                            try {
                                AdbDialogFragment().show(parentFragmentManager)
                            } catch (_: Exception) {}
                        }
                    }, 1500)
                }
                is PairState.Error -> {
                    binding.pairingCode.isEnabled = true
                    binding.port.isEnabled = true
                    binding.btnOpenDevOptions.isEnabled = true
                    dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = true
                    val error = state.throwable
                    val ctx = requireContext()
                    when (error) {
                        is ConnectException -> {
                            binding.port.error = ctx.getString(R.string.cannot_connect_port)
                        }
                        is AdbInvalidPairingCodeException -> {
                            binding.pairingCode.error = ctx.getString(R.string.paring_code_is_wrong)
                        }
                        is AdbKeyException -> {
                            Toast.makeText(ctx, ctx.getString(R.string.adb_error_key_store), Toast.LENGTH_LONG)
                                .apply { setGravity(Gravity.CENTER, 0, 0) }.show()
                        }
                        else -> {
                            binding.pairingCode.error = error.message ?: "Pairing failed"
                        }
                    }
                    binding.statusHint.text = "Pairing failed. Check the code and port."
                    binding.statusHint.setTextColor(android.graphics.Color.parseColor("#EF4444"))
                }
                else -> {}
            }
        }
    }

    fun show(fragmentManager: FragmentManager) {
        if (fragmentManager.isStateSaved) return
        show(fragmentManager, javaClass.simpleName)
    }

    override fun onStart() {
        super.onStart()
        try {
            val intent = com.rc.apks.adb.AdbPairingService.startIntent(requireContext())
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                requireContext().startForegroundService(intent)
            } else {
                requireContext().startService(intent)
            }
        } catch (_: Exception) {}
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        try {
            val intent = Intent(requireContext(), com.rc.apks.adb.AdbPairingService::class.java)
            intent.action = "stop"
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                requireContext().startForegroundService(intent)
            } else {
                requireContext().startService(intent)
            }
        } catch (_: Exception) {}
    }

    override fun getDialog(): AlertDialog? {
        return super.getDialog() as AlertDialog?
    }
}

sealed class PairState {
    object Idle : PairState()
    object InProgress : PairState()
    object Success : PairState()
    data class Error(val throwable: Throwable) : PairState()
}

@SuppressLint("NewApi")
private class ViewModel(context: Context) : androidx.lifecycle.ViewModel() {

    private val _result = MutableLiveData<PairState>(PairState.Idle)
    val result: LiveData<PairState> = _result

    private val _port = MutableLiveData<Int>()
    val port: LiveData<Int> = _port

    private val adbMdns: AdbMdns = AdbMdns(context, AdbMdns.TLS_PAIRING) {
        _port.postValue(it)
    }

    init {
        adbMdns.start()
    }

    fun run(port: Int, password: String) {
        _result.postValue(PairState.InProgress)
        GlobalScope.launch(Dispatchers.IO) {
            val host = "127.0.0.1"

            val key = try {
                AdbKey(PreferenceAdbKeyStore(ShizukuSettings.getPreferences()), "shizuku")
            } catch (e: Throwable) {
                e.printStackTrace()
                _result.postValue(PairState.Error(AdbKeyException(e)))
                return@launch
            }

            AdbPairingClient(host, port, password, key).runCatching {
                start()
            }.onFailure {
                it.printStackTrace()
                _result.postValue(PairState.Error(it))
            }.onSuccess { success ->
                if (success) {
                    _result.postValue(PairState.Success)
                } else {
                    _result.postValue(PairState.Error(AdbInvalidPairingCodeException()))
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        adbMdns.stop()
    }
}
