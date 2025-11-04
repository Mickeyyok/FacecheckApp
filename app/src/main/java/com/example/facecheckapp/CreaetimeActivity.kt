package com.example.facecheckapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class CreaetimeActivity : AppCompatActivity() {

    private lateinit var etStartTime: EditText
    private lateinit var etLateTime: EditText
    private lateinit var etEndTime: EditText
    private lateinit var btnSaveTime: Button

    private val database = FirebaseDatabase.getInstance().getReference("classes")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_createtime_class)

        etStartTime = findViewById(R.id.etStartTime)
        etLateTime = findViewById(R.id.etLateTime)
        etEndTime = findViewById(R.id.etEndTime)
        btnSaveTime = findViewById(R.id.btnSaveTime)

        val classId = intent.getStringExtra("classId") ?: ""

        btnSaveTime.setOnClickListener {
            val startTime = etStartTime.text.toString().trim()
            val lateTime = etLateTime.text.toString().trim()
            val endTime = etEndTime.text.toString().trim()

            if (startTime.isEmpty() || lateTime.isEmpty() || endTime.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกเวลาให้ครบ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔹 เก็บข้อมูลเวลาไว้ในคลาส
            val timeData = mapOf(
                "startTime" to startTime,
                "lateTime" to lateTime,
                "endTime" to endTime
            )

            database.child(classId).child("attendanceTime").setValue(timeData)
                .addOnSuccessListener {
                    Toast.makeText(this, "บันทึกเวลาสำเร็จ", Toast.LENGTH_SHORT).show()

                    // 👉 กลับไปหน้า MyClassActivity
                    val intent = Intent(this, TeacherHomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "เกิดข้อผิดพลาด: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    /**
     * ✅ ฟังก์ชันตรวจสถานะการมาเรียน
     * @param currentTime เวลาเข้าเรียนจริง เช่น "08:15"
     * @param startTime เวลาเริ่มเช็กชื่อ เช่น "08:00"
     * @param lateTime เวลาสาย เช่น "08:30"
     * @param endTime เวลาสิ้นสุด เช่น "10:00"
     * @return สถานะการเข้าเรียน ("ปกติ", "สาย", "ขาด")
     */
    private fun getAttendanceStatus(currentTime: String, startTime: String, lateTime: String, endTime: String): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

        val now = sdf.parse(currentTime)
        val start = sdf.parse(startTime)
        val late = sdf.parse(lateTime)
        val end = sdf.parse(endTime)

        return when {
            now!!.before(start) || now == start -> "ปกติ"   // เข้ามาก่อนหรือเท่ากับเวลาเริ่ม
            now.after(start) && now.before(late) -> "สาย"   // ระหว่างเวลาเริ่ม - เวลาสาย
            now.after(end) || now == end -> "ขาด"          // หลังจากเวลาสิ้นสุด
            else -> "สาย"
        }
    }
}
