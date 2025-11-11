package com.example.facecheckapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class ClassDetailActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference

    private lateinit var tabInfo: TextView
    private lateinit var tabStudent: TextView
    private lateinit var tabReport: TextView
    private lateinit var btnBack: ImageButton

    private lateinit var tvTitle: TextView
    private lateinit var tvSubjectName: TextView
    private lateinit var tvSubjectCode: TextView
    private lateinit var tvTeacherName: TextView
    private lateinit var tvDayTime: TextView
    private lateinit var tvCheckTime: TextView
    private lateinit var tvClassRoom: TextView
    private lateinit var tvYear: TextView
    private lateinit var tvSemester: TextView

    private var classId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_class_detail)

        dbRef = FirebaseDatabase.getInstance().getReference("classes")
        classId = intent.getStringExtra("classId")

        if (classId.isNullOrEmpty()) {
            Toast.makeText(this, "ไม่พบข้อมูลคลาส", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d("ClassDetailActivity", "✅ Received classId = $classId")

        // ✅ เชื่อม View
        btnBack = findViewById(R.id.btnBack)
        tabInfo = findViewById(R.id.tabInfo)
        tabStudent = findViewById(R.id.tabStudent)
        tabReport = findViewById(R.id.tabReport)

        tvTitle = findViewById(R.id.tvTitle)
        tvSubjectName = findViewById(R.id.tvSubjectName)
        tvSubjectCode = findViewById(R.id.tvSubjectCode)
        tvTeacherName = findViewById(R.id.tvTeacherName)
        tvDayTime = findViewById(R.id.tvDayTime)
        tvCheckTime = findViewById(R.id.tvCheckTime)
        tvClassRoom = findViewById(R.id.tvClassRoom)
        tvYear = findViewById(R.id.tvYear)
        tvSemester = findViewById(R.id.tvSemester)

        // 🔹 ปุ่มย้อนกลับ
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // ✅ แท็บ "ข้อมูล" (active)
        setActiveTab(tabInfo)

        // 🔹 กดแท็บ “นักศึกษา”
        tabStudent.setOnClickListener {
            val intent = Intent(this, StudentListActivity::class.java)
            intent.putExtra("classId", classId)
            startActivity(intent)
        }

        // 🔹 กดแท็บ “การเข้ารายงาน”
        /*tabReport.setOnClickListener {
            val intent = Intent(this, ReportListActivity::class.java)
            intent.putExtra("classId", classId)
            startActivity(intent)
        }*/

        // โหลดข้อมูลคลาส
        loadClassData()
    }

    // ✅ ฟังก์ชันโหลดข้อมูลจาก Firebase
    private fun loadClassData() {
        dbRef.child(classId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(this@ClassDetailActivity, "ไม่พบข้อมูลในฐานข้อมูล", Toast.LENGTH_SHORT).show()
                    return
                }

                val className = snapshot.child("className").getValue(String::class.java) ?: "-"
                val subjectCode = snapshot.child("subjectCode").getValue(String::class.java) ?: "-"
                val teacherName = snapshot.child("teacherName").getValue(String::class.java) ?: "-"
                val classRoom = snapshot.child("classRoom").getValue(String::class.java) ?: "-"
                val year = snapshot.child("year").getValue(String::class.java) ?: "-"
                val semester = snapshot.child("semester").getValue(String::class.java) ?: "-"
                val classTime = snapshot.child("classTime").getValue(String::class.java) ?: "-"
                val startTime = snapshot.child("startTime").getValue(String::class.java) ?: "-"
                val lateTime = snapshot.child("lateTime").getValue(String::class.java) ?: "-"
                val endTime = snapshot.child("endTime").getValue(String::class.java) ?: "-"

                // แสดงข้อมูลใน UI
                tvTitle.text = className
                tvSubjectName.text = className
                tvSubjectCode.text = subjectCode
                tvTeacherName.text = teacherName
                tvDayTime.text = classTime
                tvClassRoom.text = classRoom
                tvYear.text = year
                tvSemester.text = semester

                // ✅ แสดงเวลาเช็กชื่อ (ตรง/สาย/ขาด)
                val text = SpannableStringBuilder()

                val green = "ตรง "
                text.append(green)
                text.setSpan(ForegroundColorSpan(Color.parseColor("#00C853")), 0, green.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                text.append(startTime).append("  ")

                val orange = "สาย "
                text.append(orange)
                text.setSpan(
                    ForegroundColorSpan(Color.parseColor("#FF8C00")),
                    text.length - orange.length, text.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                text.append(lateTime).append("  ")

                val red = "ขาด "
                text.append(red)
                text.setSpan(
                    ForegroundColorSpan(Color.parseColor("#E53935")),
                    text.length - red.length, text.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                text.append(endTime)

                tvCheckTime.text = text
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ClassDetailActivity, "เกิดข้อผิดพลาด: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("ClassDetailActivity", "❌ Database error: ${error.message}")
            }
        })
    }

    // ✅ เปลี่ยนสีแท็บที่ active
    private fun setActiveTab(activeTab: TextView) {
        val allTabs = listOf(tabInfo, tabStudent, tabReport)
        allTabs.forEach {
            it.setTextColor(Color.parseColor("#888888"))
            it.setBackgroundResource(R.drawable.tab_unselected_bg)
        }
        activeTab.setTextColor(Color.parseColor("#2196F3"))
        activeTab.setBackgroundResource(R.drawable.tab_selected_bg)
    }
}
