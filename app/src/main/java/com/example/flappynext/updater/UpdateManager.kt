package com.example.flappynext.updater

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class UpdateManager(
    private val context: Context,
    private val githubUser: String,
    private val githubRepo: String
) {

    fun checkForUpdates(onNoUpdateFound: (() -> Unit)? = null) {
        thread {
            try {
                // Запрос к публичному GitHub API для получения последнего релиза
                val apiUrl = "https://api.github.com/repos/$githubUser/$githubRepo/releases/latest"
                val url = URL(apiUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json")
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                if (conn.responseCode == 200) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val response = reader.readText()
                    reader.close()

                    val json = JSONObject(response)
                    val tagName = json.getString("tag_name") // Например: "v2" или "2"
                    val latestVersionCode = tagName.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0

                    val currentVersionCode = getCurrentAppVersionCode()

                    if (latestVersionCode > currentVersionCode) {
                        // Ищем ссылку на файл .apk в ассетах релиза
                        val assets = json.getJSONArray("assets")
                        var apkDownloadUrl: String? = null
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            val name = asset.getString("name")
                            if (name.endsWith(".apk", ignoreCase = true)) {
                                apkDownloadUrl = asset.getString("browser_download_url")
                                break
                            }
                        }

                        if (apkDownloadUrl != null) {
                            showUpdateDialog(tagName, apkDownloadUrl)
                        }
                    } else {
                        onNoUpdateFound?.let {
                            (context as? android.app.Activity)?.runOnUiThread { it.invoke() }
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun getCurrentAppVersionCode(): Long {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode.toLong()
            }
        } catch (_: Exception) {
            1L
        }
    }

    private fun showUpdateDialog(newVersionName: String, downloadUrl: String) {
        (context as? android.app.Activity)?.runOnUiThread {
            AlertDialog.Builder(context)
                .setTitle("Доступно обновление!")
                .setMessage("Вышла новая версия $newVersionName. Обновить игру сейчас?")
                .setPositiveButton("Обновить") { _, _ ->
                    downloadAndInstallApk(downloadUrl)
                }
                .setNegativeButton("Позже", null)
                .setCancelable(true)
                .show()
        }
    }

    private fun downloadAndInstallApk(url: String) {
        val fileName = "FlappyNext_Update.apk"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val destinationFile = File(storageDir, fileName)

        if (destinationFile.exists()) {
            destinationFile.delete()
        }

        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setTitle("Загрузка обновления")
            setDescription("Скачивание новой версии Flappy Next...")
            setDestinationUri(Uri.fromFile(destinationFile))
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        }

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = manager.enqueue(request)

        val onCompleteReceiver = object : BroadcastReceiver() {
            override fun onReceive(recvContext: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
                if (id == downloadId) {
                    try {
                        context.unregisterReceiver(this)
                    } catch (_: Exception) {}
                    installApk(destinationFile)
                }
            }
        }

        // Корректная регистрация для всех версий Android (включая Android 14+)
        androidx.core.content.ContextCompat.registerReceiver(
            context,
            onCompleteReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            androidx.core.content.ContextCompat.RECEIVER_EXPORTED
        )
    }

    private fun installApk(file: File) {
        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        context.startActivity(installIntent)
    }
}