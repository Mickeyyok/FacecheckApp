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

    private fun joinSubject() {

        val code = edtSubjectCode.text.toString().trim().uppercase()

        if (code.isEmpty()) {
            Toast.makeText(this, "กรุณากรอกรหัสวิชา", Toast.LENGTH_SHORT).show()
            return
        }

        // ⭐ โหลด classes ทั้งหมดแล้ววนหาเอง (แม่นที่สุด)
        classesRef.get()
            .addOnSuccessListener { snap ->

                var foundClassId: String? = null

                for (child in snap.children) {
                    val sc = child.child("subjectCode").value?.toString()?.trim()?.uppercase()

                    if (sc == code) {
                        foundClassId = child.key
                        break
                    }
                }

                if (foundClassId == null) {
                    Toast.makeText(this, "ไม่พบรายวิชานี้ในระบบ", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // ⭐ เพิ่ม classId ให้ user
                userSubjectsRef.child(foundClassId!!).setValue(true)
                    .addOnSuccessListener {
                        Toast.makeText(this, "เข้าร่วมวิชาเรียบร้อย", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "เกิดข้อผิดพลาด", Toast.LENGTH_SHORT).show()
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
