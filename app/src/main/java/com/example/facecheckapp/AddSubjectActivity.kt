package com.example.facecheckapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddSubjectActivity : AppCompatActivity() {

    private lateinit var edtSubjectCode: EditText
    private lateinit var edtSubjectName: EditText
    private lateinit var btnJoinSubject: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_subject)

        // เชื่อม View
        edtSubjectCode = findViewById(R.id.edtSubjectCode)
        edtSubjectName = findViewById(R.id.edtSubjectName)
        btnJoinSubject = findViewById(R.id.btnJoinSubject)

        // ปุ่ม "เข้าร่วมวิชา"
        btnJoinSubject.setOnClickListener {
            val code = edtSubjectCode.text.toString().trim()
            val name = edtSubjectName.text.toString().trim()

            if (code.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกรหัสและชื่อวิชาให้ครบ", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "เพิ่มวิชาเรียบร้อย ✅", Toast.LENGTH_SHORT).show()

                // กลับไปหน้า MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}
