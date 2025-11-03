package com.example.facecheckapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CreateClassActivity : AppCompatActivity() {

    private lateinit var etClassName: EditText
    private lateinit var etSubjectCode: EditText
    private lateinit var etTeacherName: EditText
    private lateinit var etStudentYear: EditText
    private lateinit var etSemester: EditText
    private lateinit var btnNext: Button

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("classes")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_class)

        // ✅ ผูก View กับ ID จาก layout
        etClassName = findViewById(R.id.etClassName)
        etSubjectCode = findViewById(R.id.etSubjectCode)
        etTeacherName = findViewById(R.id.etTeacherName)
        etStudentYear = findViewById(R.id.etStudentyear)
        etSemester = findViewById(R.id.etsemeter)
        btnNext = findViewById(R.id.btnNext)

        // ✅ คลิก "ถัดไป"
        btnNext.setOnClickListener {
            val className = etClassName.text.toString().trim()
            val subjectCode = etSubjectCode.text.toString().trim()
            val teacherName = etTeacherName.text.toString().trim()
            val year = etStudentYear.text.toString().trim()
            val semester = etSemester.text.toString().trim()

            if (className.isEmpty() || subjectCode.isEmpty() || teacherName.isEmpty() || year.isEmpty() || semester.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกข้อมูลให้ครบถ้วน", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = auth.currentUser?.uid ?: "unknown_user"
            val classId = database.push().key ?: return@setOnClickListener

            val classData = mapOf(
                "classId" to classId,
                "className" to className,
                "subjectCode" to subjectCode,
                "teacherName" to teacherName,
                "year" to year,
                "semester" to semester,
                "createdBy" to userId
            )

            // 🧠 บันทึกข้อมูลลง Firebase
            database.child(classId).setValue(classData)
                .addOnSuccessListener {
                    Toast.makeText(this, "สร้างคลาสสำเร็จ", Toast.LENGTH_SHORT).show()

                    // 👉 ไปหน้าถัดไป Settime
                    val intent = Intent(this, CreaetimeActivity::class.java)
                    intent.putExtra("classId", classId)
                    startActivity(intent)

                }
                .addOnFailureListener {
                    Toast.makeText(this, "เกิดข้อผิดพลาด: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
