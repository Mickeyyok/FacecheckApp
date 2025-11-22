package com.example.facecheckapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    // 🌟 กำหนดชื่อไฟล์ SharedPreferences และ Keys 🌟
    private val PREF_NAME = "LoginPrefs"
    private val KEY_USERNAME = "username"
    private val KEY_PASSWORD = "password"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnBack = findViewById<Button>(R.id.btnBack)

        // 🚀 โหลดข้อมูลที่บันทึกไว้เมื่อ Activity ถูกสร้าง
        loadSavedCredentials(etUsername, etPassword)

        // ✅ ปุ่มเข้าสู่ระบบ
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกชื่อผู้ใช้และรหัสผ่าน", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val email = "$username@facecheck.com"

            // 🔐 เข้าสู่ระบบ Firebase
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    // 🌟🌟🌟 เมื่อเข้าสู่ระบบสำเร็จ: บันทึกข้อมูล 🌟🌟🌟
                    saveCredentials(username, password)
                    // 🌟🌟🌟 ------------------------------ 🌟🌟🌟

                    val userUid = auth.currentUser?.uid ?: return@addOnSuccessListener

                    // 🔍 ดึงข้อมูล role จาก Firebase
                    val userRef = FirebaseDatabase.getInstance().getReference("users").child(userUid)
                    userRef.get().addOnSuccessListener { snapshot ->
                        if (snapshot.exists()) {
                            val role = snapshot.child("role").value?.toString()

                            if (role == "teacher") {
                                Toast.makeText(this, "เข้าสู่ระบบ (อาจารย์)", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, TeacherHomeActivity::class.java))
                            } else {
                                Toast.makeText(this, "เข้าสู่ระบบ (นักศึกษา)", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, HomeActivity::class.java))
                            }
                            finish()
                        } else {
                            Toast.makeText(this, "ไม่พบข้อมูลผู้ใช้ในฐานข้อมูล", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง ❌\n${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        // 🔙 ปุ่มย้อนกลับ
        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // ==========================================================
    // 🌟 ฟังก์ชัน SharedPreferences
    // ==========================================================

    /** บันทึก Username และ Password ลงใน SharedPreferences */
    private fun saveCredentials(username: String, password: String) {
        val sharedPrefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putString(KEY_USERNAME, username)
            // ⚠️ คำเตือน: การบันทึกรหัสผ่านตรงๆ ไม่ปลอดภัย ควรใช้ Encryption ในแอปจริง
            putString(KEY_PASSWORD, password)
            apply()
        }
    }

    /** โหลด Username และ Password กลับมาแสดงใน EditText */
    private fun loadSavedCredentials(etUsername: EditText, etPassword: EditText) {
        val sharedPrefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedUsername = sharedPrefs.getString(KEY_USERNAME, "")
        val savedPassword = sharedPrefs.getString(KEY_PASSWORD, "")

        etUsername.setText(savedUsername)
        etPassword.setText(savedPassword)
    }
}