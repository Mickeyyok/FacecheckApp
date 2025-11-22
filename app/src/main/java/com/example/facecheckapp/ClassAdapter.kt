package com.example.facecheckapp

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ClassAdapter(private val classList: List<ClassData>) :
    RecyclerView.Adapter<ClassAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvClassCode: TextView = view.findViewById(R.id.tvClassCode)
        val tvClassInfo: TextView = view.findViewById(R.id.tvClassInfo)
        val tvClassTime: TextView = view.findViewById(R.id.tvClassTime)
        val btnDetail: Button = view.findViewById(R.id.btnDetail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_class, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = classList[position]

        // บรรทัด 1 — รหัสวิชา
        val line1 = "รหัสวิชา: ${item.subjectCode.orEmpty()}"

        // บรรทัด 2 — ชื่อวิชา
        val line2 = "ชื่อวิชา: ${item.className.orEmpty()}"

        // บรรทัด 3 — ห้องเรียน
        val line3 = "ห้องเรียน: ${item.classRoom.orEmpty()}"

        // บรรทัด 4 — วันที่เรียน
        // ถ้ามี classTime ให้ใช้ classTime
        // ถ้ามี dayTime ก็ใช้แทนได้เช่น "22/11/2025"
        val dayText = when {
            !item.classTime.isNullOrEmpty() -> item.classTime   // Monday 12.00–13.00
            !item.dayTime.isNullOrEmpty() -> item.dayTime       // หรือ 22/11/2025
            else -> "-"
        }

        val line4 = "วันที่เรียน: $dayText"

        // ใส่ลง TextView
        holder.tvClassCode.text = line1
        holder.tvClassInfo.text = line2
        holder.tvClassTime.text = "$line3\n$line4"

        // ปุ่มดูรายละเอียด
        holder.btnDetail.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ClassDetailActivity::class.java)
            intent.putExtra("classId", item.classId)
            intent.putExtra("className", item.className)
            intent.putExtra("classRoom", item.classRoom)
            intent.putExtra("classTime", dayText)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = classList.size
}



