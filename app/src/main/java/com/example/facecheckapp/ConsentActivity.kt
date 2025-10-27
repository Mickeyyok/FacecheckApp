package com.example.facecheckapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ConsentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consent)

        val checkAgree = findViewById<CheckBox>(R.id.checkAgree)
        val checkUnderstand = findViewById<CheckBox>(R.id.checkUnderstand)
        val btnContinue = findViewById<Button>(R.id.btnContinue)
        val btnCancel = findViewById<Button>(R.id.btnCancel)

        btnContinue.setOnClickListener {
            if (checkAgree.isChecked && checkUnderstand.isChecked) {
                Toast.makeText(this, "ขอบคุณที่ยินยอม", Toast.LENGTH_SHORT).show()

                // เปิดหน้าใหม่ (ถัดจาก Consent)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "กรุณาติ๊กยืนยันทั้งสองข้อก่อนดำเนินการต่อ", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            finish() // ปิดหน้ากลับไปหน้าก่อนหน้า
        }
    }
}
