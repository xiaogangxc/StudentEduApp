package com.student.education.util

import android.content.Context
import android.os.Environment
import com.google.gson.Gson
import com.student.education.model.StudentArchive
import java.io.File

/**
 * 档案管理工具类
 */
class ArchiveManager(private val context: Context) {
    
    companion object {
        private const val ARCHIVE_FILENAME = "student_archive_encrypted.json"
        private const val DEFAULT_PASSWORD = "123456"
        private const val DOUBAO_PACKAGE = "com.example.doubao"
    }
    
    private val gson = Gson()
    private val archiveDir: File by lazy {
        File(context.getExternalFilesDir(null), "").apply {
            if (!exists()) mkdirs()
        }
    }
    
    private val archiveFile: File
        get() = File(archiveDir, ARCHIVE_FILENAME)
    
    /**
     * 读取档案
     * @return 学生档案对象，如果不存在则返回null
     */
    fun readArchive(): StudentArchive? {
        return try {
            if (!archiveFile.exists()) {
                return null
            }
            
            val encryptedContent = archiveFile.readText()
            val decryptedContent = EncryptionUtil.decrypt(encryptedContent, DEFAULT_PASSWORD)
            gson.fromJson(decryptedContent, StudentArchive::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 保存档案
     * @param archive 学生档案对象
     * @return 是否保存成功
     */
    fun saveArchive(archive: StudentArchive): Boolean {
        return try {
            val jsonContent = gson.toJson(archive)
            val encryptedContent = EncryptionUtil.encrypt(jsonContent, DEFAULT_PASSWORD)
            archiveFile.writeText(encryptedContent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 检查档案是否存在
     */
    fun archiveExists(): Boolean {
        return archiveFile.exists()
    }
    
    /**
     * 获取档案文件路径
     */
    fun getArchivePath(): String {
        return archiveFile.absolutePath
    }
    
    /**
     * 复制档案到U盘
     * @param usbPath U盘路径
     * @return 是否复制成功
     */
    fun copyToUsb(usbPath: String): Boolean {
        return try {
            if (!archiveFile.exists()) {
                return false
            }
            
            val usbFile = File(usbPath, ARCHIVE_FILENAME)
            archiveFile.copyTo(usbFile, overwrite = true)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 从U盘导入档案
     * @param usbPath U盘路径
     * @return 是否导入成功
     */
    fun importFromUsb(usbPath: String): Boolean {
        return try {
            val usbFile = File(usbPath, ARCHIVE_FILENAME)
            if (!usbFile.exists()) {
                return false
            }
            
            usbFile.copyTo(archiveFile, overwrite = true)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 创建默认档案（用于测试）
     */
    fun createDefaultArchive(): StudentArchive {
        val archive = StudentArchive(
            studentId = "STU001",
            basicInfo = com.student.education.model.BasicInfo(
                name = "张三",
                gender = "男",
                grade = "五年级",
                stage = "小学",
                currentTerm = "2024春季",
                learningProgress = "正常",
                studyMode = "全日制",
                healthRestrictions = "无"
            ),
            subjectStatus = listOf(
                com.student.education.model.SubjectStatus(
                    subject = "数学",
                    textbookVersion = "人教版",
                    currentChapter = "分数除法",
                    masteryLevel = 0.6
                ),
                com.student.education.model.SubjectStatus(
                    subject = "语文",
                    textbookVersion = "部编版",
                    currentChapter = "古诗词鉴赏",
                    masteryLevel = 0.75
                )
            ),
            personalityAssessment = com.student.education.model.PersonalityAssessment(
                personalityType = "内向",
                communicationPreference = "温柔鼓励",
                stressTolerance = "中"
            ),
            weakPoints = listOf(
                com.student.education.model.WeakPoint(
                    subject = "数学",
                    knowledgePoint = "分数运算",
                    masteryLevel = 0.4,
                    errorCount = 5,
                    remedyPlan = "加强练习"
                )
            ),
            parentPreferences = com.student.education.model.ParentPreferences(
                tutoringStyle = "温和",
                maxDailyLearningTime = 90,
                reportDeliveryMethod = "微信推送"
            )
        )
        
        saveArchive(archive)
        return archive
    }
    
    /**
     * 将档案转换为JSON字符串（用于传递给豆包APP）
     */
    fun archiveToJson(archive: StudentArchive): String {
        return gson.toJson(archive)
    }
    
    /**
     * 从JSON字符串解析档案（用于接收豆包APP的总结）
     */
    fun jsonToArchive(json: String): StudentArchive? {
        return try {
            gson.fromJson(json, StudentArchive::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}