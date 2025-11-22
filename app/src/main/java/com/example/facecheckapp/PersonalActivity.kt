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

class PersonalActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvStudentId: TextView

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal)

        tvName = findViewById(R.id.tvName)
        tvStudentId = findViewById(R.id.tvStudentId)

        loadUserInfo()
        setupLogout()
        setupNotification()
        setupBottomNav()
    }

    /** ⭐ โหลดชื่อ + รหัสนักศึกษา จาก Firebase */
    private fun loadUserInfo() {
        val uid = auth.uid ?: return

        val userRef = db.child("users").child(uid).child("faceEmbedding")

        userRef.get().addOnSuccessListener { snap ->
            if (!snap.exists()) {
                tvName.text = "ไม่พบข้อมูลผู้ใช้"
                tvStudentId.text = "-"
                return@addOnSuccessListener
            }

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
    private fun setupNotification() {
        val btnNotification = findViewById<ImageButton>(R.id.btnNotification)
        btnNotification.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }
    }

    /** Bottom Navigation */
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
