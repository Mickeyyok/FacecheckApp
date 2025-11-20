package com.example.facecheckapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class RegScanActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var txtStatus: TextView
    private lateinit var btnRegister: Button
    private lateinit var loading: ProgressBar

    private var faceNet: FaceNetModel? = null
    private var lastBox: Rect? = null

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_regscan)

        previewView = findViewById(R.id.previewView)
        txtStatus = findViewById(R.id.faceStatusText)
        loading = findViewById(R.id.loadingBar)
        btnRegister = findViewById(R.id.btnRegister)

        faceNet = FaceNetModel(this)

        requestPermission()

        btnRegister.setOnClickListener {
            if (lastBox == null) {
                txtStatus.text = "❌ ไม่พบใบหน้า"
                return@setOnClickListener
            }
            saveFace()
        }
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 110)
        } else startCamera()
    }

    override fun onRequestPermissionsResult(req: Int, p: Array<out String>, result: IntArray) {
        if (result.isNotEmpty() && result[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        }
    }

    private fun startCamera() {
        val detector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST).build()
        )

        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            val provider = future.get()

            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            val analyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analyzer.setAnalyzer(ContextCompat.getMainExecutor(this)) { proxy ->

                val media = proxy.image ?: return@setAnalyzer proxy.close()
                val img = InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees)

                detector.process(img)
                    .addOnSuccessListener { faces ->
                        if (faces.isNotEmpty()) {
                            txtStatus.text = "พบใบหน้า ✔"
                            lastBox = faces[0].boundingBox
                        } else {
                            txtStatus.text = "ไม่พบใบหน้า"
                            lastBox = null
                        }
                    }
                    .addOnCompleteListener { proxy.close() }
            }

            provider.unbindAll()
            provider.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA, preview, analyzer)
        }, ContextCompat.getMainExecutor(this))
    }

    // ⭐ Crop + Padding
    private fun cropFace(bitmap: Bitmap, box: Rect): Bitmap {
        val pad = (box.width() * 0.2f).toInt()

        val left = (box.left - pad).coerceAtLeast(0)
        val top = (box.top - pad).coerceAtLeast(0)
        val right = (box.right + pad).coerceAtMost(bitmap.width)
        val bottom = (box.bottom + pad).coerceAtMost(bitmap.height)

        return Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top)
    }

    private fun saveFace() {
        val uid = auth.currentUser?.uid ?: return
        val bmp = previewView.bitmap ?: return
        val box = lastBox ?: return

        loading.visibility = ProgressBar.VISIBLE

        val cropped = cropFace(bmp, box)
        val embedding = faceNet!!.getEmbedding(cropped)

        FirebaseDatabase.getInstance().reference
            .child("users/$uid/faceEmbedding")
            .setValue(embedding.toList())
            .addOnSuccessListener {
                Toast.makeText(this, "ลงทะเบียนสำเร็จ!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
    }
}
