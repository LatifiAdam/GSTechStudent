package com.gstech.student.util

import android.util.Base64
import org.json.JSONObject

object Jwt {
    fun subject(token: String): String? = claim(token, "sub")
    fun role(token: String): String? = claim(token, "role")

    private fun claim(token: String, key: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            val payload = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            JSONObject(String(payload, Charsets.UTF_8)).let { json ->
                if (json.has(key) && !json.isNull(key)) json.getString(key) else null
            }
        } catch (e: Exception) {
            null
        }
    }
}
