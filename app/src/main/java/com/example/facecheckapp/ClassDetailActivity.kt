package com.example.facecheckapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class ClassDetailActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance().getReference("classes")
    private lateinit var tvDetail: TextView
    private lateinit var btnDelete: Button
    private lateinit var btnEdit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_class_detail)

        tvDetail = findViewById(R.id.tvDetail)
        btnDelete = findViewById(R.id.btnDelete)
        btnEdit = findViewById(R.id.btnEdit)

        val classId = intent.getStringExtra("classId") ?: return

        database.child(classId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(ClassData::class.java) ?: return
                tvDetail.text = """
                    ชื่อวิชา: ${data.className}
                    รหัสวิชา: ${data.subjectCode}
                    ชื่ออาจารย์: ${data.teacherName}
                    วันที่เรียน: ${data.startTime} - ${data.endTime}
                    เวลาสาย: ${data.lateTime}
                    ห้องเรียน: ${data.classRoom}
                    ปีการศึกษา: ${data.year}
                    ภาคเรียน: ${data.semester}
                """.trimIndent()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ClassDetailActivity, "โหลดข้อมูลล้มเหลว", Toast.LENGTH_SHORT).show()
            }
        })

        btnDelete.setOnClickListener {
            database.child(classId).removeValue()
            Toast.makeText(this, "ลบคลาสแล้ว", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnEdit.setOnClickListener {
            Toast.makeText(this, "ฟังก์ชันแก้ไขยังไม่เปิดใช้งาน", Toast.LENGTH_SHORT).show()
        }
    }
}
