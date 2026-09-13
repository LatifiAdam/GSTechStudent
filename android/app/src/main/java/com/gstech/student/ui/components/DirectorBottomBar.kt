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
import com.gstech.student.ui.navigation.DirectorScreen
import com.gstech.student.ui.theme.*

private data class DirectorNavItem(val screen: DirectorScreen, val label: String, val icon: ImageVector)

private val items = listOf(
    DirectorNavItem(DirectorScreen.Home, "Accueil", Icons.Filled.Home),
    DirectorNavItem(DirectorScreen.Users, "Formateurs", Icons.Filled.People),
    DirectorNavItem(DirectorScreen.Classes, "Groupes", Icons.Filled.Groups),
    DirectorNavItem(DirectorScreen.Courses, "Modules", Icons.Filled.MenuBook),
    DirectorNavItem(DirectorScreen.Validation, "Documents", Icons.Filled.Description),
    DirectorNavItem(DirectorScreen.Assign, "Affectations", Icons.Filled.Assignment),
    DirectorNavItem(DirectorScreen.Schedule, "Créneaux", Icons.Filled.Schedule),
    DirectorNavItem(DirectorScreen.Profile, "Profil", Icons.Filled.Person),
)
@Composable
fun DirectorBottomBar(route: String?, onNavigate: (DirectorScreen) -> Unit) {
    Surface(color = GSSurface, shadowElevation = 6.dp, modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = route == item.screen.route
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
                    onClick = { onNavigate(item.screen) },
                    color = if (selected) GSBluePrimary.copy(alpha = .12f) else GSSurface,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                    modifier = Modifier.width(84.dp).height(62.dp).graphicsLayer { scaleX = itemScale; scaleY = itemScale }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(item.icon, item.label, tint = itemColor, modifier = Modifier.size(21.dp))
                        Text(item.label, fontSize = 10.sp, color = itemColor)
                    }
                }
            }
        }
    }
}
