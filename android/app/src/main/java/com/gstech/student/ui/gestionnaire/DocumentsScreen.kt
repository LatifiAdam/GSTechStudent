package com.gstech.student.ui.gestionnaire

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gstech.student.data.AppContainer
import com.gstech.student.data.remote.dto.DocumentDto
import com.gstech.student.data.remote.dto.DocumentRequestDto
import com.gstech.student.data.remote.dto.RefuseJustificationRequest
import com.gstech.student.ui.theme.GSBackground
import com.gstech.student.ui.theme.GSBluePrimary
import com.gstech.student.ui.theme.GSTextPrimary
import com.gstech.student.ui.theme.GSTextSecondary
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.gstech.student.util.userFriendlyErrorMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionnaireDocumentsScreen(
    container: AppContainer,
    onBack: () -> Unit
) {
    var docs by remember {
        mutableStateOf<List<DocumentDto>>(emptyList())
    }

    var requests by remember {
        mutableStateOf<List<DocumentRequestDto>>(emptyList())
    }

    var selectedFile by remember {
        mutableStateOf<Uri?>(null)
    }

    var documentName by remember {
        mutableStateOf("")
    }

    var message by remember {
        mutableStateOf<String?>(null)
    }

    var selectedRequest by remember {
        mutableStateOf<DocumentRequestDto?>(null)
    }

    var showUploadDialog by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    /**
     * Recharge les documents et les demandes.
     */
    fun reload() {
        scope.launch {

            runCatching {
                container.documentsApi.mine()
            }.onSuccess { result ->
                docs = result
            }.onFailure { error ->
                message = userFriendlyErrorMessage(error)
            }

            runCatching {
                container.documentRequestsApi.list("en_attente")
            }.onSuccess { result ->
                requests = result
            }.onFailure { error ->
                message = userFriendlyErrorMessage(error)
            }
        }
    }

    LaunchedEffect(Unit) {
        reload()
    }

    /**
     * Sélecteur de fichiers.
     * On autorise uniquement les PDF.
     */
    val filePicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            val resolver = container.context.contentResolver
            val mime = resolver.getType(uri)
            val size = resolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
            if (mime != "application/pdf") {
                message = "Veuillez sélectionner un fichier PDF."
                return@rememberLauncherForActivityResult
            }
            if (size > 10L * 1024L * 1024L) {
                message = "Le PDF ne doit pas dépasser 10 Mo."
                return@rememberLauncherForActivityResult
            }
            selectedFile = uri
        }

    Scaffold(
        containerColor = GSBackground,

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Gestion documentaire",
                        color = GSTextPrimary
                    )
                },

                navigationIcon = {
                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = GSTextPrimary
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GSBackground
                )
            )
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showUploadDialog = true
                },

                containerColor = GSBluePrimary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Ajouter un document",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GSBackground)
                .padding(paddingValues)
                .padding(20.dp)
        ) {

            Text(
                text = "Demandes reçues",
                style = MaterialTheme.typography.headlineMedium,
                color = GSTextPrimary
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            if (requests.isEmpty()) {

                Text(
                    text = "Aucune demande en attente.",
                    color = GSTextSecondary
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),

                verticalArrangement = Arrangement.spacedBy(
                    10.dp
                )
            ) {

                items(
                    items = requests,
                    key = { request ->
                        request.idDemande
                    }
                ) { request ->

                    Card(
                        onClick = {
                            selectedRequest = request
                        },

                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {

                            val studentName =
                                listOfNotNull(
                                    request.etudiant
                                        ?.utilisateur
                                        ?.prenom,

                                    request.etudiant
                                        ?.utilisateur
                                        ?.nom
                                )
                                    .joinToString(" ")
                                    .ifBlank {
                                        "Stagiaire"
                                    }

                            Text(
                                text = studentName,
                                color = GSTextPrimary,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(
                                modifier = Modifier.height(3.dp)
                            )

                            Text(
                                text =
                                    request.document?.nomDocument
                                        ?: request.typeDocument
                                        ?: "Document",

                                color = GSTextPrimary
                            )

                            Spacer(
                                modifier = Modifier.height(3.dp)
                            )

                            Text(
                                text = request.dateDemande.take(10),
                                color = GSTextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = "Mes documents",
                style = MaterialTheme.typography.titleLarge,
                color = GSTextPrimary
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            docs.take(4).forEach { document ->

                Text(
                    text = "${document.nomDocument} • ${document.statut}",
                    color = GSTextSecondary,
                    modifier = Modifier.padding(
                        vertical = 3.dp
                    )
                )
            }

            message?.let { text ->

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = text,
                    color = GSTextSecondary
                )
            }
        }
    }

    /**
     * Dialog d'envoi d'un document au Directeur.
     */
    if (showUploadDialog) {

        AlertDialog(
            onDismissRequest = {
                showUploadDialog = false
            },

            title = {
                Text(
                    text = "Ajouter un document"
                )
            },

            text = {

                Column {

                    OutlinedTextField(
                        value = documentName,

                        onValueChange = {
                            documentName = it
                        },

                        label = {
                            Text("Nom")
                        },

                        modifier = Modifier.fillMaxWidth(),

                        singleLine = true
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Button(
                        onClick = {
                            filePicker.launch(
                                arrayOf("application/pdf")
                            )
                        },

                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text =
                                if (selectedFile == null) {
                                    "Choisir un PDF"
                                } else {
                                    "Fichier sélectionné"
                                }
                        )
                    }
                }
            },

            confirmButton = {

                TextButton(
                    enabled =
                        selectedFile != null &&
                                documentName.isNotBlank(),

                    onClick = {

                        val uri = selectedFile

                        if (uri == null) {
                            message = "Veuillez sélectionner un PDF."
                            return@TextButton
                        }

                        scope.launch {

                            runCatching {

                                val resolver =
                                    container.context.contentResolver

                                val bytes =
                                    resolver
                                        .openInputStream(uri)
                                        ?.use {
                                                input ->
                                            input.readBytes()
                                        }
                                        ?: error(
                                            "Impossible de lire le fichier."
                                        )

                                val mimeType =
                                    "application/pdf"

                                val fileName =
                                    documentName
                                        .trim()
                                        .ifBlank {
                                            "document"
                                        } +
                                            ".pdf"

                                val fileRequestBody =
                                    bytes.toRequestBody(
                                        mimeType.toMediaType()
                                    )

                                val multipart =
                                    MultipartBody.Part
                                        .createFormData(
                                            name = "file",
                                            filename = fileName,
                                            body = fileRequestBody
                                        )

                                val nameBody =
                                    documentName
                                        .trim()
                                        .toRequestBody(
                                            "text/plain".toMediaType()
                                        )

                                container.documentsApi.upload(
                                    multipart,
                                    nameBody
                                )

                            }.onSuccess {

                                message =
                                    "Document envoyé au Directeur pour validation."

                                selectedFile = null
                                documentName = ""
                                showUploadDialog = false

                                reload()

                            }.onFailure { error ->

                                message =
                                    userFriendlyErrorMessage(error)
                            }
                        }
                    }
                ) {
                    Text(
                        text = "Envoyer"
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showUploadDialog = false
                    }
                ) {
                    Text(
                        text = "Annuler"
                    )
                }
            }
        )
    }

    /**
     * Dialog de traitement d'une demande Stagiaire.
     */
    selectedRequest?.let { request ->

        AlertDialog(
            onDismissRequest = {
                selectedRequest = null
            },

            title = {
                Text(
                    text = "Demande de document"
                )
            },

            text = {

                Column {

                    val studentName =
                        listOfNotNull(
                            request.etudiant
                                ?.utilisateur
                                ?.prenom,

                            request.etudiant
                                ?.utilisateur
                                ?.nom
                        )
                            .joinToString(" ")
                            .ifBlank {
                                "Stagiaire"
                            }

                    Text(
                        text = "Stagiaire : $studentName",
                        color = GSTextPrimary
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        text =
                            "Document : ${
                                request.document?.nomDocument
                                    ?: request.typeDocument
                                    ?: "Document"
                            }",

                        color = GSTextPrimary
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        text =
                            "Voulez-vous accepter ou refuser cette demande ?",

                        color = GSTextSecondary
                    )
                }
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        scope.launch {

                            runCatching {

                                container.documentRequestsApi
                                    .generate(
                                        request.idDemande
                                    )

                            }.onSuccess {

                                selectedRequest = null

                                message =
                                    "Demande acceptée et traitement lancé."

                                reload()

                            }.onFailure { error ->

                                message =
                                    userFriendlyErrorMessage(error)
                            }
                        }
                    }
                ) {
                    Text(
                        text = "Accepter"
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {

                        scope.launch {

                            runCatching {

                                container.documentRequestsApi
                                    .refuse(
                                        request.idDemande,

                                        RefuseJustificationRequest(
                                            "Demande refusée par le Gestionnaire"
                                        )
                                    )

                            }.onSuccess {

                                selectedRequest = null

                                message =
                                    "Demande refusée."

                                reload()

                            }.onFailure { error ->

                                message =
                                    userFriendlyErrorMessage(error)
                            }
                        }
                    }
                ) {
                    Text(
                        text = "Refuser"
                    )
                }
            }
        )
    }
}
