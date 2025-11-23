package com.example.facecheckapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class NotificationActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout

    private val uid = FirebaseAuth.getInstance().uid!!
    private val dbRef: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("notifications").child(uid)

    // ⭐ เก็บ query + listener ไว้เพื่อลบตอน onStop
    private var notifQuery: Query? = null
    private var notifListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        container = findViewById(R.id.containerNotifications)

        // ปุ่มย้อนกลับ
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        setupBottomNav()
        loadNotifications()
    }

    /** ดึง notification จาก Firebase (เฉพาะ มาสาย / ขาด) */
    private fun loadNotifications() {

        // ลบ listener เก่า (กันซ้อน)
        notifQuery?.let { q ->
            notifListener?.let { q.removeEventListener(it) }
        }

        notifQuery = dbRef.orderByChild("createdAt")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val list = mutableListOf<NotificationModel>()
                val toMarkSeen = mutableListOf<String>()

                for (child in snapshot.children) {
                    val model = child.getValue(NotificationModel::class.java)
                    model?.id = child.key

                    // เอาเฉพาะ สถานะ "มาสาย" หรือ "ขาด"
                    if (model != null &&
                        (model.status == "มาสาย" || model.status == "ขาด")
                    ) {
                        list.add(model)

                        // ถ้ายังไม่ seen → เก็บไว้ mark ทีหลัง
                        if (model.seen != true) {
                            model.id?.let { toMarkSeen.add(it) }
                        }
                    }
                }

                // ใหม่สุดอยู่บน
                list.sortByDescending { it.createdAt ?: 0L }

                container.removeAllViews()
                list.forEach { addCard(it) }

                // mark ว่าอ่านแล้ว (เฉพาะที่เพิ่งโหลดมารอบนี้)
                for (id in toMarkSeen) {
                    dbRef.child(id).child("seen").setValue(true)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        notifListener = listener
        notifQuery?.addValueEventListener(listener)
    }

    // ❗ ตรงนี้สำคัญ – ถ้า Activity ไม่ visible แล้ว ให้ถอด listener
    override fun onStop() {
        super.onStop()
        notifQuery?.let { q ->
            notifListener?.let { q.removeEventListener(it) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        notifQuery?.let { q ->
            notifListener?.let { q.removeEventListener(it) }
        }
    }

    /** สร้าง card ทีละอันแล้วใส่เข้า LinearLayout */
    private fun addCard(n: NotificationModel) {
        val view = layoutInflater.inflate(R.layout.item_notification_card, container, false)

        val iconCircle = view.findViewById<View>(R.id.iconCircle)
        val tvTitle   = view.findViewById<TextView>(R.id.tvTitle)
        val tvLine1   = view.findViewById<TextView>(R.id.tvLine1)
        val tvLine2   = view.findViewById<TextView>(R.id.tvLine2)

        val status = n.status ?: ""

        when (status) {
            "มาสาย" -> {
                iconCircle.background = ContextCompat.getDrawable(
                    this,
                    R.drawable.bg_notify_late_circle   // วงกลมส้ม/เหลือง
                )

                tvTitle.text = "แจ้งเตือน: มาสาย"

                val subjectName = n.subjectName ?: "-"
                val time = n.checkinTime ?: "-"

                tvLine1.text = "[$subjectName] มาสายในวันนี้"
                tvLine2.text = "เวลา $time น. โปรดตรวจสอบสาเหตุ"
            }

            "ขาด" -> {
                iconCircle.background = ContextCompat.getDrawable(
                    this,
                    R.drawable.bg_notify_absent_circle               // วงกลมแดง
                )

                tvTitle.text = "แจ้งเตือน: ขาดเรียนวันนี้"

                val studentId = n.studentId ?: "-"
                val subjectName = n.subjectName ?: "-"

                tvLine1.text = "[$studentId] ขาดเรียนวิชา [$subjectName]"
                tvLine2.text = "ในวันนี้ หากมีเหตุจำเป็นโปรดแจ้งอาจารย์ผู้สอน"
            }

            else -> {
                // สถานะอื่นไม่ต้องแสดง
                return
            }
        }

        container.addView(view)
    }

    /** bottom nav เหมือนหน้าอื่น */
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
}
