package com.example.facecheckapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AddSubjectActivity : AppCompatActivity() {

    private lateinit var edtSubjectCode: EditText
    private lateinit var btnJoinSubject: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_subject)

        edtSubjectCode = findViewById(R.id.edtSubjectCode)
        btnJoinSubject = findViewById(R.id.btnJoinSubject)

        val uid = FirebaseAuth.getInstance().uid!!
        val db = FirebaseDatabase.getInstance()
        val userSubjectsRef = db.getReference("students").child(uid).child("subjects")
        val classesRef = db.getReference("classes")

        btnJoinSubject.setOnClickListener {
            val code = edtSubjectCode.text.toString().trim()

            if (code.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกรหัสวิชา", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 👉 ค้นหาคลาสจาก subjectCode
            classesRef.orderByChild("subjectCode").equalTo(code)
                .get().addOnSuccessListener { snap ->

                    if (!snap.exists()) {
                        Toast.makeText(this, "ไม่พบวิชานี้ในระบบ", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    // ดึง classId ตัวแรก
                    val classId = snap.children.first().key!!

                    // เพิ่มคลาสให้ user
                    userSubjectsRef.child(classId).setValue(true)

                    Toast.makeText(this, "เพิ่มวิชาเรียบร้อย", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
        }
    }
}
