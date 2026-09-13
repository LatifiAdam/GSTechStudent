package com.gstech.student.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.data.AppContainer
import com.gstech.student.model.AppNotification
import com.gstech.student.model.NotificationCategory
import com.gstech.student.ui.components.ErrorState
import com.gstech.student.ui.components.LoadingState
import com.gstech.student.ui.theme.*
import com.gstech.student.util.UiState

@Composable
fun NotificationsScreen(container: AppContainer) {
    val viewModel = remember { NotificationsViewModel(container) }
    val state by viewModel.state.collectAsState()
    val filter by viewModel.filter.collectAsState()

    Column(Modifier.fillMaxSize().background(GSBackground)) {
        Row(
            Modifier.fillMaxWidth().padding(20.dp, 20.dp, 20.dp, 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Notifications", style = MaterialTheme.typography.headlineMedium, color = GSTextPrimary)
            Icon(Icons.Filled.Notifications, contentDescription = null, tint = GSTextPrimary)
        }

        Spacer(Modifier.height(14.dp))
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip("All", filter == NotifFilter.ALL) { viewModel.setFilter(NotifFilter.ALL) }
            FilterChip("Academic", filter == NotifFilter.ACADEMIC) { viewModel.setFilter(NotifFilter.ACADEMIC) }
            FilterChip("Admin", filter == NotifFilter.ADMIN) { viewModel.setFilter(NotifFilter.ADMIN) }
            FilterChip("Urgent", filter == NotifFilter.URGENT) { viewModel.setFilter(NotifFilter.URGENT) }
        }

        Spacer(Modifier.height(14.dp))
        when (val s = state) {
            is UiState.Loading -> LoadingState()
            is UiState.Error -> ErrorState(s.message) { viewModel.load() }
            is UiState.Success -> {
                val list = viewModel.filtered(s.data)
                if (list.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("You're all caught up.", color = GSTextSecondary)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(list, key = { it.id }) { notif ->
                            NotificationCard(notif) { viewModel.markRead(notif) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
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
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}

private fun iconFor(category: NotificationCategory): ImageVector = when (category) {
    NotificationCategory.URGENT -> Icons.Filled.WarningAmber
    NotificationCategory.ACADEMIC -> Icons.Filled.Description
    NotificationCategory.ADMIN -> Icons.Filled.Check
    NotificationCategory.OTHER -> Icons.Filled.Schedule
}

@Composable
private fun NotificationCard(notif: AppNotification, onOpen: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GSSurface,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
    ) {
        Row(Modifier.padding(16.dp)) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(GSBackground),
                contentAlignment = Alignment.Center,
            ) {
                Icon(iconFor(notif.category), contentDescription = null, tint = GSBluePrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        notif.category.name.lowercase().replaceFirstChar { it.uppercase() },
                        color = GSTeal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(notif.dateTimeIso.take(10), color = GSTextSecondary, fontSize = 11.sp)
                }
                Spacer(Modifier.height(2.dp))
                Text(notif.title, style = MaterialTheme.typography.titleMedium, color = GSTextPrimary)
                Text(notif.body, color = GSTextSecondary, fontSize = 13.sp, maxLines = 2)
            }
            if (!notif.read) {
                Box(Modifier.padding(start = 6.dp).size(8.dp).clip(CircleShape).background(GSBluePrimary))
            }
        }
    }
}
