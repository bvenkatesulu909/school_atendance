package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Student
import com.example.ui.components.LowAttendanceWarningChip
import com.example.ui.viewmodel.AttendanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDirectoryScreen(
    viewModel: AttendanceViewModel,
    onStudentClick: (Long) -> Unit
) {
    val students by viewModel.filteredStudents.collectAsStateWithLifecycle()
    val allClasses by viewModel.allClasses.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilterClassId by viewModel.selectedFilterClassId.collectAsStateWithLifecycle()

    var showAddStudentDialog by remember { mutableStateOf(false) }
    var studentToEdit by remember { mutableStateOf<Student?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Students Directory", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddStudentDialog = true },
                modifier = Modifier.testTag("fab_add_student"),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Add Student"
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search by name or roll number...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search_student_directory"),
                shape = RoundedCornerShape(12.dp)
            )

            // Class Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    FilterChip(
                        selected = selectedFilterClassId == null,
                        onClick = { viewModel.setSelectedFilterClass(null) },
                        label = { Text("All Classes (${viewModel.allStudents.value.size})") },
                        modifier = Modifier.testTag("chip_filter_all")
                    )
                }
                items(allClasses) { schoolClass ->
                    FilterChip(
                        selected = selectedFilterClassId == schoolClass.id,
                        onClick = { viewModel.setSelectedFilterClass(schoolClass.id) },
                        label = { Text("${schoolClass.className} (${schoolClass.section})") },
                        modifier = Modifier.testTag("chip_filter_${schoolClass.id}")
                    )
                }
            }

            // Student List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(students, key = { it.id }) { student ->
                    val schoolClass = allClasses.find { it.id == student.classId }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStudentClick(student.id) }
                            .testTag("card_student_${student.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = student.name.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = student.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "Roll #${student.rollNumber} • ${schoolClass?.className ?: "No class"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (student.parentPhone.isNotEmpty()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Call,
                                                contentDescription = "Phone",
                                                modifier = Modifier.size(12.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = student.parentPhone,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { studentToEdit = student },
                                    modifier = Modifier.testTag("btn_edit_student_${student.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Student",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Details",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(70.dp))
                }
            }
        }
    }

    // Add / Edit Student Dialog
    if (showAddStudentDialog || studentToEdit != null) {
        val isEdit = studentToEdit != null
        val editingStudent = studentToEdit

        var name by remember { mutableStateOf(editingStudent?.name ?: "") }
        var rollNumber by remember { mutableStateOf(editingStudent?.rollNumber ?: "") }
        var classId by remember { mutableStateOf(editingStudent?.classId ?: allClasses.firstOrNull()?.id ?: 1L) }
        var parentPhone by remember { mutableStateOf(editingStudent?.parentPhone ?: "") }
        var parentEmail by remember { mutableStateOf(editingStudent?.parentEmail ?: "") }
        var notes by remember { mutableStateOf(editingStudent?.notes ?: "") }
        var classDropdownExpanded by remember { mutableStateOf(false) }

        val selectedClass = allClasses.find { it.id == classId }

        AlertDialog(
            onDismissRequest = {
                showAddStudentDialog = false
                studentToEdit = null
            },
            title = { Text(if (isEdit) "Edit Student" else "Add New Student", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_input_student_name")
                    )
                    OutlinedTextField(
                        value = rollNumber,
                        onValueChange = { rollNumber = it },
                        label = { Text("Roll Number / Student ID") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_input_roll_number")
                    )

                    ExposedDropdownMenuBox(
                        expanded = classDropdownExpanded,
                        onExpandedChange = { classDropdownExpanded = !classDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedClass?.let { "${it.className} (${it.section})" } ?: "Select Class",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Assign Class") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = classDropdownExpanded,
                            onDismissRequest = { classDropdownExpanded = false }
                        ) {
                            allClasses.forEach { schoolClass ->
                                DropdownMenuItem(
                                    text = { Text("${schoolClass.className} (${schoolClass.section})") },
                                    onClick = {
                                        classId = schoolClass.id
                                        classDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = parentPhone,
                        onValueChange = { parentPhone = it },
                        label = { Text("Parent / Guardian Phone") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = parentEmail,
                        onValueChange = { parentEmail = it },
                        label = { Text("Parent Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (name.isNotBlank() && rollNumber.isNotBlank()) {
                            if (isEdit && editingStudent != null) {
                                viewModel.updateStudent(
                                    editingStudent.copy(
                                        classId = classId,
                                        rollNumber = rollNumber,
                                        name = name,
                                        parentPhone = parentPhone,
                                        parentEmail = parentEmail,
                                        notes = notes
                                    )
                                )
                            } else {
                                viewModel.addStudent(
                                    classId = classId,
                                    rollNumber = rollNumber,
                                    name = name,
                                    parentPhone = parentPhone,
                                    parentEmail = parentEmail,
                                    notes = notes
                                )
                            }
                            showAddStudentDialog = false
                            studentToEdit = null
                        }
                    },
                    modifier = Modifier.testTag("dialog_btn_save_student")
                ) {
                    Text(if (isEdit) "Update" else "Add", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                if (isEdit && editingStudent != null) {
                    TextButton(
                        onClick = {
                            viewModel.deleteStudent(editingStudent)
                            studentToEdit = null
                        },
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                } else {
                    TextButton(onClick = { showAddStudentDialog = false; studentToEdit = null }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}
