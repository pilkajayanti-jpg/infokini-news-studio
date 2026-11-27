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

    fun load(): List<StudioProject> = decode(prefs.getString("projects", "[]") ?: "[]")

    fun save(project: StudioProject): StudioProject {
        require(project.title.isNotBlank()) { "Nama proyek wajib diisi." }
        val saved = project.copy(
            id = project.id.ifBlank { UUID.randomUUID().toString() },
            title = project.title.trim().take(120),
            description = project.description.trim().take(12000),
            category = project.category.trim().ifBlank { "Berita" }.take(60),
            updatedAt = System.currentTimeMillis()
        )
        persist(load().filterNot { it.id == saved.id } + saved)
        return saved
    }

    fun duplicate(id: String): StudioProject? {
        val original = load().firstOrNull { it.id == id } ?: return null
        return save(original.copy(id = "", title = "${original.title} (Salinan)"))
    }

    fun delete(id: String) {
        persist(load().filterNot { it.id == id })
    }

    fun exportJson(): String = encode(load()).toString(2)

    fun importJson(raw: String): Int {
        require(raw.length <= 1_000_000) { "File cadangan terlalu besar (maksimal 1 MB)." }
        val imported = decode(raw)
        require(imported.isNotEmpty()) { "File tidak berisi proyek InfoKini yang valid." }
        val merged = (load() + imported).associateBy { it.id }.values
            .sortedByDescending { it.updatedAt }
            .take(500)
        persist(merged)
        return imported.size
    }

    private fun persist(items: List<StudioProject>) {
        prefs.edit().putString("projects", encode(items).toString()).apply()
    }

    private fun encode(items: List<StudioProject>): JSONArray = JSONArray().apply {
        items.forEach { project ->
            put(JSONObject().apply {
                put("id", project.id)
                put("title", project.title)
                put("description", project.description)
                put("category", project.category)
                put("updatedAt", project.updatedAt)
            })
        }
    }

    private fun decode(raw: String): List<StudioProject> = try {
        val array = JSONArray(raw)
        (0 until minOf(array.length(), 500)).mapNotNull { index ->
            val item = array.optJSONObject(index) ?: return@mapNotNull null
            val title = item.optString("title").trim()
            val id = item.optString("id").trim()
            if (title.isBlank() || id.isBlank()) return@mapNotNull null
            StudioProject(
                id = id,
                title = title.take(120),
                description = item.optString("description").take(12000),
                category = item.optString("category", "Berita").take(60),
                updatedAt = item.optLong("updatedAt", 0L)
            )
        }.sortedByDescending { it.updatedAt }
    } catch (_: Exception) {
        emptyList()
    }
}
