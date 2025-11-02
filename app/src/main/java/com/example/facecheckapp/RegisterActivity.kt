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

        val etUserid = findViewById<EditText>(R.id.et_userid)
        val etName = findViewById<EditText>(R.id.et_name)
        val etLastname = findViewById<EditText>(R.id.et_Lastname)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val etConfirmPassword = findViewById<EditText>(R.id.et_confirm_password)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        val btnBack = findViewById<Button>(R.id.btn_back)

        btnRegister.setOnClickListener {
            val studentId = etUserid.text.toString().trim()
            val firstName = etName.text.toString().trim()
            val lastName = etLastname.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (studentId.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกข้อมูลให้ครบ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "รหัสผ่านไม่ตรงกัน", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val email = "$studentId@facecheck.com"

            // 🔹 ตรวจสอบว่ามีบัญชีอยู่แล้วหรือไม่
            auth.fetchSignInMethodsForEmail(email).addOnSuccessListener { result ->
                if (result.signInMethods?.isNotEmpty() == true) {
                    Toast.makeText(this, "มีบัญชีนี้อยู่แล้ว กรุณาเข้าสู่ระบบ", Toast.LENGTH_SHORT).show()
                } else {
                    // ✅ สมัครสมาชิกใหม่
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = auth.currentUser?.uid ?: ""
                                val user = mapOf(
                                    "student_id" to studentId,
                                    "first_name" to firstName,
                                    "last_name" to lastName
                                )
                                database.child(userId).setValue(user)

                                Toast.makeText(this, "สมัครสมาชิกสำเร็จ!", Toast.LENGTH_SHORT).show()

                                // 🚀 ไปหน้า RegScanActivity
                                startActivity(Intent(this, ConsentActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this, "เกิดข้อผิดพลาด: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                }
            }
        }

        btnBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
