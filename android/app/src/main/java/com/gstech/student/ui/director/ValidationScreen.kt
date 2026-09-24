package com.gstech.student.ui.director

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.gstech.student.data.AppContainer
import com.gstech.student.data.remote.dto.DocumentDto
import com.gstech.student.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File
import com.gstech.student.util.userFriendlyErrorMessage

@Composable
fun ValidationScreen(container: AppContainer) {
    var docs by remember { mutableStateOf<List<DocumentDto>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    fun reload(){scope.launch{runCatching{container.documentsApi.pending()}.onSuccess{docs=it;error=null}.onFailure{error=userFriendlyErrorMessage(it)}}}
    LaunchedEffect(Unit){reload()}
    Column(Modifier.fillMaxSize().background(GSBackground).padding(20.dp)){
        Text("Documents",style=MaterialTheme.typography.headlineMedium,color=GSTextPrimary)
        Spacer(Modifier.height(16.dp));error?.let{Text(it,color=GSDanger)}
        if(docs.isEmpty()) Text("Aucune modification en attente.",color=GSTextSecondary)
        LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp)){
            items(docs){doc->Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){
                Text(doc.nomDocument,color=GSTextPrimary,style=MaterialTheme.typography.titleMedium);Text(doc.typeDocument.uppercase(),color=GSTextSecondary);Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically){
                    OutlinedButton(onClick={
                        scope.launch {
                            runCatching {
                                val body = container.documentsApi.file(doc.idDocument)
                                val extension = if (doc.typeDocument.equals("pdf", true) || doc.nomDocument.endsWith(".pdf", true)) "pdf" else "docx"
                                val file = File(container.context.cacheDir, "document-${doc.idDocument}.$extension")
                                body.byteStream().use { input -> file.outputStream().use { input.copyTo(it) } }
                                val uri = FileProvider.getUriForFile(container.context, "${container.context.packageName}.fileprovider", file)
                                val mime = if (extension == "pdf") "application/pdf" else "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, mime)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                try {
                                    container.context.startActivity(Intent.createChooser(intent, "Ouvrir le document").apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                                } catch (_: ActivityNotFoundException) {
                                    throw IllegalStateException("Aucune application ne peut ouvrir ce document.")
                                }
                            }.onFailure { error = userFriendlyErrorMessage(it) }
                        }
                    }) { Text("Lire") }
                    OutlinedButton(onClick={scope.launch{runCatching{container.documentsApi.refuse(doc.idDocument,mapOf("motif" to "Refusé par le Directeur"))}.onSuccess{reload()}.onFailure{error=userFriendlyErrorMessage(it)}}}){Text("Refuser")}
                    Button(onClick={scope.launch{runCatching{container.documentsApi.approve(doc.idDocument)}.onSuccess{reload()}.onFailure{error=userFriendlyErrorMessage(it)}}}){Text("Accepter")}
                }
            }}}
        }
    }
}
