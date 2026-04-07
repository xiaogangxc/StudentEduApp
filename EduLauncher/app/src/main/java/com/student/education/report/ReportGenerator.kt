package com.student.education.report

import android.content.Context
import android.content.SharedPreferences
import com.student.education.model.StudentArchive
import java.text.SimpleDateFormat
import java.util.*

/**
 * 报告生成器
 * 生成日报、周报、月报
 */
class ReportGenerator(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "LearningRecords"
        private const val KEY_STUDY_DURATION = "study_duration_"
        private const val KEY_SUBJECTS = "subjects_"
        private const val KEY_POSTURE_ALERTS = "posture_alerts_"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * 日报数据类
     */
    data class DailyReport(
        val studentName: String,
        val date: String,
        val studyDuration: Int,  // 学习时长（分钟）
        val subjects: List<String>,  // 学习的科目
        val postureAlertCount: Int,  // 坐姿提醒次数
        val completedTasks: List<String>,  // 完成的任务
        val weakPoints: List<String>,  // 薄弱点
        val suggestions: String  // 建议
    )

    /**
     * 周报数据类
     */
    data class WeeklyReport(
        val studentName: String,
        val weekStart: String,
        val weekEnd: String,
        val totalStudyDuration: Int,
        val dailyAverageDuration: Int,
        val subjects: Map<String, Int>,  // 科目 -> 时长
        val strongestSubject: String,  // 最强科目
        val weakestSubject: String,  // 最弱科目
        val weakPointsSummary: String,
        val postureAlertTotal: Int,
        val trend: String  // 趋势：进步/稳定/需关注
    )

    /**
     * 月报数据类
     */
    data class MonthlyReport(
        val studentName: String,
        val month: String,
        val totalStudyDuration: Int,
        val studyDays: Int,  // 学习天数
        val subjects: Map<String, Int>,
        val progressSummary: String,
        val achievements: List<String>,
        val weakPoints: List<String>,
        val suggestions: String
    )

    /**
     * 生成日报
     */
    fun generateDailyReport(archive: StudentArchive?): DailyReport {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val studentName = archive?.basicInfo?.name ?: "学生"

        // 从本地存储获取今日学习数据
        val studyDuration = prefs.getInt(KEY_STUDY_DURATION + today, 0)
        val subjectsStr = prefs.getString(KEY_SUBJECTS + today, "") ?: ""
        val subjects = if (subjectsStr.isEmpty()) listOf("数学", "语文") else subjectsStr.split(",")
        val postureAlerts = prefs.getInt(KEY_POSTURE_ALERTS + today, 0)

        // 生成建议
        val suggestions = generateDailySuggestions(studyDuration, postureAlerts, archive)

        return DailyReport(
            studentName = studentName,
            date = today,
            studyDuration = studyDuration,
            subjects = subjects,
            postureAlertCount = postureAlerts,
            completedTasks = listOf("完成作业", "预习新课", "复习错题"),
            weakPoints = archive?.subjectInfo?.weakPoints ?: listOf(),
            suggestions = suggestions
        )
    }

    /**
     * 生成周报
     */
    fun generateWeeklyReport(archive: StudentArchive?): WeeklyReport {
        val calendar = Calendar.getInstance()
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)

        // 计算本周开始和结束日期
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val weekStart = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        calendar.add(Calendar.DATE, 6)
        val weekEnd = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        val studentName = archive?.basicInfo?.name ?: "学生"

        // 统计本周数据
        var totalDuration = 0
        var totalPostureAlerts = 0
        val subjectMap = mutableMapOf<String, Int>()

        calendar.add(Calendar.DATE, -6) // 回到周一
        for (i in 0..6) {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            totalDuration += prefs.getInt(KEY_STUDY_DURATION + dateStr, 0)
            totalPostureAlerts += prefs.getInt(KEY_POSTURE_ALERTS + dateStr, 0)

            val subjectsStr = prefs.getString(KEY_SUBJECTS + dateStr, "") ?: ""
            subjectsStr.split(",").forEach { subject ->
                if (subject.isNotEmpty()) {
                    subjectMap[subject] = subjectMap.getOrDefault(subject, 0) + 
                        prefs.getInt(KEY_STUDY_DURATION + dateStr, 0) / maxOf(subjectsStr.split(",").size, 1)
                }
            }
            calendar.add(Calendar.DATE, 1)
        }

        // 找出最强和最弱科目
        val strongestSubject = subjectMap.maxByOrNull { it.value }?.key ?: "数学"
        val weakestSubject = archive?.subjectInfo?.weakPoints?.firstOrNull() ?: "语文"

        return WeeklyReport(
            studentName = studentName,
            weekStart = weekStart,
            weekEnd = weekEnd,
            totalStudyDuration = totalDuration,
            dailyAverageDuration = totalDuration / 7,
            subjects = subjectMap,
            strongestSubject = strongestSubject,
            weakestSubject = weakestSubject,
            weakPointsSummary = archive?.subjectInfo?.weakPoints?.joinToString(", ") ?: "暂无",
            postureAlertTotal = totalPostureAlerts,
            trend = if (totalDuration > 300) "进步" else "需关注"
        )
    }

    /**
     * 生成月报
     */
    fun generateMonthlyReport(archive: StudentArchive?): MonthlyReport {
        val calendar = Calendar.getInstance()
        val monthStr = SimpleDateFormat("yyyy年MM月", Locale.getDefault()).format(calendar.time)
        val studentName = archive?.basicInfo?.name ?: "学生"

        // 获取本月天数
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        var totalDuration = 0
        var studyDays = 0
        val subjectMap = mutableMapOf<String, Int>()
        val achievements = mutableListOf<String>()

        for (day in 1..daysInMonth) {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            val dayDuration = prefs.getInt(KEY_STUDY_DURATION + dateStr, 0)

            if (dayDuration > 0) {
                totalDuration += dayDuration
                studyDays++

                val subjectsStr = prefs.getString(KEY_SUBJECTS + dateStr, "") ?: ""
                subjectsStr.split(",").forEach { subject ->
                    if (subject.isNotEmpty()) {
                        subjectMap[subject] = subjectMap.getOrDefault(subject, 0) + 
                            dayDuration / maxOf(subjectsStr.split(",").size, 1)
                    }
                }
            }
            calendar.add(Calendar.DATE, 1)
        }

        // 生成成就
        if (studyDays >= 20) achievements.add("本月学习天数达标")
        if (totalDuration >= 1500) achievements.add("学习时长优秀")
        if (achievements.isEmpty()) achievements.add("持续学习中")

        val progressSummary = generateProgressSummary(totalDuration, studyDays, daysInMonth)
        val suggestions = generateMonthlySuggestions(archive, subjectMap)

        return MonthlyReport(
            studentName = studentName,
            month = monthStr,
            totalStudyDuration = totalDuration,
            studyDays = studyDays,
            subjects = subjectMap,
            progressSummary = progressSummary,
            achievements = achievements,
            weakPoints = archive?.subjectInfo?.weakPoints ?: listOf(),
            suggestions = suggestions
        )
    }

    /**
     * 生成日报建议
     */
    private fun generateDailySuggestions(duration: Int, postureAlerts: Int, archive: StudentArchive?): String {
        val suggestions = mutableListOf<String>()

        when {
            duration < 30 -> suggestions.add("建议增加学习时长")
            duration > 120 -> suggestions.add("学习时长充足，注意休息")
            else -> suggestions.add("学习时长适中")
        }

        if (postureAlerts > 5) {
            suggestions.add("坐姿需加强监督")
        } else if (postureAlerts == 0) {
            suggestions.add("坐姿保持良好")
        }

        archive?.subjectInfo?.weakPoints?.firstOrNull()?.let {
            suggestions.add("建议加强$it 练习")
        }

        return suggestions.joinToString("；")
    }

    /**
     * 生成进度总结
     */
    private fun generateProgressSummary(totalDuration: Int, studyDays: Int, totalDays: Int): String {
        val rate = studyDays * 100 / totalDays
        return when {
            rate >= 80 -> "学习积极性高，坚持良好习惯"
            rate >= 60 -> "学习较稳定，可适当增加频率"
            else -> "学习频率偏低，建议制定学习计划"
        }
    }

    /**
     * 生成月度建议
     */
    private fun generateMonthlySuggestions(archive: StudentArchive?, subjectMap: Map<String, Int>): String {
        val suggestions = mutableListOf<String>()

        archive?.subjectInfo?.weakPoints?.forEach { weakPoint ->
            suggestions.add("针对$weakPoint 制定专项提升计划")
        }

        if (subjectMap.isNotEmpty()) {
            val minSubject = subjectMap.minByOrNull { it.value }?.key
            minSubject?.let { suggestions.add("适当增加$it 的学习时间") }
        }

        suggestions.add("保持规律作息，劳逸结合")

        return suggestions.joinToString("；")
    }

    /**
     * 记录学习时长
     */
    fun recordStudyDuration(durationMinutes: Int) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val current = prefs.getInt(KEY_STUDY_DURATION + today, 0)
        prefs.edit().putInt(KEY_STUDY_DURATION + today, current + durationMinutes).apply()
    }

    /**
     * 记录学习科目
     */
    fun recordSubjects(subjects: List<String>) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        prefs.edit().putString(KEY_SUBJECTS + today, subjects.joinToString(",")).apply()
    }

    /**
     * 记录坐姿提醒
     */
    fun recordPostureAlert() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val current = prefs.getInt(KEY_POSTURE_ALERTS + today, 0)
        prefs.edit().putInt(KEY_POSTURE_ALERTS + today, current + 1).apply()
    }
}