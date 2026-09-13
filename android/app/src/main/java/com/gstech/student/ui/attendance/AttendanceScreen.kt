package com.gstech.student.ui.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.data.AppContainer
import com.gstech.student.model.AttendanceRecord
import com.gstech.student.model.AttendanceStatus
import com.gstech.student.ui.components.*
import com.gstech.student.ui.theme.*
import com.gstech.student.util.UiState

@Composable
fun AttendanceScreen(container: AppContainer) {
    val viewModel = remember { AttendanceViewModel(container) }
    val state by viewModel.state.collectAsState()
    val filter by viewModel.filter.collectAsState()

    Column(Modifier.fillMaxSize().background(GSBackground)) {
        Row(
            Modifier.fillMaxWidth().padding(20.dp, 20.dp, 20.dp, 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Attendance", style = MaterialTheme.typography.headlineMedium, color = GSTextPrimary)
            Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = GSTextPrimary)
        }

        when (val s = state) {
            is UiState.Loading -> LoadingState()
            is UiState.Error -> ErrorState(s.message) { viewModel.load() }
            is UiState.Success -> AttendanceContent(
                data = s.data,
                filter = filter,
                onFilter = viewModel::setFilter,
                filtered = viewModel.filtered(s.data.history),
                trend = viewModel.monthlyTrend(s.data.history),
            )
        }
    }
}

@Composable
private fun AttendanceContent(
    data: AttendanceData,
    filter: AttendanceFilter,
    onFilter: (AttendanceFilter) -> Unit,
    filtered: List<AttendanceRecord>,
    trend: List<Pair<String, Int>>,
) {
    Column(Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(8.dp))
        GSCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularStat(percent = data.summary.percentage, size = 84.dp)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Overall Status", color = GSTextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val absencePercent = (100 - data.summary.percentage).coerceIn(0, 100)
                        val (statusText, statusColor) = when {
                            absencePercent >= 75 -> "AT RISK" to GSDanger
                            absencePercent >= 50 -> "HIGH ABSENCE" to GSDanger
                            absencePercent >= 25 -> "WARNING" to GSWarning
                            else -> "SAFE" to GSSuccess
                        }
                        StatusPill(statusText, statusColor)
                        Spacer(Modifier.width(8.dp))
                        Text("Absence: $absencePercent%", color = GSTextSecondary, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        GSCard {
            Text("Monthly Absence Trend", style = MaterialTheme.typography.titleMedium, color = GSTextPrimary)
            Spacer(Modifier.height(16.dp))
            if (trend.isEmpty()) {
                Text("No absences recorded yet.", color = GSTextSecondary)
            } else {
                val maxVal = (trend.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)
                Row(
                    Modifier.fillMaxWidth().height(120.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    trend.forEach { (month, count) ->
                        val barHeight = (count.toFloat() / maxVal * 90).dp.coerceAtLeast(8.dp)
                        val color = if (count >= maxVal) GSDanger else GSTeal
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                Modifier
                                    .width(28.dp)
                                    .height(barHeight)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(color),
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(month, fontSize = 12.sp, color = GSTextSecondary)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChipItem("All", filter == AttendanceFilter.ALL) { onFilter(AttendanceFilter.ALL) }
            FilterChipItem("Present", filter == AttendanceFilter.PRESENT) { onFilter(AttendanceFilter.PRESENT) }
            FilterChipItem("Absent", filter == AttendanceFilter.ABSENT) { onFilter(AttendanceFilter.ABSENT) }
            FilterChipItem("Late", filter == AttendanceFilter.LATE) { onFilter(AttendanceFilter.LATE) }
        }

        Spacer(Modifier.height(16.dp))
        GSCard {
            Text("Recent History", style = MaterialTheme.typography.titleMedium, color = GSTextPrimary)
            Spacer(Modifier.height(10.dp))
            if (filtered.isEmpty()) {
                Text("No records for this filter.", color = GSTextSecondary, modifier = Modifier.padding(vertical = 8.dp))
            } else {
                filtered.take(30).forEachIndexed { index, record ->
                    HistoryRow(record)
                    if (index != filtered.lastIndex) {
                        HorizontalDivider(color = GSDivider, modifier = Modifier.padding(vertical = 10.dp))
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun HistoryRow(record: AttendanceRecord) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(record.courseName, fontWeight = FontWeight.SemiBold, color = GSTextPrimary)
            Text(record.dateTimeIso?.take(10) ?: "—", color = GSTextSecondary, fontSize = 12.sp)
        }
        val (label, color) = when (record.status) {
            AttendanceStatus.PRESENT -> "Present" to GSSuccess
            AttendanceStatus.ABSENT -> "Absent" to GSDanger
            AttendanceStatus.LATE -> "Late" to GSWarning
            AttendanceStatus.UNKNOWN -> "Unknown" to GSTextSecondary
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(6.dp))
            Text(label, color = color, fontWeight = FontWeight.Medium, fontSize = 13.sp)
        }
    }
}

@Composable
private fun FilterChipItem(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) GSBluePrimary else GSSurface,
        modifier = Modifier.selectable(selected = selected, onClick = onClick),
    ) {
        Text(
            label,
            color = if (selected) Color.White else GSTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}
