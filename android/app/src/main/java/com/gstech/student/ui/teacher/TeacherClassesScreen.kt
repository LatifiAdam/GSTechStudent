package com.gstech.student.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gstech.student.data.AppContainer
import com.gstech.student.data.remote.dto.ClassDto
import com.gstech.student.data.remote.dto.StudentUserDto
import com.gstech.student.ui.components.GSCard
import com.gstech.student.ui.theme.*
import kotlinx.coroutines.launch
import com.gstech.student.util.userFriendlyErrorMessage

@Composable
fun TeacherClassesScreen(container: AppContainer, onBack: () -> Unit = {}) {
    val repo = container.adminManagementRepository
    val scope = rememberCoroutineScope()
    var classes by remember { mutableStateOf<List<ClassDto>>(emptyList()) }
    var selected by remember { mutableStateOf<ClassDto?>(null) }
    var students by remember { mutableStateOf<List<StudentUserDto>>(emptyList()) }; var selectedStudent by remember { mutableStateOf<StudentUserDto?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { runCatching { val id=container.tokenManager.userIdNow()!!; val a=repo.assignments().filter{it.idFormateur==id}; classes=repo.classes().filter{c->a.any{it.idClasse==c.idClasse}} }.onFailure{error=userFriendlyErrorMessage(it)} }
    Column(Modifier.fillMaxSize().background(GSBackground).padding(20.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(8.dp)) { IconButton(onClick=onBack){Icon(Icons.AutoMirrored.Filled.ArrowBack,"Back", tint=GSTextPrimary)}; Text("My Classes", style=MaterialTheme.typography.headlineMedium, color=GSTextPrimary) }
        error?.let { Text(it, color=GSDanger) }
        if (selected == null) {
            LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp)) { items(classes){ c -> GSCard(Modifier.clickable{ selected=c; scope.launch{students=repo.students(c.idClasse)} }){ Text(c.nomClasse, style=MaterialTheme.typography.titleMedium,color=GSTextPrimary); Text(c.description.orEmpty(),color=GSTextSecondary) } } }
        } else {
            TextButton(onClick={selected=null}){Text("← All classes")}
            Text(selected!!.nomClasse, style=MaterialTheme.typography.titleLarge,color=GSTextPrimary)
            Spacer(Modifier.height(10.dp))
            if (selectedStudent == null) LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)){ items(students){ st -> GSCard(Modifier.clickable{ selectedStudent=st }){ val u=st.utilisateur; Text("${u?.prenom.orEmpty()} ${u?.nom.orEmpty()}",style=MaterialTheme.typography.titleMedium,color=GSTextPrimary); Text("ID: ${st.idUtilisateur}",color=GSTextSecondary); Text("CIN: ${u?.cin ?: "—"} • Phone: ${u?.telephone ?: "—"}",color=GSTextSecondary) } } } else { val u=selectedStudent!!.utilisateur; TextButton(onClick={selectedStudent=null}){Text("← Stagieres")}; GSCard{Text("${u?.prenom.orEmpty()} ${u?.nom.orEmpty()}",style=MaterialTheme.typography.titleLarge,color=GSTextPrimary); Text("ID: ${selectedStudent!!.idUtilisateur}",color=GSTextSecondary); Text("Numéro Stagiere : ${selectedStudent!!.numeroEtudiant ?: "—"}",color=GSTextSecondary); Text("CIN: ${u?.cin ?: "—"}",color=GSTextSecondary); Text("Phone: ${u?.telephone ?: "—"}",color=GSTextSecondary); Text("Email: ${u?.email ?: "—"}",color=GSTextSecondary); Text("Address: ${u?.adresse ?: "—"}",color=GSTextSecondary) }}
        }
    }
}
