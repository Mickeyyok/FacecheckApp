package com.example.facecheckapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class PersonalActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance()
    
    private lateinit var tvName: TextView
    private lateinit var tvStudentId: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var btnLogout: Button
    private lateinit var profileImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()

        // Bind UI elements
        tvName = findViewById(R.id.tvName)
        tvStudentId = findViewById(R.id.tvStudentId)
        btnBack = findViewById(R.id.btnBack)
        btnLogout = findViewById(R.id.btnLogout)
        profileImage = findViewById(R.id.profileImage)

        // ดึงข้อมูลผู้ใช้
        loadUserData()

        // ปุ่มย้อนกลับ
        btnBack.setOnClickListener {
            finish()
        }

        // ปุ่มออกจากระบบ
        btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        val userUid = currentUser?.uid

        if (userUid == null) {
            // ถ้าไม่ได้ล็อกอิน ให้ไปหน้า MainActivity
            Toast.makeText(this, "กรุณาเข้าสู่ระบบ", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // Reference ไปที่ users/{uid}
        val userRef = database.getReference("users").child(userUid)

        // ดึงข้อมูลแบบครั้งเดียว (Single Value Event)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // ดึงข้อมูลแต่ละ field
                    val firstName = snapshot.child("first_name").value?.toString() ?: ""
                    val lastName = snapshot.child("last_name").value?.toString() ?: ""
                    val studentId = snapshot.child("id").value?.toString() ?: ""

                    // รวมชื่อ-นามสกุล
                    val fullName = "$firstName $lastName".trim()

                    // แสดงข้อมูลใน TextView
                    if (fullName.isNotEmpty()) {
                        tvName.text = fullName
                    } else {
                        tvName.text = "ไม่พบข้อมูลชื่อ"
                    }

                    if (studentId.isNotEmpty()) {
                        tvStudentId.text = studentId
                    } else {
                        tvStudentId.text = "-"
                    }
                } else {
                    // ถ้าไม่พบข้อมูล
                    tvName.text = "ไม่พบข้อมูล"
                    tvStudentId.text = "-"
                    Toast.makeText(this@PersonalActivity, "ไม่พบข้อมูลผู้ใช้", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // กรณีเกิด error
                Toast.makeText(
                    this@PersonalActivity,
                    "ไม่สามารถดึงข้อมูลได้: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                tvName.text = "เกิดข้อผิดพลาด"
                tvStudentId.text = "-"
            }
        })
    }

    private fun logout() {
        // Sign out จาก Firebase Authentication
        auth.signOut()

        // แสดงข้อความ
        Toast.makeText(this, "ออกจากระบบสำเร็จ", Toast.LENGTH_SHORT).show()

        // กลับไปหน้า MainActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}


