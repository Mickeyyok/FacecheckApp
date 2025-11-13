package com.example.facecheckapp

import android.app.Activity
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
            val intent = Intent()
            intent.putExtra("selectedClassId", selected.classId)
            intent.putExtra("selectedSubjectCode", selected.subjectCode)
            intent.putExtra("selectedClassName", selected.className)
            intent.putExtra("selectedClassRoom", selected.classRoom)
            intent.putExtra("selectedClassTime", selected.classTime)

            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        recyclerView.adapter = adapter
        loadSubjects()
    }

    private fun loadSubjects() {
        userSubjectsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                subjectList.clear()

                for (child in snapshot.children) {
                    val classId = child.key ?: continue

                    classesRef.child(classId).get()
                        .addOnSuccessListener { classSnap ->

                            val model = classSnap.getValue(ClassModel::class.java)

                            if (model != null) {
                                model.classId = classId
                                subjectList.add(model)
                                adapter.notifyDataSetChanged()
                            }
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
