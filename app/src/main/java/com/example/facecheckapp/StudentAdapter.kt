package com.example.facecheckapp

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StudentAdapter(
    private val studentList: MutableList<StudentData>,
    private val classId: String
) : RecyclerView.Adapter<StudentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvStudentName)
        val tvId: TextView = view.findViewById(R.id.tvStudentId)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteStudent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = studentList[position]
        val context = holder.itemView.context

        holder.tvName.text = "${student.first_name ?: ""} ${student.last_name ?: ""}"
        holder.tvId.text = "รหัส: ${student.id ?: "-"}"

        // ✅ เมื่อกดปุ่มลบ
        holder.btnDelete.setOnClickListener {
            // รหัสนักศึกษา 13 หลัก
            val studentIdNumber = student.id ?: return@setOnClickListener
            val db = FirebaseDatabase.getInstance()
            val teacherUid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener

            AlertDialog.Builder(context)
                .setTitle("ยืนยันการลบ")
                .setMessage("คุณต้องการลบนักศึกษา ${student.first_name} ${student.last_name} ออกจากห้องหรือไม่?")
                .setPositiveButton("ลบ") { _, _ ->

                    // 1. ค้นหา Firebase UID ของนักศึกษาจากรหัสนักศึกษา (Student ID)
                    // Path: /users/ (ค้นหาโดย id)
                    db.getReference("users").orderByChild("id").equalTo(studentIdNumber)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {

                                // 1a. หา Firebase UID
                                val studentFirebaseUid = userSnapshot.children.firstOrNull()?.key

                                // 2. (เดิม) ลบออกจากโหนดคลาสของอาจารย์
                                // Path: /classes/$teacherUid/$classId/students/$studentIdNumber
                                val classStudentRef = db.getReference("classes/$teacherUid/$classId/students/$studentIdNumber")

                                classStudentRef.removeValue()
                                    .addOnSuccessListener {

                                        // 3. (ใหม่) ลบออกจากวิชาส่วนตัวของนักศึกษา (ถ้าพบ Firebase UID)
                                        if (studentFirebaseUid != null) {
                                            // Path: /students/$studentFirebaseUid/subjects/$classId
                                            val studentSubjectRef = db.getReference("students/$studentFirebaseUid/subjects/$classId")
                                            studentSubjectRef.removeValue()
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "ลบนักศึกษาเรียบร้อย (ลบวิชาส่วนตัวแล้ว)", Toast.LENGTH_SHORT).show()
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(context, "ลบสำเร็จ (❌ แต่ลบวิชาส่วนตัวไม่สำเร็จ)", Toast.LENGTH_LONG).show()
                                                }
                                        } else {
                                            Toast.makeText(context, "ลบนักศึกษาเรียบร้อย (ไม่พบ UID นักศึกษา)", Toast.LENGTH_SHORT).show()
                                        }

                                        // 4. (เดิม) อัปเดต UI บนหน้าจออาจารย์
                                        studentList.removeAt(position)
                                        notifyItemRemoved(position)
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "ลบไม่สำเร็จ: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // กรณีค้นหา UID ล้มเหลว - ให้พยายามลบจากคลาสอาจารย์ต่อไป (Fallback)
                                val dbRefFallback = db.getReference("classes/$teacherUid/$classId/students/$studentIdNumber")
                                dbRefFallback.removeValue()
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "ลบนักศึกษาเรียบร้อย", Toast.LENGTH_SHORT).show()
                                        studentList.removeAt(position)
                                        notifyItemRemoved(position)
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "ลบไม่สำเร็จ: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        })
                }
                .setNegativeButton("ยกเลิก", null)
                .show()
        }
    }

    override fun getItemCount(): Int = studentList.size
}