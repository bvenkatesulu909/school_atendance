package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AttendanceStatus
import com.example.ui.components.AttendanceStatusBadge
import com.example.ui.components.AttendanceStatusToggleGroup
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusPresent
import com.example.ui.viewmodel.AttendanceViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkAttendanceScreen(
    viewModel: AttendanceViewModel,
    initialClassId: Long? = null
) {
    val allClasses by viewModel.allClasses.collectAsStateWithLifecycle()
    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val selectedClassId by viewModel.selectedClassId.collectAsStateWithLifecycle()
    val draftAttendance by viewModel.draftAttendance.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var expandedClassDropdown by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Select initial class if provided or set default
    LaunchedEffect(allClasses, initialClassId) {
        if (allClasses.isNotEmpty() && selectedClassId == null) {
            val targetId = initialClassId ?: allClasses.first().id
            viewModel.setSelectedClass(targetId)
        }
    }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    val currentClass = allClasses.find { it.id == selectedClassId }
    val classStudents = allStudents.filter { it.classId == selectedClassId }
    val filteredStudents = classStudents.filter {
        searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true) || it.rollNumber.contains(searchQuery, ignoreCase = true)
    }

    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                viewModel.setSelectedDate(format.format(selectedCal.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mark Attendance", fontWeight = FontWeight.Bold) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${classStudents.size} Enrolled Students",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        val presentCount = draftAttendance.values.count { it.first == AttendanceStatus.PRESENT }
                        Text(
                            text = "$presentCount Marked Present",
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusPresent
                        )
                    }

                    Button(
                        onClick = { viewModel.saveDraftAttendance() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_save_attendance"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save Attendance"
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Records", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Date & Class Selection Panel
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Date Picker Field
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable { datePickerDialog.show() }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("btn_select_date"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Select Date",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Attendance Date",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = selectedDate,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = if (selectedDate == viewModel.todayDate) "TODAY" else "CHANGE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        // Class Dropdown Selection
                        ExposedDropdownMenuBox(
                            expanded = expandedClassDropdown,
                            onExpandedChange = { expandedClassDropdown = !expandedClassDropdown },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = currentClass?.let { "${it.className} (${it.section})" } ?: "Select Class",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Selected Class") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Class,
                                        contentDescription = "Class",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedClassDropdown) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .testTag("dropdown_class_selector"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = expandedClassDropdown,
                                onDismissRequest = { expandedClassDropdown = false }
                            ) {
                                allClasses.forEach { schoolClass ->
                                    DropdownMenuItem(
                                        text = { Text("${schoolClass.className} - ${schoolClass.section}") },
                                        onClick = {
                                            viewModel.setSelectedClass(schoolClass.id)
                                            expandedClassDropdown = false
                                        },
                                        modifier = Modifier.testTag("item_class_${schoolClass.id}")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Batch Actions & Search Bar
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.markAllDraftStatus(AttendanceStatus.PRESENT) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_mark_all_present"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusPresent)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Mark All Present",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("All Present", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.markAllDraftStatus(AttendanceStatus.ABSENT) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_mark_all_absent"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusAbsent)
                        ) {
                            Text("All Absent", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search student name or roll #...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_student_attendance"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Student Cards
            items(filteredStudents, key = { it.id }) { student ->
                val draft = draftAttendance[student.id] ?: (AttendanceStatus.PRESENT to "")
                val currentStatus = draft.first
                val currentNote = draft.second
                var showNoteInput by remember { mutableStateOf(currentNote.isNotEmpty()) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_student_attendance_${student.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = student.name.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = student.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Roll #${student.rollNumber}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(
                                onClick = { showNoteInput = !showNoteInput },
                                modifier = Modifier.testTag("btn_toggle_note_${student.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = "Add Note",
                                    tint = if (currentNote.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Toggle Group for Present / Absent / Late / Excused
                        AttendanceStatusToggleGroup(
                            selectedStatus = currentStatus,
                            onStatusSelected = { status ->
                                viewModel.setStudentStatus(student.id, status)
                            }
                        )

                        // Optional Note Input
                        AnimatedVisibility(visible = showNoteInput || currentStatus != AttendanceStatus.PRESENT) {
                            OutlinedTextField(
                                value = currentNote,
                                onValueChange = { note -> viewModel.setStudentNote(student.id, note) },
                                label = { Text("Note / Reason (optional)") },
                                placeholder = { Text("e.g., Sick leave, Doctor's note, Traffic delay...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_note_${student.id}"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
