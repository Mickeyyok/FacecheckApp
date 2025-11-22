package com.example.facecheckapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class CreateClassActivity : AppCompatActivity() {

    private lateinit var etClassName: EditText
    private lateinit var etClassRoom: EditText
    private lateinit var etSubjectCode: EditText
    private lateinit var etTeacherName: EditText
    private lateinit var etYear: EditText
    private lateinit var etSemester: EditText

    private lateinit var btnNext: Button
    private lateinit var btnBack: Button
    private lateinit var btnBackHeader: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_class)

        // ✅ ผูก View กับ ID
        etClassName = findViewById(R.id.etClassName)
        etClassRoom = findViewById(R.id.etClassRoom)
        etSubjectCode = findViewById(R.id.etSubjectCode)
        etTeacherName = findViewById(R.id.etTeacherName)
        etYear = findViewById(R.id.etYear)
        etSemester = findViewById(R.id.etSemester)

        btnNext = findViewById(R.id.btnNext)
        btnBack = findViewById(R.id.btnBack)
        btnBackHeader = findViewById(R.id.btnBackHeader)

        // 🔙 ปุ่มย้อนกลับ
        btnBack.setOnClickListener { finish() }
        btnBackHeader.setOnClickListener { finish() }

        // ➡ ปุ่มถัดไป
        btnNext.setOnClickListener {
            val className = etClassName.text.toString().trim()
            val classRoom = etClassRoom.text.toString().trim()
            val subjectCode = etSubjectCode.text.toString().trim()
            val teacherName = etTeacherName.text.toString().trim()
            val year = etYear.text.toString().trim()
            val semester = etSemester.text.toString().trim()


            // ✅ ตรวจสอบช่องว่าง
            if (className.isEmpty() || subjectCode.isEmpty() || teacherName.isEmpty() ||
                year.isEmpty() || semester.isEmpty() || classRoom.isEmpty()
            ) {
                Toast.makeText(this, "กรุณากรอกข้อมูลให้ครบทุกช่อง", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🚀 สร้าง Intent และส่งข้อมูลไปยัง CreatetimeActivity
            val intent = Intent(this, CreatetimeActivity::class.java).apply {
                putExtra("className", className)
                putExtra("classRoom", classRoom)
                putExtra("subjectCode", subjectCode)
                putExtra("teacherName", teacherName)
                putExtra("year", year)
                putExtra("semester", semester)

            }

            startActivity(intent)
        }
    }
}