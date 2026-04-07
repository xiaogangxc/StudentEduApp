package com.student.education.service

import android.content.Context
import android.util.Log
import com.student.education.model.StudentArchive
import com.student.education.util.ArchiveManager
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * 报告定时推送调度器
 * 负责定时生成和推送日报、周报、月报
 */
class ReportScheduler(private val context: Context) {

    companion object {
        private const val TAG = "ReportScheduler"

        // 推送时间配置
        private const val DAILY_HOUR = 21      // 日报：每天21:00
        private const val DAILY_MINUTE = 0

        private const val WEEKLY_HOUR = 20     // 周报：每周日20:00
        private const val WEEKLY_MINUTE = 0
        private const val WEEKLY_DAY = Calendar.SUNDAY

        private const val MONTHLY_HOUR = 19    // 月报：每月1号19:00
        private const val MONTHLY_MINUTE = 0
        private const val MONTHLY_DATE = 1
    }

    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(3)
    private val wechatPushService = WechatPushService(context)
    private val archiveManager = ArchiveManager(context)

    /**
     * 启动所有定时任务
     */
    fun startAllSchedules() {
        Log.d(TAG, "启动报告定时推送服务")
        startDailySchedule()
        startWeeklySchedule()
        startMonthlySchedule()
    }

    /**
     * 停止所有定时任务
     */
    fun stopAllSchedules() {
        Log.d(TAG, "停止报告定时推送服务")
        scheduler.shutdown()
    }

    /**
     * 启动日报定时任务
     */
    private fun startDailySchedule() {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        // 设置今天21:00
        calendar.set(Calendar.HOUR_OF_DAY, DAILY_HOUR)
        calendar.set(Calendar.MINUTE, DAILY_MINUTE)
        calendar.set(Calendar.SECOND, 0)

        // 如果今天21:00已过，设置为明天
        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DATE, 1)
        }

        val delay = calendar.timeInMillis - now
        val period = TimeUnit.DAYS.toMillis(1) // 每天执行

        scheduler.scheduleAtFixedRate({
            try {
                Log.d(TAG, "执行日报推送任务")
                if (!wechatPushService.isDailyReportPushed()) {
                    val archive = loadCurrentArchive()
                    wechatPushService.pushDailyReport(archive) { success ->
                        if (success) {
                            Log.d(TAG, "日报推送成功")
                        } else {
                            Log.e(TAG, "日报推送失败")
                        }
                    }
                } else {
                    Log.d(TAG, "今日日报已推送，跳过")
                }
            } catch (e: Exception) {
                Log.e(TAG, "日报推送任务异常", e)
            }
        }, delay, period, TimeUnit.MILLISECONDS)

        Log.d(TAG, "日报定时任务已启动，首次执行: ${calendar.time}")
    }

    /**
     * 启动周报定时任务
     */
    private fun startWeeklySchedule() {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        // 设置本周日20:00
        calendar.set(Calendar.DAY_OF_WEEK, WEEKLY_DAY)
        calendar.set(Calendar.HOUR_OF_DAY, WEEKLY_HOUR)
        calendar.set(Calendar.MINUTE, WEEKLY_MINUTE)
        calendar.set(Calendar.SECOND, 0)

        // 如果本周日已过，设置为下周日
        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
        }

        val delay = calendar.timeInMillis - now
        val period = TimeUnit.DAYS.toMillis(7) // 每周执行

        scheduler.scheduleAtFixedRate({
            try {
                Log.d(TAG, "执行周报推送任务")
                if (!wechatPushService.isWeeklyReportPushed()) {
                    val archive = loadCurrentArchive()
                    wechatPushService.pushWeeklyReport(archive) { success ->
                        if (success) {
                            Log.d(TAG, "周报推送成功")
                        } else {
                            Log.e(TAG, "周报推送失败")
                        }
                    }
                } else {
                    Log.d(TAG, "本周周报已推送，跳过")
                }
            } catch (e: Exception) {
                Log.e(TAG, "周报推送任务异常", e)
            }
        }, delay, period, TimeUnit.MILLISECONDS)

        Log.d(TAG, "周报定时任务已启动，首次执行: ${calendar.time}")
    }

    /**
     * 启动月报定时任务
     */
    private fun startMonthlySchedule() {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        // 设置本月1号19:00
        calendar.set(Calendar.DAY_OF_MONTH, MONTHLY_DATE)
        calendar.set(Calendar.HOUR_OF_DAY, MONTHLY_HOUR)
        calendar.set(Calendar.MINUTE, MONTHLY_MINUTE)
        calendar.set(Calendar.SECOND, 0)

        // 如果本月1号已过，设置为下月1号
        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.MONTH, 1)
        }

        val delay = calendar.timeInMillis - now

        // 月报需要计算到下月1号的毫秒数
        val nextMonth = Calendar.getInstance()
        nextMonth.time = calendar.time
        nextMonth.add(Calendar.MONTH, 1)
        val period = nextMonth.timeInMillis - calendar.timeInMillis

        scheduler.scheduleAtFixedRate({
            try {
                Log.d(TAG, "执行月报推送任务")
                if (!wechatPushService.isMonthlyReportPushed()) {
                    val archive = loadCurrentArchive()
                    wechatPushService.pushMonthlyReport(archive) { success ->
                        if (success) {
                            Log.d(TAG, "月报推送成功")
                        } else {
                            Log.e(TAG, "月报推送失败")
                        }
                    }
                } else {
                    Log.d(TAG, "本月月报已推送，跳过")
                }
            } catch (e: Exception) {
                Log.e(TAG, "月报推送任务异常", e)
            }
        }, delay, period, TimeUnit.MILLISECONDS)

        Log.d(TAG, "月报定时任务已启动，首次执行: ${calendar.time}")
    }

    /**
     * 立即推送日报
     */
    fun pushDailyNow(callback: (Boolean) -> Unit) {
        val archive = loadCurrentArchive()
        wechatPushService.pushDailyReport(archive, callback)
    }

    /**
     * 立即推送周报
     */
    fun pushWeeklyNow(callback: (Boolean) -> Unit) {
        val archive = loadCurrentArchive()
        wechatPushService.pushWeeklyReport(archive, callback)
    }

    /**
     * 立即推送月报
     */
    fun pushMonthlyNow(callback: (Boolean) -> Unit) {
        val archive = loadCurrentArchive()
        wechatPushService.pushMonthlyReport(archive, callback)
    }

    /**
     * 加载当前学生档案
     */
    private fun loadCurrentArchive(): StudentArchive? {
        return try {
            archiveManager.loadFromLocal()
        } catch (e: Exception) {
            Log.e(TAG, "加载档案失败", e)
            null
        }
    }

    /**
     * 清理过期的推送标记
     */
    fun cleanExpiredMarks() {
        wechatPushService.cleanExpiredMarks()
    }
}