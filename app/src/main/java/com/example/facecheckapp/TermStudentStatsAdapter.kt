package com.example.facecheckapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TermStudentStatsAdapter(
    private val items: List<TermStudentStats>
) : RecyclerView.Adapter<TermStudentStatsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvStudentName)
        val tvCode: TextView = itemView.findViewById(R.id.tvStudentCode)
        val tvOnTimeCount: TextView = itemView.findViewById(R.id.tvOnTimeCount)
        val tvLateCount: TextView = itemView.findViewById(R.id.tvLateCount)
        val tvAbsentCount: TextView = itemView.findViewById(R.id.tvAbsentCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_term_student_stats, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val stats = items[position]
        holder.tvName.text = stats.name
        holder.tvCode.text = stats.studentCode
        holder.tvOnTimeCount.text = stats.onTimeCount.toString()
        holder.tvLateCount.text = stats.lateCount.toString()
        holder.tvAbsentCount.text = stats.absentCount.toString()
    }
}



