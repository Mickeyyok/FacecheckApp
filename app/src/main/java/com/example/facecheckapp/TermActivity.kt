package com.example.facecheckapp

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class TermActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton

    private lateinit var tvOnTimeCount: TextView
    private lateinit var tvLateCount: TextView
    private lateinit var tvAbsentCount: TextView
    private lateinit var tvTerm: TextView
    private lateinit var tvStudentCount: TextView

    private lateinit var rvStudents: RecyclerView
    private val studentStatsList = mutableListOf<TermStudentStats>()
    private lateinit var adapter: TermStudentStatsAdapter

    private var selectedTerm = 1
    private var classYearThai: Int? = null

    // Firebase
    private lateinit var db: DatabaseReference

    // cache users -> (fullName, studentId)
    private val usersCache = mutableMapOf<String, Pair<String, String>>()
    private var usersLoaded: Boolean = false

    // classId ส่งมาจากหน้าเลือกวิชา
    private val classId: String? by lazy {
        intent.getStringExtra("classId")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_term)

        btnBack = findViewById(R.id.btnBack)

        tvOnTimeCount = findViewById(R.id.tvOnTimeCount)
        tvLateCount = findViewById(R.id.tvLateCount)
        tvAbsentCount = findViewById(R.id.tvAbsentCount)
        tvTerm = findViewById(R.id.tvTerm)
        tvStudentCount = findViewById(R.id.tvStudentCount)

        rvStudents = findViewById(R.id.rvStudents)

        // Firebase Reference
        db = FirebaseDatabase
            .getInstance("https://facecheckapp-dea12-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .reference

        // RecyclerView
        adapter = TermStudentStatsAdapter(studentStatsList)
        rvStudents.layoutManager = LinearLayoutManager(this)
        rvStudents.adapter = adapter

        // Reset ค่าเมื่อเปลี่ยน class
        selectedTerm = 1
        classYearThai = null
        studentStatsList.clear()
        usersCache.clear()
        usersLoaded = false
        adapter.notifyDataSetChanged()

        // โหลด semester + year จาก classes ก่อน แล้วค่อยโหลดสถิติ
        loadClassTermAndStats()

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun updateTermDisplay() {
        val yearThai = classYearThai ?: (java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) + 543)
        tvTerm.text = "$selectedTerm / $yearThai"
    }

    // ----------------------------------------------------
    // โหลด semester + year จาก classes/{classId}
    // ----------------------------------------------------
    private fun loadClassTermAndStats() {
        val id = classId
        if (id.isNullOrEmpty()) {
            // Reset เป็นค่า default
            selectedTerm = 1
            classYearThai = null
            ensureUsersLoaded { loadTermStats() }
            return
        }

        // Reset ก่อนโหลดข้อมูลใหม่
        selectedTerm = 1
        classYearThai = null

        db.child("classes").child(id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // semester
                    val semStr = snapshot.child("semester").getValue(String::class.java)
                    val semInt = semStr?.toIntOrNull()
                    if (semInt != null) {
                        selectedTerm = semInt
                    }

                    // year (พ.ศ.)
                    val yearStr = snapshot.child("year").getValue(String::class.java)
                    val yearThaiInt = yearStr?.toIntOrNull()
                    if (yearThaiInt != null) {
                        classYearThai = yearThaiInt
                    }

                    android.util.Log.d("TermActivity", 
                        "Loaded class: classId=$id, semester=$selectedTerm, yearThai=$classYearThai")

                    updateTermDisplay()
                    ensureUsersLoaded { loadTermStats() }
                }

                override fun onCancelled(error: DatabaseError) {
                    android.util.Log.e("TermActivity", 
                        "Failed to load class: ${error.message}")
                    ensureUsersLoaded { loadTermStats() }
                }
            })
    }

    // ----------------------------------------------------
    // โหลด users ทั้งหมดเข้า cache
    // ----------------------------------------------------
    private fun ensureUsersLoaded(onReady: () -> Unit) {
        if (usersLoaded) {
            onReady()
            return
        }

        db.child("users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    usersCache.clear()

                    for (userSnap in snapshot.children) {
                        val uid = userSnap.key ?: continue

                        // อ่านข้อมูลจาก users/{uid}/... โดยตรง (ไม่ใช่ faceEmbedding)
                        val role = userSnap.child("role").getValue(String::class.java) ?: ""
                        if (role != "student") continue

                        val firstName =
                            userSnap.child("first_name").getValue(String::class.java) ?: ""
                        val lastName =
                            userSnap.child("last_name").getValue(String::class.java) ?: ""
                        val studentId =
                            userSnap.child("id").getValue(String::class.java) ?: ""

                        val fullName = listOf(firstName, lastName)
                            .filter { it.isNotBlank() }
                            .joinToString(" ")

                        usersCache[uid] = fullName to studentId
                    }

                    usersLoaded = true
                    onReady()
                }

                override fun onCancelled(error: DatabaseError) {
                    usersLoaded = true
                    onReady()
                }
            })
    }

    // ----------------------------------------------------
    // โหลดสถิติรายเทอม
    //   history/{uid}/{historyId}
    // ----------------------------------------------------
    private fun loadTermStats() {
        // ปี ค.ศ. (history.year เก็บเป็น 2025 ฯลฯ)
        // ถ้า classYearThai > 2500 แสดงว่าเป็น พ.ศ. ต้องลบ 543
        // ถ้า <= 2500 แสดงว่าเป็น ค.ศ. แล้ว ไม่ต้องลบ
        val yearAd = if (classYearThai != null) {
            if (classYearThai!! > 2500) {
                classYearThai!! - 543  // พ.ศ. -> ค.ศ.
            } else {
                classYearThai!!  // ค.ศ. แล้ว
            }
        } else {
            java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        }
        val yearAdStr = yearAd.toString()
        val termStr = selectedTerm.toString()

        studentStatsList.clear()
        var totalOnTimeCount = 0
        var totalLateCount = 0
        var totalAbsentCount = 0

        // Map สำหรับเก็บสถิติของแต่ละ student: uid -> TermStudentStats
        val statsMap = mutableMapOf<String, TermStudentStats>()

        db.child("history")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var totalRecords = 0
                    var yearFiltered = 0
                    var semesterFiltered = 0
                    var classIdFiltered = 0

                    for (userHistSnap in snapshot.children) {
                        val uid = userHistSnap.key ?: continue

                        for (recordSnap in userHistSnap.children) {
                            totalRecords++

                            val yearInDb =
                                recordSnap.child("year").getValue(String::class.java) ?: ""
                            val sem =
                                recordSnap.child("semester").getValue(String::class.java) ?: ""

                            // ถ้ามี year ใน record ให้กรองตามนั้น
                            // ถ้าไม่มี (ข้อมูลเก่า) ให้ข้ามการกรอง year
                            if (yearInDb.isNotEmpty() && yearInDb != yearAdStr) {
                                yearFiltered++
                                continue
                            }

                            // กรอง semester เฉพาะถ้ามีค่าและไม่ใช่ "2" (ข้อมูลเก่าที่ hardcode)
                            // ถ้าเป็น "2" หรือว่าง ให้แสดง (เพื่อรองรับข้อมูลเก่า)
                            if (sem.isNotEmpty() && sem != "2" && sem != termStr) {
                                semesterFiltered++
                                continue
                            }

                            val recordClassId =
                                recordSnap.child("classId").getValue(String::class.java) ?: ""
                            if (!classId.isNullOrEmpty() && recordClassId != classId) {
                                classIdFiltered++
                                continue
                            }

                            val status =
                                recordSnap.child("status").getValue(String::class.java) ?: ""

                            // สร้างหรืออัปเดตสถิติของ student
                            val stats = statsMap.getOrPut(uid) {
                                val (name, code) = usersCache[uid] ?: ("ไม่พบชื่อ" to "-")
                                TermStudentStats(
                                    uid = uid,
                                    name = name,
                                    studentCode = code
                                )
                            }

                            // นับจำนวนครั้งของแต่ละสถานะ
                            when (status) {
                                "ตรงเวลา" -> {
                                    stats.onTimeCount++
                                    totalOnTimeCount++
                                }
                                "มาสาย" -> {
                                    stats.lateCount++
                                    totalLateCount++
                                }
                                "ขาด" -> {
                                    stats.absentCount++
                                    totalAbsentCount++
                                }
                            }
                        }
                    }

                    // เพิ่ม student ที่มีสถิติเข้า list
                    studentStatsList.addAll(statsMap.values)
                    // เรียงตามชื่อ
                    studentStatsList.sortBy { it.name }

                    // Debug
                    android.util.Log.d("TermActivity", 
                        "Filter: total=$totalRecords, year=$yearFiltered, " +
                        "semester=$semesterFiltered, classId=$classIdFiltered, " +
                        "yearAdStr=$yearAdStr (from classYearThai=$classYearThai), termStr=$termStr, " +
                        "classId=$classId, found=${studentStatsList.size} students, " +
                        "onTime=$totalOnTimeCount, late=$totalLateCount, absent=$totalAbsentCount")

                    // อัปเดต UI บน main thread
                    runOnUiThread {
                        tvOnTimeCount.text = "$totalOnTimeCount ครั้ง"
                        tvLateCount.text = "$totalLateCount ครั้ง"
                        tvAbsentCount.text = "$totalAbsentCount ครั้ง"
                        tvStudentCount.text = "รายชื่อนักศึกษา (ทั้งหมด ${studentStatsList.size} คน)"
                        adapter.notifyDataSetChanged()

                        // แสดง Toast ถ้าไม่เจอข้อมูล
                        if (totalRecords == 0) {
                            Toast.makeText(
                                this@TermActivity,
                                "ไม่พบข้อมูล history ในระบบ",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@TermActivity,
                        "โหลดข้อมูลไม่สำเร็จ: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
