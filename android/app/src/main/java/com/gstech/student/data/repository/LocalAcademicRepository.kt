package com.gstech.student.data.repository

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

class LocalAcademicRepository(context: Context) {
    private val prefs = context.getSharedPreferences("gstech_academic", Context.MODE_PRIVATE)
    init {
        val now = LocalDate.now()
        val cycle = if (now.monthValue >= 8) now.year else now.year - 1
        val previous = prefs.getInt("announcement_cycle", -1)
        if (previous != -1 && previous != cycle && now.monthValue >= 8) prefs.edit().remove("announcements").apply()
        prefs.edit().putInt("announcement_cycle", cycle).apply()
    }

    data class Grade(val studentId: String, val courseId: String, val values: List<Double>)
    data class LocalAnnouncement(val id: String, val authorId: String, val classId: String, val title: String, val body: String, val createdAt: Long)

    fun saveGrades(studentId: String, courseId: String, values: List<Double>) {
        val all = grades().filterNot { it.studentId == studentId && it.courseId == courseId }.toMutableList()
        all += Grade(studentId, courseId, values.take(3))
        val arr = JSONArray()
        all.forEach { g ->
            arr.put(JSONObject().apply {
                put("studentId", g.studentId); put("courseId", g.courseId)
                put("values", JSONArray(g.values))
            })
        }
        prefs.edit().putString("grades", arr.toString()).apply()
    }

    fun grades(studentId: String? = null, courseId: String? = null): List<Grade> {
        val raw = prefs.getString("grades", "[]") ?: "[]"
        val arr = JSONArray(raw)
        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val g = Grade(o.getString("studentId"), o.getString("courseId"), buildList {
                    val a = o.optJSONArray("values") ?: JSONArray()
                    for (j in 0 until a.length()) add(a.optDouble(j))
                })
                if ((studentId == null || g.studentId == studentId) && (courseId == null || g.courseId == courseId)) add(g)
            }
        }
    }

    fun addAnnouncement(authorId: String, classId: String, title: String, body: String) {
        val all = announcements().toMutableList()
        all += LocalAnnouncement(java.util.UUID.randomUUID().toString(), authorId, classId, title, body, System.currentTimeMillis())
        val arr = JSONArray()
        all.forEach { a -> arr.put(JSONObject().apply {
            put("id", a.id); put("authorId", a.authorId); put("classId", a.classId); put("title", a.title); put("body", a.body); put("createdAt", a.createdAt)
        }) }
        prefs.edit().putString("announcements", arr.toString()).apply()
    }

    fun announcements(authorId: String? = null, classId: String? = null): List<LocalAnnouncement> {
        val arr = JSONArray(prefs.getString("announcements", "[]") ?: "[]")
        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val a = LocalAnnouncement(o.getString("id"), o.getString("authorId"), o.getString("classId"), o.getString("title"), o.getString("body"), o.getLong("createdAt"))
                if ((authorId == null || a.authorId == authorId) && (classId == null || a.classId == classId)) add(a)
            }
        }.sortedByDescending { it.createdAt }
    }
}
