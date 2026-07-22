package com.example.data

import androidx.room.TypeConverter
import com.example.data.model.AttendanceStatus

class Converters {
    @TypeConverter
    fun fromAttendanceStatus(status: AttendanceStatus): String {
        return status.name
    }

    @TypeConverter
    fun toAttendanceStatus(statusStr: String): AttendanceStatus {
        return try {
            AttendanceStatus.valueOf(statusStr)
        } catch (e: Exception) {
            AttendanceStatus.PRESENT
        }
    }
}
