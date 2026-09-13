package com.gstech.student.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.ui.navigation.Screen
import com.gstech.student.ui.theme.GSBluePrimary
import com.gstech.student.ui.theme.GSSurface
import com.gstech.student.ui.theme.GSTextPrimary
import com.gstech.student.ui.theme.GSTextSecondary

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun GSBottomBar(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit
) {
    val bottomNavItems = listOf(
        BottomNavItem(Screen.Home, "Home", Icons.Filled.Home),
        BottomNavItem(Screen.Schedule, "Schedule", Icons.Filled.Schedule),
        BottomNavItem(Screen.Attendance, "Attendance", Icons.Filled.CalendarMonth),
        BottomNavItem(Screen.Grades, "Grades", Icons.Filled.StarBorder),
        BottomNavItem(Screen.Announcements, "Announcements", Icons.Filled.Campaign),
        BottomNavItem(Screen.Documents, "Documents", Icons.Filled.Description),
        BottomNavItem(Screen.Profile, "Profile", Icons.Filled.Person),
    )

    val scrollState = rememberScrollState()

    Surface(
        color = GSSurface,
        tonalElevation = 4.dp,
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentRoute == item.screen.route
                val itemColor by animateColorAsState(
                    targetValue = if (selected) GSBluePrimary else GSTextSecondary,
                    animationSpec = tween(220),
                    label = "bottomBarItemColor"
                )
                val itemScale by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (selected) 1f else 0.96f,
                    animationSpec = tween(220, easing = FastOutSlowInEasing),
                    label = "bottomBarItemScale"
                )
                Surface(
                    color = if (selected) GSBluePrimary.copy(alpha = 0.12f) else GSSurface,
                    shape = RoundedCornerShape(14.dp),
                    onClick = { onNavigate(item.screen) },
                    modifier = Modifier
                        .width(94.dp)
                        .height(66.dp)
                        .padding(horizontal = 4.dp)
                        .graphicsLayer { scaleX = itemScale; scaleY = itemScale },
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = itemColor,
                            modifier = Modifier.size(21.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = item.label,
                            fontSize = 10.sp,
                            color = itemColor,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GSCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(220)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = GSSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            content = content
        )
    }
}

@Composable
fun StatusPill(
    text: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .padding(
                horizontal = 10.dp,
                vertical = 4.dp
            )
    ) {
        Text(
            text = text,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
fun SectionTitle(
    title: String,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = GSTextPrimary
        )

        action?.invoke()
    }
}

@Composable
fun CircularStat(
    percent: Int,
    size: androidx.compose.ui.unit.Dp = 88.dp,
    color: Color = GSBluePrimary
) {
    val animatedPercent by androidx.compose.animation.core.animateFloatAsState(
        targetValue = percent.coerceIn(0, 100).toFloat(),
        animationSpec = tween(650, easing = FastOutSlowInEasing),
        label = "circularStatPercent"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size)
    ) {
        CircularProgressIndicator(
            progress = {
                (animatedPercent / 100f).coerceIn(0f, 1f)
            },
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 8.dp,
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )

        Text(
            text = "${animatedPercent.toInt()}%",
            fontWeight = FontWeight.Bold,
            fontSize = (size.value / 4.2).sp,
            color = GSTextPrimary
        )
    }
}

@Composable
fun AnimatedSection(
    visible: Boolean = true,
    delayMillis: Int = 0,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(320, delayMillis = delayMillis)) +
            slideInVertically(
                animationSpec = tween(360, delayMillis = delayMillis, easing = FastOutSlowInEasing),
                initialOffsetY = { it / 12 }
            )
    ) {
        content()
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = GSTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = GSBluePrimary
            )
        ) {
            Text("Retry")
        }
    }
}

@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = GSBluePrimary
        )
    }
}

fun CircleShapeColor(color: Color) = color