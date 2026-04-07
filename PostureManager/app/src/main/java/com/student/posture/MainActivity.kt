package com.student.posture

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mediapipe solutions pose.Pose
import com.google.mediapipe solutions.pose.PoseLandmark
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 坐姿管理主界面
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "PostureManager"
        private const val CAMERA_PERMISSION_REQUEST = 1001
        
        // 坐姿异常阈值
        private const val HEAD_TILT_THRESHOLD = 30.0  // 头部前倾角度阈值
        private const val SPINE_TILT_THRESHOLD = 20.0  // 脊柱弯曲角度阈值
        private const val SHOULDER_TILT_THRESHOLD = 15.0  // 双肩水平度阈值
        
        // 语音提示间隔（毫秒）
        private const val VOICE_INTERVAL = 5000L
    }

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false
    
    private var lastVoiceTime = 0L
    private var isPostureAbnormal = false
    private var isAudioFocusLost = false

    private lateinit var tvStatus: TextView
    private lateinit var tvHeadAngle: TextView
    private lateinit var tvSpineAngle: TextView
    private lateinit var tvShoulderAngle: TextView
    private lateinit var warningOverlay: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initAudio()
        initTts()
        
        cameraExecutor = Executors.newSingleThreadExecutor()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        if (checkCameraPermission()) {
            startCamera()
        } else {
            requestCameraPermission()
        }
        
        registerAudioFocusListener()
    }

    private fun initViews() {
        tvStatus = findViewById(R.id.tvStatus)
        tvHeadAngle = findViewById(R.id.tvHeadAngle)
        tvSpineAngle = findViewById(R.id.tvSpineAngle)
        tvShoulderAngle = findViewById(R.id.tvShoulderAngle)
        warningOverlay = findViewById(R.id.warningOverlay)
    }

    private fun initAudio() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(audioAttributes)
            .setAcceptsDelayedFocusGain(false)
            .setWillPauseWhenDucked(false)
            .build()
    }

    private fun initTts() {
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.CHINESE)
                isTtsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                             result != TextToSpeech.LANG_NOT_SUPPORTED
            }
        }
    }

    private fun registerAudioFocusListener() {
        val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS -> {
                    isAudioFocusLost = true
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    isAudioFocusLost = true
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    // 豆包正在说话，降低音量但不停止
                    isAudioFocusLost = true
                }
                AudioManager.AUDIOFOCUS_GAIN -> {
                    isAudioFocusLost = false
                }
            }
        }
        
        audioManager.requestAudioFocus(audioFocusRequest!!)
        audioManager.addOnAudioFocusChangeListener(focusChangeListener)
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(this, R.string.camera_permission_required, Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(findViewById<androidx.camera.view.PreviewView>(
                        R.id.previewView).surfaceProvider)
                }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, PoseAnalyzer { poseResult ->
                        runOnUiThread {
                            processPoseResult(poseResult)
                        }
                    })
                }

            // 使用前置摄像头
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis
                )
            } catch (e: Exception) {
                Log.e(TAG, "Use case binding failed", e)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * 处理姿态检测结果
     */
    private fun processPoseResult(poseResult: PoseResult) {
        // 更新角度显示
        tvHeadAngle.text = "头部: ${poseResult.headAngle.toInt()}°"
        tvSpineAngle.text = "脊柱: ${poseResult.spineAngle.toInt()}°"
        tvShoulderAngle.text = "双肩: ${poseResult.shoulderAngle.toInt()}°"
        
        // 判断坐姿是否异常
        val isAbnormal = poseResult.headAngle > HEAD_TILT_THRESHOLD ||
                         poseResult.spineAngle > SPINE_TILT_THRESHOLD ||
                         poseResult.shoulderAngle > SHOULDER_TILT_THRESHOLD
        
        if (isAbnormal && !isPostureAbnormal) {
            // 坐姿从正常变为异常
            isPostureAbnormal = true
            showWarning()
            speakWarning()
        } else if (!isAbnormal && isPostureAbnormal) {
            // 坐姿从异常变为正常
            isPostureAbnormal = false
            hideWarning()
        }
    }

    private fun showWarning() {
        tvStatus.text = getString(R.string.status_warning)
        tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_warning))
        warningOverlay.visibility = View.VISIBLE
    }

    private fun hideWarning() {
        tvStatus.text = getString(R.string.status_normal)
        tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_normal))
        warningOverlay.visibility = View.GONE
    }

    private fun speakWarning() {
        // 只有在豆包没有占用音频焦点时才语音提示
        if (isAudioFocusLost || !isTtsReady) {
            return
        }
        
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastVoiceTime < VOICE_INTERVAL) {
            return
        }
        
        lastVoiceTime = currentTime
        
        // 请求音频焦点
        val result = audioManager.requestAudioFocus(audioFocusRequest!!)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            textToSpeech?.speak(
                getString(R.string.posture_tip),
                TextToSpeech.QUEUE_FLUSH,
                null,
                "posture_warning"
            )
            
            // 释放音频焦点
            Handler(Looper.getMainLooper()).postDelayed({
                audioManager.abandonAudioFocusRequest(audioFocusRequest!!)
            }, 2000)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
    }
}

/**
 * 姿态分析器
 */
class PoseAnalyzer(
    private val onPoseDetected: (PoseResult) -> Unit
) : ImageAnalysis.Analyzer {

    override fun analyze(imageProxy: androidx.camera.core.ImageProxy) {
        // 这里简化处理，实际应该使用MediaPipe Pose进行姿态检测
        // 由于MediaPipe集成较复杂，这里提供一个简化版本
        
        // 模拟检测结果
        val result = PoseResult(
            headAngle = 0.0,  // 应该从MediaPipe计算得出
            spineAngle = 0.0,
            shoulderAngle = 0.0,
            confidence = 0.9
        )
        
        onPoseDetected(result)
        imageProxy.close()
    }
}

/**
 * 姿态检测结果
 */
data class PoseResult(
    val headAngle: Double,      // 头部前倾角度
    val spineAngle: Double,     // 脊柱弯曲角度
    val shoulderAngle: Double,  // 双肩水平度
    val confidence: Double      // 置信度
)