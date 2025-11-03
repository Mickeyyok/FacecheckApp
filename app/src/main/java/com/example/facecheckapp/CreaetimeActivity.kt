package com.example.facecheckapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

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

            val timeData = mapOf(
                "startTime" to startTime,
                "lateTime" to lateTime,
                "endTime" to endTime
            )

            // 🧠 บันทึกเวลาเข้า class เดิม
            database.child(classId).child("attendanceTime").setValue(timeData)
                .addOnSuccessListener {
                    Toast.makeText(this, "บันทึกเวลาสำเร็จ", Toast.LENGTH_SHORT).show()

                    // 👉 ไปหน้า MyClassActivity แล้วเคลียร์ stack
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
}
