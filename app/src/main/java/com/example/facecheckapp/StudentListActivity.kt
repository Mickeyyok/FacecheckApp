package com.example.facecheckapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class StudentListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var btnAddStudent: Button
    private lateinit var btnBack: ImageButton

    private lateinit var tabInfo: TextView
    private lateinit var tabStudent: TextView
    private lateinit var tabReportTerm: TextView
    private lateinit var tabReportDay: TextView

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

        // ✅ แท็บ
        tabInfo = findViewById(R.id.tabInfo)
        tabStudent = findViewById(R.id.tabStudent)
        tabReportTerm = findViewById(R.id.tabReportTerm)
        tabReportDay = findViewById(R.id.tabReportDay)

        teacherUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        classId = intent.getStringExtra("classId")

        if (classId == null) {
            Toast.makeText(this, "ไม่พบข้อมูลคลาส", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = StudentAdapter(studentList, classId!!)
        recyclerView.adapter = adapter

        loadStudents()

        btnAddStudent.setOnClickListener {
            val intent = Intent(this, AddStudentActivity::class.java)
            intent.putExtra("classId", classId)
            startActivity(intent)
        }

        // ย้อนกลับ
        btnBack.setOnClickListener {
            val intent = Intent(this, TeacherHomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        // ✅ ตั้งสีแท็บ active
        setActiveTab(tabStudent)

        // ✅ กดแท็บ "ข้อมูล"
        tabInfo.setOnClickListener {
            val intent = Intent(this, ClassDetailActivity::class.java)
            intent.putExtra("classId", classId)
            startActivity(intent)
            finish()
        }

        // แท็บ Report (ยังไม่เปิดใช้งาน)
        tabReportDay.setOnClickListener {
            val intent = Intent(this, RealTimeActivity::class.java)
            intent.putExtra("classId", classId)
            startActivity(intent)
        }

        tabReportTerm.setOnClickListener {
            val intent = Intent(this, TermActivity::class.java)
            intent.putExtra("classId", classId)
            startActivity(intent)
        }
    }

    private fun loadStudents() {
        val classRef = database.getReference("classes/$teacherUid/$classId/students")

        classRef.addListenerForSingleValueEvent(object : ValueEventListener {
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

    // ✅ ใช้ ContextCompat เพื่อป้องกัน Deprecated Warning
    private fun setActiveTab(activeTab: TextView) {

        val allTabs = listOf(tabInfo, tabStudent, tabReportDay, tabReportTerm)

        allTabs.forEach {
            it.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
            it.setBackgroundResource(R.drawable.tab_unselected_bg)
        }

        activeTab.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark))
        activeTab.setBackgroundResource(R.drawable.tab_selected_bg)
    }

}