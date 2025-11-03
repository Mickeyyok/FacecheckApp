package com.example.facecheckapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TeacherHomeActivity : AppCompatActivity() {

    private lateinit var recyclerClasses: RecyclerView
    private lateinit var btnCreateClass: Button
    private lateinit var tvMyClassTitle: TextView

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("classes")
    private val classList = mutableListOf<ClassData>()
    private lateinit var adapter: ClassAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_main)

        recyclerClasses = findViewById(R.id.recyclerClasses)
        btnCreateClass = findViewById(R.id.btn_create_class)
        tvMyClassTitle = findViewById(R.id.tvMyClassTitle)

        recyclerClasses.layoutManager = LinearLayoutManager(this)
        adapter = ClassAdapter(classList)
        recyclerClasses.adapter = adapter

        val userId = auth.currentUser?.uid ?: return

        // โหลดคลาสจาก Firebase
        loadClasses(userId)

        // ปุ่มสร้างคลาส
        btnCreateClass.setOnClickListener {
            val intent = Intent(this, CreateClassActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadClasses(userId: String) {
        database.orderByChild("createdBy").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    classList.clear()
                    for (classSnap in snapshot.children) {
                        val classData = classSnap.getValue(ClassData::class.java)
                        if (classData != null) {
                            classList.add(classData)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@TeacherHomeActivity, "โหลดข้อมูลล้มเหลว", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
