package com.student.education

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.student.education.util.ArchiveManager

/**
 * 控制界面Activity - 启动/关闭系统
 */
class ControlActivity : AppCompatActivity() {

    companion object {
        private const val DEFAULT_PASSWORD = "123456"
        const val ACTION_STOP_SYSTEM = "com.student.education.STOP_SYSTEM"
    }

    private lateinit var archiveManager: ArchiveManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 保持屏幕常亮
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        setContentView(R.layout.activity_control)

        archiveManager = ArchiveManager(this)
        
        setupUI()
    }

    private fun setupUI() {
        // 启动系统按钮
        findViewById<Button>(R.id.btnStartSystem).setOnClickListener {
            showPasswordDialog(isStart = true)
        }

        // 关闭系统按钮
        findViewById<Button>(R.id.btnStopSystem).setOnClickListener {
            showStopConfirmation()
        }
    }

    /**
     * 显示密码验证对话框
     */
    private fun showPasswordDialog(isStart: Boolean) {
        val editText = EditText(this).apply {
            hint = getString(R.string.hint_password)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or 
                        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setPadding(50, 30, 50, 30)
        }

        AlertDialog.Builder(this)
            .setTitle(if (isStart) R.string.btn_start else R.string.btn_stop)
            .setView(editText)
            .setPositiveButton(R.string.btn_confirm) { _, _ ->
                val password = editText.text.toString()
                if (password == DEFAULT_PASSWORD) {
                    if (isStart) {
                        navigateToMain()
                    } else {
                        stopSystem()
                    }
                } else {
                    Toast.makeText(this, R.string.msg_password_error, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    /**
     * 显示停止确认对话框
     */
    private fun showStopConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.btn_stop)
            .setMessage("确定要关闭系统吗？")
            .setPositiveButton(R.string.btn_confirm) { _, _ ->
                showPasswordDialog(isStart = false)
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    /**
     * 导航到主界面
     */
    private fun navigateToMain() {
        // 检查豆包APP是否安装
        if (!isAppInstalled("com.example.doubao")) {
            Toast.makeText(this, R.string.msg_doubao_not_installed, Toast.LENGTH_LONG).show()
            return
        }

        // 检查坐姿管理程序是否安装
        if (!isAppInstalled("com.student.posture")) {
            Toast.makeText(this, R.string.msg_posture_not_installed, Toast.LENGTH_LONG).show()
            return
        }

        // 检查档案是否存在
        if (!archiveManager.archiveExists()) {
            Toast.makeText(this, R.string.msg_archive_not_found, Toast.LENGTH_LONG).show()
            // 创建默认档案用于测试
            archiveManager.createDefaultArchive()
        }

        // 启动主界面
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    /**
     * 停止系统
     */
    private fun stopSystem() {
        // 发送停止广播
        val intent = Intent(ACTION_STOP_SYSTEM)
        sendBroadcast(intent)
        
        // 关闭豆包APP
        killApp("com.example.doubao")
        
        // 关闭坐姿管理程序
        killApp("com.student.posture")
        
        Toast.makeText(this, R.string.msg_system_stopped, Toast.LENGTH_SHORT).show()
    }

    /**
     * 检查APP是否安装
     */
    private fun isAppInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * 关闭指定APP
     */
    private fun killApp(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            
            Runtime.getRuntime().exec("am force-stop $packageName")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
