package com.example.facecheckapp

data class TermStudentStats(
    val uid: String,
    val name: String,
    val studentCode: String,
    var onTimeCount: Int = 0,
    var lateCount: Int = 0,
    var absentCount: Int = 0
)



