package com.example.facecheckapp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerHistory: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private val historyList = mutableListOf<HistoryModel>()

    private val uid = FirebaseAuth.getInstance().uid!!
    private val db = FirebaseDatabase.getInstance()

    private val currentYear = "2025"
    private val currentSemester = "2"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        recyclerHistory = findViewById(R.id.recyclerHistory)
        recyclerHistory.layoutManager = LinearLayoutManager(this)

        adapter = HistoryAdapter(historyList)
        recyclerHistory.adapter = adapter

        loadHistory()
        setupBottomNav()
    }


    /** โหลดประวัติเข้าเรียน */
    private fun loadHistory() {

        val ref = db.getReference("attendance")
            .child(uid)
            .child(currentYear)
            .child(currentSemester)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                historyList.clear()

                if (!snapshot.exists()) {
                    Toast.makeText(this@HistoryActivity, "ไม่พบบันทึกการเข้าเรียน", Toast.LENGTH_SHORT).show()
                    adapter.notifyDataSetChanged()
                    return
                }

                for (data in snapshot.children) {
                    val date = data.child("date").getValue(String::class.java) ?: "-"
                    val subject = data.child("subjectName").getValue(String::class.java) ?: "-"
                    val status = data.child("status").getValue(String::class.java) ?: "-"

                    historyList.add(HistoryModel(date, subject, status))
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HistoryActivity, "โหลดข้อมูลล้มเหลว", Toast.LENGTH_SHORT).show()
            }
        })
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

        navSetting.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
            overridePendingTransition(0, 0)
        }
    }
}
