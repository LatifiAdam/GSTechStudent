package com.gstech.student.data.repository

import com.gstech.student.data.local.TokenManager
import com.gstech.student.data.remote.UsersApi
import com.gstech.student.data.remote.dto.UserDto
import com.gstech.student.data.remote.dto.ChangePasswordRequest
import com.gstech.student.model.Student
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

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

    suspend fun uploadProfileImage(bytes: ByteArray, fileName: String, contentType: String) {
        val requestBody = bytes.toRequestBody(contentType.toMediaType())
        val part = MultipartBody.Part.createFormData("file", fileName, requestBody)
        api.uploadProfileImage(part)
    }

    suspend fun getProfileImageBitmap(): Bitmap? {
        val response = api.getMyProfileImage()
        if (!response.isSuccessful) return null
        val bytes = response.body()?.bytes() ?: return null
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
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
