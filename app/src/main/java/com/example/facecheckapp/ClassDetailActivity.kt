package com.example.facecheckapp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.database.*

class ClassDetailActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference

    private lateinit var tabInfo: TextView
    private lateinit var tabStudent: TextView
    private lateinit var tabReportTerm: TextView
    private lateinit var tabReportDay: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var btnDeleteClass: Button
    private lateinit var btnEditClass: Button

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

    // ตัวแปร Snapshot เพื่อเก็บข้อมูลเวลาเดิมที่ละเอียดก่อนส่งไปหน้าแก้ไข
    private var snapshotClassTime: String = "-"
    private var snapshotDayTime: String = "-" // เพิ่ม DayTime สำหรับส่งไปแก้ไขวันที่
    private var snapshotStartTime: String = "-"
    private var snapshotLateTime: String = "-"
    private var snapshotEndTime: String = "-"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_class_detail)

        dbRef = FirebaseDatabase.getInstance().reference.child("classes")
        classId = intent.getStringExtra("classId")

        if (classId.isNullOrEmpty()) {
            Toast.makeText(this, "ไม่พบข้อมูลคลาส", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // เชื่อม View
        btnBack = findViewById(R.id.btnBack)
        tabInfo = findViewById(R.id.tabInfo)
        tabStudent = findViewById(R.id.tabStudent)
        tabReportTerm = findViewById(R.id.tabReportTerm)
        tabReportDay = findViewById(R.id.tabReportDay)
        btnDeleteClass = findViewById(R.id.btnDeleteClass)
        btnEditClass = findViewById(R.id.btnEditClass)

        tvTitle = findViewById(R.id.tvTitle)
        tvSubjectName = findViewById(R.id.tvSubjectName)
        tvSubjectCode = findViewById(R.id.tvSubjectCode)
        tvTeacherName = findViewById(R.id.tvTeacherName)
        tvDayTime = findViewById(R.id.tvDayTime)
        tvCheckTime = findViewById(R.id.tvCheckTime)
        tvClassRoom = findViewById(R.id.tvClassRoom)
        tvYear = findViewById(R.id.tvYear)
        tvSemester = findViewById(R.id.tvSemester)

        // ย้อนกลับ
        btnBack.setOnClickListener {
            val intent = Intent(this, TeacherHomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }


        // ตั้งแท็บเริ่มต้น
        setActiveTab(tabInfo)

        // ไปหน้า รายชื่อนักศึกษา
        tabStudent.setOnClickListener {
            val intent = Intent(this, StudentListActivity::class.java)
            intent.putExtra("classId", classId)
            startActivity(intent)
        }

        // แท็บ Report
        tabReportDay.setOnClickListener {
            val intent = Intent(this, RealTimeActivity::class.java)
            intent.putExtra("classId", classId)
            startActivity(intent)
        }

        tabReportTerm.setOnClickListener {
            val intent = Intent(this, TermActivity::class.java)
            intent.putExtra("classId", classId)
            startActivity(intent)
        }

        // ลบคลาส
        btnDeleteClass.setOnClickListener { confirmDeleteClass() }

        // เปิดหน้าแก้ไข
        btnEditClass.setOnClickListener {
            val intent = Intent(this, EditClassActivity::class.java)

            intent.putExtra("classId", classId)

            // 1. ดึงค่าจาก TextViews (ต้องระวังการใช้ Label เช่น "ผู้สอน: ")
            intent.putExtra("className", tvSubjectName.text.toString())
            intent.putExtra("subjectCode", tvSubjectCode.text.toString())

            // 💡 ตัดคำนำหน้า "ผู้สอน: " ออกก่อนส่ง
            val teacherText = tvTeacherName.text.toString().replace("ผู้สอน: ", "")
            intent.putExtra("teacherName", teacherText)

            intent.putExtra("classRoom", tvClassRoom.text.toString())
            intent.putExtra("year", tvYear.text.toString())
            intent.putExtra("semester", tvSemester.text.toString())

            // 2. ข้อมูลเวลาเดิม (จาก Snapshot)
            intent.putExtra("classTime", snapshotClassTime)
            intent.putExtra("dayTime", snapshotDayTime) // ส่ง DayTime
            intent.putExtra("startTime", snapshotStartTime)
            intent.putExtra("lateTime", snapshotLateTime)
            intent.putExtra("endTime", snapshotEndTime)

            startActivity(intent)
        }
    }

    //  เพิ่ม onResume() เพื่อโหลดข้อมูลใหม่ทุกครั้งที่กลับมา
    override fun onResume() {
        super.onResume()
        loadClassData()
    }



    private fun loadClassData() {
        dbRef.child(classId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (!snapshot.exists()) return

                val className = snapshot.child("className").getValue(String::class.java) ?: "-"
                val subjectCode = snapshot.child("subjectCode").getValue(String::class.java) ?: "-"
                val teacherName = snapshot.child("teacherName").getValue(String::class.java) ?: "-"
                val classRoom = snapshot.child("classRoom").getValue(String::class.java) ?: "-"
                val year = snapshot.child("year").getValue(String::class.java) ?: "-"
                val semester = snapshot.child("semester").getValue(String::class.java) ?: "-"

                // ดึงข้อมูลเวลาที่ละเอียดและข้อมูลรวม
                val classTime = snapshot.child("classTime").getValue(String::class.java) ?: "-"
                val dayTime = snapshot.child("dayTime").getValue(String::class.java) ?: "-" // ดึง DayTime

                val startTime = snapshot.child("startTime").getValue(String::class.java) ?: "-"
                val lateTime = snapshot.child("lateTime").getValue(String::class.java) ?: "-"
                val endTime = snapshot.child("endTime").getValue(String::class.java) ?: "-"

                // 💾 เก็บค่า snapshot สำหรับส่งไปหน้าแก้ไข
                snapshotClassTime = classTime
                snapshotDayTime = dayTime // เก็บ DayTime
                snapshotStartTime = startTime
                snapshotLateTime = lateTime
                snapshotEndTime = endTime

                tvTitle.text = className
                tvSubjectName.text = className
                tvSubjectCode.text = subjectCode
                tvTeacherName.text = teacherName// แสดงผลพร้อม Label
                tvDayTime.text = classTime // แสดงผล วัน-เวลาเรียนรวม
                tvClassRoom.text = classRoom
                tvYear.text = year
                tvSemester.text = semester

                // เวลา 3 สี
                val text = SpannableStringBuilder()

                val green = "ตรง "
                text.append(green)
                text.setSpan(ForegroundColorSpan(Color.parseColor("#00C853")), 0, green.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                text.append(startTime).append("  ")

                val orange = "สาย "
                text.append(orange)
                text.setSpan(ForegroundColorSpan(Color.parseColor("#FF8C00")), text.length - orange.length, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                text.append(lateTime).append("  ")

                val red = "ขาด "
                text.append(red)
                text.setSpan(ForegroundColorSpan(Color.parseColor("#E53935")), text.length - red.length, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                text.append(endTime)

                tvCheckTime.text = text
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ClassDetailActivity, "โหลดข้อมูลล้มเหลว", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun confirmDeleteClass() {
        AlertDialog.Builder(this)
            .setTitle("ยืนยันการลบคลาส")
            .setMessage("คุณแน่ใจหรือไม่ว่าต้องการลบคลาสนี้?\nข้อมูลทั้งหมดจะหายไปถาวร")
            .setPositiveButton("ตกลง") { _, _ ->
                deleteClassFromFirebase()
            }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }

    private fun deleteClassFromFirebase() {
        val classIdToDelete = classId ?: return
        val dbRoot = FirebaseDatabase.getInstance().reference

        // 1. ลบข้อมูลหลัก (Classes) และข้อมูล Students ที่ผูกกับ ClassId
        val deletionTasks = mutableListOf<com.google.android.gms.tasks.Task<Void>>()

        // Task 1: ลบโหนดหลักของคลาส (classes/{classId})
        deletionTasks.add(dbRoot.child("classes").child(classIdToDelete).removeValue())

        // Task 2: ลบข้อมูลนักศึกษาที่ผูกกับคลาส (สมมติว่าอยู่ใน students/{classId})
        // (อิงตามโค้ดเดิมของคุณที่พยายามลบ /students/$classId)
        deletionTasks.add(dbRoot.child("students").child(classIdToDelete).removeValue())


        // Task 3: ลบรายการประวัติการเช็คชื่อทั้งหมดที่เกี่ยวข้องกับคลาสนี้
        // (สมมติว่าแต่ละรายการใน 'history' มี field ชื่อ 'classId' เก็บอยู่)

        val historyQuery = dbRoot.child("history").orderByChild("classId").equalTo(classIdToDelete)

        historyQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                // เก็บ Task การลบแต่ละรายการใน history
                for (historySnap in snapshot.children) {
                    deletionTasks.add(historySnap.ref.removeValue())
                }

                // รัน Task การลบทั้งหมดพร้อมกัน
                com.google.android.gms.tasks.Tasks.whenAll(deletionTasks)
                    .addOnSuccessListener {
                        Toast.makeText(this@ClassDetailActivity, "ลบคลาสและข้อมูลที่เกี่ยวข้องทั้งหมดเรียบร้อย ✅", Toast.LENGTH_LONG).show()
                        // กลับไปหน้า Home หลังจากลบสำเร็จ
                        val intent = Intent(this@ClassDetailActivity, TeacherHomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this@ClassDetailActivity, "❌ ลบคลาสไม่สำเร็จ: ${e.message}", Toast.LENGTH_LONG).show()
                        Log.e("ClassDetailActivity", "Bulk Delete error: ${e.message}")
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ClassDetailActivity, "❌ การค้นหาข้อมูลล้มเหลว: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setActiveTab(activeTab: TextView) {

        val allTabs = listOf(tabInfo, tabStudent, tabReportDay, tabReportTerm)

        allTabs.forEach {
            it.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
            it.setBackgroundResource(R.drawable.tab_unselected_bg)
        }

        activeTab.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark))
        activeTab.setBackgroundResource(R.drawable.tab_selected_bg)
    }

}