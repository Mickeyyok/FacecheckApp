package com.example.facecheckapp

import android.app.AlertDialog
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TermActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton

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

        btnBack = findViewById(R.id.btnBack1)

        tvOnTimeCount = findViewById(R.id.tvOnTimeCount)
        tvLateCount = findViewById(R.id.tvLateCount)
        tvAbsentCount = findViewById(R.id.tvAbsentCount)
        tvTerm = findViewById(R.id.tvTerm)
        tvTermLayout = findViewById(R.id.tvTermLayout)

        updateTermDisplay()

        tvTermLayout.setOnClickListener {
            showTermSelectorDialog()
        }

        btnBack.setOnClickListener {
            finish()
        }
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
