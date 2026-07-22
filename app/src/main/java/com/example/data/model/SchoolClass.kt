package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "school_classes")
data class SchoolClass(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val className: String,
    val section: String,
    val roomNumber: String,
    val schedule: String
)
