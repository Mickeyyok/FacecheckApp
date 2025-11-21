package com.example.facecheckapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
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

        saveHistory(
            classId, className, subjectCode, classRoom,
            dayTime, classTime, checkinTime, tvStatus.text.toString()
        )

        btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        btnHistory.setOnClickListener {
            val i = Intent(this, HistoryActivity::class.java)
            i.putExtra("classId", classId)   // ส่งวิชาที่เพิ่งเช็คชื่อไปให้
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
        val hisId = db.push().key ?: return

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
            "year" to "2025",
            "semester" to "2",
            "timestamp" to System.currentTimeMillis()
        )

        db.child("history").child(uid).child(hisId).setValue(data)
    }
}
