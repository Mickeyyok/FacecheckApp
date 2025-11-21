package com.example.facecheckapp

data class HistoryModel(
    val date: String,      // 3 ต.ค. 2568
    val subject: String,   // SP 999-1 วิชาการตลาด
    val status: String,    // ตรงเวลา / มาสาย / ขาด
    val timestamp: Long    // ใช้สำหรับ sort (ล่าสุดก่อน)
)
