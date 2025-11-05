package com.example.facecheckapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class RealtimeActivity : AppCompatActivity() {

    private lateinit var datePickerLayout: LinearLayout
    private lateinit var tvDate: TextView
    private lateinit var btnBack: ImageButton
    
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_realtime)


        datePickerLayout = findViewById(R.id.datePickerLayout)
        tvDate = findViewById(R.id.tvDate)
        btnBack = findViewById(R.id.btnBack)


        updateDateDisplay()


        datePickerLayout.setOnClickListener {
            showDatePicker()
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
}

