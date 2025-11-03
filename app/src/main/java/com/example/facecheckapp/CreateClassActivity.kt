package com.example.facecheckapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import android.content.Intent


class CreateClassActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().getReference("classes")

    private lateinit var etClassName: EditText
    private lateinit var etTeacherName: EditText
    private lateinit var etSubjectCode: EditText
    private lateinit var etStudentLimit: EditText
    private lateinit var etStartTime: EditText
    private lateinit var etLateTime: EditText
    private lateinit var etEndTime: EditText
    private lateinit var btnCreateClass: Button
    private lateinit var btnAddStudent: Button

    private var classId: String? = null // ใช้ตรวจว่ากำลัง “แก้ไข” คลาสเดิมอยู่หรือไม่

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_class)

        auth = FirebaseAuth.getInstance()

        // เชื่อม View
        etClassName = findViewById(R.id.etClassName)
        etTeacherName = findViewById(R.id.etTeacherName)
        etSubjectCode = findViewById(R.id.etSubjectCode)
        etStudentLimit = findViewById(R.id.etStudentLimit)
        etStartTime = findViewById(R.id.etStartTime)
        etLateTime = findViewById(R.id.etLateTime)
        etEndTime = findViewById(R.id.etEndTime)
        btnCreateClass = findViewById(R.id.btnCreateClass)
        btnAddStudent = findViewById(R.id.btnAddStudent)

        val teacherUid = auth.currentUser?.uid ?: return

        // โหลดข้อมูลคลาสเดิม (ถ้ามี)
        loadExistingClass(teacherUid)

        // ✅ ปุ่ม "สร้าง / แก้ไขคลาส"
        btnCreateClass.setOnClickListener {
            val className = etClassName.text.toString().trim()
            val teacherName = etTeacherName.text.toString().trim()
            val subjectCode = etSubjectCode.text.toString().trim()
            val studentLimit = etStudentLimit.text.toString().trim()
            val startTime = etStartTime.text.toString().trim()
            val lateTime = etLateTime.text.toString().trim()
            val endTime = etEndTime.text.toString().trim()

            if (className.isEmpty() || teacherName.isEmpty() || subjectCode.isEmpty() || studentLimit.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกข้อมูลให้ครบ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val classData = mapOf(
                "className" to className,
                "teacherName" to teacherName,
                "subjectCode" to subjectCode,
                "studentLimit" to studentLimit,
                "startTime" to startTime,
                "lateTime" to lateTime,
                "endTime" to endTime
            )

            if (classId == null) {
                // 🔹 ยังไม่มี class -> สร้างใหม่
                val newId = database.child(teacherUid).push().key!!
                database.child(teacherUid).child(newId).setValue(classData)
                Toast.makeText(this, "สร้างคลาสสำเร็จ", Toast.LENGTH_SHORT).show()
                classId = newId
                btnCreateClass.text = "แก้ไขคลาส"
            } else {
                // 🔹 มี classId แล้ว -> แก้ไขข้อมูลเดิม
                database.child(teacherUid).child(classId!!).updateChildren(classData)
                Toast.makeText(this, "อัปเดตข้อมูลเรียบร้อย", Toast.LENGTH_SHORT).show()
            }
        }
        // ➕ ปุ่มเพิ่มรายชื่อนักศึกษา
        // ➕ ปุ่มเพิ่มรายชื่อนักศึกษา
        btnAddStudent.setOnClickListener {
            if (classId == null) {
                Toast.makeText(this, "กรุณาสร้างคลาสก่อนเพิ่มนักศึกษา", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, AddStudentActivity::class.java)
            intent.putExtra("classId", classId)
            startActivity(intent)
        }

    }

    private fun loadExistingClass(teacherUid: String) {
        // โหลดคลาสแรกของอาจารย์
        database.child(teacherUid).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val firstClass = snapshot.children.first()
                classId = firstClass.key
                etClassName.setText(firstClass.child("className").value?.toString() ?: "")
                etTeacherName.setText(firstClass.child("teacherName").value?.toString() ?: "")
                etSubjectCode.setText(firstClass.child("subjectCode").value?.toString() ?: "")
                etStudentLimit.setText(firstClass.child("studentLimit").value?.toString() ?: "")
                etStartTime.setText(firstClass.child("startTime").value?.toString() ?: "")
                etLateTime.setText(firstClass.child("lateTime").value?.toString() ?: "")
                etEndTime.setText(firstClass.child("endTime").value?.toString() ?: "")

                btnCreateClass.text = "แก้ไขคลาส"
            }
        }
    }
}
