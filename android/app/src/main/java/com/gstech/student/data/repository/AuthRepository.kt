package com.gstech.student.data.repository

import com.gstech.student.data.local.TokenManager
import com.gstech.student.data.remote.AuthApi
import com.gstech.student.data.remote.dto.LoginRequest
import com.gstech.student.data.remote.dto.LogoutRequest
import com.gstech.student.model.Role
import com.gstech.student.util.Jwt

class AuthRepository(
    private val api: AuthApi,
    private val tokenManager: TokenManager,
) {
    suspend fun login(email: String, password: String): Role {
        val tokens = api.login(LoginRequest(email.trim(), password))
        // Replace the previous account session completely so a new login
        // can never inherit the previous user's role/navigation.
        tokenManager.clear()
        val userId = Jwt.subject(tokens.accessToken)
            ?: error("Login succeeded but the token had no subject claim.")
        val role = Role.fromClaim(Jwt.role(tokens.accessToken))
            ?: error("Login succeeded but the token had no recognizable role claim.")
        tokenManager.saveSession(tokens.accessToken, tokens.refreshToken, userId, role.name)
        return role
    }

    suspend fun logout() {
        val refreshToken = tokenManager.refreshTokenNow()
        try {
            if (refreshToken != null) api.logout(LogoutRequest(refreshToken))
        } finally {
            tokenManager.clear()
        }
    }

    suspend fun isSignedIn(): Boolean = tokenManager.accessTokenNow() != null

    suspend fun currentRole(): Role? = Role.fromClaim(tokenManager.roleNow())
}

