package com.student.education.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.student.education.model.StudentArchive
import com.student.education.report.ReportGenerator
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

/**
 * 微信小程序推送服务
 * 用于推送学习报告给家长微信
 */
class WechatPushService(private val context: Context) {

    companion object {
        private const val TAG = "WechatPushService"
        private const val PREFS_NAME = "WechatPushPrefs"
        private const val KEY_PARENT_OPEN_ID = "parent_open_id"
        private const val KEY_DAILY_PUSHED = "daily_pushed_"
        private const val KEY_WEEKLY_PUSHED = "weekly_pushed_"
        private const val KEY_MONTHLY_PUSHED = "monthly_pushed_"
        
        // 微信小程序配置（需要替换为实际的配置）
        private const val APP_ID = "your_app_id_here"
        private const val APP_SECRET = "your_app_secret_here"
        private const val TEMPLATE_ID_DAILY = "your_daily_template_id"
        private const val TEMPLATE_ID_WEEKLY = "your_weekly_template_id"
        private const val TEMPLATE_ID_MONTHLY = "your_monthly_template_id"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val reportGenerator = ReportGenerator(context)

    /**
     * 保存家长微信OpenID
     */
    fun saveParentOpenId(openId: String) {
        prefs.edit().putString(KEY_PARENT_OPEN_ID, openId).apply()
        Log.d(TAG, "家长OpenID已保存")
    }

    /**
     * 获取存储的家长OpenID
     */
    fun getStoredParentOpenId(): String? {
        return prefs.getString(KEY_PARENT_OPEN_ID, null)
    }

    /**
     * 检查今日日报是否已推送
     */
    fun isDailyReportPushed(): Boolean {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return prefs.getBoolean(KEY_DAILY_PUSHED + today, false)
    }

    /**
     * 检查本周周报是否已推送
     */
    fun isWeeklyReportPushed(): Boolean {
        val week = SimpleDateFormat("yyyy-ww", Locale.getDefault()).format(Date())
        return prefs.getBoolean(KEY_WEEKLY_PUSHED + week, false)
    }

    /**
     * 检查本月月报是否已推送
     */
    fun isMonthlyReportPushed(): Boolean {
        val month = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        return prefs.getBoolean(KEY_MONTHLY_PUSHED + month, false)
    }

    /**
     * 推送日报
     */
    fun pushDailyReport(archive: StudentArchive?, callback: (Boolean) -> Unit) {
        val parentOpenId = getStoredParentOpenId()
        if (parentOpenId.isNullOrEmpty()) {
            Log.e(TAG, "家长OpenID未配置")
            callback(false)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val report = reportGenerator.generateDailyReport(archive)
                val success = sendTemplateMessage(
                    parentOpenId,
                    TEMPLATE_ID_DAILY,
                    buildDailyTemplateData(report),
                    "pages/report/daily"
                )

                if (success) {
                    markDailyPushed()
                }

                withContext(Dispatchers.Main) {
                    callback(success)
                }
            } catch (e: Exception) {
                Log.e(TAG, "推送日报失败", e)
                withContext(Dispatchers.Main) {
                    callback(false)
                }
            }
        }
    }

    /**
     * 推送周报
     */
    fun pushWeeklyReport(archive: StudentArchive?, callback: (Boolean) -> Unit) {
        val parentOpenId = getStoredParentOpenId()
        if (parentOpenId.isNullOrEmpty()) {
            Log.e(TAG, "家长OpenID未配置")
            callback(false)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val report = reportGenerator.generateWeeklyReport(archive)
                val success = sendTemplateMessage(
                    parentOpenId,
                    TEMPLATE_ID_WEEKLY,
                    buildWeeklyTemplateData(report),
                    "pages/report/weekly"
                )

                if (success) {
                    markWeeklyPushed()
                }

                withContext(Dispatchers.Main) {
                    callback(success)
                }
            } catch (e: Exception) {
                Log.e(TAG, "推送周报失败", e)
                withContext(Dispatchers.Main) {
                    callback(false)
                }
            }
        }
    }

    /**
     * 推送月报
     */
    fun pushMonthlyReport(archive: StudentArchive?, callback: (Boolean) -> Unit) {
        val parentOpenId = getStoredParentOpenId()
        if (parentOpenId.isNullOrEmpty()) {
            Log.e(TAG, "家长OpenID未配置")
            callback(false)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val report = reportGenerator.generateMonthlyReport(archive)
                val success = sendTemplateMessage(
                    parentOpenId,
                    TEMPLATE_ID_MONTHLY,
                    buildMonthlyTemplateData(report),
                    "pages/report/monthly"
                )

                if (success) {
                    markMonthlyPushed()
                }

                withContext(Dispatchers.Main) {
                    callback(success)
                }
            } catch (e: Exception) {
                Log.e(TAG, "推送月报失败", e)
                withContext(Dispatchers.Main) {
                    callback(false)
                }
            }
        }
    }

    /**
     * 发送模板消息
     */
    private suspend fun sendTemplateMessage(
        openId: String,
        templateId: String,
        data: Map<String, String>,
        page: String
    ): Boolean {
        return try {
            // 获取access_token
            val accessToken = getAccessToken() ?: return false

            // 构建请求体
            val jsonBody = JSONObject().apply {
                put("touser", openId)
                put("template_id", templateId)
                put("page", page)
                put("data", JSONObject(data))
            }

            // 发送请求
            val url = URL("https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=$accessToken")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            connection.outputStream.use { os ->
                os.write(jsonBody.toString().toByteArray())
            }

            val responseCode = connection.responseCode
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            Log.d(TAG, "推送响应: $response")

            if (responseCode == 200) {
                val jsonResponse = JSONObject(response)
                jsonResponse.optInt("errcode", -1) == 0
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "发送模板消息失败", e)
            false
        }
    }

    /**
     * 获取微信access_token
     */
    private suspend fun getAccessToken(): String? {
        return try {
            val url = URL("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=$APP_ID&secret=$APP_SECRET")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            val jsonResponse = JSONObject(response)
            jsonResponse.optString("access_token", null)
        } catch (e: Exception) {
            Log.e(TAG, "获取access_token失败", e)
            null
        }
    }

    /**
     * 构建日报模板数据
     */
    private fun buildDailyTemplateData(report: ReportGenerator.DailyReport): Map<String, String> {
        return mapOf(
            "thing1" to report.studentName,
            "time2" to report.date,
            "thing3" to "${report.studyDuration}分钟",
            "thing4" to report.subjects.joinToString(", "),
            "thing5" to if (report.postureAlertCount > 0) "坐姿提醒${report.postureAlertCount}次" else "坐姿良好"
        )
    }

    /**
     * 构建周报模板数据
     */
    private fun buildWeeklyTemplateData(report: ReportGenerator.WeeklyReport): Map<String, String> {
        return mapOf(
            "thing1" to report.studentName,
            "time2" to "${report.weekStart} 至 ${report.weekEnd}",
            "thing3" to "${report.totalStudyDuration}分钟",
            "thing4" to report.strongestSubject,
            "thing5" to report.weakPointsSummary
        )
    }

    /**
     * 构建月报模板数据
     */
    private fun buildMonthlyTemplateData(report: ReportGenerator.MonthlyReport): Map<String, String> {
        return mapOf(
            "thing1" to report.studentName,
            "time2" to report.month,
            "thing3" to "${report.totalStudyDuration}分钟",
            "thing4" to report.progressSummary,
            "thing5" to report.suggestions
        )
    }

    /**
     * 标记日报已推送
     */
    private fun markDailyPushed() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        prefs.edit().putBoolean(KEY_DAILY_PUSHED + today, true).apply()
    }

    /**
     * 标记周报已推送
     */
    private fun markWeeklyPushed() {
        val week = SimpleDateFormat("yyyy-ww", Locale.getDefault()).format(Date())
        prefs.edit().putBoolean(KEY_WEEKLY_PUSHED + week, true).apply()
    }

    /**
     * 标记月报已推送
     */
    private fun markMonthlyPushed() {
        val month = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        prefs.edit().putBoolean(KEY_MONTHLY_PUSHED + month, true).apply()
    }

    /**
     * 清除过期的推送标记
     */
    fun cleanExpiredMarks() {
        val editor = prefs.edit()
        val allKeys = prefs.all.keys

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val week = SimpleDateFormat("yyyy-ww", Locale.getDefault()).format(Date())
        val month = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

        allKeys.forEach { key ->
            when {
                key.startsWith(KEY_DAILY_PUSHED) && !key.endsWith(today) -> editor.remove(key)
                key.startsWith(KEY_WEEKLY_PUSHED) && !key.endsWith(week) -> editor.remove(key)
                key.startsWith(KEY_MONTHLY_PUSHED) && !key.endsWith(month) -> editor.remove(key)
            }
        }

        editor.apply()
    }
}