package com.student.education.model

import com.google.gson.annotations.SerializedName

/**
 * 学生档案数据模型
 */
data class StudentArchive(
    @SerializedName("student_id")
    val studentId: String = "",
    
    @SerializedName("basic_info")
    val basicInfo: BasicInfo = BasicInfo(),
    
    @SerializedName("subject_status")
    val subjectStatus: List<SubjectStatus> = emptyList(),
    
    @SerializedName("personality_assessment")
    val personalityAssessment: PersonalityAssessment = PersonalityAssessment(),
    
    @SerializedName("weak_points")
    val weakPoints: List<WeakPoint> = emptyList(),
    
    @SerializedName("parent_preferences")
    val parentPreferences: ParentPreferences = ParentPreferences(),
    
    @SerializedName("learning_records")
    val learningRecords: List<LearningRecord> = emptyList(),
    
    @SerializedName("posture_records")
    val postureRecords: List<PostureRecord> = emptyList()
)

/**
 * 基础信息
 */
data class BasicInfo(
    @SerializedName("name")
    val name: String = "",
    
    @SerializedName("gender")
    val gender: String = "",
    
    @SerializedName("grade")
    val grade: String = "",
    
    @SerializedName("stage")
    val stage: String = "",
    
    @SerializedName("current_term")
    val currentTerm: String = "",
    
    @SerializedName("learning_progress")
    val learningProgress: String = "",
    
    @SerializedName("study_mode")
    val studyMode: String = "",
    
    @SerializedName("health_restrictions")
    val healthRestrictions: String = ""
)

/**
 * 学科状态
 */
data class SubjectStatus(
    @SerializedName("subject")
    val subject: String = "",
    
    @SerializedName("textbook_version")
    val textbookVersion: String = "",
    
    @SerializedName("current_chapter")
    val currentChapter: String = "",
    
    @SerializedName("mastery_level")
    val masteryLevel: Double = 0.0
)

/**
 * 性格评估
 */
data class PersonalityAssessment(
    @SerializedName("personality_type")
    val personalityType: String = "",
    
    @SerializedName("communication_preference")
    val communicationPreference: String = "",
    
    @SerializedName("stress_tolerance")
    val stressTolerance: String = ""
)

/**
 * 薄弱知识点
 */
data class WeakPoint(
    @SerializedName("subject")
    val subject: String = "",
    
    @SerializedName("knowledge_point")
    val knowledgePoint: String = "",
    
    @SerializedName("mastery_level")
    val masteryLevel: Double = 0.0,
    
    @SerializedName("error_count")
    val errorCount: Int = 0,
    
    @SerializedName("remedy_plan")
    val remedyPlan: String = ""
)

/**
 * 家长偏好
 */
data class ParentPreferences(
    @SerializedName("tutoring_style")
    val tutoringStyle: String = "",
    
    @SerializedName("max_daily_learning_time")
    val maxDailyLearningTime: Int = 60,
    
    @SerializedName("report_delivery_method")
    val reportDeliveryMethod: String = ""
)

/**
 * 学习记录
 */
data class LearningRecord(
    @SerializedName("date")
    val date: String = "",
    
    @SerializedName("subject")
    val subject: String = "",
    
    @SerializedName("content")
    val content: String = "",
    
    @SerializedName("duration")
    val duration: Int = 0,
    
    @SerializedName("summary")
    val summary: String = ""
)

/**
 * 坐姿记录
 */
data class PostureRecord(
    @SerializedName("date")
    val date: String = "",
    
    @SerializedName("correction_count")
    val correctionCount: Int = 0,
    
    @SerializedName("average_posture_score")
    val averagePostureScore: Double = 0.0
)