package com.gstech.student.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.ui.navigation.AdminScreen
import com.gstech.student.ui.theme.*

private data class AdminNavItem(val screen: AdminScreen, val label: String, val icon: ImageVector)
private val items = listOf(
    AdminNavItem(AdminScreen.Home, "Home", Icons.Filled.Home),
    AdminNavItem(AdminScreen.Users, "Users", Icons.Filled.People),
    AdminNavItem(AdminScreen.Establishments, "Assign", Icons.Filled.Business),
    AdminNavItem(AdminScreen.Profile, "Profile", Icons.Filled.Person),
)

@Composable
fun AdminBottomBar(currentRoute: String?, onNavigate: (AdminScreen) -> Unit) {
    Surface(color = GSSurface, shadowElevation = 6.dp, modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 5.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            items.forEach { item ->
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
                Surface(color = if (selected) GSBluePrimary.copy(alpha = .12f) else GSSurface, shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp), onClick = { onNavigate(item.screen) }, modifier = Modifier.width(90.dp).height(62.dp).graphicsLayer { scaleX = itemScale; scaleY = itemScale }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(item.icon, item.label, tint = itemColor, modifier = Modifier.size(21.dp))
                        Spacer(Modifier.height(3.dp))
                        Text(item.label, fontSize = 10.sp, color = itemColor)
                    }
                }
            }
        }
    }
}
