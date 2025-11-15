package com.example.facecheckapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeActivity : AppCompatActivity() {

    private lateinit var tvSelectedSubject: TextView
    private lateinit var btnAddSubject: Button

    private val uid = FirebaseAuth.getInstance().uid!!
    private lateinit var db: FirebaseDatabase
    private lateinit var userSubjectsRef: DatabaseReference

    private val PICK_SUBJECT = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        tvSelectedSubject = findViewById(R.id.tvSelectedSubject)
        btnAddSubject = findViewById(R.id.btnAddSubject)

        db = FirebaseDatabase.getInstance()
        userSubjectsRef = db.getReference("students").child(uid).child("subjects")

        loadSelectedSubject()

        btnAddSubject.setOnClickListener {
            startActivity(Intent(this, AddSubjectActivity::class.java))
        }

        tvSelectedSubject.setOnClickListener {
            val intent = Intent(this, SubjectListActivity::class.java)
            startActivityForResult(intent, PICK_SUBJECT)
        }

        // ⬇⬇⬇ เรียก Bottom Navigation
        setupBottomNav()
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



    /** โหลดวิชาแรกของนักศึกษา */
    private fun loadSelectedSubject() {
        userSubjectsRef.get().addOnSuccessListener { snap ->
            if (!snap.exists()) {
                tvSelectedSubject.text = "กรุณาเลือกวิชา"
                return@addOnSuccessListener
            }

            val classId = snap.children.first().key ?: return@addOnSuccessListener

            db.getReference("classes").child(classId).get()
                .addOnSuccessListener { data ->
                    if (!data.exists()) {
                        tvSelectedSubject.text = "คลาสถูกลบโดยอาจารย์"
                        return@addOnSuccessListener
                    }

                    val code = data.child("subjectCode").getValue(String::class.java) ?: ""
                    val name = data.child("className").getValue(String::class.java) ?: ""
                    val room = data.child("classRoom").getValue(String::class.java) ?: ""

                    val start = data.child("startTime").getValue(String::class.java) ?: ""
                    val end = data.child("endTime").getValue(String::class.java) ?: ""
                    val classTime = data.child("classTime").getValue(String::class.java) ?: ""

                    val timeLine = when {
                        start.isNotEmpty() && end.isNotEmpty() -> "$start - $end น."
                        classTime.isNotEmpty() -> classTime
                        else -> "-"
                    }

                    tvSelectedSubject.text = "$code $name\nอาคาร $room ห้อง $room\n$timeLine"
                }
        }
    }


    /** รับค่าจากหน้า SubjectList เมื่อเลือกวิชาใหม่ */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_SUBJECT && resultCode == Activity.RESULT_OK && data != null) {
            val code = data.getStringExtra("selectedSubjectCode") ?: ""
            val name = data.getStringExtra("selectedClassName") ?: ""
            val room = data.getStringExtra("selectedClassRoom") ?: ""
            val time = data.getStringExtra("selectedClassTime") ?: "-"

            tvSelectedSubject.text = "$code $name\nอาคาร $room ห้อง $room\n$time"
        }
    }
}
