package com.example.facecheckapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import java.util.*

class EditTimeActivity : AppCompatActivity() {

    private lateinit var tvClassTime: EditText // ใช้สำหรับ วันที่เรียน (DayTime)
    private lateinit var edtStartTime: EditText
    private lateinit var edtLateTime: EditText
    private lateinit var edtEndTime: EditText
    private lateinit var btnBack: Button
    private lateinit var btnSave: Button

    private lateinit var dbRef: DatabaseReference
    private var classId: String? = null

    // ตัวแปรข้อมูลวิชาหลักที่ส่งมาจาก EditClassActivity
    private var className: String? = null
    private var subjectCode: String? = null
    private var teacherName: String? = null
    private var year: String? = null
    private var semester: String? = null
    private var classRoom: String? = null
    private var classTimeFromEdit: String? = null // classTime ที่แก้ไขแล้วจากหน้า EditClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_time)

        // เชื่อม View
        tvClassTime = findViewById(R.id.tvClassTime) // วันที่เรียน
        edtStartTime = findViewById(R.id.edtStartTime)
        edtLateTime = findViewById(R.id.edtLateTime)
        edtEndTime = findViewById(R.id.edtEndTime)
        btnBack = findViewById(R.id.btnBack)
        btnSave = findViewById(R.id.btnSave)

        dbRef = FirebaseDatabase.getInstance().getReference("classes")

        // รับ classId
        classId = intent.getStringExtra("classId")

        // 🌟🌟🌟 รับข้อมูลหลักที่แก้ไขแล้วจาก EditClassActivity 🌟🌟🌟
        className = intent.getStringExtra("className")
        subjectCode = intent.getStringExtra("subjectCode")
        teacherName = intent.getStringExtra("teacherName")
        year = intent.getStringExtra("year")
        semester = intent.getStringExtra("semester")
        classRoom = intent.getStringExtra("classRoom")
        classTimeFromEdit = intent.getStringExtra("classTime")
        // 🌟🌟🌟 --------------------------------------------- 🌟🌟🌟

        if (classId.isNullOrEmpty()) {
            Toast.makeText(this, "ไม่พบข้อมูลคลาส", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadTimeData()

        // DatePicker Listener
        tvClassTime.setOnClickListener { showDatePicker(tvClassTime) }

        // TimePicker Listeners
        edtStartTime.setOnClickListener { showTimePicker(edtStartTime) }
        edtLateTime.setOnClickListener { showTimePicker(edtLateTime) }
        edtEndTime.setOnClickListener { showTimePicker(edtEndTime) }

        // 💾 ปุ่มบันทึกข้อมูลทั้งหมด
        btnSave.setOnClickListener {

            val dayTime = tvClassTime.text.toString()
            val startTime = edtStartTime.text.toString()
            val lateTime = edtLateTime.text.toString()
            val endTime = edtEndTime.text.toString()

            if (dayTime.isEmpty() || startTime.isEmpty() || lateTime.isEmpty() || endTime.isEmpty()) {
                Toast.makeText(this, "กรุณาเลือกวันและเวลาให้ครบ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 💡 สร้างข้อความรวม classTime ใหม่ เพื่อแสดงผลใน ClassDetailActivity
            val fullClassTime = "$dayTime $startTime - $endTime น."


            // 🚨 อัปเดตข้อมูลทั้งหมด: ข้อมูลหลักที่แก้ไขแล้ว + ข้อมูลเวลาใหม่
            val updates = mutableMapOf<String, Any>(
                // 🌟 ข้อมูลหลักที่แก้ไขแล้วจาก EditClassActivity (ต้องใส่ทั้งหมด)
                "className" to (className ?: ""),
                "subjectCode" to (subjectCode ?: ""),
                "teacherName" to (teacherName ?: ""),
                "year" to (year ?: ""),
                "semester" to (semester ?: ""),
                "classRoom" to (classRoom ?: ""),

                // ข้อมูลเวลาใหม่ที่แก้ไขในหน้านี้
                "classTime" to fullClassTime, // ใช้ค่าที่คำนวณใหม่
                "dayTime" to dayTime,
                "startTime" to startTime,
                "lateTime" to lateTime,
                "endTime" to endTime
            )

            dbRef.child(classId!!).updateChildren(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "บันทึกข้อมูลสำเร็จ", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, ClassDetailActivity::class.java)
                    intent.putExtra("classId", classId)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)

                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "บันทึกไม่สำเร็จ", Toast.LENGTH_SHORT).show()
                }
        }

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun loadTimeData() {
        dbRef.child(classId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // ดึงวันจาก Key 'dayTime' เพื่อมาแสดงใน tvClassTime
                tvClassTime.setText(snapshot.child("dayTime").value?.toString() ?: "")
                edtStartTime.setText(snapshot.child("startTime").value?.toString() ?: "")
                edtLateTime.setText(snapshot.child("lateTime").value?.toString() ?: "")
                edtEndTime.setText(snapshot.child("endTime").value?.toString() ?: "")
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /** 🗓️ ฟังก์ชันเลือก "วันที่" */
    private fun showDatePicker(target: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, y, m, d ->
            val formattedDate = String.format("%02d/%02d/%04d", d, m + 1, y)
            target.setText(formattedDate)
        }, year, month, day)

        datePicker.show()
    }


    /** ⏰ ฟังก์ชันเลือกเวลาอย่างเดียว (ปรับปรุงให้ใช้ค่าปัจจุบันเป็นค่าเริ่มต้น) */
    private fun showTimePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        var hour = calendar.get(Calendar.HOUR_OF_DAY)
        var minute = calendar.get(Calendar.MINUTE)

        val currentTime = editText.text.toString()
        if (currentTime.matches("\\d{2}:\\d{2}".toRegex())) {
            try {
                hour = currentTime.substring(0, 2).toInt()
                minute = currentTime.substring(3, 5).toInt()
            } catch (e: NumberFormatException) {
                // ใช้เวลาปัจจุบันเป็นค่าเริ่มต้น
            }
        }

        TimePickerDialog(this, { _, h, m ->
            editText.setText(String.format("%02d:%02d", h, m))
        }, hour, minute, true).show()
    }
}