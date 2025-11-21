package com.example.facecheckapp

import android.os.Bundle
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
    private val list = mutableListOf<HistoryModel>()

    private val uid = FirebaseAuth.getInstance().uid!!
    private val db = FirebaseDatabase.getInstance().reference

    private var classId: String = ""   // ถ้ามี = filter เฉพาะวิชานั้น

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // ถ้ามาจากหน้าหลังเช็คชื่อจะมี classId ส่งมา
        classId = intent.getStringExtra("classId") ?: ""

        recycler = findViewById(R.id.recyclerHistory)
        recycler.layoutManager = LinearLayoutManager(this)

        adapter = HistoryAdapter(list)
        recycler.adapter = adapter

        loadHistory()
    }

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
                list.clear()

                for (data in snapshot.children) {

                    // ✅ ใช้ timestamp จริงจาก Firebase
                    val ts = data.child("timestamp").value
                        ?.toString()
                        ?.toLongOrNull() ?: 0L

                    val className   = data.child("className").value?.toString() ?: "ไม่พบชื่อวิชา"
                    val subjectCode = data.child("subjectCode").value?.toString() ?: ""
                    val status      = data.child("status").value?.toString() ?: "-"

                    // แปลงเป็นวันที่แบบ "3 ต.ค. 2568"
                    val formattedDate = formatDate(ts)

                    // บรรทัดกลาง: "SP 999-1 วิชาการตลาด"
                    val subjectLine = "$subjectCode $className".trim()

                    list.add(
                        HistoryModel(
                            date = formattedDate,
                            subject = subjectLine,
                            status = status,
                            timestamp = ts
                        )
                    )
                }

                // 🔥 เรียงจากล่าสุด → เก่าสุด
                list.sortByDescending { it.timestamp }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) { }
        })
    }

    private fun formatDate(timestamp: Long): String {
        if (timestamp <= 0L) return "-"
        val sdf = SimpleDateFormat("d MMM yyyy", Locale("th", "TH"))
        return sdf.format(Date(timestamp))
    }
}
