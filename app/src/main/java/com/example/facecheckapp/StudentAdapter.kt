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
import com.google.firebase.database.FirebaseDatabase

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
            val teacherUid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener

            AlertDialog.Builder(context)
                .setTitle("ยืนยันการลบ")
                .setMessage("คุณต้องการลบนักศึกษา ${student.first_name} ${student.last_name} ออกจากห้องหรือไม่?")
                .setPositiveButton("ลบ") { _, _ ->
                    val dbRef = FirebaseDatabase.getInstance()
                        .getReference("classes/$teacherUid/$classId/students/${student.id}")

                    dbRef.removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(context, "ลบนักศึกษาเรียบร้อย", Toast.LENGTH_SHORT).show()
                            studentList.removeAt(position)
                            notifyItemRemoved(position)
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "ลบไม่สำเร็จ: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("ยกเลิก", null)
                .show()
        }
    }

    override fun getItemCount(): Int = studentList.size
}
