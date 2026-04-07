package com.student.education

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.student.education.model.StudentArchive
import com.student.education.service.ReportScheduler
import com.student.education.util.ArchiveManager

/**
 * 主界面Activity
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val DOUBAO_PACKAGE = "com.example.doubao"
        private const val DOUBAO_ACTIVITY = "com.example.doubao.MainActivity"
        private const val POSTURE_PACKAGE = "com.student.posture"
        private const val POSTURE_ACTIVITY = "com.student.posture.MainActivity"
        private const val REQUEST_STORAGE_PERMISSION = 1001
    }

    private lateinit var archiveManager: ArchiveManager
    private lateinit var reportScheduler: ReportScheduler
    private var currentArchive: StudentArchive? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 保持屏幕常亮
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        setContentView(R.layout.activity_main)

        archiveManager = ArchiveManager(this)
        reportScheduler = ReportScheduler(this)
        
        // 加载档案
        loadArchive()
        
        // 启动定时报告推送
        reportScheduler.startAllSchedules()
        
        setupUI()
        registerStopReceiver()
    }

    private fun loadArchive() {
        currentArchive = archiveManager.readArchive()
        if (currentArchive == null) {
            // 创建默认档案
            currentArchive = archiveManager.createDefaultArchive()
        }
    }

    private fun setupUI() {
        // 作业辅导按钮 - 同时启动豆包和坐姿管理
        findViewById<Button>(R.id.btnHomework).setOnClickListener {
            startHomeworkMode()
        }

        // 坐姿管理按钮
        findViewById<Button>(R.id.btnPosture).setOnClickListener {
            startPostureManager()
        }

        // 档案查看按钮
        findViewById<Button>(R.id.btnArchive).setOnClickListener {
            viewArchive()
        }

        // U盘拷贝按钮
        findViewById<Button>(R.id.btnUsb).setOnClickListener {
            showUsbOptions()
        }

        // 设置按钮
        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            showSettings()
        }

        // 报告推送按钮
        findViewById<Button>(R.id.btnReportPush).setOnClickListener {
            showReportPushOptions()
        }
    }

    /**
     * 启动作业辅导模式（同时启动豆包和坐姿管理）
     */
    private fun startHomeworkMode() {
        // 检查档案
        val archive = currentArchive
        if (archive == null) {
            Toast.makeText(this, R.string.msg_archive_not_found, Toast.LENGTH_SHORT).show()
            return
        }

        // 启动豆包APP并传递档案信息
        startDoubao(archive)
        
        // 启动坐姿管理程序
        startPostureManager()

        Toast.makeText(this, R.string.msg_system_running, Toast.LENGTH_SHORT).show()
    }

    /**
     * 启动豆包APP
     */
    private fun startDoubao(archive: StudentArchive) {
        try {
            val intent = Intent().apply {
                setClassName(DOUBAO_PACKAGE, DOUBAO_ACTIVITY)
                // 传递档案信息
                putExtra("student_archive", archiveManager.archiveToJson(archive))
                putExtra("student_name", archive.basicInfo.name)
                putExtra("student_grade", archive.basicInfo.grade)
                putExtra("student_stage", archive.basicInfo.stage)
                putExtra("personality_type", archive.personalityAssessment.personalityType)
                putExtra("communication_preference", archive.personalityAssessment.communicationPreference)
                putExtra("tutoring_style", archive.parentPreferences.tutoringStyle)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, R.string.msg_doubao_not_installed, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * 启动坐姿管理程序
     */
    private fun startPostureManager() {
        try {
            val intent = Intent().apply {
                setClassName(POSTURE_PACKAGE, POSTURE_ACTIVITY)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, R.string.msg_posture_not_installed, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * 查看档案
     */
    private fun viewArchive() {
        val intent = Intent(this, ArchiveActivity::class.java)
        startActivity(intent)
    }

    /**
     * 显示U盘操作选项
     */
    private fun showUsbOptions() {
        val options = arrayOf("导出到U盘", "从U盘导入")
        
        AlertDialog.Builder(this)
            .setTitle("U盘操作")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> exportToUsb()
                    1 -> importFromUsb()
                }
            }
            .show()
    }

    /**
     * 导出到U盘
     */
    private fun exportToUsb() {
        if (!archiveManager.archiveExists()) {
            Toast.makeText(this, R.string.msg_archive_not_found, Toast.LENGTH_SHORT).show()
            return
        }

        // 检查存储权限
        if (!checkStoragePermission()) {
            requestStoragePermission()
            return
        }

        // 获取U盘路径（简化处理，实际应该检测U盘挂载）
        val usbPath = Environment.getExternalStorageDirectory().absolutePath + "/USB"
        
        if (archiveManager.copyToUsb(usbPath)) {
            Toast.makeText(this, R.string.msg_copy_success, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, R.string.msg_copy_failed, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 从U盘导入
     */
    private fun importFromUsb() {
        // 检查存储权限
        if (!checkStoragePermission()) {
            requestStoragePermission()
            return
        }

        // 获取U盘路径
        val usbPath = Environment.getExternalStorageDirectory().absolutePath + "/USB"
        
        if (archiveManager.importFromUsb(usbPath)) {
            Toast.makeText(this, "导入成功", Toast.LENGTH_SHORT).show()
            loadArchive() // 重新加载档案
        } else {
            Toast.makeText(this, "导入失败，请检查U盘", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 检查存储权限
     */
    private fun checkStoragePermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == 
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * 请求存储权限
     */
    private fun requestStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivityForResult(intent, REQUEST_STORAGE_PERMISSION)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivityForResult(intent, REQUEST_STORAGE_PERMISSION)
            }
        } else {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                REQUEST_STORAGE_PERMISSION
            )
        }
    }

    /**
     * 显示设置
     */
    private fun showSettings() {
        Toast.makeText(this, "设置功能开发中", Toast.LENGTH_SHORT).show()
    }

    /**
     * 显示报告推送选项
     */
    private fun showReportPushOptions() {
        val options = arrayOf(
            "立即推送日报",
            "立即推送周报",
            "立即推送月报",
            "配置家长微信",
            "查看推送状态"
        )

        AlertDialog.Builder(this)
            .setTitle("家长报告推送")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> pushDailyReport()
                    1 -> pushWeeklyReport()
                    2 -> pushMonthlyReport()
                    3 -> configureParentWechat()
                    4 -> showPushStatus()
                }
            }
            .show()
    }

    /**
     * 推送日报
     */
    private fun pushDailyReport() {
        Toast.makeText(this, "正在生成并推送日报...", Toast.LENGTH_SHORT).show()
        reportScheduler.pushDailyNow { success ->
            runOnUiThread {
                if (success) {
                    Toast.makeText(this, "日报推送成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "日报推送失败，请检查网络和家长微信配置", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * 推送周报
     */
    private fun pushWeeklyReport() {
        Toast.makeText(this, "正在生成并推送周报...", Toast.LENGTH_SHORT).show()
        reportScheduler.pushWeeklyNow { success ->
            runOnUiThread {
                if (success) {
                    Toast.makeText(this, "周报推送成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "周报推送失败，请检查网络和家长微信配置", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * 推送月报
     */
    private fun pushMonthlyReport() {
        Toast.makeText(this, "正在生成并推送月报...", Toast.LENGTH_SHORT).show()
        reportScheduler.pushMonthlyNow { success ->
            runOnUiThread {
                if (success) {
                    Toast.makeText(this, "月报推送成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "月报推送失败，请检查网络和家长微信配置", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * 配置家长微信
     */
    private fun configureParentWechat() {
        val editText = android.widget.EditText(this).apply {
            hint = "请输入家长微信OpenID"
        }

        AlertDialog.Builder(this)
            .setTitle("配置家长微信")
            .setMessage("请输入家长的微信OpenID，用于接收学习报告")
            .setView(editText)
            .setPositiveButton("保存") { _, _ ->
                val openId = editText.text.toString().trim()
                if (openId.isNotEmpty()) {
                    val service = com.student.education.service.WechatPushService(this)
                    service.saveParentOpenId(openId)
                    Toast.makeText(this, "家长微信配置成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "OpenID不能为空", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 显示推送状态
     */
    private fun showPushStatus() {
        val service = com.student.education.service.WechatPushService(this)
        val parentOpenId = service.getStoredParentOpenId()

        val message = if (parentOpenId.isNullOrEmpty()) {
            "家长微信未配置，请先配置家长微信OpenID\n\n" +
            "定时推送设置：\n" +
            "• 日报：每天21:00推送\n" +
            "• 周报：每周日20:00推送\n" +
            "• 月报：每月1号19:00推送"
        } else {
            "家长微信已配置\n" +
            "• 日报：${if (service.isDailyReportPushed()) "今日已推送" else "今日未推送"}\n" +
            "• 周报：${if (service.isWeeklyReportPushed()) "本周已推送" else "本周未推送"}\n" +
            "• 月报：${if (service.isMonthlyReportPushed()) "本月已推送" else "本月未推送"}\n\n" +
            "定时推送设置：\n" +
            "• 日报：每天21:00推送\n" +
            "• 周报：每周日20:00推送\n" +
            "• 月报：每月1号19:00推送"
        }

        AlertDialog.Builder(this)
            .setTitle("推送状态")
            .setMessage(message)
            .setPositiveButton("确定", null)
            .show()
    }

    /**
     * 注册停止系统广播接收器
     */
    private fun registerStopReceiver() {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == ControlActivity.ACTION_STOP_SYSTEM) {
                    finish()
                }
            }
        }
        registerReceiver(receiver, IntentFilter(ControlActivity.ACTION_STOP_SYSTEM))
    }

    override fun onDestroy() {
        super.onDestroy()
        // 移除屏幕常亮标志
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}