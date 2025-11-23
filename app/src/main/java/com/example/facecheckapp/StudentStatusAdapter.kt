package com.example.facecheckapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class StudentStatus(
    val uid: String = "",
    val name: String = "",
    val studentCode: String = "",
    val status: String = ""
)

class StudentStatusAdapter(
    private val items: List<StudentStatus>
) : RecyclerView.Adapter<StudentStatusAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvStudentName)
        val tvCode: TextView = itemView.findViewById(R.id.tvStudentCode)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_status, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val s = items[position]
        holder.tvName.text = s.name
        holder.tvCode.text = s.studentCode
        holder.tvStatus.text = s.status

        val color = when (s.status) {
            "ตรงเวลา" -> Color.parseColor("#2E7D32")
            "มาสาย"   -> Color.parseColor("#FFC107")
            "ขาด"      -> Color.parseColor("#F44336")
            else       -> Color.BLACK
        }
        holder.tvStatus.setTextColor(color)
    }
}
