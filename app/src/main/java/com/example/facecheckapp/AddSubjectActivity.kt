package com.example.facecheckapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AddSubjectActivity : AppCompatActivity() {

    private lateinit var edtSubjectCode: EditText
    private lateinit var btnJoinSubject: Button

    private val db = FirebaseDatabase.getInstance()
    private val uid = FirebaseAuth.getInstance().uid!!
    private val userSubjectsRef = db.getReference("students").child(uid).child("subjects")
    private val classesRef = db.getReference("classes")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_subject)

        edtSubjectCode = findViewById(R.id.edtSubjectCode)
        btnJoinSubject = findViewById(R.id.btnJoinSubject)

        btnJoinSubject.setOnClickListener {
            joinSubject()
        }
    }

    // ใน AddSubjectActivity.kt
    // ใน AddSubjectActivity.kt

    private fun joinSubject() {
        val code = edtSubjectCode.text.toString().trim().uppercase()

        if (code.isEmpty()) {
            Toast.makeText(this, "กรุณากรอกรหัสวิชา", Toast.LENGTH_SHORT).show()
            return
        }

        classesRef.get()
            .addOnSuccessListener { snap ->
                var foundClassId: String? = null
                // ✅ ประกาศ teacherUid นอกลูป
                var teacherUid: String? = null

                for (child in snap.children) {
                    val sc = child.child("subjectCode").value?.toString()?.trim()?.uppercase()

                    if (sc == code) {
                        foundClassId = child.key
                        // 🔴 1. ดึง Teacher UID และกำหนดค่าให้กับตัวแปรนอกลูป
                        teacherUid = child.child("createdBy").value?.toString()
                        break
                    }
                }

                // ❌ ตรวจสอบว่าพบ class และ teacherUid หรือไม่
                if (foundClassId.isNullOrEmpty()) {
                    Toast.makeText(this, "ไม่พบรายวิชานี้ในระบบ", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                // ✅ ตอนนี้ teacherUid ใช้งานได้แล้ว
                if (teacherUid.isNullOrEmpty()) {
                    Toast.makeText(this, "ข้อมูลอาจารย์ผู้สร้างคลาสไม่สมบูรณ์", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // 🟢 2. ดึงข้อมูลนักเรียนปัจจุบัน (โค้ดส่วนนี้ไม่ได้แก้ไข)
                db.getReference("users").child(uid).get().addOnSuccessListener { userSnap ->
                    val firstName = userSnap.child("first_name").value?.toString() ?: "นักเรียน"
                    val lastName = userSnap.child("last_name").value?.toString() ?: ""
                    val studentId = userSnap.child("id").value?.toString() ?: uid // ใช้ UID เป็น Student ID fallback

                    val studentData = mapOf(
                        "first_name" to firstName,
                        "last_name" to lastName,
                        "status" to "ปกติ"
                    )

                    // 🔵 3. เพิ่มนักเรียนเข้าโหนดคลาสของอาจารย์
                    // ใช้ teacherUid ที่ดึงมาอย่างถูกต้อง
                    val teacherClassRef = db.getReference("classes/$teacherUid/$foundClassId/students/$studentId")

                    teacherClassRef.setValue(studentData)
                        .addOnSuccessListener {
                            // ✅ 4. เพิ่มวิชาเข้าโหนดของนักเรียนเอง
                            userSubjectsRef.child(foundClassId).setValue(true)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "เข้าร่วมวิชาเรียบร้อย", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "เกิดข้อผิดพลาดในการบันทึกวิชา", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "เกิดข้อผิดพลาดในการเพิ่มรายชื่ออาจารย์", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "เชื่อมต่อฐานข้อมูลผิดพลาด", Toast.LENGTH_SHORT).show()
            }
    }

    /** 🔽 โค้ด Bottom Navigation แยกเป็นฟังก์ชัน 🔽 */
    private fun setupBottomNav() {
        val navHome = findViewById<LinearLayout>(R.id.navHome)
        val navHistory = findViewById<LinearLayout>(R.id.navHistory)
        val navSetting = findViewById<LinearLayout>(R.id.navSetting)

        navHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            overridePendingTransition(0, 0)
        }

        navHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
            overridePendingTransition(0, 0)
        }

        navSetting.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
            overridePendingTransition(0, 0)
        }
    }
}
