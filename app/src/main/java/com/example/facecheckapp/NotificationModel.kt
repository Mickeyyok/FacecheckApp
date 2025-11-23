package com.example.facecheckapp

data class NotificationModel(
    var id: String? = null,          // key ใน Firebase
    var status: String? = null,      // "มาสาย" หรือ "ขาด"
    var subjectCode: String? = null, // รหัสวิชา
    var subjectName: String? = null, // ชื่อวิชา
    var studentId: String? = null,   // รหัสนักศึกษา
    var checkinTime: String? = null, // เวลาเช็กชื่อ
    var createdAt: Long? = null,     // ใช้ sort
    var seen: Boolean? = null        // true = อ่านแล้ว, false/null = ยังไม่อ่าน
)
