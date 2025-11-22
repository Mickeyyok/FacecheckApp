package com.example.facecheckapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private val list = mutableListOf<HistoryModel>()   // ใช้กับ Recycler / Adapter

    private lateinit var spinnerYear: Spinner
    private lateinit var spinnerTerm: Spinner

    // เก็บ raw data ทั้งหมดก่อน filter
    private val allHistory = mutableListOf<HistoryItem>()

    private val uid = FirebaseAuth.getInstance().uid!!
    private val db = FirebaseDatabase.getInstance().reference

    private var classId: String = ""   // ถ้ามี = filter เฉพาะวิชานั้น

    // ตัวแปรเก็บค่าที่เลือกใน spinner
    private var selectedYear: String? = null
    private var selectedTerm: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // รับ classId ถ้ามีส่งมาจากหน้าอื่น
        classId = intent.getStringExtra("classId") ?: ""

        recycler = findViewById(R.id.recyclerHistory)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = HistoryAdapter(list)
        recycler.adapter = adapter

        spinnerYear = findViewById(R.id.spinnerYear)
        spinnerTerm = findViewById(R.id.spinnerTerm)

        // ปุ่มกระดิ่งด้านบน
        val btnNotification = findViewById<ImageButton>(R.id.btnNotification)
        btnNotification.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        // Bottom Nav ด้านล่าง
        setupBottomNav()

        // โหลดข้อมูลประวัติทั้งหมด
        loadHistory()
    }

    /** Bottom Navigation เหมือน HomeActivity */
    private fun setupBottomNav() {
        val navHome = findViewById<LinearLayout>(R.id.navHome)
        val navHistory = findViewById<LinearLayout>(R.id.navHistory)
        val navSetting = findViewById<LinearLayout>(R.id.navSetting)

        navHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            overridePendingTransition(0, 0)
        }

        // อยู่หน้า History แล้ว จะไม่ทำอะไรก็ได้
        navHistory.setOnClickListener {
            // ถ้าอยากให้รีเฟรชใหม่ก็เขียน loadHistory() ตรงนี้เพิ่มได้
        }

        navSetting.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
            overridePendingTransition(0, 0)
        }
    }

    /** โหลดข้อมูล history จาก Firebase */
    private fun loadHistory() {

        // มี classId -> ดูเฉพาะวิชานั้น, ถ้าไม่มี -> ดูทุกวิชา
        val query: Query = if (classId.isNotEmpty()) {
            db.child("history").child(uid)
                .orderByChild("classId")
                .equalTo(classId)
        } else {
            db.child("history").child(uid)
        }

        query.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                allHistory.clear()

                for (data in snapshot.children) {

                    val ts = data.child("timestamp").value
                        ?.toString()
                        ?.toLongOrNull() ?: 0L

                    val className   = data.child("className").value?.toString() ?: "ไม่พบชื่อวิชา"
                    val subjectCode = data.child("subjectCode").value?.toString() ?: ""
                    val status      = data.child("status").value?.toString() ?: "-"

                    // ⭐ ต้องมีปี/ภาคเรียนเก็บใน Firebase ด้วย
                    val year = data.child("year").value?.toString() ?: ""
                    val term = data.child("term").value?.toString() ?: ""

                    allHistory.add(
                        HistoryItem(
                            year = year,
                            term = term,
                            timestamp = ts,
                            className = className,
                            subjectCode = subjectCode,
                            status = status
                        )
                    )
                }

                // ตั้งค่า Spinner จากข้อมูลจริง
                setupSpinnersFromData()

                // ใช้ filter ตาม year/term ที่เลือก (ค่าเริ่มต้นกำหนดใน setupSpinners)
                applyFilter()
            }

            override fun onCancelled(error: DatabaseError) { }
        })
    }

    /** ดึง year/term จาก allHistory ไปลงใน Spinner */
    private fun setupSpinnersFromData() {

        // ถ้าไม่มีข้อมูลเลย ให้ใส่ "ทั้งหมด" ไว้เฉย ๆ
        if (allHistory.isEmpty()) {
            val emptyAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                listOf("ทั้งหมด")
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            spinnerYear.adapter = emptyAdapter
            spinnerTerm.adapter = emptyAdapter
            selectedYear = null
            selectedTerm = null
            return
        }

        // ดึงปี/เทอมที่มีจริง (ไม่ซ้ำ)
        val years = allHistory.map { it.year }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()

        val terms = allHistory.map { it.term }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()

        // เพิ่ม "ทั้งหมด" เป็นตัวเลือกแรก
        val yearOptions = mutableListOf("ทั้งหมด")
        yearOptions.addAll(years)

        val termOptions = mutableListOf("ทั้งหมด")
        termOptions.addAll(terms)

        val yearAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            yearOptions
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        val termAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            termOptions
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        spinnerYear.adapter = yearAdapter
        spinnerTerm.adapter = termAdapter

        // ⭐ default: ใช้ปี/ภาคเรียนของ record ล่าสุด
        val latest = allHistory.maxByOrNull { it.timestamp }
        val defaultYear = latest?.year
        val defaultTerm = latest?.term

        val yearIndex = if (defaultYear != null) {
            yearOptions.indexOf(defaultYear).takeIf { it >= 0 } ?: 0
        } else 0

        val termIndex = if (defaultTerm != null) {
            termOptions.indexOf(defaultTerm).takeIf { it >= 0 } ?: 0
        } else 0

        // ตั้ง selection โดยยังไม่ต้องสนใจ onItemSelected แรก
        spinnerYear.setSelection(yearIndex, false)
        spinnerTerm.setSelection(termIndex, false)

        selectedYear = if (yearIndex == 0) null else yearOptions[yearIndex]
        selectedTerm = if (termIndex == 0) null else termOptions[termIndex]

        // Listener เวลาเปลี่ยนค่าจากผู้ใช้
        spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedYear = if (position == 0) null else yearOptions[position]
                applyFilter()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedYear = null
                applyFilter()
            }
        }

        spinnerTerm.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedTerm = if (position == 0) null else termOptions[position]
                applyFilter()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedTerm = null
                applyFilter()
            }
        }
    }

    /** เอา year/term ที่เลือก มา filter ลง list ที่ผูกกับ Adapter */
    private fun applyFilter() {
        list.clear()

        val filtered = allHistory.filter { item ->
            val yearMatch = selectedYear?.let { it == item.year } ?: true
            val termMatch = selectedTerm?.let { it == item.term } ?: true
            yearMatch && termMatch
        }.sortedByDescending { it.timestamp }

        filtered.forEach { item ->
            val formattedDate = formatDate(item.timestamp)
            val subjectLine = "${item.subjectCode} ${item.className}".trim()

            list.add(
                HistoryModel(
                    date = formattedDate,
                    subject = subjectLine,
                    status = item.status,
                    timestamp = item.timestamp
                )
            )
        }

        adapter.notifyDataSetChanged()
    }

    private fun formatDate(timestamp: Long): String {
        if (timestamp <= 0L) return "-"
        val sdf = SimpleDateFormat("d MMM yyyy", Locale("th", "TH"))
        return sdf.format(Date(timestamp))
    }

    /** internal model เก็บข้อมูลดิบก่อนแปลงเป็น HistoryModel */
    private data class HistoryItem(
        val year: String,
        val term: String,
        val timestamp: Long,
        val className: String,
        val subjectCode: String,
        val status: String
    )
}
