package com.gstech.student.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.R
import com.gstech.student.ui.navigation.Screen
import com.gstech.student.ui.theme.GSBackground
import com.gstech.student.ui.theme.GSBluePrimary
import com.gstech.student.ui.theme.GSDanger
import com.gstech.student.ui.theme.GSDivider
import com.gstech.student.ui.theme.GSSuccess
import com.gstech.student.ui.theme.GSSurface
import com.gstech.student.ui.theme.GSTextPrimary
import com.gstech.student.ui.theme.GSTextSecondary
import com.gstech.student.ui.theme.GSTeal
import com.gstech.student.ui.theme.GSWarning

// Shared visual language used across the app and based on the supplied UI reference:
// light canvas, white outlined cards, compact headers, blue emphasis, green status pills.

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
        BottomNavItem(Screen.Announcements, "News", Icons.Filled.Campaign),
        BottomNavItem(Screen.Documents, "Docs", Icons.Filled.Description),
        BottomNavItem(Screen.Profile, "Profile", Icons.Filled.Person),
    )
    val scrollState = rememberScrollState()

    Surface(
        color = GSSurface,
        tonalElevation = 0.dp,
        shadowElevation = 10.dp,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .border(1.dp, GSDivider),
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
                    animationSpec = tween(200),
                    label = "bottomBarItemColor"
                )
                val itemScale by animateFloatAsState(
                    targetValue = if (selected) 1f else 0.98f,
                    animationSpec = tween(200, easing = FastOutSlowInEasing),
                    label = "bottomBarItemScale"
                )
                Surface(
                    color = if (selected) GSBluePrimary.copy(alpha = 0.10f) else Color.Transparent,
                    shape = RoundedCornerShape(14.dp),
                    onClick = { onNavigate(item.screen) },
                    modifier = Modifier
                        .width(88.dp)
                        .height(60.dp)
                        .padding(horizontal = 3.dp)
                        .graphicsLayer { scaleX = itemScale; scaleY = itemScale },
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(item.icon, contentDescription = item.label, tint = itemColor, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = item.label,
                            fontSize = 10.sp,
                            color = itemColor,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
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
            .animateContentSize(animationSpec = tween(180)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GSSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, GSDivider),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content,
        )
    }
}

@Composable
fun GSHeader(
    userName: String? = null,
    roleLabel: String = "SIS PLATFORM",
    greeting: String? = null,
    avatarText: String? = null,
    modifier: Modifier = Modifier,
) {
    val initials = avatarText?.trim()?.take(2)?.uppercase()
        ?: userName?.trim()?.split(" ")?.filter { it.isNotBlank() }?.take(2)?.joinToString("") { it.first().toString() }?.uppercase()
        ?: "GS"

    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = GSSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GSDivider),
                    modifier = Modifier.size(42.dp),
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(R.drawable.logo_v5),
                        contentDescription = "GSTech",
                        modifier = Modifier.padding(4.dp),
                    )
                }
                Spacer(Modifier.width(9.dp))
                Column {
                    Text("GSTech", color = GSBluePrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(roleLabel, color = GSTeal, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(GSBluePrimary.copy(alpha = 0.10f))
                    .border(1.dp, GSBluePrimary.copy(alpha = 0.10f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(initials, color = GSBluePrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (greeting != null || userName != null) {
            Spacer(Modifier.height(17.dp))
            Text(
                text = greeting ?: "Bonjour, ${userName ?: ""}".trim(),
                style = MaterialTheme.typography.headlineSmall,
                color = GSTextPrimary,
            )
            if (userName != null && greeting != null) {
                Text(userName, color = GSTextSecondary, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun GSScreenHeader(
    title: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineMedium, color = GSTextPrimary)
            subtitle?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(2.dp))
                Text(it, color = GSTextSecondary, fontSize = 12.sp)
            }
        }
        if (icon != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = GSSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, GSDivider),
                modifier = Modifier.size(42.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = GSBluePrimary, modifier = Modifier.size(21.dp))
                }
            }
        }
    }
}

@Composable
fun GSMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    valueColor: Color = GSBluePrimary,
) {
    GSCard(modifier) {
        Text(label.uppercase(), color = GSTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp)
        Spacer(Modifier.height(5.dp))
        Text(value, color = valueColor, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        supportingText?.let {
            Spacer(Modifier.height(2.dp))
            Text(it, color = GSTextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
fun StatusPill(text: String, color: Color = GSSuccess) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 9.dp, vertical = 4.dp),
    ) {
        Text(text = text, color = color, fontWeight = FontWeight.Bold, fontSize = 10.sp)
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
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge, color = GSTextPrimary)
        action?.invoke()
    }
}

@Composable
fun CircularStat(
    percent: Int,
    size: Dp = 84.dp,
    color: Color = GSBluePrimary,
) {
    val animatedPercent by animateFloatAsState(
        targetValue = percent.coerceIn(0, 100).toFloat(),
        animationSpec = tween(650, easing = FastOutSlowInEasing),
        label = "circularStatPercent",
    )
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
        CircularProgressIndicator(
            progress = { (animatedPercent / 100f).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 7.dp,
            color = color,
            trackColor = color.copy(alpha = 0.13f),
        )
        Text(
            text = "${animatedPercent.toInt()}%",
            fontWeight = FontWeight.Bold,
            fontSize = (size.value / 4.2).sp,
            color = GSTextPrimary,
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
                initialOffsetY = { it / 12 },
            ),
    ) { content() }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = GSDanger.copy(alpha = 0.08f),
            border = androidx.compose.foundation.BorderStroke(1.dp, GSDanger.copy(alpha = 0.12f)),
        ) {
            Text(message, color = GSTextSecondary, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = GSBluePrimary),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        ) { Text("Retry") }
    }
}

@Composable
fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = GSBluePrimary, strokeWidth = 3.dp)
            Spacer(Modifier.height(10.dp))
            Text("Loading…", color = GSTextSecondary, fontSize = 12.sp)
        }
    }
}

fun CircleShapeColor(color: Color) = color
