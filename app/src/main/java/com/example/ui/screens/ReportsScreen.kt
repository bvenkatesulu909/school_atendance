package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AttendanceStatus
import com.example.ui.components.LowAttendanceWarningChip
import com.example.ui.components.MetricStatCard
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusAbsentBg
import com.example.ui.theme.StatusPresent
import com.example.ui.theme.StatusPresentBg
import com.example.ui.viewmodel.AttendanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: AttendanceViewModel,
    onStudentClick: (Long) -> Unit
) {
    val context = LocalContext.current
    val classSummaries by viewModel.classSummaries.collectAsStateWithLifecycle()
    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()

    val totalStudents = allStudents.size
    val totalPresent = classSummaries.sumOf { it.todayPresent }
    val totalAbsent = classSummaries.sumOf { it.todayAbsent }
    val totalLate = classSummaries.sumOf { it.todayLate }
    val totalExcused = classSummaries.sumOf { it.todayExcused }

    fun shareReport() {
        val sb = StringBuilder()
        sb.appendLine("====================================")
        sb.appendLine("  SCHOOL ATTENDANCE REPORT")
        sb.appendLine("  Date: $selectedDate")
        sb.appendLine("====================================")
        sb.appendLine()
        sb.appendLine("SUMMARY METRICS:")
        sb.appendLine("Total Students: $totalStudents")
        sb.appendLine("Present: $totalPresent")
        sb.appendLine("Absent: $totalAbsent")
        sb.appendLine("Late: $totalLate")
        sb.appendLine("Excused: $totalExcused")
        sb.appendLine()
        sb.appendLine("CLASS BREAKDOWN:")
        classSummaries.forEach { summary ->
            sb.appendLine("- ${summary.schoolClass.className} (${summary.schoolClass.section}): ${summary.attendancePercentage.toInt()}% Present [${summary.todayPresent}/${summary.totalStudents}]")
        }
        sb.appendLine()
        sb.appendLine("Generated via Attendance Tracker App")

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            putExtra(Intent.EXTRA_SUBJECT, "Attendance Report - $selectedDate")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Attendance Report")
        context.startActivity(shareIntent)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance Reports", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Share Report Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Export Attendance Summary",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Generate and share summary for $selectedDate",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                            )
                        }

                        Button(
                            onClick = { shareReport() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("btn_share_report")
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Summary Stats Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Daily Stats Breakdown ($selectedDate)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Present", style = MaterialTheme.typography.labelMedium, color = StatusPresent)
                                Text("$totalPresent", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Absent", style = MaterialTheme.typography.labelMedium, color = StatusAbsent)
                                Text("$totalAbsent", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Late", style = MaterialTheme.typography.labelMedium)
                                Text("$totalLate", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Excused", style = MaterialTheme.typography.labelMedium)
                                Text("$totalExcused", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Class Analytics Progress Bars
            item {
                Text(
                    text = "Class Attendance Rates",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(classSummaries) { summary ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = summary.schoolClass.className,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${summary.attendancePercentage.toInt()}%",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (summary.attendancePercentage >= 75f) StatusPresent else StatusAbsent
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { (summary.attendancePercentage / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (summary.attendancePercentage >= 75f) StatusPresent else StatusAbsent,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Enrolled: ${summary.totalStudents} | Present: ${summary.todayPresent} | Absent: ${summary.todayAbsent}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
