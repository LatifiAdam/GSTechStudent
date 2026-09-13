package com.gstech.student.util

import android.util.Log
import com.gstech.student.data.local.TokenManager
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.util.Locale

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

object SessionManager {
    private var tokenManager: TokenManager? = null
    var onSessionExpired: (() -> Unit)? = null

    fun initialize(tokenManager: TokenManager) {
        this.tokenManager = tokenManager
    }

    suspend fun handleSessionExpired() {
        tokenManager?.clear()
        onSessionExpired?.invoke()
    }
}

fun userFriendlyErrorMessage(error: Throwable): String {
    if (error is HttpException) {
        val serverMessage = extractServerMessage(error)
        if (!serverMessage.isNullOrBlank()) return translateValidationMessage(serverMessage)

        return when (error.code()) {
            400 -> "Les informations saisies sont invalides. Vérifiez les champs indiqués."
            401 -> "Votre session a expiré. Veuillez vous reconnecter."
            403 -> "Vous n’êtes pas autorisé à effectuer cette action."
            404 -> "La ressource demandée est introuvable."
            409 -> "Cette information existe déjà. Vérifiez les valeurs saisies."
            422 -> "Certaines informations saisies sont invalides."
            429 -> "Trop de tentatives. Veuillez patienter avant de réessayer."
            in 500..599 -> "Le serveur rencontre un problème. Veuillez réessayer plus tard."
            else -> "Une erreur est survenue. Veuillez réessayer."
        }
    }

    if (error is IOException) {
        return "Impossible de contacter le serveur. Vérifiez la connexion au réseau local."
    }

    val raw = error.message?.trim().orEmpty()
    if (raw.isBlank()) return "Une erreur inattendue est survenue."

    return translateValidationMessage(raw)
}

private fun extractServerMessage(error: HttpException): String? {
    return try {
        val raw = error.response()?.errorBody()?.string()?.trim() ?: return null
        if (raw.isBlank()) return null
        val json = JSONObject(raw)
        val value = json.opt("message")
        when (value) {
            is JSONArray -> buildString {
                for (i in 0 until value.length()) {
                    val item = value.optString(i).trim()
                    if (item.isNotBlank()) {
                        if (isNotEmpty()) append(" ")
                        append(item)
                    }
                }
            }.ifBlank { null }
            is String -> value
            else -> null
        }
    } catch (e: Exception) {
        Log.d("GSTechError", "Unable to parse server error response", e)
        null
    }
}

private fun translateValidationMessage(message: String): String {
    var text = message.trim()
    val lower = text.lowercase(Locale.getDefault())

    text = when {
        lower.contains("telephone") && (lower.contains("match") || lower.contains("number") || lower.contains("digits")) ->
            "Le téléphone doit contenir uniquement des chiffres."
        lower.contains("email") && (lower.contains("valid") || lower.contains("email")) ->
            "L’adresse e-mail est invalide."
        lower.contains("password") && (lower.contains("min") || lower.contains("length")) ->
            "Le mot de passe est trop court."
        lower.contains("nom") && lower.contains("required") ->
            "Le nom est obligatoire."
        lower.contains("prenom") && lower.contains("required") ->
            "Le prénom est obligatoire."
        lower.contains("numerostagiaire") && lower.contains("required") ->
            "Le numéro de stagiaire est obligatoire."
        lower.contains("promotion") && lower.contains("required") ->
            "La promotion est obligatoire."
        lower.contains("must be a string") ->
            "La valeur saisie n’est pas valide."
        lower.contains("must be a number") ->
            "La valeur saisie doit être un nombre."
        else -> text
    }

    if (text.equals("Bad Request", ignoreCase = true)) {
        return "Les informations saisies sont invalides. Vérifiez les champs."
    }
    if (text.equals("Forbidden", ignoreCase = true)) {
        return "Vous n’êtes pas autorisé à effectuer cette action."
    }
    return text
}

suspend fun <T> safeCall(block: suspend () -> T): UiState<T> {
    return try {
        UiState.Success(block())
    } catch (e: HttpException) {
        if (e.code() == 401) SessionManager.handleSessionExpired()
        UiState.Error(userFriendlyErrorMessage(e))
    } catch (e: IOException) {
        UiState.Error(userFriendlyErrorMessage(e))
    } catch (e: Exception) {
        UiState.Error(userFriendlyErrorMessage(e))
    }
}
