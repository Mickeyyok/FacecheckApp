package com.example.facecheckapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth // ต้อง Import FirebaseAuth ด้วย

// ⭐ เปลี่ยนชื่อคลาสเป็น SettingActivityTeacher
class SettingActivityTeacher : AppCompatActivity() {

    // ถ้ามีการใช้ FirebaseAuth ควรประกาศไว้ (แม้จะใช้แค่ signOut)
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Layout นี้ถูกต้องแล้วสำหรับอาจารย์
        setContentView(R.layout.activity_setting_teacher)


        /** 🔽 Bottom Navigation 🔽 */
        val navHome = findViewById<LinearLayout>(R.id.navHome)
        val navSetting = findViewById<LinearLayout>(R.id.navSetting)

        // 1. หน้าหลัก (navHome)
        navHome.setOnClickListener {
            // ⭐ ถูกต้อง: ชี้ไปที่หน้าหลักอาจารย์
            startActivity(Intent(this, TeacherHomeActivity::class.java))
            overridePendingTransition(0, 0)
        }

        // 2. ตั้งค่า (navSetting)
        navSetting.setOnClickListener {
            // ⭐ แก้ไข: ชี้กลับมาที่หน้านี้เอง (SettingActivityTeacher)
            // การคลิกปุ่ม Activity ปัจจุบัน ควรจะเป็นการไม่ทำอะไร หรือทำ Intent ใหม่
            // ในกรณีนี้ เราจะทำ Intent ไปที่หน้านี้เอง เพื่อให้ Highlight ที่ปุ่มไม่หายไป
            startActivity(Intent(this, SettingActivityTeacher::class.java))
            overridePendingTransition(0, 0)
        }

        /** 🔽 เมนูเนื้อหา 🔽 */

        // 3. ข้อมูลส่วนตัว
        val lnProfile = findViewById<LinearLayout>(R.id.lnProfile)
        lnProfile.setOnClickListener {
            // ⭐ แก้ไข: ชี้ไปที่หน้าข้อมูลส่วนตัวของอาจารย์
            startActivity(Intent(this, PersonalActivityTeacher::class.java))
        }

        // 4. ออกจากระบบ
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            auth.signOut() // 🌟 เพิ่มการ Sign Out
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}