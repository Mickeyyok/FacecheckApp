package com.example.facecheckapp

import android.content.Intent
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

        holder.tvClassCode.text = "${item.subjectCode ?: "-"} ${item.className ?: ""}"
        holder.tvClassInfo.text = "ห้อง ${item.classRoom ?: "-"}"
        holder.tvClassTime.text = "${item.startTime ?: "-"} - ${item.endTime ?: "-"} น."

        // ✅ เปิดหน้า ClassDetailActivity พร้อมส่ง classId ไปด้วย
        holder.btnDetail.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ClassDetailActivity::class.java)
            intent.putExtra("classId", item.classId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = classList.size
}
