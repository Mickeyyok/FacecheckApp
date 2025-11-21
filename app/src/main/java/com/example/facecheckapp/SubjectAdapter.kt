package com.example.facecheckapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SubjectAdapter(
    private val items: List<ClassModel>,
    private val onClick: (ClassModel) -> Unit
) : RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder>() {

    class SubjectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvSubjectTitle)
        val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject, parent, false)
        return SubjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        val item = items[position]

        val code = item.subjectCode ?: ""
        val name = item.className ?: ""
        val room = item.classRoom ?: ""

        val start = item.startTime ?: ""
        val end = item.endTime ?: ""
        val classTime = item.classTime ?: ""

        val timeLine = when {
            start.isNotEmpty() && end.isNotEmpty() -> "$start - $end น."
            classTime.isNotEmpty() -> classTime
            else -> "-"
        }

        holder.tvTitle.text = "$code $name"
        holder.tvLocation.text = "อาคาร $room ห้อง $room"
        holder.tvTime.text = timeLine

        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = items.size
}