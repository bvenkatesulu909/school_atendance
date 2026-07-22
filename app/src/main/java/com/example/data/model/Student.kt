package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "students",
    foreignKeys = [
        ForeignKey(
            entity = SchoolClass::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["classId"])]
)
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val classId: Long,
    val rollNumber: String,
    val name: String,
    val parentPhone: String = "",
    val parentEmail: String = "",
    val notes: String = ""
)
