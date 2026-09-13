package com.gstech.student.data.repository

import com.gstech.student.data.remote.NotificationsApi
import com.gstech.student.data.remote.dto.NotificationDto
import com.gstech.student.model.AppNotification
import com.gstech.student.model.NotificationCategory

class NotificationsRepository(private val api: NotificationsApi) {

    suspend fun getNotifications(): List<AppNotification> =
        api.getNotifications().map { it.toModel() }

    suspend fun markRead(id: String) {
        api.markRead(id)
    }

    private fun NotificationDto.toModel(): AppNotification {
        val category = when {
            type.contains("urgent", ignoreCase = true) -> NotificationCategory.URGENT
            type.contains("absence", ignoreCase = true) || type.contains("justification", ignoreCase = true) ->
                NotificationCategory.ACADEMIC
            type.contains("document", ignoreCase = true) || type.contains("admin", ignoreCase = true) ->
                NotificationCategory.ADMIN
            else -> NotificationCategory.OTHER
        }
        val title = type.replace('_', ' ').replaceFirstChar { it.uppercase() }
        return AppNotification(
            id = idNotification,
            category = category,
            title = title,
            body = message,
            read = lue,
            dateTimeIso = dateEnvoi,
        )
    }
}
