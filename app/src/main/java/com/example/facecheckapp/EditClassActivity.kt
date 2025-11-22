package com.example.facecheckapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class EditClassActivity : AppCompatActivity() {

    // เชื่อม View กับ ID ตาม Layout activity_edit_class.xml
    private lateinit var etClassName: EditText
    private lateinit var etClassRoom: EditText
    private lateinit var etSubjectCode: EditText
    private lateinit var etTeacherName: EditText
    private lateinit var etYear: EditText
    private lateinit var etSemester: EditText
    private lateinit var btnNext: Button
    private lateinit var btnBack: ImageButton // ImageButton ใน Header
    private lateinit var btnCancelBottom: Button // ปุ่มยกเลิกด้านล่าง (ID ใหม่)

    // ตัวแปรสำหรับเก็บ classId และข้อมูลเวลาเดิม
    private var classId: String? = null
    private lateinit var dbRef: DatabaseReference
    private var snapshotClassTime: String = "-"
    private var snapshotStartTime: String = "-"
    private var snapshotLateTime: String = "-"
    private var snapshotEndTime: String = "-"
    private var snapshotDayTime: String = "-"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_class)

        dbRef = FirebaseDatabase.getInstance().getReference("classes")
        classId = intent.getStringExtra("classId")

        if (classId.isNullOrEmpty()) {
            Toast.makeText(this, "ไม่พบรหัสคลาสสำหรับการแก้ไข", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // ✅ ผูก View กับ ID
        etClassName = findViewById(R.id.etClassName)
        etClassRoom = findViewById(R.id.etClassRoom)
        etSubjectCode = findViewById(R.id.etSubjectCode)
        etTeacherName = findViewById(R.id.etTeacherName)
        etYear = findViewById(R.id.etYear)
        etSemester = findViewById(R.id.etSemester)

        btnNext = findViewById(R.id.btnNext)
        // 1. ImageButton ใน Header
        btnBack = findViewById<ImageButton>(R.id.btnBackHeader)
        // 2. Button ยกเลิกด้านล่าง (ID ที่แก้ไขแล้ว)
        btnCancelBottom = findViewById(R.id.btnCancelBottom)

        // 🌟 โหลดข้อมูลเดิมเข้ามาในช่องแก้ไข
        loadClassData()

        // 🔙 ปุ่มย้อนกลับ/ยกเลิก
        btnBack.setOnClickListener { finish() }
        btnCancelBottom.setOnClickListener { finish() }


        // ➡ ปุ่มถัดไป (ส่งข้อมูลไปหน้าแก้ไขเวลา)
        btnNext.setOnClickListener {
            val className = etClassName.text.toString().trim()
            val classRoom = etClassRoom.text.toString().trim()
            val subjectCode = etSubjectCode.text.toString().trim()
            val teacherName = etTeacherName.text.toString().trim()
            val year = etYear.text.toString().trim()
            val semester = etSemester.text.toString().trim()

            // ✅ ตรวจสอบช่องว่าง (ข้อมูลหลัก)
            if (className.isEmpty() || subjectCode.isEmpty() || teacherName.isEmpty() ||
                year.isEmpty() || semester.isEmpty() || classRoom.isEmpty()
            ) {
                Toast.makeText(this, "กรุณากรอกข้อมูลหลักให้ครบทุกช่อง", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ ส่งข้อมูลที่แก้ไขแล้ว (รวมถึงข้อมูลเวลาเดิม) ไปหน้า EditTimeActivity
            val intent = Intent(this, EditTimeActivity::class.java).apply {
                putExtra("classId", classId) // ID คลาสที่ใช้ในการอัปเดต

                // ข้อมูลหลักที่แก้ไขแล้ว
                putExtra("className", className)
                putExtra("classRoom", classRoom)
                putExtra("subjectCode", subjectCode)
                putExtra("teacherName", teacherName)
                putExtra("year", year)
                putExtra("semester", semester)

                // ข้อมูลเวลาเดิม (เพื่อให้ EditTimeActivity นำไปแสดงและอัปเดต)
                putExtra("classTime", snapshotClassTime)
                putExtra("dayTime", snapshotDayTime)
                putExtra("startTime", snapshotStartTime)
                putExtra("lateTime", snapshotLateTime)
                putExtra("endTime", snapshotEndTime)
            }
            startActivity(intent)
        }
    }

    /** ⭐ โหลดข้อมูลวิชาเดิมจาก Firebase และแสดงผลใน EditTexts */
    private fun loadClassData() {
        dbRef.child(classId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(this@EditClassActivity, "คลาสถูกลบแล้ว", Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }

                // ดึงข้อมูลและกำหนดให้ EditTexts
                etClassName.setText(snapshot.child("className").getValue(String::class.java) ?: "")
                etSubjectCode.setText(snapshot.child("subjectCode").getValue(String::class.java) ?: "")
                etTeacherName.setText(snapshot.child("teacherName").getValue(String::class.java) ?: "")
                etClassRoom.setText(snapshot.child("classRoom").getValue(String::class.java) ?: "")
                etYear.setText(snapshot.child("year").getValue(String::class.java) ?: "")
                etSemester.setText(snapshot.child("semester").getValue(String::class.java) ?: "")

                // 💾 เก็บข้อมูลเวลาเดิมไว้ในตัวแปร Snapshot
                snapshotClassTime = snapshot.child("classTime").getValue(String::class.java) ?: "-"
                snapshotDayTime = snapshot.child("dayTime").getValue(String::class.java) ?: "-"
                snapshotStartTime = snapshot.child("startTime").getValue(String::class.java) ?: "-"
                snapshotLateTime = snapshot.child("lateTime").getValue(String::class.java) ?: "-"
                snapshotEndTime = snapshot.child("endTime").getValue(String::class.java) ?: "-"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EditClassActivity, "โหลดข้อมูลเดิมไม่สำเร็จ", Toast.LENGTH_SHORT).show()
            }
        })
    }
}