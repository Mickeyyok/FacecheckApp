package com.example.facecheckapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerHistory: RecyclerView
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // ✅ เชื่อม RecyclerView
        recyclerHistory = findViewById(R.id.recyclerHistory)

        // 🔸 จำลองข้อมูลประวัติ
        val historyData = listOf(
            HistoryModel("15 ต.ค. 2025", "ระบบฐานข้อมูล", "มาสาย"),
            HistoryModel("10 ต.ค. 2025", "การเขียนโปรแกรม", "ตรงเวลา"),
            HistoryModel("8 ต.ค. 2025", "โครงสร้างข้อมูล", "ขาด"),
            HistoryModel("3 ต.ค. 2025", "คณิตศาสตร์คอมพิวเตอร์", "ตรงเวลา")
        )

        // ✅ ตั้งค่า Adapter
        adapter = HistoryAdapter(historyData)
        recyclerHistory.layoutManager = LinearLayoutManager(this)
        recyclerHistory.adapter = adapter
    }
}
