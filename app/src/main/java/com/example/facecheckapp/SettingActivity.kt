package com.example.facecheckapp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_setting)


        /** 🔽 โค้ด Bottom Navigation แยกเป็นฟังก์ชัน 🔽 */

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

