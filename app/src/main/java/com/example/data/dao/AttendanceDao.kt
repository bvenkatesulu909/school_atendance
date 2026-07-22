package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AttendanceRecord
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    // --- School Classes ---
    @Query("SELECT * FROM school_classes ORDER BY className ASC")
    fun getAllClasses(): Flow<List<SchoolClass>>

    @Query("SELECT * FROM school_classes WHERE id = :classId LIMIT 1")
    suspend fun getClassById(classId: Long): SchoolClass?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(schoolClass: SchoolClass): Long

    @Update
    suspend fun updateClass(schoolClass: SchoolClass)

    @Delete
    suspend fun deleteClass(schoolClass: SchoolClass)

    // --- Students ---
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY rollNumber ASC, name ASC")
    fun getStudentsByClass(classId: Long): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :studentId LIMIT 1")
    suspend fun getStudentById(studentId: Long): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    // --- Attendance Records ---
    @Query("SELECT * FROM attendance_records WHERE classId = :classId AND date = :date")
    fun getAttendanceForClassAndDate(classId: Long, date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE date = :date")
    fun getAttendanceForDate(date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceForStudent(studentId: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records ORDER BY date DESC")
    fun getAllAttendanceRecords(): Flow<List<AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceRecord(record: AttendanceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceRecords(records: List<AttendanceRecord>)

    @Query("DELETE FROM attendance_records WHERE id = :recordId")
    suspend fun deleteAttendanceRecord(recordId: Long)
}
