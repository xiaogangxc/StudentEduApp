package com.student.education

import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.student.education.model.StudentArchive
import com.student.education.util.ArchiveManager

/**
 * 档案查看Activity
 */
class ArchiveActivity : AppCompatActivity() {

    private lateinit var archiveManager: ArchiveManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 保持屏幕常亮
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        setContentView(R.layout.activity_archive)

        archiveManager = ArchiveManager(this)
        
        loadAndDisplayArchive()
    }

    private fun loadAndDisplayArchive() {
        val archive = archiveManager.readArchive()
        
        if (archive != null) {
            displayArchive(archive)
        } else {
            findViewById<TextView>(R.id.tvArchiveContent).text = "暂无档案信息"
        }
    }

    private fun displayArchive(archive: StudentArchive) {
        val content = buildString {
            appendLine("=== 基础信息 ===")
            appendLine("姓名: ${archive.basicInfo.name}")
            appendLine("性别: ${archive.basicInfo.gender}")
            appendLine("年级: ${archive.basicInfo.grade}")
            appendLine("学段: ${archive.basicInfo.stage}")
            appendLine("当前学期: ${archive.basicInfo.currentTerm}")
            appendLine("学习进度: ${archive.basicInfo.learningProgress}")
            appendLine("就读模式: ${archive.basicInfo.studyMode}")
            appendLine("健康禁忌: ${archive.basicInfo.healthRestrictions}")
            appendLine()
            
            appendLine("=== 学科状态 ===")
            archive.subjectStatus.forEach { subject ->
                appendLine("${subject.subject}:")
                appendLine("  教材版本: ${subject.textbookVersion}")
                appendLine("  当前章节: ${subject.currentChapter}")
                appendLine("  掌握度: ${(subject.masteryLevel * 100).toInt()}%")
                appendLine()
            }
            
            appendLine("=== 性格评估 ===")
            appendLine("性格类型: ${archive.personalityAssessment.personalityType}")
            appendLine("沟通偏好: ${archive.personalityAssessment.communicationPreference}")
            appendLine("抗压能力: ${archive.personalityAssessment.stressTolerance}")
            appendLine()
            
            appendLine("=== 薄弱知识点 ===")
            archive.weakPoints.forEach { point ->
                appendLine("${point.subject} - ${point.knowledgePoint}:")
                appendLine("  掌握度: ${(point.masteryLevel * 100).toInt()}%")
                appendLine("  出错次数: ${point.errorCount}")
                appendLine("  补救方案: ${point.remedyPlan}")
                appendLine()
            }
            
            appendLine("=== 家长偏好 ===")
            appendLine("辅导风格: ${archive.parentPreferences.tutoringStyle}")
            appendLine("每日最大学习时长: ${archive.parentPreferences.maxDailyLearningTime}分钟")
            appendLine("报告推送方式: ${archive.parentPreferences.reportDeliveryMethod}")
        }
        
        findViewById<TextView>(R.id.tvArchiveContent).text = content
    }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}