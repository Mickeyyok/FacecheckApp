package com.example.facecheckapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TeacherHomeActivity : AppCompatActivity() {

    private lateinit var recyclerClasses: RecyclerView
    private lateinit var btnCreateClass: Button
    private lateinit var tvMyClassTitle: TextView

    private val auth = FirebaseAuth.getInstance()
    private val classesRef = FirebaseDatabase.getInstance().getReference("classes")

    // ลิสต์เก็บข้อมูลคลาส
    private val classList = mutableListOf<ClassData>()
    private lateinit var adapter: ClassAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_main)

        recyclerClasses = findViewById(R.id.recyclerClasses)
        btnCreateClass = findViewById(R.id.btn_create_class)
        tvMyClassTitle = findViewById(R.id.tvMyClassTitle)

        recyclerClasses.layoutManager = LinearLayoutManager(this)
        adapter = ClassAdapter(classList)
        recyclerClasses.adapter = adapter

        val userId = auth.currentUser?.uid ?: return

        // โหลดคลาสที่อาจารย์คนนี้สร้าง
        loadClasses(userId)

        // ไปหน้า CreateClass
        btnCreateClass.setOnClickListener {
            startActivity(Intent(this, CreateClassActivity::class.java))
        }
        setupBottomNav()
    }

    /** Bottom Navigation - แก้ไข Intent ให้เป็นของอาจารย์ */
    private fun setupBottomNav() {
        val navHome = findViewById<LinearLayout>(R.id.navHome)
        val navSetting = findViewById<LinearLayout>(R.id.navSetting)

        // 1. หน้าหลัก (navHome)
        navHome.setOnClickListener {
            // ⭐ เปลี่ยนเป็น TeacherHomeActivity::class.java เพื่อให้วนกลับมาที่หน้านี้
            startActivity(Intent(this, TeacherHomeActivity::class.java))
            overridePendingTransition(0, 0)
        }

        // 2. ตั้งค่า (navSetting)
        navSetting.setOnClickListener {
            // ⭐ เปลี่ยนเป็น SettingActivityTeacher::class.java (สมมติว่าคุณมีคลาสนี้)
            startActivity(Intent(this, SettingActivityTeacher::class.java))
            overridePendingTransition(0, 0)
        }
    }

    // ... (ฟังก์ชัน loadClasses(userId: String) ยังคงเดิม)
    private fun loadClasses(userId: String) {
        classesRef
            .orderByChild("createdBy")
            .equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    classList.clear()

                    for (classSnap in snapshot.children) {

                        // key ของ node นี้คือ classId
                        val classId = classSnap.key ?: continue

                        // ดึงค่าแต่ละ field แล้ว .toString() เอง
                        val subjectCode = classSnap.child("subjectCode").value?.toString() ?: ""
                        val className  = classSnap.child("className").value?.toString()  ?: ""
                        val classRoom  = classSnap.child("classRoom").value?.toString()  ?: ""
                        val dayTime    = classSnap.child("dayTime").value?.toString()    ?: ""
                        val startTime  = classSnap.child("startTime").value?.toString()  ?: ""
                        val endTime    = classSnap.child("endTime").value?.toString()    ?: ""
                        val lateTime   = classSnap.child("lateTime").value?.toString()   ?: ""

                        // ถ้ามี field อื่น ๆ ที่เก็บเป็น Long เช่น createdAt, year, term
                        // เราจะไม่ map มันเข้าคลาส เพื่อเลี่ยง error แปลง Long -> String

                        val classData = ClassData(
                            classId     = classId,
                            subjectCode = subjectCode,
                            className   = className,
                            classRoom   = classRoom,
                            dayTime     = dayTime,
                            startTime   = startTime,
                            endTime     = endTime,
                            lateTime    = lateTime,
                            createdBy   = userId
                        )

                        classList.add(classData)
                    }

                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@TeacherHomeActivity,
                        "โหลดข้อมูลล้มเหลว: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}