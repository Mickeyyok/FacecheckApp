package com.example.facecheckapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class RegScanActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var faceStatusText: TextView
    private lateinit var borderCircle: View
    private lateinit var btnRegister: Button
    private lateinit var loadingBar: ProgressBar
    private var isFaceDetected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_regscan)

        // 🔹 ผูกตัวแปรกับ XML
        previewView = findViewById(R.id.previewView)
        faceStatusText = findViewById(R.id.faceStatusText)
        borderCircle = findViewById(R.id.borderCircle)
        btnRegister = findViewById(R.id.btnRegister)
        loadingBar = findViewById(R.id.loadingBar)

        // 🔒 ขอสิทธิ์กล้อง
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        }

        // 🚀 ปุ่มลงทะเบียน
        btnRegister.setOnClickListener {
            if (isFaceDetected) {
                faceStatusText.text = "✅ ตรวจพบใบหน้าแล้ว กำลังวิเคราะห์..."
                faceStatusText.setBackgroundColor(Color.parseColor("#4CAF50"))
                loadingBar.visibility = View.VISIBLE

                // ⏳ รอ 3 วิก่อนเปลี่ยนหน้า
                Handler(Looper.getMainLooper()).postDelayed({
                    loadingBar.visibility = View.GONE
                    val intent = Intent(this, SuccessActivity::class.java)
                    startActivity(intent)
                    finish()
                }, 3000)
            } else {
                faceStatusText.text = "❌ โปรดให้กล้องตรวจจับใบหน้าก่อนลงทะเบียน"
                faceStatusText.setBackgroundColor(Color.parseColor("#F44336"))
            }
        }
    }

    // 🎥 เริ่มกล้อง
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                        analyzeImage(imageProxy)
                    }
                }

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            } catch (exc: Exception) {
                Log.e("CameraX", "กล้องทำงานผิดพลาด: ${exc.message}")
            }

        }, ContextCompat.getMainExecutor(this))
    }

    // 🎯 วิเคราะห์ใบหน้า
    private fun analyzeImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return imageProxy.close()
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .enableTracking()
            .build()

        val detector = FaceDetection.getClient(options)

        detector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    if (!isFaceDetected) {
                        Log.d("FaceCheck", "✅ ตรวจพบใบหน้า")
                        isFaceDetected = true
                        faceStatusText.text = "✅ ตรวจพบใบหน้าแล้ว!"
                        faceStatusText.setBackgroundColor(Color.parseColor("#4CAF50"))
                        borderCircle.setBackgroundResource(R.drawable.circle_green)
                    }
                } else {
                    if (isFaceDetected) {
                        Log.d("FaceCheck", "❌ ไม่พบใบหน้า")
                        isFaceDetected = false
                        faceStatusText.text = "❌ โปรดหันหน้าเข้ากล้อง"
                        faceStatusText.setBackgroundColor(Color.parseColor("#F44336"))
                        borderCircle.setBackgroundResource(R.drawable.circle_red)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FaceCheck", "การตรวจจับล้มเหลว: ${e.message}")
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            Log.e("Permission", "ไม่ได้รับอนุญาตให้ใช้กล้อง")
        }
    }
}
