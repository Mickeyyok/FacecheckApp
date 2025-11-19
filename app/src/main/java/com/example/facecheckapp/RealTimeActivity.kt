package com.example.facecheckapp

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class RealTimeActivity : AppCompatActivity() {

    private lateinit var datePickerLayout: LinearLayout
    private lateinit var tvDate: TextView
    private lateinit var btnBack: ImageButton

    private lateinit var tvOnTimeCount: TextView
    private lateinit var tvLateCount: TextView
    private lateinit var tvAbsentCount: TextView
    private lateinit var tvTerm: TextView
    private lateinit var tvTermLayout: LinearLayout

    private val calendar = Calendar.getInstance()
    private var selectedTerm = 1
    private val year = Calendar.getInstance().get(Calendar.YEAR) + 543

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_realtime)


        datePickerLayout = findViewById(R.id.datePickerLayout)
        tvDate = findViewById(R.id.tvDate)
        btnBack = findViewById(R.id.btnBack)

        tvOnTimeCount = findViewById(R.id.tvOnTimeCount)
        tvLateCount = findViewById(R.id.tvLateCount)
        tvAbsentCount = findViewById(R.id.tvAbsentCount)
        tvTerm = findViewById(R.id.tvTerm)
        tvTermLayout = findViewById(R.id.tvTermLayout)

        updateDateDisplay()
        updateTermDisplay()

        datePickerLayout.setOnClickListener {
            showDatePicker()
        }

        tvTermLayout.setOnClickListener {
            showTermSelectorDialog()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(Calendar.YEAR, selectedYear)
                calendar.set(Calendar.MONTH, selectedMonth)
                calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
                updateDateDisplay()
            },
            year,
            month,
            day
        ).show()
    }

    private fun updateDateDisplay() {
        val dateString = formatThaiDate(calendar.time)
        tvDate.text = dateString
    }

    private fun formatThaiDate(date: Date): String {
        val thaiMonths = arrayOf(
            "มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน",
            "พฤษภาคม", "มิถุนายน", "กรกฎาคม", "สิงหาคม",
            "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม"
        )

        val cal = Calendar.getInstance()
        cal.time = date
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = thaiMonths[cal.get(Calendar.MONTH)]
        val year = cal.get(Calendar.YEAR) + 543

        return "$day $month $year"
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

