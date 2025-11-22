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

        // ดึงข้อมูลหลัก
        val subjectCode = item.subjectCode.orEmpty()
        val className = item.className.orEmpty()
        val classRoom = item.classRoom.orEmpty()

        // 🌟🌟🌟 ส่วนที่ถูกแก้ไข: สร้างข้อความวันที่และเวลาให้สมบูรณ์ 🌟🌟🌟
        val day = item.dayTime.orEmpty() // เช่น "วันจันทร์"
        val start = item.startTime.orEmpty() // เช่น "11:00"
        val end = item.endTime.orEmpty() // เช่น "13:00"
        val genericTime = item.classTime.orEmpty() // เช่น "วันจันทร์ 11.00 - 13.00"

        val timeLine: String = when {
            // ใช้ classTime ถ้ามันมีค่าที่จัดรูปแบบแล้ว
            genericTime.isNotEmpty() -> genericTime
            // รวม วัน + เวลา ถ้ามีข้อมูลแยกกัน
            day.isNotEmpty() && start.isNotEmpty() && end.isNotEmpty() -> "$day $start - $end น."
            // ใช้ วันที่เรียน (dayTime) ถ้ามีแค่ข้อมูลวัน
            day.isNotEmpty() -> day
            else -> "-"
        }
        // 🌟🌟🌟 สิ้นสุดการสร้างข้อความวันที่และเวลา 🌟🌟🌟

        // บรรทัด 1 — รหัสวิชา
        val line1 = "รหัสวิชา: $subjectCode"

        // บรรทัด 2 — ชื่อวิชา
        val line2 = "ชื่อวิชา: $className"

        // บรรทัด 3 — ห้องเรียน
        val line3 = "ห้องเรียน: $classRoom"

        // บรรทัด 4 — วันที่เรียน (ใช้ timeLine ที่ประกอบแล้ว)
        val line4 = "วันที่เรียน: $timeLine"


        // ใส่ลง TextViews
        holder.tvClassCode.text = line1
        holder.tvClassInfo.text = line2
        // รวม ห้องเรียน และ วันที่เรียน
        holder.tvClassTime.text = "$line3\n$line4"

        // ปุ่มดูรายละเอียด
        holder.btnDetail.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ClassDetailActivity::class.java)
            // ส่งข้อมูลที่จำเป็นทั้งหมดไป
            intent.putExtra("classId", item.classId)
            intent.putExtra("className", item.className)
            intent.putExtra("classRoom", item.classRoom)
            intent.putExtra("classTime", timeLine) // ส่ง timeLine ที่ประกอบเสร็จแล้ว
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = classList.size
}