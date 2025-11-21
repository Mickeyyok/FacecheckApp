package com.example.facecheckapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(
    private val items: List<HistoryModel>
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.txtDate)
        val tvSubject: TextView = itemView.findViewById(R.id.txtSubject)
        val tvStatus: TextView = itemView.findViewById(R.id.txtStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = items[position]

        // บรรทัดที่ 1 : วันที่
        holder.tvDate.text = item.date

        // บรรทัดที่ 2 : รหัสวิชา + ชื่อวิชา
        holder.tvSubject.text = item.subject

        // บรรทัดที่ 3 : สถานะ
        holder.tvStatus.text = item.status

        // สีสถานะ
        when (item.status) {
            "ตรงเวลา" -> holder.tvStatus.setTextColor(Color.parseColor("#2E7D32")) // เขียว
            "มาสาย"   -> holder.tvStatus.setTextColor(Color.parseColor("#F57C00")) // ส้ม
            "ขาด"     -> holder.tvStatus.setTextColor(Color.parseColor("#C62828")) // แดง
            else       -> holder.tvStatus.setTextColor(Color.BLACK)
        }
    }

    override fun getItemCount(): Int = items.size
}