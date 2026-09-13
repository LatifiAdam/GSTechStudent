package com.gstech.student.ui.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gstech.student.data.AppContainer
import com.gstech.student.data.remote.dto.DocumentDto
import com.gstech.student.model.DocumentRequest
import com.gstech.student.util.UiState
import com.gstech.student.util.safeCall
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

val documentTypes = listOf("bulletin" to "Bulletin", "certificat_scolarite" to "Certificate of Enrollment", "releve_notes" to "Transcript of Records", "attestation_reussite" to "Certificate of Completion")

class DocumentsViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<DocumentRequest>>>(UiState.Loading)
    val state: StateFlow<UiState<List<DocumentRequest>>> = _state.asStateFlow()
    private val _approved = MutableStateFlow<List<DocumentDto>>(emptyList())
    val approved: StateFlow<List<DocumentDto>> = _approved.asStateFlow()
    private val _submitting = MutableStateFlow(false)
    val submitting: StateFlow<Boolean> = _submitting.asStateFlow()
    init { load() }
    fun load() { _state.value=UiState.Loading; viewModelScope.launch { _state.value=safeCall{container.documentsRepository.getMyRequests()}; _approved.value=runCatching{container.documentsRepository.getApprovedDocuments()}.getOrDefault(emptyList()) } }
    fun requestDocument(type:String){viewModelScope.launch{_submitting.value=true;runCatching{container.documentsRepository.requestDocument(type)};_submitting.value=false;load()}}
    fun requestApprovedDocument(id:String){viewModelScope.launch{_submitting.value=true;runCatching{container.documentsRepository.requestApprovedDocument(id)};_submitting.value=false;load()}}
}
