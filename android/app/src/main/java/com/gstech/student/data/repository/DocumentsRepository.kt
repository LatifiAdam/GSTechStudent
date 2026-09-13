package com.gstech.student.data.repository

import com.gstech.student.data.remote.DocumentRequestsApi
import com.gstech.student.data.remote.DocumentsApi
import com.gstech.student.data.remote.dto.DocumentDto
import com.gstech.student.data.remote.dto.CreateDocumentRequest
import com.gstech.student.model.DocumentRequest
import okhttp3.ResponseBody

class DocumentsRepository(private val api: DocumentRequestsApi, private val documentsApi: DocumentsApi) {

    suspend fun getMyRequests(): List<DocumentRequest> =
        api.list().map { DocumentRequest(it.idDemande, it.typeDocument ?: "document", it.statut, it.dateDemande) }

    suspend fun requestDocument(typeDocument: String): DocumentRequest {
        val dto = api.create(CreateDocumentRequest(typeDocument = typeDocument))
        return DocumentRequest(dto.idDemande, dto.typeDocument ?: "document", dto.statut, dto.dateDemande)
    }


    suspend fun getApprovedDocuments(): List<DocumentDto> = documentsApi.approved()

    suspend fun getRequestFile(id: String): ResponseBody = api.file(id)

    suspend fun requestApprovedDocument(idDocument: String): DocumentRequest {
        val dto = api.create(CreateDocumentRequest(idDocument = idDocument))
        return DocumentRequest(dto.idDemande, dto.typeDocument ?: "document", dto.statut, dto.dateDemande)
    }
}
