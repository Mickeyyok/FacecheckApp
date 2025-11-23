package com.example.facecheckapp

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.util.Calendar
import java.util.Date

class RealTimeActivity : AppCompatActivity() {

    // ---------- UI ----------
    private lateinit var datePickerLayout: LinearLayout
    private lateinit var tvDate: TextView
    private lateinit var btnBack: ImageButton

    private lateinit var tvOnTimeCount: TextView
    private lateinit var tvLateCount: TextView
    private lateinit var tvAbsentCount: TextView
    private lateinit var tvStudentCount: TextView

    private lateinit var rvStudents: RecyclerView
    private val studentStatusList = mutableListOf<StudentStatus>()
    private lateinit var studentAdapter: StudentStatusAdapter

    // ---------- Logic ----------
    private val calendar: Calendar = Calendar.getInstance()
    private var selectedTerm: Int = 1              // ค่าเทอม (จะถูกตั้งใหม่จาก classes/{classId})
    private var classYearThai: Int? = null        // ปี พ.ศ. ของวิชานั้น (จาก classes/{classId}/year)

    // Firebase
    private lateinit var db: DatabaseReference

    // cache users -> (fullName, studentId)
    private val usersCache = mutableMapOf<String, Pair<String, String>>()
    private var usersLoaded: Boolean = false

    // classId ส่งมาจากหน้าเลือกวิชา
    private val classId: String? by lazy {
        intent.getStringExtra("classId")
    }

    // ----------------------------------------------------
    // onCreate
    // ----------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_realtime)

        // --- findViewById ---
        datePickerLayout = findViewById(R.id.datePickerLayout)
        tvDate = findViewById(R.id.tvDate)
        btnBack = findViewById(R.id.btnBack)

        tvOnTimeCount = findViewById(R.id.tvOnTimeCount)
        tvLateCount = findViewById(R.id.tvLateCount)
        tvAbsentCount = findViewById(R.id.tvAbsentCount)
        tvStudentCount = findViewById(R.id.tvStudentCount)

        rvStudents = findViewById(R.id.rvStudents)

        // --- Firebase Reference ---
        db = FirebaseDatabase
            .getInstance("https://facecheckapp-dea12-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .reference

        // --- RecyclerView ---
        studentAdapter = StudentStatusAdapter(studentStatusList)
        rvStudents.layoutManager = LinearLayoutManager(this)
        rvStudents.adapter = studentAdapter

        updateDateDisplay()

        // Reset ค่าเมื่อเปลี่ยน class
        selectedTerm = 1
        classYearThai = null
        studentStatusList.clear()
        usersCache.clear()
        usersLoaded = false  // Reset เพื่อให้โหลด users cache ใหม่
        studentAdapter.notifyDataSetChanged()


        loadClassTermAndStats()

        // เปลี่ยนวันที่
        datePickerLayout.setOnClickListener {
            showDatePicker()
        }

        btnBack.setOnClickListener { finish() }
    }


    // เลือกวันที่

    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(Calendar.YEAR, selectedYear)
                calendar.set(Calendar.MONTH, selectedMonth)
                calendar.set(Calendar.DAY_OF_MONTH, selectedDay)

                updateDateDisplay()

                ensureUsersLoaded { loadDailyStats() }
            },
            year,
            month,
            day
        ).show()
    }

    private fun updateDateDisplay() {
        tvDate.text = formatThaiDate(calendar.time)
    }

    private fun formatThaiDate(date: Date): String {
        val thaiMonths = arrayOf(
            "มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน",
            "พฤษภาคม", "มิถุนายน", "กรกฎาคม", "สิงหาคม",
            "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม"
        )

        val cal = Calendar.getInstance()
        cal.time = date
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = thaiMonths[cal.get(Calendar.MONTH)]
        val yearThai = cal.get(Calendar.YEAR) + 543

        return "$day $month $yearThai"
    }



    // โหลด semester + year จาก classes/{classId}
    //   semester: "1" / "2"
    //   year: "2568" (ปี พ.ศ.)

    private fun loadClassTermAndStats() {
        val id = classId
        if (id.isNullOrEmpty()) {
            // Reset เป็นค่า default
            selectedTerm = 1
            classYearThai = null
            ensureUsersLoaded { loadDailyStats() }
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

                    android.util.Log.d("RealTimeActivity", 
                        "Loaded class: classId=$id, semester=$selectedTerm, yearThai=$classYearThai, " +
                        "yearStr=$yearStr, semStr=$semStr")

                    ensureUsersLoaded { loadDailyStats() }
                }

                override fun onCancelled(error: DatabaseError) {
                    android.util.Log.e("RealTimeActivity", 
                        "Failed to load class: ${error.message}")
                    ensureUsersLoaded { loadDailyStats() }
                }
            })
    }


    // โหลด users ทั้งหมดเข้า cache

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

                        // อ่านข้อมูลจาก users/{uid}/... โดยตรง
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


    // โหลดสถิติ + รายชื่อ
    //   history/{uid}/{historyId}

    private fun loadDailyStats() {
        // เวลาเริ่ม/จบของวันนั้น
        val startCal = Calendar.getInstance().apply {
            time = calendar.time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = startCal.timeInMillis
        val endOfDay = startOfDay + 24L * 60 * 60 * 1000 - 1

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
            calendar.get(Calendar.YEAR)
        }
        val yearAdStr = yearAd.toString()
        val termStr = selectedTerm.toString()

        studentStatusList.clear()
        var onTimeCount = 0
        var lateCount = 0
        var absentCount = 0
        
        // ใช้ Set เพื่อเก็บ uid ที่นับแล้ว (ป้องกันการนับซ้ำ)
        val countedUids = mutableSetOf<String>()

        db.child("history")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Debug: ตรวจสอบข้อมูลที่กรอง
                    var totalRecords = 0
                    var dateFiltered = 0
                    var yearFiltered = 0
                    var semesterFiltered = 0
                    var classIdFiltered = 0

                    for (userHistSnap in snapshot.children) {
                        val uid = userHistSnap.key ?: continue

                        for (recordSnap in userHistSnap.children) {
                            totalRecords++

                            val ts = recordSnap.child("timestamp").getValue(Long::class.java)
                                ?: recordSnap.child("timestamp").getValue(Double::class.java)
                                    ?.toLong()
                                ?: continue

                            if (ts < startOfDay || ts > endOfDay) {
                                dateFiltered++
                                continue
                            }

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

                            // นับแค่ครั้งแรกที่เจอแต่ละ uid (ป้องกันการนับซ้ำ)
                            if (!countedUids.contains(uid)) {
                                countedUids.add(uid)
                                when (status) {
                                    "ตรงเวลา" -> onTimeCount++
                                    "มาสาย" -> lateCount++
                                    "ขาด" -> absentCount++
                                }
                            }

                            // ---------- โหลดชื่อจาก users ถ้าใน cache ยังไม่มี ----------
                            val cachePair = usersCache[uid]
                            if (cachePair == null) {
                                // กันไม่ให้ยิงซ้ำ
                                usersCache[uid] = "" to ""

                                // อ่านข้อมูลจาก users/{uid}/... โดยตรง (ไม่ใช่ faceEmbedding)
                                db.child("users")
                                    .child(uid)
                                    .addListenerForSingleValueEvent(object :
                                        ValueEventListener {
                                        override fun onDataChange(userSnap: DataSnapshot) {
                                            val firstName = userSnap.child("first_name")
                                                .getValue(String::class.java) ?: ""
                                            val lastName = userSnap.child("last_name")
                                                .getValue(String::class.java) ?: ""
                                            val studentId = userSnap.child("id")
                                                .getValue(String::class.java) ?: ""

                                            val fullName = listOf(firstName, lastName)
                                                .filter { it.isNotBlank() }
                                                .joinToString(" ")

                                            usersCache[uid] = fullName to studentId

                                            val index =
                                                studentStatusList.indexOfFirst { it.uid == uid }
                                            if (index >= 0) {
                                                val old = studentStatusList[index]
                                                studentStatusList[index] = old.copy(
                                                    name = if (fullName.isNotBlank()) fullName else "ไม่พบชื่อ",
                                                    studentCode = if (studentId.isNotBlank()) studentId else "-"
                                                )
                                                runOnUiThread {
                                                    studentAdapter.notifyItemChanged(index)
                                                }
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {}
                                    })
                            }

                            // ใช้ค่าจาก cache ตอนนี้ (หรือยังว่างอยู่ก็ fallback)
                            val (nameFromCache, codeFromCache) =
                                usersCache[uid] ?: ("" to "")

                            val displayName =
                                if (nameFromCache.isNotBlank()) nameFromCache else "ไม่พบชื่อ"
                            val displayCode =
                                if (codeFromCache.isNotBlank()) codeFromCache else "-"

                            val item = StudentStatus(
                                uid = uid,
                                name = displayName,
                                studentCode = displayCode,
                                status = status
                            )

                            val idx = studentStatusList.indexOfFirst { it.uid == uid }
                            if (idx >= 0) {
                                studentStatusList[idx] = item
                            } else {
                                studentStatusList.add(item)
                            }
                        }
                    }

                    // Debug: แสดงข้อมูลการกรอง
                    android.util.Log.d("RealTimeActivity", 
                        "Filter: total=$totalRecords, date=$dateFiltered, year=$yearFiltered, " +
                        "semester=$semesterFiltered, classId=$classIdFiltered, " +
                        "yearAdStr=$yearAdStr (from classYearThai=$classYearThai), termStr=$termStr, " +
                        "classId=$classId, found=${studentStatusList.size} students, " +
                        "onTime=$onTimeCount, late=$lateCount, absent=$absentCount")
                    
                    // อัปเดต UI บน main thread
                    runOnUiThread {
                        tvOnTimeCount.text = onTimeCount.toString()
                        tvLateCount.text = lateCount.toString()
                        tvAbsentCount.text = absentCount.toString()
                        tvStudentCount.text = "ทั้งหมด ${studentStatusList.size} คน"
                        studentAdapter.notifyDataSetChanged()
                        
                        // แสดง Toast ถ้าไม่เจอข้อมูล
                        if (totalRecords == 0) {
                            Toast.makeText(
                                this@RealTimeActivity,
                                "ไม่พบข้อมูล history ในระบบ",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@RealTimeActivity,
                        "โหลดข้อมูลไม่สำเร็จ: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
