package com.gstech.student.data.repository

import com.gstech.student.data.local.TokenManager
import com.gstech.student.data.remote.UsersApi
import com.gstech.student.data.remote.dto.UserDto
import com.gstech.student.data.remote.dto.ChangePasswordRequest
import com.gstech.student.model.Student

class ProfileRepository(
    private val api: UsersApi,
    private val tokenManager: TokenManager,
) {
    suspend fun currentUserId(): String =
        tokenManager.userIdNow() ?: error("No signed-in user.")

    suspend fun getCurrentStudent(): Student {
        val id = currentUserId()
        return api.getUser(id).toStudent()
    }

    suspend fun rawUser(id: String): UserDto = api.getUser(id)

    suspend fun changePassword(currentPassword: String, newPassword: String) {
        val response = api.changePassword(currentUserId(), ChangePasswordRequest(currentPassword, newPassword))
        check(response.isSuccessful) { "Unable to update password." }
    }

    private fun UserDto.toStudent() = Student(
        id = idUtilisateur,
        firstName = prenom,
        lastName = nom,
        email = email,
        studentNumber = numeroEtudiant,
        promotion = promotion,
        cin = cin,
        telephone = telephone,
        adresse = adresse
    )
}
