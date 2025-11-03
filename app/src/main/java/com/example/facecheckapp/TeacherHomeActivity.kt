package com.example.facecheckapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TeacherHomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var listView: ListView
    private lateinit var btnCreateClass: Button
    private val database = FirebaseDatabase.getInstance().getReference("classes")
    private val classList = mutableListOf<Pair<String, String>>() // (classId, className)
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_main)

        auth = FirebaseAuth.getInstance()
        val teacherUid = auth.currentUser?.uid ?: return
        listView = findViewById(R.id.list_classes)
        btnCreateClass = findViewById(R.id.btn_create_class)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        listView.adapter = adapter

        loadTeacherClasses(teacherUid)

        // 🔹 ปุ่มสร้างคลาส
        btnCreateClass.setOnClickListener {
            val intent = Intent(this, CreateClassActivity::class.java)
            startActivity(intent)
        }

        // 🔹 แตะที่คลาสเพื่อ “แก้ไข” หรือ “ลบ”
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedClassId = classList[position].first
            val selectedClassName = classList[position].second

            showClassOptionsDialog(teacherUid, selectedClassId, selectedClassName)
        }
    }

    private fun loadTeacherClasses(teacherUid: String) {
        database.child(teacherUid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                classList.clear()
                val displayList = mutableListOf<String>()

                for (child in snapshot.children) {
                    val classId = child.key ?: continue
                    val className = child.child("className").value?.toString() ?: "ไม่ทราบชื่อคลาส"
                    classList.add(Pair(classId, className))
                    displayList.add("📘 $className")
                }

                adapter.clear()
                adapter.addAll(displayList)
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showClassOptionsDialog(teacherUid: String, classId: String, className: String) {
        val options = arrayOf("แก้ไขคลาส", "ลบคลาส")
        AlertDialog.Builder(this)
            .setTitle("จัดการคลาส: $className")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editClass(classId)
                    1 -> deleteClass(teacherUid, classId)
                }
            }
            .show()
    }

    private fun editClass(classId: String) {
        val intent = Intent(this, CreateClassActivity::class.java)
        intent.putExtra("classId", classId)
        startActivity(intent)
    }

    private fun deleteClass(teacherUid: String, classId: String) {
        AlertDialog.Builder(this)
            .setTitle("ลบคลาส")
            .setMessage("คุณต้องการลบคลาสนี้หรือไม่?")
            .setPositiveButton("ลบ") { _, _ ->
                database.child(teacherUid).child(classId).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "ลบคลาสสำเร็จ", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }
}
