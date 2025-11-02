package com.example.facecheckapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(private val items: List<HistoryModel>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateText: TextView = itemView.findViewById(R.id.txtDate)
        val subjectText: TextView = itemView.findViewById(R.id.txtSubject)
        val statusText: TextView = itemView.findViewById(R.id.txtStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.dateText.text = item.date
        holder.subjectText.text = item.subject
        holder.statusText.text = item.status

        // 🎨 ใส่สีตามสถานะ
        when (item.status) {
            "ตรงเวลา" -> holder.statusText.setTextColor(Color.parseColor("#2E7D32")) // เขียว
            "มาสาย" -> holder.statusText.setTextColor(Color.parseColor("#F9A825")) // เหลือง
            "ขาด" -> holder.statusText.setTextColor(Color.parseColor("#C62828")) // แดง
            else -> holder.statusText.setTextColor(Color.BLACK)
        }
    }

    override fun getItemCount(): Int = items.size
}
