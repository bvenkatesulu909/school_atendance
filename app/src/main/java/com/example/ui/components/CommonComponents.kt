package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusAbsentBg
import com.example.ui.theme.StatusExcused
import com.example.ui.theme.StatusExcusedBg
import com.example.ui.theme.StatusLate
import com.example.ui.theme.StatusLateBg
import com.example.ui.theme.StatusPresent
import com.example.ui.theme.StatusPresentBg

@Composable
fun AttendanceStatusBadge(
    status: AttendanceStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, fgColor, icon) = when (status) {
        AttendanceStatus.PRESENT -> Triple(StatusPresentBg, StatusPresent, Icons.Default.CheckCircle)
        AttendanceStatus.ABSENT -> Triple(StatusAbsentBg, StatusAbsent, Icons.Default.Cancel)
        AttendanceStatus.LATE -> Triple(StatusLateBg, StatusLate, Icons.Default.Schedule)
        AttendanceStatus.EXCUSED -> Triple(StatusExcusedBg, StatusExcused, Icons.Default.EventNote)
    }

    Surface(
        modifier = modifier.clip(RoundedCornerShape(50)),
        color = bgColor,
        contentColor = fgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = status.label,
                modifier = Modifier.size(14.dp),
                tint = fgColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status.label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = fgColor
            )
        }
    }
}

@Composable
fun AttendanceStatusToggleGroup(
    selectedStatus: AttendanceStatus,
    onStatusSelected: (AttendanceStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AttendanceStatus.values().forEach { status ->
            val isSelected = selectedStatus == status
            val (baseColor, bgColor) = when (status) {
                AttendanceStatus.PRESENT -> StatusPresent to StatusPresentBg
                AttendanceStatus.ABSENT -> StatusAbsent to StatusAbsentBg
                AttendanceStatus.LATE -> StatusLate to StatusLateBg
                AttendanceStatus.EXCUSED -> StatusExcused to StatusExcusedBg
            }

            val animatedBg by animateColorAsState(
                targetValue = if (isSelected) baseColor else bgColor,
                label = "statusBg"
            )
            val animatedFg by animateColorAsState(
                targetValue = if (isSelected) Color.White else baseColor,
                label = "statusFg"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(animatedBg)
                    .border(
                        width = if (isSelected) 0.dp else 1.dp,
                        color = baseColor.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onStatusSelected(status) }
                    .testTag("status_toggle_${status.name.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = status.label,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = animatedFg,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LowAttendanceWarningChip(
    percentage: Float,
    modifier: Modifier = Modifier
) {
    if (percentage < 75f) {
        Surface(
            modifier = modifier.clip(RoundedCornerShape(8.dp)),
            color = StatusAbsentBg,
            contentColor = StatusAbsent
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Low attendance warning",
                    modifier = Modifier.size(12.dp),
                    tint = StatusAbsent
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Low (${percentage.toInt()}%)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatusAbsent
                )
            }
        }
    }
}
