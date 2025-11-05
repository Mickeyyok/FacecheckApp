package com.example.facecheckapp

import android.app.AlertDialog
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TermActivity : AppCompatActivity() {

    private lateinit var tvOnTimeCount: TextView
    private lateinit var tvLateCount: TextView
    private lateinit var tvAbsentCount: TextView
    private lateinit var tvTerm: TextView
    private lateinit var tvTermLayout: LinearLayout
    private var selectedTerm = 1
    private val year = 2568

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_term)


        tvOnTimeCount = findViewById(R.id.tvOnTimeCount)
        tvLateCount = findViewById(R.id.tvLateCount)
        tvAbsentCount = findViewById(R.id.tvAbsentCount)
        tvTerm = findViewById(R.id.tvTerm)
        tvTermLayout = findViewById(R.id.tvTermLayout)


        updateTermDisplay()


        tvTermLayout.setOnClickListener {
            showTermSelectorDialog()
        }

        //  (ตัวอย่าง - สามารถเปลี่ยนเป็นดึงข้อมูลจาก Firebase ได้)
        // คำนวณจำนวนครั้งจากข้อมูลนักศึกษา
        // ตรงเวลา: 0+20+30+20+20 = 90 ครั้ง
        // มาสาย: 30+10+0+5+10 = 55 ครั้ง
        // ขาด: 0+0+0+5+0 = 5 ครั้ง
        
        tvOnTimeCount.text = "90 ครั้ง"
        tvLateCount.text = "55 ครั้ง"
        tvAbsentCount.text = "5 ครั้ง"
    }

    private fun showTermSelectorDialog() {
        val terms = arrayOf("เทอม 1", "เทอม 2")
        val currentIndex = selectedTerm - 1

        AlertDialog.Builder(this)
            .setTitle("เลือกเทอม")
            .setSingleChoiceItems(terms, currentIndex) { dialog, which ->
                selectedTerm = which + 1
                updateTermDisplay()
                dialog.dismiss()
            }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }

    private fun updateTermDisplay() {
        tvTerm.text = "$selectedTerm / $year"
    }
}
