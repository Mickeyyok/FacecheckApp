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

import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var tvSelectedSubject: TextView
    private lateinit var btnAddSubject: Button
    private lateinit var btnCheckin: Button

    private val uid = FirebaseAuth.getInstance().uid!!
    private lateinit var db: FirebaseDatabase
    private lateinit var userSubjectsRef: DatabaseReference

    private val PICK_SUBJECT = 2000
    private var selectedClassId: String? = null  // ⭐ เก็บวิชาที่เลือกปัจจุบัน

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        tvSelectedSubject = findViewById(R.id.tvSelectedSubject)
        btnAddSubject = findViewById(R.id.btnAddSubject)
        btnCheckin = findViewById(R.id.btnCheckin)

        db = FirebaseDatabase.getInstance()
        userSubjectsRef = db.getReference("students").child(uid).child("subjects")

        loadSelectedSubject()

        btnAddSubject.setOnClickListener {
            startActivity(Intent(this, AddSubjectActivity::class.java))
        }

        // 👉 เปิดหน้าเลือกวิชาใหม่
        tvSelectedSubject.setOnClickListener {
            val intent = Intent(this, SubjectListActivity::class.java)
            startActivityForResult(intent, PICK_SUBJECT)
        }

        // 👉 ปุ่มเช็คชื่อ
        btnCheckin.setOnClickListener {
            openLocationCheck()
        }

        setupBottomNav()
    }

    /** 🔽 Bottom Navigation */
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

    /** โหลดวิชาที่เลือกไว้ล่าสุด */
    private fun loadSelectedSubject() {

        // ถ้าเลือกวิชาไว้ก่อนแล้ว
        selectedClassId?.let { id ->
            loadSubjectById(id)
            return
        }

        userSubjectsRef.get().addOnSuccessListener { snap ->
            if (!snap.exists()) {
                tvSelectedSubject.text = "กรุณาเลือกวิชา"
                disableCheckin("ยังไม่ได้เลือกวิชา")
                return@addOnSuccessListener
            }

            val classId = snap.children.first().key ?: return@addOnSuccessListener
            selectedClassId = classId
            loadSubjectById(classId)
        }
    }

    /** โหลดข้อมูลคลาสและตรวจสิทธิ์เวลา */
    private fun loadSubjectById(classId: String) {

        val ref = db.getReference("classes").child(classId)

        ref.get().addOnSuccessListener { data ->

            if (!data.exists()) {
                tvSelectedSubject.text = "คลาสถูกลบโดยอาจารย์"
                disableCheckin("คลาสถูกลบ")
                return@addOnSuccessListener
            }

            val code = data.child("subjectCode").value.toString()
            val name = data.child("className").value.toString()
            val room = data.child("classRoom").value.toString()
            val start = data.child("startTime").value.toString()
            val end = data.child("endTime").value.toString()

            val timeLine = "$start - $end น."

            tvSelectedSubject.text =
                "$code $name\nอาคาร $room ห้อง $room\n$timeLine"

            // ⭐ ตรวจเวลาว่ากดเช็คชื่อได้หรือไม่
            checkClassTime(start, end)
        }
    }

    /** ⭐ เช็คเวลาเรียนว่าปุ่มเช็คชื่อควรเปิด/ปิด */
    private fun checkClassTime(start: String, end: String) {

        if (start.isEmpty() || end.isEmpty()) {
            disableCheckin("ไม่มีข้อมูลเวลาเรียน")
            return
        }

        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

        val now = sdf.parse(getCurrentTime())
        val startTime = sdf.parse(start)
        val endTime = sdf.parse(end)

        when {
            now.before(startTime) -> {
                disableCheckin("ยังไม่ถึงเวลาเรียน")
            }
            now.after(endTime) -> {
                disableCheckin("หมดเวลาเช็คชื่อแล้ว")
            }
            else -> {
                enableCheckin()
            }
        }
    }

    /** ปิดปุ่มเช็คชื่อ */
    private fun disableCheckin(text: String) {
        btnCheckin.isEnabled = false
        btnCheckin.alpha = 0.45f
        btnCheckin.text = text
    }

    /** เปิดปุ่มเช็คชื่อ */
    private fun enableCheckin() {
        btnCheckin.isEnabled = true
        btnCheckin.alpha = 1f
        btnCheckin.text = "เช็คชื่อ"
    }

    /** เวลา ณ ปัจจุบัน */
    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    /** ไปหน้าเช็คชื่อ LocationCheckActivity */
    private fun openLocationCheck() {

        val classId = selectedClassId ?: return

        db.getReference("classes").child(classId).get()
            .addOnSuccessListener { data ->

                val subjectCode = data.child("subjectCode").value.toString()
                val className = data.child("className").value.toString()
                val classRoom = data.child("classRoom").value.toString()
                val start = data.child("startTime").value.toString()
                val end = data.child("endTime").value.toString()

                val timeLine = "$start - $end น."

                val intent = Intent(this, LocationCheckActivity::class.java)
                intent.putExtra("subjectCode", subjectCode)
                intent.putExtra("className", className)
                intent.putExtra("classRoom", classRoom)
                intent.putExtra("classTime", timeLine)
                intent.putExtra("classId", classId)
                startActivity(intent)
            }
    }

    /** เมื่อผู้ใช้เลือกวิชาใหม่ */
    override fun onActivityResult(req: Int, result: Int, data: Intent?) {
        super.onActivityResult(req, result, data)

        if (req == PICK_SUBJECT && result == Activity.RESULT_OK && data != null) {

            val classId = data.getStringExtra("selectedClassId") ?: return
            selectedClassId = classId

            loadSubjectById(classId)
        }
    }
}
