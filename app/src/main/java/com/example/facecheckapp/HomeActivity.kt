package com.example.facecheckapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeActivity : AppCompatActivity() {

    private lateinit var tvSelectedSubject: TextView
    private lateinit var btnAddSubject: Button

    private val uid = FirebaseAuth.getInstance().uid!!
    private lateinit var db: FirebaseDatabase
    private lateinit var userSubjectsRef: DatabaseReference

    private val PICK_SUBJECT = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        tvSelectedSubject = findViewById(R.id.tvSelectedSubject)
        btnAddSubject = findViewById(R.id.btnAddSubject)

        db = FirebaseDatabase.getInstance()
        userSubjectsRef = db.getReference("students").child(uid).child("subjects")

        loadSelectedSubject()

        btnAddSubject.setOnClickListener {
            startActivity(Intent(this, AddSubjectActivity::class.java))
        }

        tvSelectedSubject.setOnClickListener {
            val intent = Intent(this, SubjectListActivity::class.java)
            startActivityForResult(intent, PICK_SUBJECT)
        }
    }

    private fun loadSelectedSubject() {
        userSubjectsRef.get().addOnSuccessListener { snap ->

            if (!snap.exists()) {
                tvSelectedSubject.text = "กรุณาเลือกวิชา"
                return@addOnSuccessListener
            }

            val classId = snap.children.first().key!!

            db.getReference("classes").child(classId).get()
                .addOnSuccessListener { data ->

                    val code = data.child("subjectCode").value.toString()
                    val name = data.child("className").value.toString()
                    val room = data.child("classRoom").value.toString()
                    val time = data.child("classTime").value.toString()

                    tvSelectedSubject.text = "$code $name\nห้อง $room\n$time"
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_SUBJECT && resultCode == Activity.RESULT_OK) {

            val code = data?.getStringExtra("selectedSubjectCode")
            val name = data?.getStringExtra("selectedClassName")
            val room = data?.getStringExtra("selectedClassRoom")
            val time = data?.getStringExtra("selectedClassTime")

            tvSelectedSubject.text = "$code $name\nห้อง $room\n$time"
        }
    }
}
