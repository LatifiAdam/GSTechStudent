package com.gstech.student.data.local

import android.content.Context
import android.util.Base64
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.charset.StandardCharsets
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties

class TokenManager(context: Context) {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    init {
        File(appContext.filesDir, LEGACY_DATASTORE_FILE).delete()
    }

    private val accessTokenState = MutableStateFlow(read(ACCESS_TOKEN))
    private val refreshTokenState = MutableStateFlow(read(REFRESH_TOKEN))
    private val userIdState = MutableStateFlow(read(USER_ID))
    private val roleState = MutableStateFlow(read(ROLE))

    val accessTokenFlow: Flow<String?> = accessTokenState.asStateFlow()
    val userIdFlow: Flow<String?> = userIdState.asStateFlow()
    val roleFlow: Flow<String?> = roleState.asStateFlow()

    suspend fun accessTokenNow(): String? = accessTokenState.value
    suspend fun refreshTokenNow(): String? = refreshTokenState.value
    suspend fun userIdNow(): String? = userIdState.value
    suspend fun roleNow(): String? = roleState.value

    suspend fun saveSession(accessToken: String, refreshToken: String?, userId: String, role: String?) {
        write(ACCESS_TOKEN, accessToken)
        if (refreshToken != null) write(REFRESH_TOKEN, refreshToken) else remove(REFRESH_TOKEN)
        write(USER_ID, userId)
        if (role != null) write(ROLE, role) else remove(ROLE)
        accessTokenState.value = accessToken
        refreshTokenState.value = refreshToken
        userIdState.value = userId
        roleState.value = role
    }

    suspend fun saveAccessToken(accessToken: String) {
        write(ACCESS_TOKEN, accessToken)
        accessTokenState.value = accessToken
    }

    suspend fun clear() {
        preferences.edit().clear().apply()
        accessTokenState.value = null
        refreshTokenState.value = null
        userIdState.value = null
        roleState.value = null
    }

    private fun read(key: String): String? {
        val value = preferences.getString(key, null) ?: return null
        return try {
            decrypt(value)
        } catch (_: Exception) {
            null
        }
    }

    private fun write(key: String, value: String) {
        preferences.edit().putString(key, encrypt(value)).apply()
    }

    private fun remove(key: String) {
        preferences.edit().remove(key).apply()
    }

    private fun getKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        val existing = keyStore.getKey(KEY_ALIAS, null)
        if (existing is SecretKey) return existing

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build(),
        )
        return generator.generateKey()
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getKey())
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(iv + ciphertext, Base64.NO_WRAP)
    }

    private fun decrypt(value: String): String {
        val data = Base64.decode(value, Base64.NO_WRAP)
        require(data.size > GCM_IV_LENGTH)
        val iv = data.copyOfRange(0, GCM_IV_LENGTH)
        val ciphertext = data.copyOfRange(GCM_IV_LENGTH, data.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getKey(), GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8)
    }

    private companion object {
        const val PREFERENCES_NAME = "gstech_secure_session"
        const val LEGACY_DATASTORE_FILE = "datastore/gstech_session.preferences_pb"
        const val ACCESS_TOKEN = "access_token"
        const val REFRESH_TOKEN = "refresh_token"
        const val USER_ID = "user_id"
        const val ROLE = "role"
        const val KEY_ALIAS = "gstech_session_key"
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_IV_LENGTH = 12
        const val GCM_TAG_LENGTH = 128
    }
}
