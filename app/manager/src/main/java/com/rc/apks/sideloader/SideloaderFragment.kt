package com.rc.apks.sideloader

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rc.apks.R
import com.rc.apks.databinding.FragmentSideloaderBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.zip.ZipFile

class SideloaderFragment : Fragment() {

    private var _binding: FragmentSideloaderBinding? = null
    private val binding get() = _binding!!

    private val filesList = mutableListOf<SideloadFile>()
    private val filteredList = mutableListOf<SideloadFile>()
    private lateinit var adapter: SideloaderAdapter
    private var currentSearchQuery = ""
    private var currentInstallJob: Job? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startScan()
        } else {
            Toast.makeText(context, R.string.storage_permission_required, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSideloaderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SideloaderAdapter(
            filteredList,
            onInstallClicked = { sideloadFile ->
                sideloadPackage(sideloadFile)
            },
            onUninstallClicked = { sideloadFile ->
                showUninstallConfirmation(sideloadFile)
            }
        )

        binding.sideloadList.layoutManager = LinearLayoutManager(context)
        binding.sideloadList.adapter = adapter

        // Search functionality
        binding.searchEditText.addTextChangedListener { text ->
            currentSearchQuery = text?.toString() ?: ""
            filterList()
        }

        // Pull to refresh
        binding.swipeRefresh.setOnRefreshListener {
            cachedFilesList = null
            startScan()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
            startScan()
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startScan()
        }
    }

    private fun filterList() {
        filteredList.clear()
        if (currentSearchQuery.isBlank()) {
            filteredList.addAll(filesList)
        } else {
            val query = currentSearchQuery.lowercase()
            filteredList.addAll(filesList.filter {
                it.name.lowercase().contains(query) ||
                        it.packageName.lowercase().contains(query) ||
                        it.file.name.lowercase().contains(query)
            })
        }
        adapter.notifyDataSetChanged()

        if (filteredList.isEmpty() && filesList.isNotEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.noFilesText.text = getString(R.string.search_no_results)
        } else if (filesList.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.noFilesText.text = "No APK or APKS files found in storage"
        } else {
            binding.emptyState.visibility = View.GONE
        }
    }

    private fun checkPermissionsAndScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                startScan()
            } else {
                showStoragePermissionPrompt()
            }
        } else {
            val permission = Manifest.permission.READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
                startScan()
            } else {
                showStoragePermissionPrompt()
            }
        }
    }

    private fun showStoragePermissionPrompt() {
        binding.emptyState.visibility = View.VISIBLE
        binding.noFilesText.text = getString(R.string.storage_permission_needed)
        binding.btnGrantAccess.visibility = View.VISIBLE
        binding.btnGrantAccess.setOnClickListener { requestStoragePermission() }
        binding.progressBar.visibility = View.GONE
        binding.swipeRefresh.isRefreshing = false
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${requireContext().packageName}")
                }
                startActivity(intent)
            } catch (e: Exception) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivity(intent)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun startScan() {
        if (cachedFilesList != null) {
            filesList.clear()
            filesList.addAll(cachedFilesList!!)
            filterList()
            binding.progressBar.visibility = View.GONE
            binding.swipeRefresh.isRefreshing = false
        } else {
            binding.progressBar.visibility = View.VISIBLE
            binding.swipeRefresh.isRefreshing = false
        }
        binding.emptyState.visibility = View.GONE

        lifecycleScope.launch {
            val scanned = scanStorage(requireContext())
            filesList.clear()
            filesList.addAll(scanned)
            cachedFilesList = scanned
            filterList()
            binding.progressBar.visibility = View.GONE
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private suspend fun scanStorage(context: Context): List<SideloadFile> = withContext(Dispatchers.IO) {
        val result = mutableListOf<SideloadFile>()
        val pathsToScan = listOf(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            Environment.getExternalStorageDirectory()
        )

        val pm = context.packageManager
        val scannedFiles = mutableSetOf<File>()

        for (dir in pathsToScan) {
            if (dir != null && dir.exists() && dir.isDirectory) {
                scanDirRecursive(dir, scannedFiles)
            }
        }

        for (file in scannedFiles) {
            try {
                if (file.name.endsWith(".apk", ignoreCase = true)) {
                    val info = pm.getPackageArchiveInfo(file.absolutePath, 0)
                    val appInfo = info?.applicationInfo
                    if (info != null && appInfo != null) {
                        appInfo.sourceDir = file.absolutePath
                        appInfo.publicSourceDir = file.absolutePath
                        val appLabel = pm.getApplicationLabel(appInfo).toString()
                        val appIcon = pm.getApplicationIcon(appInfo)

                        val isAppInstalled = try {
                            val installedInfo = pm.getPackageInfo(info.packageName, 0)
                            installedInfo != null
                        } catch (e: PackageManager.NameNotFoundException) {
                            false
                        }

                        result.add(
                            SideloadFile(
                                file = file,
                                name = appLabel,
                                packageName = info.packageName,
                                versionName = info.versionName ?: "1.0",
                                sizeText = getReadableSize(file.length()),
                                icon = appIcon,
                                isApks = false,
                                isInstalled = isAppInstalled
                            )
                        )
                    }
                } else if (file.name.endsWith(".apks", ignoreCase = true)) {
                    val sideloadFile = parseApksFile(context, file)
                    if (sideloadFile != null) {
                        result.add(sideloadFile)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        result.sortedBy { it.name.lowercase() }
    }

    private fun scanDirRecursive(dir: File, result: MutableSet<File>) {
        val files = dir.listFiles() ?: return
        for (f in files) {
            if (f.isDirectory) {
                if (!f.name.startsWith(".") && f.name != "Android") {
                    scanDirRecursive(f, result)
                }
            } else if (f.isFile) {
                if (f.name.endsWith(".apk", ignoreCase = true) || f.name.endsWith(".apks", ignoreCase = true)) {
                    result.add(f)
                }
            }
        }
    }

    private fun parseApksFile(context: Context, file: File): SideloadFile? {
        var zipFile: ZipFile? = null
        try {
            zipFile = ZipFile(file)
            val baseEntry = zipFile.entries().asSequence().firstOrNull { it.name.endsWith("base.apk") }
                ?: zipFile.entries().asSequence().firstOrNull { it.name.contains("base") && it.name.endsWith(".apk") }
                ?: zipFile.entries().asSequence().firstOrNull { it.name.endsWith(".apk") }

            if (baseEntry != null) {
                val tempFile = File(context.cacheDir, "temp_base.apk")
                zipFile.getInputStream(baseEntry).use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }

                val pm = context.packageManager
                val info = pm.getPackageArchiveInfo(tempFile.absolutePath, 0)
                val appInfo = info?.applicationInfo
                if (info != null && appInfo != null) {
                    appInfo.sourceDir = tempFile.absolutePath
                    appInfo.publicSourceDir = tempFile.absolutePath
                    val appLabel = pm.getApplicationLabel(appInfo).toString()
                    val appIcon = pm.getApplicationIcon(appInfo)

                    val isAppInstalled = try {
                        val installedInfo = pm.getPackageInfo(info.packageName, 0)
                        installedInfo != null
                    } catch (e: PackageManager.NameNotFoundException) {
                        false
                    }

                    return SideloadFile(
                        file = file,
                        name = "$appLabel (Split)",
                        packageName = info.packageName,
                        versionName = info.versionName ?: "1.0",
                        sizeText = getReadableSize(file.length()),
                        icon = appIcon,
                        isApks = true,
                        isInstalled = isAppInstalled
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            zipFile?.close()
        }
        return null
    }

    private fun getReadableSize(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
        val pre = "KMGTPE"[exp - 1]
        return String.format("%.1f %sB", bytes / Math.pow(1024.0, exp.toDouble()), pre)
    }

    private fun showUninstallConfirmation(sideloadFile: SideloadFile) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.confirm_uninstall_title)
            .setMessage(getString(R.string.confirm_uninstall_message, sideloadFile.name))
            .setPositiveButton(R.string.action_uninstall) { _, _ ->
                uninstallPackage(sideloadFile)
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun sideloadPackage(sideloadFile: SideloadFile) {
        if (!Shizuku.pingBinder()) {
            Toast.makeText(context, "Shizuku/ADB service is not running or connected.", Toast.LENGTH_LONG).show()
            return
        }

        // Prevent duplicate installs
        if (currentInstallJob?.isActive == true) {
            Toast.makeText(context, R.string.installing, Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        currentInstallJob = lifecycleScope.launch {
            val success = withContext(Dispatchers.IO) {
                if (sideloadFile.isApks) {
                    installApksViaShizuku(sideloadFile.file)
                } else {
                    installApkViaShizuku(sideloadFile.file)
                }
            }

            binding.progressBar.visibility = View.GONE
            if (success) {
                sideloadFile.isInstalled = true
                val position = filteredList.indexOf(sideloadFile)
                if (position >= 0) {
                    adapter.notifyItemChanged(position)
                }
                Toast.makeText(context, "Installation successful!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Installation failed. Check Shizuku connection.", Toast.LENGTH_LONG).show()
            }
            currentInstallJob = null
        }
    }

    private fun uninstallPackage(sideloadFile: SideloadFile) {
        if (!Shizuku.pingBinder()) {
            Toast.makeText(context, "Shizuku/ADB service is not running or connected.", Toast.LENGTH_LONG).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val success = withContext(Dispatchers.IO) {
                try {
                    val process = Shizuku.newProcess(
                        arrayOf("pm", "uninstall", sideloadFile.packageName),
                        null,
                        null
                    )
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    var line: String?
                    var output = ""
                    while (reader.readLine().also { line = it } != null) {
                        output += line + "\n"
                    }
                    process.waitFor()
                    output.contains("Success", ignoreCase = true) || process.exitValue() == 0
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }

            binding.progressBar.visibility = View.GONE
            if (success) {
                sideloadFile.isInstalled = false
                val position = filteredList.indexOf(sideloadFile)
                if (position >= 0) {
                    adapter.notifyItemChanged(position)
                }
                Toast.makeText(context, "Uninstalled successfully!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Uninstall failed. Check Shizuku connection.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun installApkViaShizuku(file: File): Boolean {
        try {
            val process = Shizuku.newProcess(
                arrayOf("pm", "install", "-r", "-S", file.length().toString(), "-"),
                null,
                null
            )
            val out = process.outputStream
            file.inputStream().use { input ->
                input.copyTo(out)
            }
            out.flush()
            out.close()

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            var output = ""
            while (reader.readLine().also { line = it } != null) {
                output += line + "\n"
            }
            process.waitFor()
            return output.contains("Success", ignoreCase = true) || process.exitValue() == 0
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun installApksViaShizuku(file: File): Boolean {
        var zipFile: ZipFile? = null
        val tempDir = File(requireContext().cacheDir, "apks_extract")
        if (tempDir.exists()) tempDir.deleteRecursively()
        tempDir.mkdirs()

        try {
            zipFile = ZipFile(file)
            val entries = zipFile.entries()
            val extractedFiles = mutableListOf<File>()

            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (entry.name.endsWith(".apk")) {
                    val destFile = File(tempDir, entry.name)
                    zipFile.getInputStream(entry).use { input ->
                        FileOutputStream(destFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    extractedFiles.add(destFile)
                }
            }

            if (extractedFiles.isEmpty()) return false

            val createProc = Shizuku.newProcess(arrayOf("pm", "install-create"), null, null)
            val createReader = BufferedReader(InputStreamReader(createProc.inputStream))
            var createLine: String?
            var createOutput = ""
            while (createReader.readLine().also { createLine = it } != null) {
                createOutput += createLine + "\n"
            }
            createProc.waitFor()

            val sessionId = "\\[(\\d+)\\]".toRegex().find(createOutput)?.groupValues?.get(1) ?: return false

            for (apk in extractedFiles) {
                val writeProc = Shizuku.newProcess(
                    arrayOf("pm", "install-write", "-S", apk.length().toString(), sessionId, apk.name, "-"),
                    null,
                    null
                )
                val out = writeProc.outputStream
                apk.inputStream().use { input ->
                    input.copyTo(out)
                }
                out.flush()
                out.close()
                writeProc.waitFor()
            }

            val commitProc = Shizuku.newProcess(arrayOf("pm", "install-commit", sessionId), null, null)
            val commitReader = BufferedReader(InputStreamReader(commitProc.inputStream))
            var commitLine: String?
            var commitOutput = ""
            while (commitReader.readLine().also { commitLine = it } != null) {
                commitOutput += commitLine + "\n"
            }
            commitProc.waitFor()

            return commitOutput.contains("Success", ignoreCase = true) || commitProc.exitValue() == 0
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            zipFile?.close()
            tempDir.deleteRecursively()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentInstallJob?.cancel()
        _binding = null
    }

    companion object {
        private var cachedFilesList: List<SideloadFile>? = null
    }
}
