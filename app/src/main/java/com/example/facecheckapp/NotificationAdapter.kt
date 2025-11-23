package com.example.facecheckapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class NotificationAdapter(
    private val items: List<NotificationModel>
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val iconCircle: View = v.findViewById(R.id.iconCircle)
        val tvIcon: TextView = v.findViewById(R.id.tvIcon)
        val tvTitle: TextView = v.findViewById(R.id.tvTitle)
        val tvLine1: TextView = v.findViewById(R.id.tvLine1)
        val tvLine2: TextView = v.findViewById(R.id.tvLine2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification_card, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val ctx = holder.itemView.context

        when (item.status) {
            "มาสาย" -> {
                holder.iconCircle.background = ContextCompat.getDrawable(
                    ctx,
                    R.drawable.bg_notify_late_circle   // วงกลมสีเหลือง/ส้ม
                )
                holder.tvIcon.text = "!"
                val studentId = item.studentId ?: "-"
                val time = item.checkinTime ?: "-"
                holder.tvTitle.text = "แจ้งเตือน: มาสาย"
                holder.tvLine1.text = "[$studentId] มาสายในวันนี้"
                holder.tvLine2.text = "เวลา $time น. โปรดตรวจสอบสาเหตุ"
            }

            "ขาด" -> {
                holder.iconCircle.background = ContextCompat.getDrawable(
                    ctx,
                    R.drawable.circle_red              // วงกลมสีแดง
                )
                holder.tvIcon.text = "!"
                val studentId = item.studentId ?: "-"
                val code = item.subjectCode ?: "-"
                holder.tvTitle.text = "แจ้งเตือน: ขาดเรียนวันนี้"
                holder.tvLine1.text = "[$studentId] ขาดเรียนวิชา [$code]"
                holder.tvLine2.text = "ในวันนี้ หากมีเหตุจำเป็นโปรดแจ้งอาจารย์ผู้สอน"
            }
        }
    }
}
