package com.gstech.student.data.repository
import com.gstech.student.data.remote.EstablishmentsApi
import com.gstech.student.data.remote.dto.*
class EstablishmentsRepository(private val api:EstablishmentsApi){
 suspend fun all()=api.getAll()
 suspend fun create(name:String,region:String)=api.create(CreateEstablishmentRequest(name,region))
 suspend fun update(id:String,name:String)=api.update(id,UpdateEstablishmentRequest(name))
 suspend fun delete(id:String)=api.delete(id).also { check(it.isSuccessful) { "Unable to delete establishment (${it.code()})" } }
 suspend fun assignDirector(establishmentId:String,userId:String)=api.assignDirector(establishmentId,AssignEstablishmentUserRequest(userId))
 suspend fun addGestionnaire(establishmentId:String,userId:String)=api.addGestionnaire(establishmentId,AssignEstablishmentUserRequest(userId))
 suspend fun removeGestionnaire(establishmentId:String,userId:String)=api.removeGestionnaire(establishmentId,userId)
 suspend fun removeDirector(establishmentId:String)=api.removeDirector(establishmentId)
 suspend fun mine()=api.mine()
}
