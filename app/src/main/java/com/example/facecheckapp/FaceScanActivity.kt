package com.example.facecheckapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.media.Image
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

class FaceScanActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var txtStatus: TextView
    private lateinit var btnStartScan: Button

    private lateinit var faceNet: FaceNetModel
    private var registeredEmbed: FloatArray? = null

    private val auth = FirebaseAuth.getInstance()
    private var cameraProvider: ProcessCameraProvider? = null

    private var classId: String? = null
    private var endTime: String? = null

    private var lastProcessTime = 0L
    private var isProcessing = false  // ป้องกันสแกนซ้ำ

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_scan)

        previewView = findViewById(R.id.previewView)
        txtStatus = findViewById(R.id.faceStatusText)
        btnStartScan = findViewById(R.id.btnStartScan)

        faceNet = FaceNetModel(this)

        classId = intent.getStringExtra("classId")

        if (classId == null) {
            Toast.makeText(this, "เกิดข้อผิดพลาด: classId = null", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        loadClassEndTime()
        loadRegisteredEmbedding()

        btnStartScan.setOnClickListener { requestCamera() }
    }

    private fun loadClassEndTime() {
        val ref = FirebaseDatabase.getInstance().reference.child("classes/$classId")
        ref.get().addOnSuccessListener {
            endTime = it.child("endTime").value?.toString()
        }
    }

    private fun loadRegisteredEmbedding() {
        val uid = auth.currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().reference.child("users/$uid/faceEmbedding")

        txtStatus.text = "กำลังโหลดข้อมูลใบหน้า..."

        ref.get().addOnSuccessListener {
            val list = it.value as? List<Double> ?: return@addOnSuccessListener
            registeredEmbed = list.map { v -> v.toFloat() }.toFloatArray()
            txtStatus.text = "พร้อมเริ่มสแกน"
        }
    }

    private fun requestCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 200)
        } else startCamera()
    }

    private fun startCamera() {

        if (!checkTimeAllowed()) return

        txtStatus.text = "กำลังเปิดกล้อง..."

        val detector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .build()
        )

        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({

            cameraProvider = future.get()
            val provider = cameraProvider!!

            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            val analyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analyzer.setAnalyzer(ContextCompat.getMainExecutor(this)) { proxy ->

                if (isProcessing) { proxy.close(); return@setAnalyzer }

                val now = System.currentTimeMillis()
                if (now - lastProcessTime < 600) {
                    proxy.close()
                    return@setAnalyzer
                }
                lastProcessTime = now

                val reg = registeredEmbed ?: return@setAnalyzer
                val mediaImage = proxy.image ?: return@setAnalyzer proxy.close()

                val bitmap = mediaImageToBitmap(mediaImage, proxy.imageInfo.rotationDegrees)
                val inputImage = InputImage.fromBitmap(bitmap, 0)

                detector.process(inputImage)
                    .addOnSuccessListener { faces ->
                        if (faces.isNotEmpty()) {
                            txtStatus.text = "กำลังตรวจสอบใบหน้า..."

                            val box = faces[0].boundingBox
                            val faceBmp = cropFace(bitmap, box)

                            val emb = faceNet.getEmbedding(faceBmp)
                            val dist = l2(emb, reg)

                            if (dist < 1.35f) {
                                isProcessing = true
                                goSuccess()
                            }
                        } else {
                            txtStatus.text = "ไม่พบใบหน้า"
                        }
                    }
                    .addOnCompleteListener { proxy.close() }
            }

            provider.unbindAll()
            provider.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA, preview, analyzer)

        }, ContextCompat.getMainExecutor(this))
    }

    private fun mediaImageToBitmap(img: Image, rotation: Int): Bitmap {
        val y = img.planes[0].buffer
        val u = img.planes[1].buffer
        val v = img.planes[2].buffer

        val ySize = y.remaining()
        val uSize = u.remaining()
        val vSize = v.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        y.get(nv21, 0, ySize)
        v.get(nv21, ySize, vSize)
        u.get(nv21, ySize + vSize, uSize)

        val yuv = YuvImage(nv21, ImageFormat.NV21, img.width, img.height, null)
        val out = ByteArrayOutputStream()
        yuv.compressToJpeg(Rect(0, 0, img.width, img.height), 80, out)

        val bytes = out.toByteArray()
        var bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        val matrix = Matrix()
        matrix.postRotate(rotation.toFloat())
        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)

        return bmp
    }

    private fun cropFace(bmp: Bitmap, box: Rect): Bitmap {
        val pad = (box.width() * 0.2f).toInt()
        val left = (box.left - pad).coerceAtLeast(0)
        val top = (box.top - pad).coerceAtLeast(0)
        val right = (box.right + pad).coerceAtMost(bmp.width)
        val bottom = (box.bottom + pad).coerceAtMost(bmp.height)
        return Bitmap.createBitmap(bmp, left, top, right - left, bottom - top)
    }

    private fun checkTimeAllowed(): Boolean {
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val now = sdf.parse(getCurrentTime())
            val end = sdf.parse(endTime ?: return true)
            if (now.after(end)) {
                txtStatus.text = "หมดเวลาเช็คชื่อ"
                Toast.makeText(this, "เกินเวลาเช็คชื่อแล้ว!", Toast.LENGTH_LONG).show()
                false
            } else true
        } catch (_: Exception) {
            true
        }
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    private fun l2(a: FloatArray, b: FloatArray): Float {
        var sum = 0f
        for (i in a.indices) sum += (a[i] - b[i]).pow(2)
        return sqrt(sum)
    }

    private fun goSuccess() {
        releaseCamera()

        val intent = Intent(this, CheckinSuccessActivity::class.java)
        intent.putExtra("classId", classId)
        intent.putExtra("checkinTime", getCurrentTime())

        startActivity(intent)
        finish()
    }

    private fun releaseCamera() {
        try { cameraProvider?.unbindAll() } catch (_: Exception) {}
        cameraProvider = null
    }

    override fun onPause() {
        super.onPause()
        releaseCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseCamera()
    }
}
