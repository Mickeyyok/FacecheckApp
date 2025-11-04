package com.example.facecheckapp

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class CreaetimeActivity : AppCompatActivity() {

    private lateinit var spinnerDay: Spinner
    private lateinit var etStartTime: EditText
    private lateinit var etLateTime: EditText
    private lateinit var etEndTime: EditText
    private lateinit var btnSaveTime: Button
    private lateinit var btnBack: Button

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("classes")
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_createtime_class)

        spinnerDay = findViewById(R.id.spinnerDay)
        etStartTime = findViewById(R.id.etStartTime)
        etLateTime = findViewById(R.id.etLateTime)
        etEndTime = findViewById(R.id.etEndTime)
        btnSaveTime = findViewById(R.id.btnSaveTime)
        btnBack = findViewById(R.id.btnBack)

        val days = listOf("เลือกวันเรียน", "จันทร์", "อังคาร", "พุธ", "พฤหัสบดี", "ศุกร์")
        spinnerDay.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, days)

        etStartTime.setOnClickListener { showTimePicker(etStartTime) }
        etLateTime.setOnClickListener { showTimePicker(etLateTime) }
        etEndTime.setOnClickListener { showTimePicker(etEndTime) }

        btnBack.setOnClickListener { finish() }

        btnSaveTime.setOnClickListener {
            val day = spinnerDay.selectedItem.toString()
            val start = etStartTime.text.toString()
            val late = etLateTime.text.toString()
            val end = etEndTime.text.toString()

            if (day == "เลือกวันเรียน" || start.isEmpty() || end.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกข้อมูลให้ครบ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val className = intent.getStringExtra("className") ?: ""
            val classRoom = intent.getStringExtra("classRoom") ?: ""
            val subjectCode = intent.getStringExtra("subjectCode") ?: ""
            val teacherName = intent.getStringExtra("teacherName") ?: ""
            val year = intent.getStringExtra("year") ?: ""
            val semester = intent.getStringExtra("semester") ?: ""
            val userId = auth.currentUser?.uid ?: "unknown"

            val classId = database.push().key ?: return@setOnClickListener

            val newClass = ClassData(
                classId = classId,
                className = className,
                subjectCode = subjectCode,
                teacherName = teacherName,
                year = year,
                semester = semester,
                classRoom = classRoom,
                startTime = "$day $start",
                endTime = end,
                lateTime = late,
                createdBy = userId
            )

            database.child(classId).setValue(newClass)
                .addOnSuccessListener {
                    val intent = Intent(this, ClassDetailActivity::class.java)
                    intent.putExtra("classId", classId)
                    startActivity(intent)
                    finish()
                }
        }
    }

    private fun showTimePicker(target: EditText) {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        TimePickerDialog(this, { _, h, m ->
            target.setText(String.format("%02d:%02d", h, m))
        }, hour, minute, true).show()
    }
}
