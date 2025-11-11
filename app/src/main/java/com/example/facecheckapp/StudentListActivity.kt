package com.example.facecheckapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class StudentListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var btnAddStudent: Button
    private lateinit var btnBack: ImageButton

    private val studentList = mutableListOf<StudentData>()
    private lateinit var adapter: StudentAdapter

    private val database = FirebaseDatabase.getInstance()
    private lateinit var teacherUid: String
    private var classId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_list)

        recyclerView = findViewById(R.id.recyclerViewStudents)
        tvTotal = findViewById(R.id.tvTotal)
        btnAddStudent = findViewById(R.id.btnAddStudent)
        btnBack = findViewById(R.id.btnBack)

        teacherUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        classId = intent.getStringExtra("classId")

        if (classId == null) {
            Toast.makeText(this, "ไม่พบข้อมูลคลาส", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = StudentAdapter(studentList, classId!!) // ✅ เพิ่ม classId เข้ามา
        recyclerView.adapter = adapter


        loadStudents()

        btnAddStudent.setOnClickListener {
            val intent = Intent(this, AddStudentActivity::class.java)
            intent.putExtra("classId", classId)
            startActivity(intent)
        }

        btnBack.setOnClickListener { finish() }
    }

    private fun loadStudents() {
        val classRef = database.getReference("classes/$teacherUid/$classId/students")

        classRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                studentList.clear()
                for (child in snapshot.children) {
                    val firstName = child.child("first_name").value?.toString() ?: "-"
                    val lastName = child.child("last_name").value?.toString() ?: "-"
                    val studentId = child.key ?: "-"
                    val status = child.child("status").value?.toString() ?: "ปกติ"
                    studentList.add(StudentData(firstName, lastName, studentId, status))
                }

                studentList.sortBy { it.id }
                adapter.notifyDataSetChanged()
                tvTotal.text = "รายชื่อนักศึกษา (ทั้งหมด ${studentList.size} คน)"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@StudentListActivity, "โหลดข้อมูลไม่สำเร็จ", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
