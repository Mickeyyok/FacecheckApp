package com.example.facecheckapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class CheckinSuccessActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvName: TextView
    private lateinit var tvCode: TextView
    private lateinit var tvRoom: TextView
    private lateinit var tvDateTime: TextView
    private lateinit var tvCheckinTime: TextView
    private lateinit var statusBox: LinearLayout

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkin_success)

        tvStatus = findViewById(R.id.tvStatus)
        tvName = findViewById(R.id.tvName)
        tvCode = findViewById(R.id.tvCode)
        tvRoom = findViewById(R.id.tvRoom)
        tvDateTime = findViewById(R.id.tvDateTime)
        tvCheckinTime = findViewById(R.id.tvCheckinTime)
        statusBox = findViewById(R.id.statusBox)

        val btnHome = findViewById<Button>(R.id.btnHome)
        val btnHistory = findViewById<Button>(R.id.btnHistory)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        val classId = intent.getStringExtra("classId") ?: ""
        val className = intent.getStringExtra("className") ?: "-"
        val subjectCode = intent.getStringExtra("subjectCode") ?: "-"
        val classRoom = intent.getStringExtra("classRoom") ?: "-"
        val classTime = intent.getStringExtra("classTime") ?: "-"
        val dayTime = intent.getStringExtra("dayTime") ?: "-"
        val checkinTime = intent.getStringExtra("checkinTime") ?: "-"
        val status = intent.getStringExtra("status") ?: "ตรงเวลา"

        tvName.text = className
        tvCode.text = subjectCode
        tvRoom.text = classRoom
        tvDateTime.text = "$dayTime ($classTime)"
        tvCheckinTime.text = checkinTime

        if (status == "มาสาย") {
            tvStatus.text = "มาสาย"
            statusBox.setBackgroundResource(R.drawable.bg_late_orange_box)
        } else {
            tvStatus.text = "ตรงเวลา"
            statusBox.setBackgroundResource(R.drawable.bg_success_box)
        }

        // บันทึกประวัติ + สร้าง notification ถ้า มาสาย / ขาด
        saveHistory(
            classId, className, subjectCode, classRoom,
            dayTime, classTime, checkinTime, status
        )

        btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        btnHistory.setOnClickListener {
            val i = Intent(this, HistoryActivity::class.java)
            i.putExtra("classId", classId)
            startActivity(i)
        }

        btnBack.setOnClickListener { finish() }
    }

    private fun saveHistory(
        classId: String,
        className: String,
        subjectCode: String,
        classRoom: String,
        dayTime: String,
        classTime: String,
        checkTime: String,
        status: String
    ) {
        val uid = auth.uid ?: return
        
        // อ่าน year และ semester จาก class
        db.child("classes").child(classId)
            .addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    // อ่าน year (พ.ศ.) และ semester จาก class
                    val yearThaiStr = snapshot.child("year").getValue(String::class.java) ?: ""
                    val semesterStr = snapshot.child("semester").getValue(String::class.java) ?: ""
                    
                    // แปลงปี พ.ศ. เป็น ค.ศ. (history เก็บเป็น ค.ศ.)
                    val yearAd = if (yearThaiStr.isNotEmpty()) {
                        val yearThaiInt = yearThaiStr.toIntOrNull() ?: 0
                        if (yearThaiInt > 2500) yearThaiInt - 543 else yearThaiInt
                    } else {
                        Calendar.getInstance().get(Calendar.YEAR)
                    }
                    val yearAdStr = yearAd.toString()
                    
                    // ใช้ semester จาก class หรือ default เป็น "1"
                    val semester = if (semesterStr.isNotEmpty()) semesterStr else "1"
                    
                    val hisId = db.child("history").child(uid).push().key ?: return
                    val now = System.currentTimeMillis()

                    val data = mapOf(
                        "historyId" to hisId,
                        "classId" to classId,
                        "className" to className,
                        "subjectCode" to subjectCode,
                        "classRoom" to classRoom,
                        "dayTime" to dayTime,
                        "classTime" to classTime,
                        "checkinTime" to checkTime,
                        "status" to status,
                        "year" to yearAdStr,
                        "semester" to semester,
                        "timestamp" to now
                    )

                    db.child("history").child(uid).child(hisId).setValue(data)
                        .addOnSuccessListener {
                            // ถ้า มาสาย หรือ ขาด → สร้าง notification
                            if (status == "มาสาย" || status == "ขาด") {
                                createNotification(
                                    uid = uid,
                                    className = className,
                                    subjectCode = subjectCode,
                                    checkTime = checkTime,
                                    status = status
                                )
                            }
                        }
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    // ถ้าโหลด class ไม่สำเร็จ ให้ใช้ค่า default
                    val hisId = db.child("history").child(uid).push().key ?: return
                    val now = System.currentTimeMillis()
                    val yearAd = Calendar.getInstance().get(Calendar.YEAR)

                    val data = mapOf(
                        "historyId" to hisId,
                        "classId" to classId,
                        "className" to className,
                        "subjectCode" to subjectCode,
                        "classRoom" to classRoom,
                        "dayTime" to dayTime,
                        "classTime" to classTime,
                        "checkinTime" to checkTime,
                        "status" to status,
                        "year" to yearAd.toString(),
                        "semester" to "1",
                        "timestamp" to now
                    )

                    db.child("history").child(uid).child(hisId).setValue(data)
                        .addOnSuccessListener {
                            if (status == "มาสาย" || status == "ขาด") {
                                createNotification(
                                    uid = uid,
                                    className = className,
                                    subjectCode = subjectCode,
                                    checkTime = checkTime,
                                    status = status
                                )
                            }
                        }
                }
            })
    }

    /** สร้างข้อมูลแจ้งเตือนใน /notifications/{uid}/{notifId} */
    private fun createNotification(
        uid: String,
        className: String,
        subjectCode: String,
        checkTime: String,
        status: String
    ) {
        db.child("users").child(uid).get()
            .addOnSuccessListener { snap ->
                val studentId = snap.child("id").value?.toString() ?: "-"

                val notifRef = db.child("notifications").child(uid).push()
                val notifId = notifRef.key ?: return@addOnSuccessListener

                val notif = NotificationModel(
                    id = notifId,
                    status = status,              // "มาสาย" หรือ "ขาด"
                    subjectCode = subjectCode,
                    subjectName = className,
                    studentId = studentId,
                    checkinTime = checkTime,
                    createdAt = System.currentTimeMillis(),
                    seen = false                  // ✅ ทุกอันใหม่ = ยังไม่อ่าน
                )

                notifRef.setValue(notif)
            }
    }
}
