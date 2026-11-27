package id.infokini.news.v2

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

internal data class StudioProject(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val updatedAt: Long
)

internal class StudioProjectRepository(context: Context) {
    private val prefs = context.getSharedPreferences("infokini_projects_v2", Context.MODE_PRIVATE)

    fun load(): List<StudioProject> {
        val raw = prefs.getString("projects", "[]") ?: "[]"
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                val item = array.optJSONObject(index) ?: return@mapNotNull null
                StudioProject(
                    id = item.optString("id"),
                    title = item.optString("title"),
                    description = item.optString("description"),
                    category = item.optString("category", "Berita"),
                    updatedAt = item.optLong("updatedAt")
                ).takeIf { it.id.isNotBlank() && it.title.isNotBlank() }
            }.sortedByDescending { it.updatedAt }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(project: StudioProject): StudioProject {
        val saved = project.copy(
            id = project.id.ifBlank { UUID.randomUUID().toString() },
            title = project.title.trim(),
            description = project.description.trim(),
            updatedAt = System.currentTimeMillis()
        )
        val items = load().filterNot { it.id == saved.id } + saved
        persist(items)
        return saved
    }

    fun delete(id: String) {
        persist(load().filterNot { it.id == id })
    }

    private fun persist(items: List<StudioProject>) {
        val array = JSONArray()
        items.forEach { project ->
            array.put(JSONObject().apply {
                put("id", project.id)
                put("title", project.title)
                put("description", project.description)
                put("category", project.category)
                put("updatedAt", project.updatedAt)
            })
        }
        prefs.edit().putString("projects", array.toString()).apply()
    }
}
