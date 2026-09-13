package com.gstech.student.ui.components
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.gstech.student.ui.navigation.GestionnaireScreen
import com.gstech.student.ui.theme.*
private data class Item(val r:String,val l:String,val i:ImageVector)
private val items=listOf(
    Item(GestionnaireScreen.Home.route,"Accueil",Icons.Filled.Home),
    Item(GestionnaireScreen.Classes.route,"Groupes",Icons.Filled.Groups),
    Item(GestionnaireScreen.Demandes.route,"Traiter une demande",Icons.Filled.Description),
    Item(GestionnaireScreen.Stagiaires.route,"Stagiaires",Icons.Filled.People),
    Item(GestionnaireScreen.Profile.route,"Profil",Icons.Filled.Person)
)
@Composable fun GestionnaireBottomBar(route:String?,onNavigate:(String)->Unit){Surface(color=GSSurface,shadowElevation=6.dp,modifier=Modifier.fillMaxWidth().navigationBarsPadding()){Row(Modifier.fillMaxWidth().padding(4.dp),horizontalArrangement=Arrangement.SpaceEvenly,verticalAlignment=Alignment.CenterVertically){items.forEach{item->val sel=route==item.r
            val itemColor by animateColorAsState(if (sel) GSBluePrimary else GSTextSecondary, tween(220), label = "navColor")
            val itemScale by animateFloatAsState(if (sel) 1f else 0.96f, tween(220, easing = FastOutSlowInEasing), label = "navScale");Surface(onClick={onNavigate(item.r)},color=if(sel)GSBluePrimary.copy(alpha=.12f) else GSSurface,shape=RoundedCornerShape(14.dp),modifier=Modifier.weight(1f).height(62.dp).graphicsLayer { scaleX = itemScale; scaleY = itemScale }){Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Icon(item.i,item.l,tint=itemColor,modifier=Modifier.size(21.dp));Text(item.l,fontSize=10.sp,color=itemColor)}}}}}}
