package com.gstech.student.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.ui.navigation.TeacherScreen
import com.gstech.student.ui.theme.GSBluePrimary
import com.gstech.student.ui.theme.GSSurface
import com.gstech.student.ui.theme.GSTextSecondary

private data class TeacherNavItem(val screen: TeacherScreen, val label: String, val icon: ImageVector)

private val teacherNavItems = listOf(
    TeacherNavItem(TeacherScreen.Home, "Home", Icons.Filled.Home),
    TeacherNavItem(TeacherScreen.Schedule, "Schedule", Icons.Filled.CalendarMonth),
    TeacherNavItem(TeacherScreen.Attendance, "Attendance", Icons.Filled.CheckCircle),
    TeacherNavItem(TeacherScreen.Classes, "Classes", Icons.Filled.Groups),
    TeacherNavItem(TeacherScreen.Grading, "Grading", Icons.Filled.Grade),
    TeacherNavItem(TeacherScreen.Announcements, "Annonces", Icons.Filled.Campaign),
    TeacherNavItem(TeacherScreen.Profile, "Profile", Icons.Filled.Person),
)

@Composable
fun TeacherBottomBar(currentRoute: String?, onNavigate: (TeacherScreen) -> Unit) {
    val scrollState = rememberScrollState()
    Surface(
        color = GSSurface,
        tonalElevation = 4.dp,
        shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState).padding(horizontal = 8.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            teacherNavItems.forEach { item ->
                val selected = currentRoute == item.screen.route
                val itemColor by animateColorAsState(
                    targetValue = if (selected) GSBluePrimary else GSTextSecondary,
                    animationSpec = tween(220),
                    label = "navColor"
                )
                val itemScale by animateFloatAsState(
                    targetValue = if (selected) 1f else 0.96f,
                    animationSpec = tween(220, easing = FastOutSlowInEasing),
                    label = "navScale"
                )
                Surface(
                    color = if (selected) GSBluePrimary.copy(alpha = 0.12f) else GSSurface,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                    onClick = { onNavigate(item.screen) },
                    modifier = Modifier.width(94.dp).height(66.dp).padding(horizontal = 4.dp).graphicsLayer { scaleX = itemScale; scaleY = itemScale },
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(item.icon, contentDescription = item.label, tint = itemColor, modifier = Modifier.size(21.dp))
                        Spacer(Modifier.height(4.dp))
                        Text(item.label, fontSize = 10.sp, color = itemColor, maxLines = 1)
                    }
                }
            }
        }
    }
}
