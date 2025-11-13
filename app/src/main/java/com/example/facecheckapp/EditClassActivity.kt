package com.example.facecheckapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class EditClassActivity : AppCompatActivity() {

    private lateinit var edtClassName: EditText
    private lateinit var edtSubjectCode: EditText
    private lateinit var edtTeacherName: EditText
    private lateinit var edtYear: EditText
    private lateinit var edtSemester: EditText
    private lateinit var edtClassRoom: EditText

    private lateinit var btnNext: Button
    private lateinit var btnCancel: Button

    private var classId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_class)

        edtClassName = findViewById(R.id.edtClassName)
        edtSubjectCode = findViewById(R.id.edtSubjectCode)
        edtTeacherName = findViewById(R.id.edtTeacherName)
        edtYear = findViewById(R.id.edtYear)
        edtSemester = findViewById(R.id.edtSemester)
        edtClassRoom = findViewById(R.id.edtClassRoom)

        btnNext = findViewById(R.id.btnNext)
        btnCancel = findViewById(R.id.btnCancel)

        classId = intent.getStringExtra("classId")

        if (classId == null) {
            Toast.makeText(this, "ไม่พบข้อมูลคลาส", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadClassData()

        btnNext.setOnClickListener {
            val intent = Intent(this, EditTimeActivity::class.java)

            intent.putExtra("classId", classId)

            // ส่งข้อมูลทั้งหมดไปหน้า EditTime
            intent.putExtra("className", edtClassName.text.toString())
            intent.putExtra("subjectCode", edtSubjectCode.text.toString())
            intent.putExtra("teacherName", edtTeacherName.text.toString())
            intent.putExtra("year", edtYear.text.toString())
            intent.putExtra("semester", edtSemester.text.toString())
            intent.putExtra("classRoom", edtClassRoom.text.toString())

            startActivity(intent)
        }


        btnCancel.setOnClickListener { finish() }
    }

    private fun loadClassData() {
        val db = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("classes")

        db.child(classId!!).get().addOnSuccessListener { snapshot ->
            edtClassName.setText(snapshot.child("className").value?.toString() ?: "")
            edtSubjectCode.setText(snapshot.child("subjectCode").value?.toString() ?: "")
            edtTeacherName.setText(snapshot.child("teacherName").value?.toString() ?: "")
            edtYear.setText(snapshot.child("year").value?.toString() ?: "")
            edtSemester.setText(snapshot.child("semester").value?.toString() ?: "")
            edtClassRoom.setText(snapshot.child("classRoom").value?.toString() ?: "")
        }
    }
}
