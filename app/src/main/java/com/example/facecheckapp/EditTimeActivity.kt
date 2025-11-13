package com.example.facecheckapp

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import java.util.*

class EditTimeActivity : AppCompatActivity() {

    private lateinit var tvClassTime: EditText
    private lateinit var edtStartTime: EditText
    private lateinit var edtLateTime: EditText
    private lateinit var edtEndTime: EditText
    private lateinit var btnBack: Button
    private lateinit var btnSave: Button

    private lateinit var dbRef: DatabaseReference
    private var classId: String? = null

    // ตัวแปรข้อมูลวิชา
    private var className: String? = null
    private var subjectCode: String? = null
    private var teacherName: String? = null
    private var year: String? = null
    private var semester: String? = null
    private var classRoom: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_time)

        // เชื่อม View
        tvClassTime = findViewById(R.id.tvClassTime)
        edtStartTime = findViewById(R.id.edtStartTime)
        edtLateTime = findViewById(R.id.edtLateTime)
        edtEndTime = findViewById(R.id.edtEndTime)
        btnBack = findViewById(R.id.btnBack)
        btnSave = findViewById(R.id.btnSave)

        dbRef = FirebaseDatabase.getInstance().getReference("classes")

        // รับ classId
        classId = intent.getStringExtra("classId")

        // รับข้อมูลจาก EditClassActivity
        className = intent.getStringExtra("className")
        subjectCode = intent.getStringExtra("subjectCode")
        teacherName = intent.getStringExtra("teacherName")
        year = intent.getStringExtra("year")
        semester = intent.getStringExtra("semester")
        classRoom = intent.getStringExtra("classRoom")

        if (classId.isNullOrEmpty()) {
            Toast.makeText(this, "ไม่พบข้อมูลคลาส", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadTimeData()

        // TimePicker
        edtStartTime.setOnClickListener { showTimePicker(edtStartTime) }
        edtLateTime.setOnClickListener { showTimePicker(edtLateTime) }
        edtEndTime.setOnClickListener { showTimePicker(edtEndTime) }

        // บันทึกข้อมูลรวม
        btnSave.setOnClickListener {

            val updates = mapOf(
                "className" to className,
                "subjectCode" to subjectCode,
                "teacherName" to teacherName,
                "year" to year,
                "semester" to semester,
                "classRoom" to classRoom,

                // เวลา
                "classTime" to tvClassTime.text.toString(),
                "startTime" to edtStartTime.text.toString(),
                "lateTime" to edtLateTime.text.toString(),
                "endTime" to edtEndTime.text.toString()
            )

            dbRef.child(classId!!).updateChildren(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "บันทึกข้อมูลสำเร็จ", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, ClassDetailActivity::class.java)
                    intent.putExtra("classId", classId)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)

                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "บันทึกไม่สำเร็จ", Toast.LENGTH_SHORT).show()
                }
        }

        btnBack.setOnClickListener { finish() }
    }

    private fun loadTimeData() {
        dbRef.child(classId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tvClassTime.setText(snapshot.child("classTime").value?.toString() ?: "")
                edtStartTime.setText(snapshot.child("startTime").value?.toString() ?: "")
                edtLateTime.setText(snapshot.child("lateTime").value?.toString() ?: "")
                edtEndTime.setText(snapshot.child("endTime").value?.toString() ?: "")
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showTimePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, h, m ->
            editText.setText(String.format("%02d:%02d", h, m))
        }, hour, minute, true).show()
    }
}
