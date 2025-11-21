package com.example.facecheckapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SubjectListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SubjectAdapter
    private val subjectList = ArrayList<ClassModel>()

    private val uid = FirebaseAuth.getInstance().uid!!
    private val db = FirebaseDatabase.getInstance()
    private val userSubjectsRef = db.getReference("students").child(uid).child("subjects")
    private val classesRef = db.getReference("classes")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subject_list)

        recyclerView = findViewById(R.id.recyclerSubjects)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = SubjectAdapter(subjectList) { selected ->
            val start = selected.startTime ?: ""
            val end = selected.endTime ?: ""
            val classTime = selected.classTime ?: ""

            val timeLine = when {
                start.isNotEmpty() && end.isNotEmpty() -> "$start - $end น."
                classTime.isNotEmpty() -> classTime
                else -> "-"
            }

            val intent = Intent()
            intent.putExtra("selectedClassId", selected.classId)
            intent.putExtra("selectedSubjectCode", selected.subjectCode)
            intent.putExtra("selectedClassName", selected.className)
            intent.putExtra("selectedClassRoom", selected.classRoom)
            intent.putExtra("selectedClassTime", timeLine)

            setResult(RESULT_OK, intent)
            finish()
        }

        recyclerView.adapter = adapter
        loadSubjects()
    }

    private fun loadSubjects() {
        userSubjectsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                subjectList.clear()

                if (!snapshot.exists()) {
                    adapter.notifyDataSetChanged()
                    return
                }

                for (child in snapshot.children) {
                    val classId = child.key ?: continue

                    classesRef.child(classId).get()
                        .addOnSuccessListener { classSnap ->
                            if (!classSnap.exists()) {
                                // ถ้า class ถูกลบที่ฝั่งอาจารย์ → ลบออกจาก subjects ของนักเรียนด้วย
                                userSubjectsRef.child(classId).removeValue()
                                return@addOnSuccessListener
                            }

                            // 🧩 ดึงค่าจาก Firebase แล้วแปลงเป็น String ปลอดภัย
                            val subjectCode = classSnap.child("subjectCode").value?.toString()
                            val className = classSnap.child("className").value?.toString()
                            val classRoom = classSnap.child("classRoom").value?.toString()
                            val dayTime = classSnap.child("dayTime").value?.toString()
                            val startTime = classSnap.child("startTime").value?.toString()
                            val endTime = classSnap.child("endTime").value?.toString()
                            val lateTime = classSnap.child("lateTime").value?.toString()
                            val year = classSnap.child("year").value?.toString()
                            val term = classSnap.child("term").value?.toString()
                            // ถ้ามี field อื่นใน DB ก็เติมแบบนี้ได้เลย

                            // ✅ สร้าง ClassModel ด้วยมือ แทน getValue()
                            val model = ClassModel().apply {
                                this.classId = classId
                                this.subjectCode = subjectCode
                                this.className = className
                                this.classRoom = classRoom
                                this.dayTime = dayTime
                                this.startTime = startTime
                                this.endTime = endTime
                                this.lateTime = lateTime
                                this.year = year
                                this.term = term
                            }

                            subjectList.add(model)
                            adapter.notifyDataSetChanged()
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
