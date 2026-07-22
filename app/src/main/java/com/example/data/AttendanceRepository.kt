package com.example.data

import com.example.data.dao.AttendanceDao
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.ClassAttendanceSummary
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.data.model.StudentAttendanceStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AttendanceRepository(private val dao: AttendanceDao) {

    val allClasses: Flow<List<SchoolClass>> = dao.getAllClasses()
    val allStudents: Flow<List<Student>> = dao.getAllStudents()
    val allAttendanceRecords: Flow<List<AttendanceRecord>> = dao.getAllAttendanceRecords()

    fun getStudentsForClass(classId: Long): Flow<List<Student>> = dao.getStudentsByClass(classId)

    fun getAttendanceForClassAndDate(classId: Long, date: String): Flow<List<AttendanceRecord>> =
        dao.getAttendanceForClassAndDate(classId, date)

    fun getAttendanceForDate(date: String): Flow<List<AttendanceRecord>> =
        dao.getAttendanceForDate(date)

    fun getAttendanceForStudent(studentId: Long): Flow<List<AttendanceRecord>> =
        dao.getAttendanceForStudent(studentId)

    suspend fun getClassById(classId: Long): SchoolClass? = dao.getClassById(classId)
    suspend fun getStudentById(studentId: Long): Student? = dao.getStudentById(studentId)

    suspend fun insertClass(schoolClass: SchoolClass): Long = dao.insertClass(schoolClass)
    suspend fun updateClass(schoolClass: SchoolClass) = dao.updateClass(schoolClass)
    suspend fun deleteClass(schoolClass: SchoolClass) = dao.deleteClass(schoolClass)

    suspend fun insertStudent(student: Student): Long = dao.insertStudent(student)
    suspend fun updateStudent(student: Student) = dao.updateStudent(student)
    suspend fun deleteStudent(student: Student) = dao.deleteStudent(student)

    suspend fun saveAttendanceRecord(record: AttendanceRecord): Long = dao.insertAttendanceRecord(record)
    suspend fun saveAttendanceBatch(records: List<AttendanceRecord>) = dao.insertAttendanceRecords(records)

    suspend fun deleteAttendanceRecord(recordId: Long) = dao.deleteAttendanceRecord(recordId)

    // Calculate statistics for all classes on a given date
    fun getClassSummariesForDate(date: String): Flow<List<ClassAttendanceSummary>> {
        return combine(allClasses, allStudents, dao.getAttendanceForDate(date)) { classes, students, records ->
            classes.map { clazz ->
                val classStudents = students.filter { it.classId == clazz.id }
                val classRecords = records.filter { it.classId == clazz.id }

                val present = classRecords.count { it.status == AttendanceStatus.PRESENT }
                val absent = classRecords.count { it.status == AttendanceStatus.ABSENT }
                val late = classRecords.count { it.status == AttendanceStatus.LATE }
                val excused = classRecords.count { it.status == AttendanceStatus.EXCUSED }

                val totalStudents = classStudents.size
                val percentage = if (totalStudents > 0 && classRecords.isNotEmpty()) {
                    ((present + late + excused).toFloat() / totalStudents.toFloat()) * 100f
                } else {
                    0f
                }

                ClassAttendanceSummary(
                    schoolClass = clazz,
                    totalStudents = totalStudents,
                    todayPresent = present,
                    todayAbsent = absent,
                    todayLate = late,
                    todayExcused = excused,
                    attendancePercentage = percentage
                )
            }
        }
    }

    // Get attendance stats for a specific student across all dates
    fun getStudentStats(studentId: Long): Flow<StudentAttendanceStats?> {
        return combine(allStudents, allClasses, dao.getAttendanceForStudent(studentId)) { students, classes, records ->
            val student = students.find { it.id == studentId } ?: return@combine null
            val className = classes.find { it.id == student.classId }?.let { "${it.className} (${it.section})" } ?: "Unassigned"

            val total = records.size
            val present = records.count { it.status == AttendanceStatus.PRESENT }
            val absent = records.count { it.status == AttendanceStatus.ABSENT }
            val late = records.count { it.status == AttendanceStatus.LATE }
            val excused = records.count { it.status == AttendanceStatus.EXCUSED }

            val percentage = if (total > 0) {
                ((present + late + excused).toFloat() / total.toFloat()) * 100f
            } else 100f

            StudentAttendanceStats(
                student = student,
                className = className,
                totalRecords = total,
                presentCount = present,
                absentCount = absent,
                lateCount = late,
                excusedCount = excused,
                attendancePercentage = percentage
            )
        }
    }

    // Ensure database has sample seed data if empty
    suspend fun checkAndSeedInitialData() {
        val existingClasses = allClasses.first()
        if (existingClasses.isEmpty()) {
            val class1Id = dao.insertClass(
                SchoolClass(
                    className = "Grade 10 - Mathematics",
                    section = "Section A",
                    roomNumber = "Room 301",
                    schedule = "Mon, Wed, Fri - 08:30 AM"
                )
            )
            val class2Id = dao.insertClass(
                SchoolClass(
                    className = "Grade 10 - Physics",
                    section = "Section B",
                    roomNumber = "Lab 102",
                    schedule = "Tue, Thu - 10:00 AM"
                )
            )
            val class3Id = dao.insertClass(
                SchoolClass(
                    className = "Grade 9 - History",
                    section = "Section A",
                    roomNumber = "Room 205",
                    schedule = "Mon, Tue, Thu - 11:30 AM"
                )
            )

            // Seed Students for Class 1 (Math 10A)
            val studentsClass1 = listOf(
                Student(classId = class1Id, rollNumber = "10A-01", name = "Liam Anderson", parentPhone = "+1 555-0101", parentEmail = "m.anderson@example.com", notes = "Class Representative"),
                Student(classId = class1Id, rollNumber = "10A-02", name = "Olivia Bennett", parentPhone = "+1 555-0102", parentEmail = "j.bennett@example.com"),
                Student(classId = class1Id, rollNumber = "10A-03", name = "Ethan Carter", parentPhone = "+1 555-0103", parentEmail = "r.carter@example.com", notes = "Math Olympiad team"),
                Student(classId = class1Id, rollNumber = "10A-04", name = "Sophia Davis", parentPhone = "+1 555-0104", parentEmail = "s.davis@example.com"),
                Student(classId = class1Id, rollNumber = "10A-05", name = "Jackson Evans", parentPhone = "+1 555-0105", parentEmail = "p.evans@example.com"),
                Student(classId = class1Id, rollNumber = "10A-06", name = "Ava Foster", parentPhone = "+1 555-0106", parentEmail = "d.foster@example.com")
            )

            // Seed Students for Class 2 (Physics 10B)
            val studentsClass2 = listOf(
                Student(classId = class2Id, rollNumber = "10B-01", name = "Noah Garcia", parentPhone = "+1 555-0201", parentEmail = "a.garcia@example.com"),
                Student(classId = class2Id, rollNumber = "10B-02", name = "Emma Hayes", parentPhone = "+1 555-0202", parentEmail = "c.hayes@example.com", notes = "Science Club Vice President"),
                Student(classId = class2Id, rollNumber = "10B-03", name = "Oliver Jackson", parentPhone = "+1 555-0203", parentEmail = "m.jackson@example.com"),
                Student(classId = class2Id, rollNumber = "10B-04", name = "Charlotte King", parentPhone = "+1 555-0204", parentEmail = "t.king@example.com")
            )

            // Seed Students for Class 3 (History 9A)
            val studentsClass3 = listOf(
                Student(classId = class3Id, rollNumber = "09A-01", name = "Lucas Martinez", parentPhone = "+1 555-0301", parentEmail = "e.martinez@example.com"),
                Student(classId = class3Id, rollNumber = "09A-02", name = "Mia Nelson", parentPhone = "+1 555-0302", parentEmail = "b.nelson@example.com"),
                Student(classId = class3Id, rollNumber = "09A-03", name = "Benjamin Parker", parentPhone = "+1 555-0303", parentEmail = "k.parker@example.com")
            )

            val studentIdsClass1 = studentsClass1.map { dao.insertStudent(it) }
            val studentIdsClass2 = studentsClass2.map { dao.insertStudent(it) }
            val studentIdsClass3 = studentsClass3.map { dao.insertStudent(it) }

            // Generate past 5 dates for realistic history
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()

            for (dayOffset in 0..4) {
                val pastCal = calendar.clone() as Calendar
                pastCal.add(Calendar.DAY_OF_YEAR, -dayOffset)
                val dateStr = dateFormat.format(pastCal.time)

                // Attendance for Class 1
                studentIdsClass1.forEachIndexed { index, id ->
                    val status = when {
                        index == 3 && dayOffset == 0 -> AttendanceStatus.LATE
                        index == 4 && dayOffset <= 1 -> AttendanceStatus.ABSENT
                        index == 5 && dayOffset == 2 -> AttendanceStatus.EXCUSED
                        else -> AttendanceStatus.PRESENT
                    }
                    val note = if (status == AttendanceStatus.LATE) "Arrived 15m late - traffic" else if (status == AttendanceStatus.EXCUSED) "Medical appointment note" else ""
                    dao.insertAttendanceRecord(
                        AttendanceRecord(
                            studentId = id,
                            classId = class1Id,
                            date = dateStr,
                            status = status,
                            note = note
                        )
                    )
                }

                // Attendance for Class 2
                studentIdsClass2.forEachIndexed { index, id ->
                    val status = if (index == 2 && dayOffset == 1) AttendanceStatus.ABSENT else AttendanceStatus.PRESENT
                    dao.insertAttendanceRecord(
                        AttendanceRecord(
                            studentId = id,
                            classId = class2Id,
                            date = dateStr,
                            status = status
                        )
                    )
                }

                // Attendance for Class 3
                studentIdsClass3.forEachIndexed { idIndex, id ->
                    dao.insertAttendanceRecord(
                        AttendanceRecord(
                            studentId = id,
                            classId = class3Id,
                            date = dateStr,
                            status = AttendanceStatus.PRESENT
                        )
                    )
                }
            }
        }
    }
}
