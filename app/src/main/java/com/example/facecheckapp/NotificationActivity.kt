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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        container = findViewById(R.id.containerNotifications)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        setupBottomNav()
        loadNotifications()
    }

    /** ดึง notification จาก Firebase (เฉพาะ มาสาย / ขาด) */
    private fun loadNotifications() {
        dbRef.orderByChild("createdAt")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val list = mutableListOf<NotificationModel>()
                    val toMarkSeen = mutableListOf<String>()

                    for (child in snapshot.children) {
                        val model = child.getValue(NotificationModel::class.java)
                        model?.id = child.key

                        if (model != null &&
                            (model.status == "มาสาย" || model.status == "ขาด")
                        ) {
                            list.add(model)

                            if (model.seen != true) {
                                model.id?.let { toMarkSeen.add(it) }
                            }
                        }
                    }

                    list.sortByDescending { it.createdAt ?: 0L }

                    container.removeAllViews()
                    list.forEach { addCard(it) }

                    // mark ว่าอ่านแล้ว
                    for (id in toMarkSeen) {
                        dbRef.child(id).child("seen").setValue(true)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
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
                    R.drawable.bg_notify_late_circle
                )

                tvTitle.text = "แจ้งเตือน: มาสาย"

                val studentId = n.studentId ?: "-"
                val time = n.checkinTime ?: "-"

                tvLine1.text = "[$studentId] มาสายในวันนี้"
                tvLine2.text = "เวลา $time น. โปรดตรวจสอบสาเหตุ"
            }

            "ขาด" -> {
                iconCircle.background = ContextCompat.getDrawable(
                    this,
                    R.drawable.circle_red
                )

                tvTitle.text = "แจ้งเตือน: ขาดเรียนวันนี้"

                val studentId = n.studentId ?: "-"
                val code = n.subjectCode ?: "-"

                tvLine1.text = "[$studentId] ขาดเรียนวิชา [$code]"
                tvLine2.text = "ในวันนี้ หากมีเหตุจำเป็นโปรดแจ้งอาจารย์ผู้สอน"
            }

            else -> return
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
