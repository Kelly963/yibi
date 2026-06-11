package com.yibi.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Looper
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * 自动更新检查器
 * 通过 GitHub Releases API 检测新版本并提示用户更新
 *
 * 仓库：https://github.com/Kelly963/yibi
 * Release 标签格式：v2.0.0
 */
object UpdateChecker {

    private const val GITHUB_API = "https://api.github.com/repos/Kelly963/yibi/releases/latest"
    private const val GITHUB_RELEASES_PAGE = "https://github.com/Kelly963/yibi/releases"
    private const val REQUEST_TIMEOUT_MS = 10_000L

    /**
     * 检查更新回调
     */
    sealed class UpdateResult {
        data class UpdateAvailable(
            val tagName: String,
            val downloadUrl: String,
            val body: String
        ) : UpdateResult()
        object NoUpdate : UpdateResult()
        object NoRelease : UpdateResult()
        data class Error(val message: String) : UpdateResult()
    }

    data class ReleaseInfo(
        val tagName: String,
        val name: String,
        val body: String,
        val publishedAt: String,
        val htmlUrl: String,
        val prerelease: Boolean
    )

    /**
     * 检查更新（协程版本）
     */
    suspend fun checkForUpdate(context: Context): UpdateResult {
        return withTimeoutOrNull(REQUEST_TIMEOUT_MS) {
            withContext(Dispatchers.IO) {
                try {
                    val connection = URL(GITHUB_API).openConnection() as HttpURLConnection
                    connection.apply {
                        requestMethod = "GET"
                        setRequestProperty("Accept", "application/vnd.github+json")
                        setRequestProperty("User-Agent", "Yibi-UpdateChecker")
                        connectTimeout = 8000
                        readTimeout = 8000
                    }

                    val responseCode = connection.responseCode

                    if (responseCode == 404) {
                        return@withContext UpdateResult.NoRelease
                    }
                    if (responseCode != 200) {
                        return@withContext UpdateResult.Error("HTTP $responseCode")
                    }

                    val jsonText = BufferedReader(InputStreamReader(connection.inputStream)).use {
                        it.readText()
                    }
                    connection.disconnect()

                    val release = JSONObject(jsonText)
                    val tagName = release.optString("tag_name", "")
                    val body = release.optString("body", "")
                    val prerelease = release.optBoolean("prerelease", false)

                    if (tagName.isEmpty()) {
                        return@withContext UpdateResult.NoRelease
                    }
                    if (prerelease) {
                        return@withContext UpdateResult.NoUpdate  // 忽略预发布
                    }

                    // 比较版本
                    val currentVersion = getCurrentVersionName(context)
                    if (!isNewerVersion(parseVersion(tagName), parseVersion(currentVersion))) {
                        return@withContext UpdateResult.NoUpdate
                    }

                    // 查找 APK 下载链接
                    val assets = release.optJSONArray("assets")
                    var downloadUrl = release.optString("html_url", GITHUB_RELEASES_PAGE)
                    if (assets != null && assets.length() > 0) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            val name = asset.optString("name", "")
                            if (name.endsWith(".apk")) {
                                downloadUrl = asset.optString("browser_download_url", downloadUrl)
                                break
                            }
                        }
                    }

                    UpdateResult.UpdateAvailable(
                        tagName = tagName,
                        downloadUrl = downloadUrl,
                        body = body
                    )
                } catch (e: Exception) {
                    UpdateResult.Error(e.message ?: "未知错误")
                }
            }
        } ?: UpdateResult.Error("网络超时")
    }

    /**
     * 获取所有 Release 列表
     */
    suspend fun fetchAllReleases(): List<ReleaseInfo> {
        return withTimeoutOrNull(REQUEST_TIMEOUT_MS) {
            withContext(Dispatchers.IO) {
                try {
                    val url = "https://api.github.com/repos/Kelly963/yibi/releases"
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.apply {
                        requestMethod = "GET"
                        setRequestProperty("Accept", "application/vnd.github+json")
                        setRequestProperty("User-Agent", "Yibi-UpdateChecker")
                        connectTimeout = 8000
                        readTimeout = 8000
                    }

                    if (connection.responseCode != 200) {
                        return@withContext emptyList()
                    }

                    val jsonText = BufferedReader(InputStreamReader(connection.inputStream)).use {
                        it.readText()
                    }
                    connection.disconnect()

                    val jsonArray = org.json.JSONArray(jsonText)
                    val releases = mutableListOf<ReleaseInfo>()
                    for (i in 0 until jsonArray.length()) {
                        val release = jsonArray.getJSONObject(i)
                        releases.add(
                            ReleaseInfo(
                                tagName = release.optString("tag_name", ""),
                                name = release.optString("name", ""),
                                body = release.optString("body", ""),
                                publishedAt = release.optString("published_at", ""),
                                htmlUrl = release.optString("html_url", ""),
                                prerelease = release.optBoolean("prerelease", false)
                            )
                        )
                    }
                    releases.filter { !it.prerelease }
                } catch (e: Exception) {
                    emptyList()
                }
            }
        } ?: emptyList()
    }

    /**
     * 检查并显示更新对话框（完整封装，在 MainActivity.onCreate 中调用）
     */
    fun checkAndShowUpdateDialog(context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            when (val result = checkForUpdate(context)) {
                is UpdateResult.UpdateAvailable -> {
                    showUpdateDialog(context, result)
                }
                is UpdateResult.NoUpdate,
                is UpdateResult.NoRelease,
                is UpdateResult.Error -> {
                    // 静默处理
                }
            }
        }
    }

    /**
     * 显示更新对话框
     */
    private fun showUpdateDialog(context: Context, update: UpdateResult.UpdateAvailable) {
        val title = "v${update.tagName.replaceFirst("v", "")} 更新内容"

        val builder = android.app.AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(update.body.ifEmpty { "暂无更新说明" })
            .setPositiveButton("立即更新") { _, _ ->
                openDownloadPage(context, update.downloadUrl)
            }
            .setNegativeButton("稍后提醒") { dialog, _ ->
                dialog.dismiss()
            }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            builder.create().show()
        } else {
            android.os.Handler(Looper.getMainLooper()).post {
                builder.create().show()
            }
        }
    }

    /**
     * 打开下载页面
     */
    private fun openDownloadPage(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "请在浏览器中打开：$url", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * 获取当前版本名
     */
    fun getCurrentVersionName(context: Context): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0)
                .versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    /**
     * 解析版本字符串（支持 v2.0.0 / 2.0.0 / 2.0 等格式）
     * 返回 [major, minor, patch]
     */
    private fun parseVersion(version: String): IntArray {
        val cleaned = version.removePrefix("v").removePrefix("V")
        val parts = cleaned.split(".").mapNotNull { it.toIntOrNull() }
        return intArrayOf(
            parts.getOrElse(0) { 0 },
            parts.getOrElse(1) { 0 },
            parts.getOrElse(2) { 0 }
        )
    }

    /**
     * 比较两个 semver
     */
    private fun isNewerVersion(latest: IntArray, current: IntArray): Boolean {
        for (i in 0 until 3) {
            when {
                latest[i] > current[i] -> return true
                latest[i] < current[i] -> return false
            }
        }
        return false // 相等
    }
}
