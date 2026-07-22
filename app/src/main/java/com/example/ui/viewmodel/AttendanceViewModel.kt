package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AttendanceRepository
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.ClassAttendanceSummary
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.data.model.StudentAttendanceStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AttendanceRepository

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayDate: String = dateFormat.format(Date())

    private val _selectedDate = MutableStateFlow(todayDate)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedClassId = MutableStateFlow<Long?>(null)
    val selectedClassId: StateFlow<Long?> = _selectedClassId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilterClassId = MutableStateFlow<Long?>(null)
    val selectedFilterClassId: StateFlow<Long?> = _selectedFilterClassId.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = AttendanceRepository(db.attendanceDao())

        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
    }

    val allClasses: StateFlow<List<SchoolClass>> = repository.allClasses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allStudents: StateFlow<List<Student>> = repository.allStudents
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Class summaries for selected date
    val classSummaries: StateFlow<List<ClassAttendanceSummary>> = selectedDate
        .flatMapLatest { date -> repository.getClassSummariesForDate(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered students list for directory
    val filteredStudents: StateFlow<List<Student>> = combine(
        repository.allStudents,
        searchQuery,
        selectedFilterClassId
    ) { students, query, classFilter ->
        students.filter { student ->
            val matchesQuery = query.isEmpty() ||
                    student.name.contains(query, ignoreCase = true) ||
                    student.rollNumber.contains(query, ignoreCase = true)
            val matchesClass = classFilter == null || student.classId == classFilter
            matchesQuery && matchesClass
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Draft attendance states for marking screen: Map<StudentId, Pair<AttendanceStatus, Note>>
    private val _draftAttendance = MutableStateFlow<Map<Long, Pair<AttendanceStatus, String>>>(emptyMap())
    val draftAttendance: StateFlow<Map<Long, Pair<AttendanceStatus, String>>> = _draftAttendance.asStateFlow()

    // Load attendance records into draft for a class and date
    fun loadDraftAttendance(classId: Long, date: String) {
        _selectedClassId.value = classId
        _selectedDate.value = date

        viewModelScope.launch {
            repository.getStudentsForClass(classId).collect { students ->
                repository.getAttendanceForClassAndDate(classId, date).collect { records ->
                    val recordMap = records.associate { it.studentId to (it.status to it.note) }
                    val initialDraft = students.associate { student ->
                        val existing = recordMap[student.id]
                        student.id to (existing ?: (AttendanceStatus.PRESENT to ""))
                    }
                    _draftAttendance.value = initialDraft
                }
            }
        }
    }

    fun setStudentStatus(studentId: Long, status: AttendanceStatus) {
        val current = _draftAttendance.value.toMutableMap()
        val currentPair = current[studentId] ?: (AttendanceStatus.PRESENT to "")
        current[studentId] = status to currentPair.second
        _draftAttendance.value = current
    }

    fun setStudentNote(studentId: Long, note: String) {
        val current = _draftAttendance.value.toMutableMap()
        val currentPair = current[studentId] ?: (AttendanceStatus.PRESENT to "")
        current[studentId] = currentPair.first to note
        _draftAttendance.value = current
    }

    fun markAllDraftStatus(status: AttendanceStatus) {
        val current = _draftAttendance.value.toMutableMap()
        current.keys.forEach { studentId ->
            val note = current[studentId]?.second ?: ""
            current[studentId] = status to note
        }
        _draftAttendance.value = current
    }

    fun saveDraftAttendance() {
        val classId = selectedClassId.value ?: return
        val date = selectedDate.value
        val draft = draftAttendance.value

        viewModelScope.launch {
            val records = draft.map { (studentId, pair) ->
                AttendanceRecord(
                    studentId = studentId,
                    classId = classId,
                    date = date,
                    status = pair.first,
                    note = pair.second
                )
            }
            repository.saveAttendanceBatch(records)
            _toastMessage.value = "Attendance saved for $date!"
        }
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
        selectedClassId.value?.let { loadDraftAttendance(it, date) }
    }

    fun setSelectedClass(classId: Long) {
        _selectedClassId.value = classId
        loadDraftAttendance(classId, selectedDate.value)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedFilterClass(classId: Long?) {
        _selectedFilterClassId.value = classId
    }

    // Class CRUD
    fun addClass(className: String, section: String, roomNumber: String, schedule: String) {
        viewModelScope.launch {
            repository.insertClass(
                SchoolClass(
                    className = className,
                    section = section,
                    roomNumber = roomNumber,
                    schedule = schedule
                )
            )
            _toastMessage.value = "Class $className added!"
        }
    }

    fun updateClass(schoolClass: SchoolClass) {
        viewModelScope.launch {
            repository.updateClass(schoolClass)
            _toastMessage.value = "Class updated!"
        }
    }

    fun deleteClass(schoolClass: SchoolClass) {
        viewModelScope.launch {
            repository.deleteClass(schoolClass)
            _toastMessage.value = "Class deleted!"
        }
    }

    // Student CRUD
    fun addStudent(
        classId: Long,
        rollNumber: String,
        name: String,
        parentPhone: String,
        parentEmail: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.insertStudent(
                Student(
                    classId = classId,
                    rollNumber = rollNumber,
                    name = name,
                    parentPhone = parentPhone,
                    parentEmail = parentEmail,
                    notes = notes
                )
            )
            _toastMessage.value = "Student $name added!"
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch {
            repository.updateStudent(student)
            _toastMessage.value = "Student details updated!"
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            _toastMessage.value = "Student removed!"
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun getStudentStatsFlow(studentId: Long): StateFlow<StudentAttendanceStats?> {
        return repository.getStudentStats(studentId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    }

    fun getStudentHistoryFlow(studentId: Long): StateFlow<List<AttendanceRecord>> {
        return repository.getAttendanceForStudent(studentId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }
}
