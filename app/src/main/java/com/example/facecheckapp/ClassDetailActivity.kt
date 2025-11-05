package com.example.facecheckapp

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class ClassDetailActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_class_detail)

        // ✅ ใช้ Firebase Realtime Database
        dbRef = FirebaseDatabase.getInstance().getReference("classes")

        val classId = intent.getStringExtra("classId")
        if (classId.isNullOrEmpty()) {
            Toast.makeText(this, "ไม่พบข้อมูลคลาส", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d("ClassDetailActivity", "✅ Received classId = $classId")

        // 🔹 เชื่อม View ต่าง ๆ
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val tvSubjectName = findViewById<TextView>(R.id.tvSubjectName)
        val tvSubjectCode = findViewById<TextView>(R.id.tvSubjectCode)
        val tvTeacherName = findViewById<TextView>(R.id.tvTeacherName)
        val tvDayTime = findViewById<TextView>(R.id.tvDayTime)
        val tvCheckTime = findViewById<TextView>(R.id.tvCheckTime)
        val tvClassRoom = findViewById<TextView>(R.id.tvClassRoom)
        val tvYear = findViewById<TextView>(R.id.tvYear)
        val tvSemester = findViewById<TextView>(R.id.tvSemester)

        // 🔹 ปุ่มย้อนกลับ
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // 🔹 โหลดข้อมูลจาก Realtime Database
        dbRef.child(classId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(this@ClassDetailActivity, "ไม่พบข้อมูลในฐานข้อมูล", Toast.LENGTH_SHORT).show()
                    return
                }

                val className = snapshot.child("className").getValue(String::class.java) ?: "-"
                val subjectCode = snapshot.child("subjectCode").getValue(String::class.java) ?: "-"
                val teacherName = snapshot.child("teacherName").getValue(String::class.java) ?: "-"
                val classRoom = snapshot.child("classRoom").getValue(String::class.java) ?: "-"
                val year = snapshot.child("year").getValue(String::class.java) ?: "-"
                val semester = snapshot.child("semester").getValue(String::class.java) ?: "-"

                // 🔹 วันที่เรียน และเวลาตั้งคลาส
                val classTime = snapshot.child("classTime").getValue(String::class.java) ?: "-"
                val dayTime = snapshot.child("dayTime").getValue(String::class.java) ?: "-"

                // 🔹 เวลาต่าง ๆ สำหรับการเช็กชื่อ
                val startTime = snapshot.child("startTime").getValue(String::class.java) ?: "-"
                val lateTime = snapshot.child("lateTime").getValue(String::class.java) ?: "-"
                val endTime = snapshot.child("endTime").getValue(String::class.java) ?: "-"

                // 🔹 แสดงข้อมูลพื้นฐาน
                tvTitle.text = className
                tvSubjectName.text = className
                tvSubjectCode.text = subjectCode
                tvTeacherName.text = teacherName
                tvDayTime.text = classTime // วันที่เรียน
                tvClassRoom.text = classRoom
                tvYear.text = year
                tvSemester.text = semester

                // 🔹 แสดงเวลาเช็กชื่อ (ตรง / สาย / ขาด)
                val text = SpannableStringBuilder()

                // 🟢 ตรง
                val green = "ตรง"
                text.append(green)
                text.setSpan(
                    ForegroundColorSpan(Color.parseColor("#00C853")),
                    text.length - green.length, text.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                text.append(startTime)

                // 🟠 สาย
                val orange = "  สาย"
                text.append(orange)
                text.setSpan(
                    ForegroundColorSpan(Color.parseColor("#FF8C00")),
                    text.length - orange.length + 2, text.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                text.append(lateTime)

                // 🔴 ขาด
                val red = "  ขาด"
                text.append(red)
                text.setSpan(
                    ForegroundColorSpan(Color.parseColor("#E53935")),
                    text.length - red.length + 2, text.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                text.append(endTime)

                tvCheckTime.text = text
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ClassDetailActivity, "เกิดข้อผิดพลาด: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("ClassDetailActivity", "❌ Database error: ${error.message}")
            }
        })
    }
}
