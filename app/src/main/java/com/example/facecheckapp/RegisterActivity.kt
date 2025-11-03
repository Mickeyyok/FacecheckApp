package com.example.facecheckapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().getReference("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        // ✳️ผูกตัวแปรกับ
        val rbStudent = findViewById<RadioButton>(R.id.rb_student)
        val rbTeacher = findViewById<RadioButton>(R.id.rb_teacher)
        val etUserId = findViewById<EditText>(R.id.et_userid)
        val etName = findViewById<EditText>(R.id.et_name)
        val etLastname = findViewById<EditText>(R.id.et_Lastname)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val etConfirmPassword = findViewById<EditText>(R.id.et_confirm_password)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        val btnBack = findViewById<Button>(R.id.btn_back)

        // 🔹 ปุ่มสมัครสมาชิก
        btnRegister.setOnClickListener {
            val userId = etUserId.text.toString().trim()
            val firstName = etName.text.toString().trim()
            val lastName = etLastname.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            // ✅ ตรวจว่ากรอกครบหรือไม่
            if (userId.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกข้อมูลให้ครบ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "รหัสผ่านไม่ตรงกัน", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isTeacher = rbTeacher.isChecked
            val email = "$userId@facecheck.com"

            // ✅ ตรวจรูปแบบรหัส
            if (isTeacher) {
                if (userId.length != 5 || !userId.all { it.isDigit() }) {
                    Toast.makeText(this, "รหัสอาจารย์ต้องเป็นตัวเลข 5 หลักเท่านั้น", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            } else {
                if (userId.length != 13 || !userId.all { it.isDigit() }) {
                    Toast.makeText(this, "รหัสนักศึกษาต้องเป็นตัวเลข 13 หลักเท่านั้น", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // ✅ ตรวจว่ามีบัญชีอยู่แล้วหรือไม่
            auth.fetchSignInMethodsForEmail(email).addOnSuccessListener { result ->
                if (result.signInMethods?.isNotEmpty() == true) {
                    Toast.makeText(this, "มีบัญชีนี้อยู่แล้ว กรุณาเข้าสู่ระบบ", Toast.LENGTH_SHORT).show()
                } else {
                    // ✅ สมัครสมาชิกใหม่
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userUid = auth.currentUser?.uid ?: return@addOnCompleteListener
                                val user = mapOf(
                                    "id" to userId,
                                    "first_name" to firstName,
                                    "last_name" to lastName,
                                    "role" to if (isTeacher) "teacher" else "student"
                                )

                                // ✅ บันทึกข้อมูลลง Firebase Database
                                database.child(userUid).setValue(user)
                                    .addOnSuccessListener {
                                        if (isTeacher) {
                                            Toast.makeText(this, "สมัครอาจารย์สำเร็จ! กรุณาเข้าสู่ระบบ", Toast.LENGTH_LONG).show()
                                            startActivity(Intent(this, MainActivity::class.java))
                                        } else {
                                            Toast.makeText(this, "สมัครนักศึกษาสำเร็จ! ไปสแกนใบหน้าเลย", Toast.LENGTH_SHORT).show()
                                            startActivity(Intent(this, ConsentActivity::class.java))
                                        }
                                        finish()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "บันทึกข้อมูลไม่สำเร็จ: ${it.message}", Toast.LENGTH_LONG).show()
                                    }
                            } else {
                                Toast.makeText(this, "เกิดข้อผิดพลาด: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                }
            }
        }

        // 🔙 ปุ่มย้อนกลับ
        btnBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
