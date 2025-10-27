package com.example.facecheckapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // เชื่อม View กับ ID
        val etUserid = findViewById<EditText>(R.id.et_userid)
        val etName = findViewById<EditText>(R.id.et_name)
        val etlastName = findViewById<EditText>(R.id.et_Lastname)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val etConfirmPassword = findViewById<EditText>(R.id.et_confirm_password)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        val btnBack = findViewById<Button>(R.id.btn_back)

        // ปุ่มสมัครสมาชิก
        btnRegister.setOnClickListener {
            val userId = etUserid.text.toString().trim()
            val etname = etName.text.toString().trim()
            val etlastName = etlastName.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            // ตรวจสอบว่ากรอกครบหรือไม่
            if ( userId.isEmpty() || etname.isEmpty() ||etlastName.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty()
            ) {
                Toast.makeText(this, "กรุณากรอกข้อมูลให้ครบทุกช่อง", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ตรวจสอบว่ารหัสผ่านตรงกันไหม
            if (password != confirmPassword) {
                Toast.makeText(this, "รหัสผ่านไม่ตรงกัน", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ ถ้าผ่านทั้งหมด
            Toast.makeText(this, "สมัครสมาชิกสำเร็จ!", Toast.LENGTH_SHORT).show()

            // กลับไปหน้า MainActivity
            val intent = Intent(this, ConsentActivity::class.java)
            startActivity(intent)
            finish()
        }

        // ปุ่มย้อนกลับ
        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
