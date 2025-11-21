package com.example.facecheckapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.media.Image
import android.os.Bundle
import android.util.Size
import android.widget.Button
import android.widget.ProgressBar
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
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.min
import kotlin.math.sqrt

class RegScanActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var txtStatus: TextView
    private lateinit var btnRegister: Button
    private lateinit var loading: ProgressBar

    private var faceNet: FaceNetModel? = null
    private var cameraProvider: ProcessCameraProvider? = null

    // embedding ใบหน้าล่าสุด (normalize แล้ว)
    private var lastEmbedding: FloatArray? = null

    private val auth = FirebaseAuth.getInstance()
    private var lastProcessTime = 0L

    // ⭐ background thread สำหรับงานกล้อง/AI
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_regscan)

        previewView = findViewById(R.id.previewView)
        txtStatus = findViewById(R.id.faceStatusText)
        loading = findViewById(R.id.loadingBar)
        btnRegister = findViewById(R.id.btnRegister)

        faceNet = FaceNetModel(this)
        cameraExecutor = Executors.newSingleThreadExecutor()

        requestPermission()

        btnRegister.setOnClickListener {
            val emb = lastEmbedding
            if (emb == null) {
                txtStatus.text = "❌ ยังจับใบหน้าไม่ได้ / ใกล้กล้องอีกนิด"
            } else {
                saveFace(emb)
            }
        }
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 110)
        } else {
            startCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 110 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            Toast.makeText(this, "ต้องการสิทธิ์กล้องเพื่อสแกนหน้า", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCamera() {

        val detectorOptions = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .build()
        val detector: FaceDetector = FaceDetection.getClient(detectorOptions)

        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            cameraProvider = future.get()
            val provider = cameraProvider!!

            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            val analysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(640, 480))                // ลดขนาดภาพ
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()

            analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                processImageProxy(imageProxy, detector)
            }

            provider.unbindAll()
            provider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                preview,
                analysis
            )
        }, ContextCompat.getMainExecutor(this))
    }

    private fun processImageProxy(
        imageProxy: ImageProxy,
        detector: FaceDetector
    ) {
        val now = System.currentTimeMillis()
        if (now - lastProcessTime < 400) {       // ประมวลผลทุก ~0.4 วินาที
            imageProxy.close()
            return
        }
        lastProcessTime = now

        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val input = InputImage.fromMediaImage(mediaImage, rotationDegrees)

        detector.process(input)
            .addOnSuccessListener(cameraExecutor) { faces ->
                if (faces.isEmpty()) {
                    lastEmbedding = null
                    txtStatus.post { txtStatus.text = "ไม่พบใบหน้า" }
                    imageProxy.close()
                    return@addOnSuccessListener
                }

                val bmp = mediaImageToBitmap(mediaImage, rotationDegrees)
                val face = chooseLargestFace(faces)
                val box = face.boundingBox

                val faceArea = box.width().toFloat() * box.height().toFloat()
                val frameArea = bmp.width.toFloat() * bmp.height.toFloat()
                if (faceArea / frameArea < 0.01f) {
                    lastEmbedding = null
                    txtStatus.post { txtStatus.text = "เข้าใกล้กล้องอีกนิด" }
                    imageProxy.close()
                    return@addOnSuccessListener
                }

                val crop = cropFace(bmp, box)
                val rawEmb = faceNet?.getEmbedding(crop)
                if (rawEmb == null) {
                    lastEmbedding = null
                    txtStatus.post { txtStatus.text = "อ่านใบหน้าไม่ได้" }
                } else {
                    lastEmbedding = normalize(rawEmb)
                    txtStatus.post { txtStatus.text = "พร้อมบันทึก (ถือให้นิ่งแล้วกดปุ่ม)" }
                }

                imageProxy.close()
            }
            .addOnFailureListener(cameraExecutor) {
                lastEmbedding = null
                imageProxy.close()
            }
    }

    private fun saveFace(embedding: FloatArray) {
        val uid = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "ยังไม่ได้ล็อกอิน", Toast.LENGTH_SHORT).show()
            return
        }

        loading.visibility = ProgressBar.VISIBLE
        txtStatus.text = "กำลังบันทึกใบหน้า..."

        FirebaseDatabase.getInstance().reference
            .child("users")
            .child(uid)
            .child("faceEmbedding")
            .setValue(embedding.toList())
            .addOnSuccessListener {
                loading.visibility = ProgressBar.GONE
                Toast.makeText(this, "ลงทะเบียนใบหน้าสำเร็จ!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                loading.visibility = ProgressBar.GONE
                Toast.makeText(this, "บันทึกไม่สำเร็จ", Toast.LENGTH_SHORT).show()
            }
    }

    // ---------- Utils: แปลงภาพ / crop หน้า / normalize -------------

    private fun mediaImageToBitmap(img: Image, rotation: Int): Bitmap {
        val yBuffer = img.planes[0].buffer
        val uBuffer = img.planes[1].buffer
        val vBuffer = img.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuv = YuvImage(nv21, ImageFormat.NV21, img.width, img.height, null)
        val out = ByteArrayOutputStream()
        yuv.compressToJpeg(Rect(0, 0, img.width, img.height), 80, out)
        var bmp = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size())

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

        val faceBmp = Bitmap.createBitmap(bmp, left, top, right - left, bottom - top)

        val size = min(faceBmp.width, faceBmp.height)
        val square = Bitmap.createBitmap(
            faceBmp,
            (faceBmp.width - size) / 2,
            (faceBmp.height - size) / 2,
            size,
            size
        )

        return Bitmap.createScaledBitmap(square, 160, 160, true)
    }

    private fun chooseLargestFace(faces: List<Face>): Face {
        var best = faces[0]
        var bestArea = 0
        for (f in faces) {
            val b = f.boundingBox
            val area = b.width() * b.height()
            if (area > bestArea) {
                bestArea = area
                best = f
            }
        }
        return best
    }

    private fun normalize(arr: FloatArray): FloatArray {
        var sum = 0f
        for (v in arr) sum += v * v
        val norm = sqrt(sum)
        if (norm == 0f) return arr
        val out = FloatArray(arr.size)
        for (i in arr.indices) out[i] = arr[i] / norm
        return out
    }

    override fun onPause() {
        super.onPause()
        try { cameraProvider?.unbindAll() } catch (_: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        try { cameraProvider?.unbindAll() } catch (_: Exception) {}
        cameraExecutor.shutdown()
    }
}
