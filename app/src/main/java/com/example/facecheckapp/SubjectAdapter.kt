package com.example.facecheckapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SubjectAdapter(
    private val items: ArrayList<ClassModel>,
    private val onSelected: (ClassModel) -> Unit
) : RecyclerView.Adapter<SubjectAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSubjectTitle: TextView = view.findViewById(R.id.tvSubjectTitle)
        val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // Title
        holder.tvSubjectTitle.text =
            "${item.subjectCode} ${item.className}"

        // Location
        holder.tvLocation.text =
            "อาคาร ${item.classRoom}"

        // Time
        val time = if (!item.classTime.isNullOrEmpty())
            item.classTime
        else
            "${item.startTime} - ${item.endTime}"

        holder.tvTime.text = time

        holder.itemView.setOnClickListener {
            onSelected(item)
        }
    }
}
