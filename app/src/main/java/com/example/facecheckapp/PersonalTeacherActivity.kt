package com.example.facecheckapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class PersonalActivityTeacher : AppCompatActivity() { // ⭐ เปลี่ยนชื่อคลาสเป็น Teacher

    private lateinit var tvName: TextView
    private lateinit var tvStudentId: TextView // ⭐ อาจเปลี่ยนเป็น tvTeacherId หรือ tvId

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_teacher) // ⭐ ใช้ Layout เดิมได้ ถ้า UI เหมือนกัน

        tvName = findViewById(R.id.tvName)
        tvStudentId = findViewById(R.id.tvStudentId) // ⭐ หรือตาม ID ของ TextView ที่ใช้แสดง ID อาจารย์

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        loadUserInfo()
        setupLogout()

    }

    /** ⭐ โหลดชื่อ + รหัสอาจารย์ จาก Firebase */
    private fun loadUserInfo() {
        val uid = auth.uid ?: return

        // 🌟🌟🌟 แก้ไข Reference ให้ชี้ไปที่โหนดหลักของผู้ใช้ (users/{uid}) 🌟🌟🌟
        // สมมติว่าข้อมูล ชื่อ, สกุล, id อยู่ในโหนดหลักของ UID
        val userRef = db.child("users").child(uid)
        // ❌ โค้ดเดิม: val userRef = db.child("users").child(uid).child("faceEmbedding")

        userRef.get().addOnSuccessListener { snap ->
            if (!snap.exists()) {
                tvName.text = "ไม่พบข้อมูลผู้ใช้"
                tvStudentId.text = "-"
                return@addOnSuccessListener
            }

            // ตรวจสอบว่า key first_name, last_name, id อยู่ตรงนี้จริงหรือไม่
            // ถ้าข้อมูลอยู่ในโหนดหลัก userRef
            val first = snap.child("first_name").value?.toString() ?: ""
            val last = snap.child("last_name").value?.toString() ?: ""
            val sid = snap.child("id").value?.toString() ?: ""

            tvName.text = "$first $last"
            tvStudentId.text = sid
        }.addOnFailureListener {
            tvName.text = "โหลดข้อมูลไม่สำเร็จ"
            tvStudentId.text = "-"

        }
    }


    /** ปุ่มออกจากระบบ */
    private fun setupLogout() {
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    /** ปุ่มแจ้งเตือน */


    /** Bottom Navigation
    private fun setupBottomNav() {
        // ⭐ อาจต้องปรับเปลี่ยน Navigation ไปยัง Activity ของอาจารย์
        val navHome = findViewById<LinearLayout>(R.id.navHome)
        val navHistory = findViewById<LinearLayout>(R.id.navHistory)
        val navSetting = findViewById<LinearLayout>(R.id.navSetting)

        navHome.setOnClickListener {
            startActivity(Intent(this, HomeActivityTeacher::class.java)) // ⭐ เปลี่ยนเป็น HomeActivityTeacher
            overridePendingTransition(0, 0)
        }

        navHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivityTeacher::class.java)) // ⭐ เปลี่ยนเป็น HistoryActivityTeacher
            overridePendingTransition(0, 0)
        }

        navSetting.setOnClickListener {
            startActivity(Intent(this, SettingActivityTeacher::class.java)) // ⭐ เปลี่ยนเป็น SettingActivityTeacher
            overridePendingTransition(0, 0)
        }
    }*/
}