package com.example.facecheckapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class CreatetimeActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("classes")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_createtime_class)

        val edtDateTime = findViewById<EditText>(R.id.edtDateTime)
        val etStartTime = findViewById<EditText>(R.id.etStartTime)
        val etLateTime = findViewById<EditText>(R.id.etLateTime)
        val etEndTime = findViewById<EditText>(R.id.etEndTime)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnSaveTime = findViewById<Button>(R.id.btnSaveTime)

        // ✅ รับข้อมูลจากหน้า CreateClassActivity
        val className = intent.getStringExtra("className")
        val classRoom = intent.getStringExtra("classRoom")
        val subjectCode = intent.getStringExtra("subjectCode")
        val teacherName = intent.getStringExtra("teacherName")
        val year = intent.getStringExtra("year")
        val semester = intent.getStringExtra("semester")
        val classTime = intent.getStringExtra("classTime")

        // 🔙 ปุ่มย้อนกลับ
        btnBack.setOnClickListener {
            finish()
        }

        // ✅ ปุ่ม "บันทึกเวลา"
        btnSaveTime.setOnClickListener {
            val dayTime = edtDateTime.text.toString().trim()
            val startTime = etStartTime.text.toString().trim()
            val lateTime = etLateTime.text.toString().trim()
            val endTime = etEndTime.text.toString().trim()

            if (dayTime.isEmpty() || startTime.isEmpty() || lateTime.isEmpty() || endTime.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกเวลาให้ครบ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ สร้าง object ข้อมูลคลาส
            val classId = database.push().key ?: UUID.randomUUID().toString()
            val createdBy = auth.currentUser?.uid ?: "unknown"

            val classData = ClassData(
                classId = classId,
                className = className,
                classRoom = classRoom,
                subjectCode = subjectCode,
                teacherName = teacherName,
                year = year,
                semester = semester,
                classTime = classTime,
                dayTime = dayTime,
                startTime = startTime,
                lateTime = lateTime,
                endTime = endTime,
                createdBy = createdBy
            )

            // ✅ บันทึกลง Firebase
            database.child(classId).setValue(classData)
                .addOnSuccessListener {
                    Toast.makeText(this, "สร้างคลาสสำเร็จ 🎉", Toast.LENGTH_SHORT).show()

                    // กลับไปหน้า TeacherHomeActivity
                    val intent = Intent(this, TeacherHomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "บันทึกข้อมูลล้มเหลว ❌", Toast.LENGTH_SHORT).show()
                }
        }

        // 🔹 ส่วนของ Date/Time Picker
        edtDateTime.setOnClickListener { showDateTimePicker(edtDateTime) }
        etStartTime.setOnClickListener { showTimePicker(etStartTime) }
        etLateTime.setOnClickListener { showTimePicker(etLateTime) }
        etEndTime.setOnClickListener { showTimePicker(etEndTime) }
    }

    // 🗓️ ฟังก์ชันเลือกวัน + เวลา
    private fun showDateTimePicker(target: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePicker = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                val formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                target.setText("$formattedDate $formattedTime น.")
            }, hour, minute, true)

            timePicker.show()
        }, year, month, day)

        datePicker.show()
    }

    // ⏰ ฟังก์ชันเลือกเวลาอย่างเดียว
    private fun showTimePicker(target: EditText) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, h, m ->
            target.setText(String.format("%02d:%02d", h, m))
        }, hour, minute, true).show()
    }
}
