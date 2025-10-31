package com.example.facecheckapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HomepageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        val btnaddsubject = findViewById<Button>(R.id.btnAddSubject)

        btnaddsubject.setOnClickListener {
            val intent = Intent(this, AddSubjectActivity::class.java)
            startActivity(intent)
        }
        }
    }
