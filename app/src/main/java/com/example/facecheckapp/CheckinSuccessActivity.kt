package com.example.facecheckapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
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

        val classId = intent.getStringExtra("classId") ?: return
        val checkTime = intent.getStringExtra("checkinTime") ?: "-"
        val className = intent.getStringExtra("className") ?: "-"
        val subjectCode = intent.getStringExtra("subjectCode") ?: "-"
        val classRoom = intent.getStringExtra("classRoom") ?: "-"
        val classTime = intent.getStringExtra("classTime") ?: "-"
        val dayTime = intent.getStringExtra("dayTime") ?: "-"
        val endTime = intent.getStringExtra("endTime") ?: "-"

        tvCheckinTime.text = checkTime

        // ใส่ค่าคลาส
        tvName.text = className
        tvCode.text = subjectCode
        tvRoom.text = classRoom
        tvDateTime.text = "$dayTime ($classTime)"

        // ตรวจสาย/เกินเวลา/ตรงเวลา
        if (isTimeOver(checkTime, endTime)) {
            tvStatus.text = "เช็คชื่อเกินเวลา"
            statusBox.setBackgroundResource(R.drawable.bg_late_orange_box)
        } else {
            tvStatus.text = "ตรงเวลา"
            statusBox.setBackgroundResource(R.drawable.bg_success_box)
        }

        btnHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("classId", classId)   // ⭐ ส่งกลับไป
            startActivity(intent)
            finish()
        }


        btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        btnBack.setOnClickListener { finish() }
    }

    private fun isTimeOver(checkin: String, end: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val checkT = sdf.parse(checkin)
            val endT = sdf.parse(end)
            checkT.after(endT)
        } catch (e: Exception) {
            false
        }
    }
}
