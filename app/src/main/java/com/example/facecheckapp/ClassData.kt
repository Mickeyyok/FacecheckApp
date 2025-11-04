package com.example.facecheckapp

data class ClassData(
    val classId: String? = null,       // รหัสคลาส (Firebase push key)
    val className: String? = null,     // ชื่อวิชา
    val subjectCode: String? = null,   // รหัสวิชา
    val teacherName: String? = null,   // ชื่ออาจารย์
    val year: String? = null,          // ปีการศึกษา
    val semester: String? = null,      // ภาคเรียน
    val classRoom: String? = null,     // ห้องเรียน
    val startTime: String? = null,     // เวลาเริ่มเรียน
    val endTime: String? = null,       // เวลาจบเรียน
    val lateTime: String? = null,      // ⏰ เวลาสาย (เพิ่มใหม่)
    val createdBy: String? = null      // คนสร้างคลาส
)
