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

        // ใน SubjectAdapter.kt
        class SubjectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitle: TextView = view.findViewById(R.id.tvSubjectTitle)
            val tvLocation: TextView = view.findViewById(R.id.tvLocation)


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
            val teacher = item.teacherName ?: "" // ดึงข้อมูลจาก ClassModel

            val start = item.startTime ?: ""
            val end = item.endTime ?: ""


            // 🔥 ให้ใช้ classTime ก่อนเสมอ


            holder.tvTitle.text = "$code $name"
            holder.tvLocation.text = "ห้องเรียน $room"



            holder.itemView.setOnClickListener { onClick(item) }
        }



        override fun getItemCount(): Int = items.size
    }