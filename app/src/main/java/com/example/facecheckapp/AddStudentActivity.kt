package com.example.facecheckapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AddStudentActivity : AppCompatActivity() {

    private lateinit var etStudentId: EditText
    private lateinit var btnAddStudent: Button
    private lateinit var listView: ListView
    private lateinit var btnBack: ImageButton

    private val database = FirebaseDatabase.getInstance()
    private lateinit var teacherUid: String
    private var classId: String? = null

    private val studentList = mutableListOf<StudentData>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_student)

        etStudentId = findViewById(R.id.etStudentId)
        btnAddStudent = findViewById(R.id.btnAddStudent)
        listView = findViewById(R.id.studentListView)
        btnBack = findViewById(R.id.btnBack)

        teacherUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        classId = intent.getStringExtra("classId")

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf<String>())
        listView.adapter = adapter

        loadExistingStudents()

        btnAddStudent.setOnClickListener {
            val studentId = etStudentId.text.toString().trim()
            if (studentId.length != 13) {
                Toast.makeText(this, "กรุณากรอกรหัสนักศึกษาให้ถูกต้อง (13 หลัก)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            checkAndAddStudent(studentId)
        }

        btnBack.setOnClickListener { finish() }
    }

    private fun loadExistingStudents() {
        val classRef = database.getReference("classes/$teacherUid/$classId/students")
        classRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                adapter.clear()
                for (child in snapshot.children) {
                    val firstName = child.child("first_name").value?.toString() ?: "-"
                    val lastName = child.child("last_name").value?.toString() ?: "-"
                    val studentId = child.key ?: "-"
                    adapter.add("$firstName $lastName ($studentId)")
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun checkAndAddStudent(studentId: String) {
        val usersRef = database.getReference("users")

        // 🔍 ตรวจว่ารหัสนี้มีอยู่ใน users หรือไม่ (แก้จาก student_id → id)
        usersRef.orderByChild("id").equalTo(studentId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnap in snapshot.children) {
                            val firstName = userSnap.child("first_name").value?.toString() ?: "-"
                            val lastName = userSnap.child("last_name").value?.toString() ?: "-"
                            val studentData = mapOf(
                                "first_name" to firstName,
                                "last_name" to lastName,
                                "status" to "ปกติ"
                            )

                            val studentRef = database.getReference("classes/$teacherUid/$classId/students/$studentId")
                            studentRef.setValue(studentData)

                            Toast.makeText(this@AddStudentActivity, "เพิ่มนักศึกษาสำเร็จ", Toast.LENGTH_SHORT).show()
                            etStudentId.text.clear()
                        }
                    } else {
                        Toast.makeText(this@AddStudentActivity, "ไม่พบนักศึกษารหัสนี้ในระบบ", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

}
