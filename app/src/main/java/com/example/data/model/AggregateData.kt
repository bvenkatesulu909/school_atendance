package com.example.data.model

data class StudentAttendanceStats(
    val student: Student,
    val className: String,
    val totalRecords: Int,
    val presentCount: Int,
    val absentCount: Int,
    val lateCount: Int,
    val excusedCount: Int,
    val attendancePercentage: Float
)

data class ClassAttendanceSummary(
    val schoolClass: SchoolClass,
    val totalStudents: Int,
    val todayPresent: Int,
    val todayAbsent: Int,
    val todayLate: Int,
    val todayExcused: Int,
    val attendancePercentage: Float
)

data class StudentAttendanceHistory(
    val record: AttendanceRecord,
    val className: String
)
