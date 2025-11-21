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
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.*

class FaceScanActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var txtStatus: TextView
    private lateinit var btnStartScan: Button

    private lateinit var faceNet: FaceNetModel
    private var registeredEmbed: FloatArray? = null

    private var cameraProvider: ProcessCameraProvider? = null
    private val auth = FirebaseAuth.getInstance()

    private var classId = ""
    private var className = ""
    private var subjectCode = ""
    private var classRoom = ""
    private var classTime = ""
    private var dayTime = ""
    private var endTime = ""
    private var lateTime = ""

    private var lastProcess = 0L
    @Volatile private var isProcessing = false

    // ⭐ Executor สำหรับงานกล้อง
    private lateinit var cameraExecutor: ExecutorService

    // นับจำนวนเฟรมที่ "หน้าเหมือน" เพื่อลด false positive
    private var successCount = 0
    private val REQUIRED_SUCCESS_FRAME = 3   // ต้องตรงกันกี่เฟรมติดกันค่อยให้ผ่าน
    private val PROCESS_INTERVAL_MS = 700L   // หน่วงการประมวลผลแต่ละเฟรม (ช่วยลดกระตุก)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_scan)

        previewView = findViewById(R.id.previewView)
        txtStatus = findViewById(R.id.faceStatusText)
        btnStartScan = findViewById(R.id.btnStartScan)

        faceNet = FaceNetModel(this)
        cameraExecutor = Executors.newSingleThreadExecutor()

        classId = intent.getStringExtra("classId") ?: ""
        className = intent.getStringExtra("className") ?: "-"
        subjectCode = intent.getStringExtra("subjectCode") ?: "-"
        classRoom = intent.getStringExtra("classRoom") ?: "-"
        classTime = intent.getStringExtra("classTime") ?: "-"
        dayTime = intent.getStringExtra("dayTime") ?: "-"

        loadClassTimes()
        loadFaceEmbedding()

        btnStartScan.setOnClickListener { requestCamera() }
    }

    // -------------------------- LOAD DATA -----------------------------

    private fun loadClassTimes() {
        val ref = FirebaseDatabase.getInstance().reference.child("classes/$classId")

        ref.get().addOnSuccessListener {
            endTime = it.child("endTime").value?.toString() ?: ""
            lateTime = it.child("lateTime").value?.toString() ?: ""
        }
    }

    private fun loadFaceEmbedding() {
        val user = auth.currentUser
        if (user == null) {
            txtStatus.text = "ยังไม่ได้ล็อกอิน"
            return
        }

        val uid = user.uid
        val ref = FirebaseDatabase.getInstance()
            .reference.child("users/$uid/faceEmbedding")

        txtStatus.text = "กำลังโหลดข้อมูลใบหน้า..."

        ref.get().addOnSuccessListener { snap ->
            val value = snap.value ?: run {
                registeredEmbed = null
                txtStatus.text = "ยังไม่มีข้อมูลใบหน้า"
                return@addOnSuccessListener
            }

            val list = value as? List<*> ?: run {
                registeredEmbed = null
                txtStatus.text = "ข้อมูลใบหน้าไม่ถูกต้อง"
                return@addOnSuccessListener
            }

            val floats = FloatArray(list.size)
            for (i in list.indices) {
                val v = list[i]
                floats[i] = when (v) {
                    is Double -> v.toFloat()
                    is Long -> v.toFloat()
                    is Int -> v.toFloat()
                    is Float -> v
                    else -> 0f
                }
            }

            registeredEmbed = normalize(floats)
            txtStatus.text = "พร้อมสแกน"
        }.addOnFailureListener {
            registeredEmbed = null
            txtStatus.text = "โหลดข้อมูลผิดพลาด"
        }
    }

    // -------------------------- CAMERA -----------------------------

    private fun requestCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 200)
        } else startCamera()
    }


    private fun startCamera() {

        if (registeredEmbed == null) {
            Toast.makeText(this, "ยังไม่มีข้อมูลใบหน้าที่ลงทะเบียน", Toast.LENGTH_SHORT).show()
            txtStatus.text = "กรุณาลงทะเบียนใบหน้าก่อน"
            return
        }

        txtStatus.text = "กำลังเปิดกล้อง..."

        val detector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
                .build()
        )

        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({

            cameraProvider = future.get()
            val provider = cameraProvider!!

            val preview = Preview.Builder()
                .build()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()

            analysis.setAnalyzer(cameraExecutor) { proxy ->

                if (isProcessing) {
                    proxy.close(); return@setAnalyzer
                }

                val now = System.currentTimeMillis()
                if (now - lastProcess < PROCESS_INTERVAL_MS) {
                    proxy.close(); return@setAnalyzer
                }
                lastProcess = now

                val mediaImage = proxy.image
                if (mediaImage == null) {
                    proxy.close(); return@setAnalyzer
                }

                val bmp = try {
                    mediaImageToBitmap(mediaImage, proxy.imageInfo.rotationDegrees)
                } catch (e: Exception) {
                    proxy.close(); return@setAnalyzer
                }

                val input = InputImage.fromBitmap(bmp, 0)
                detector.process(input)
                    .addOnSuccessListener { faces ->

                        if (faces.isEmpty()) {
                            txtStatus.post { txtStatus.text = "ไม่พบใบหน้า" }
                            successCount = 0
                            proxy.close()
                            return@addOnSuccessListener
                        }

                        val face = chooseLargestFace(faces)
                        val bbox = face.boundingBox

                        val faceArea = bbox.width().toFloat() * bbox.height().toFloat()
                        val frameArea = bmp.width.toFloat() * bmp.height.toFloat()
                        if (faceArea / frameArea < 0.008f) { // ปรับเป็น 0.8% ของภาพ
                            txtStatus.post { txtStatus.text = "ใบหน้าเล็กไป ใกล้กล้องอีกนิด" }
                            successCount = 0
                            proxy.close()
                            return@addOnSuccessListener
                        }

                        val crop = cropFace(bmp, bbox)
                        val emb = faceNet.getEmbedding(crop)

                        val reg = registeredEmbed
                        if (reg == null) {
                            txtStatus.post { txtStatus.text = "ไม่มีข้อมูลลงทะเบียน" }
                            successCount = 0
                            proxy.close()
                            return@addOnSuccessListener
                        }

                        val same = isSamePerson(emb, reg)

                        if (same) {
                            successCount++
                            txtStatus.post {
                                txtStatus.text = "ใบหน้าตรงกับที่ลงทะเบียน ($successCount/$REQUIRED_SUCCESS_FRAME)"
                            }

                            if (successCount >= REQUIRED_SUCCESS_FRAME && !isProcessing) {
                                isProcessing = true
                                proxy.close()
                                finishScan()
                                return@addOnSuccessListener
                            }
                        } else {
                            successCount = 0
                            txtStatus.post {
                                txtStatus.text = "ใบหน้าไม่ตรง ลองจัดหน้าใหม่"
                            }
                        }

                        proxy.close()
                    }
                    .addOnFailureListener {
                        proxy.close()
                    }
            }

            provider.unbindAll()
            provider.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA, preview, analysis)

        }, ContextCompat.getMainExecutor(this))
    }

    // ------------------------- CHECK-IN LOGIC ---------------------------

    private fun finishScan() {
        releaseCamera()

        val checkin = getTime()

        val status = when {
            toMinutes(checkin) > toMinutes(endTime) -> "ขาด"
            toMinutes(checkin) > toMinutes(lateTime) -> "มาสาย"
            else -> "ตรงเวลา"
        }

        val i = Intent(this, CheckinSuccessActivity::class.java)
        i.putExtra("classId", classId)
        i.putExtra("className", className)
        i.putExtra("subjectCode", subjectCode)
        i.putExtra("classRoom", classRoom)
        i.putExtra("classTime", classTime)
        i.putExtra("dayTime", dayTime)
        i.putExtra("endTime", endTime)
        i.putExtra("lateTime", lateTime)
        i.putExtra("status", status)
        i.putExtra("checkinTime", checkin)

        startActivity(i)
        finish()
    }

    private fun getTime(): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    private fun toMinutes(hhmm: String): Int {
        return try {
            val parts = hhmm.split(":")
            val h = parts[0].toInt()
            val m = parts[1].toInt()
            h * 60 + m
        } catch (e: Exception) {
            0
        }
    }

    // --------------------------- IMAGE UTILS ----------------------------

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
        val pad = (box.width() * 0.2).toInt()

        val left = (box.left - pad).coerceAtLeast(0)
        val top = (box.top - pad).coerceAtLeast(0)
        val right = (box.right + pad).coerceAtMost(bmp.width)
        val bottom = (box.bottom + pad).coerceAtMost(bmp.height)

        return Bitmap.createBitmap(bmp, left, top, right - left, bottom - top)
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

    // --------------------------- EMBED UTILS ----------------------------

    // normalize ให้เป็นเวกเตอร์หน่วย
    private fun normalize(arr: FloatArray): FloatArray {
        var sum = 0f
        for (v in arr) sum += v * v
        val norm = sqrt(sum)
        if (norm == 0f) return arr
        val out = FloatArray(arr.size)
        for (i in arr.indices) out[i] = arr[i] / norm
        return out
    }

    private fun l2Distance(a: FloatArray, b: FloatArray): Float {
        val n = min(a.size, b.size)
        var sum = 0f
        for (i in 0 until n) {
            val d = a[i] - b[i]
            sum += d * d
        }
        return sqrt(sum)
    }

    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        val n = min(a.size, b.size)
        var s = 0f
        for (i in 0 until n) {
            s += a[i] * b[i]
        }
        return s
    }

    /** ฟังก์ชันตัดสินว่าเป็นคนเดียวกันหรือไม่ */
    private fun isSamePerson(currentEmb: FloatArray, registeredEmb: FloatArray): Boolean {
        val a = normalize(currentEmb)
        val b = normalize(registeredEmb)

        val cos = cosineSimilarity(a, b)
        val l2  = l2Distance(a, b)

        // ถ้าแสงเปลี่ยนแล้วไม่ผ่าน ลองลด cos ลงมานิดหน่อย / เพิ่ม L2 ขึ้นหน่อยก็ได้
        val COS_THRESHOLD = 0.75f   // เดิมอาจตั้งไว้ 0.8
        val L2_THRESHOLD  = 1.05f   // เดิมอาจตั้งไว้ 0.9

        return (cos >= COS_THRESHOLD) && (l2 <= L2_THRESHOLD)
    }


    // --------------------------- LIFECYCLE ----------------------------

    private fun releaseCamera() {
        try { cameraProvider?.unbindAll() } catch (_: Exception) {}
    }

    override fun onPause() {
        super.onPause()
        releaseCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseCamera()
        cameraExecutor.shutdown()
    }
}
