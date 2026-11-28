package id.infokini.news.v2

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

internal data class SavedVideoClip(
    val uri: String,
    val startSeconds: String,
    val endSeconds: String,
    val durationMs: Long
)

internal object VideoDraftStore {
    private const val PREFS = "infokini_video_draft_v2"
    private const val KEY_CLIPS = "clips"

    fun load(context: Context): List<SavedVideoClip> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_CLIPS, "[]") ?: "[]"
        return try {
            val array = JSONArray(raw)
            (0 until minOf(array.length(), 100)).mapNotNull { index ->
                val item = array.optJSONObject(index) ?: return@mapNotNull null
                val uri = item.optString("uri").trim()
                if (uri.isBlank()) return@mapNotNull null
                SavedVideoClip(
                    uri = uri,
                    startSeconds = item.optString("startSeconds", "0").filter(Char::isDigit).ifBlank { "0" },
                    endSeconds = item.optString("endSeconds", "0").filter(Char::isDigit).ifBlank { "0" },
                    durationMs = item.optLong("durationMs", 0L).coerceAtLeast(0L)
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, clips: List<SavedVideoClip>) {
        val array = JSONArray()
        clips.take(100).forEach { clip ->
            array.put(JSONObject().apply {
                put("uri", clip.uri)
                put("startSeconds", clip.startSeconds)
                put("endSeconds", clip.endSeconds)
                put("durationMs", clip.durationMs)
            })
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CLIPS, array.toString())
            .apply()
    }
}
