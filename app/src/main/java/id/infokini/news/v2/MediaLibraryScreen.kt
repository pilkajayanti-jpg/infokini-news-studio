package id.infokini.news.v2

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@Composable
internal fun MediaLibraryScreen() {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("infokini_media_library_v2", 0) }
    val media = remember(context) {
        mutableStateListOf<String>().apply {
            addAll(prefs.getStringSet("uris", emptySet()).orEmpty().sorted())
        }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { selected ->
        selected.forEach { uri ->
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) { }
            if (!media.contains(uri.toString())) media.add(uri.toString())
        }
        prefs.edit().putStringSet("uris", media.toSet()).apply()
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("GALERI MEDIA INFOKINI", color = Color(0xFF27C5E8), fontWeight = FontWeight.Black, fontSize = 20.sp)
        Text("Impor foto, video, atau audio dari perangkat. Izin baca dokumen disimpan agar media tetap dapat dibuka pada sesi berikutnya.", color = Color(0xFF9FB3C9), fontSize = 12.sp)
        Button(onClick = { picker.launch(arrayOf("image/*", "video/*", "audio/*")) }, modifier = Modifier.fillMaxWidth()) {
            Text("＋ Tambahkan media dari perangkat")
        }
        if (media.isEmpty()) {
            Text("Belum ada media. Tekan tombol di atas untuk mengimpor file.", color = Color(0xFF9FB3C9))
        } else {
            Text("MEDIA TERSIMPAN • ${media.size}", color = Color(0xFF27C5E8), fontWeight = FontWeight.Bold, fontSize = 11.sp)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(media.toList(), key = { it }) { value ->
                    val uri = android.net.Uri.parse(value)
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF10243D))) {
                        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(uri.lastPathSegment?.substringAfterLast('/') ?: "Media perangkat", color = Color.White, fontWeight = FontWeight.Bold)
                            Text(value, color = Color(0xFF9FB3C9), fontSize = 9.sp, maxLines = 2)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = {
                                    val mime = context.contentResolver.getType(uri) ?: "*/*"
                                    val intent = Intent(Intent.ACTION_VIEW).setDataAndType(uri, mime)
                                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    try { context.startActivity(intent) } catch (_: Exception) { }
                                }) { Text("Buka") }
                                OutlinedButton(onClick = {
                                    media.remove(value)
                                    prefs.edit().putStringSet("uris", media.toSet()).apply()
                                    try { context.contentResolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) { }
                                }) { Text("Hapus dari galeri") }
                            }
                        }
                    }
                }
            }
        }
    }
}
