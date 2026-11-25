package id.infokini.news

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Navy = Color(0xFF080D4A)
private val Cyan = Color(0xFF13BDEB)
private val Ink = Color(0xFF172033)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(8, 13, 74)
        setContent { InfoKiniStudio() }
    }
}

@Composable
fun InfoKiniStudio() {
    var headline by remember { mutableStateOf("") }
    var ticker by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedMedia by remember { mutableStateOf<Uri?>(null) }
    var mediaLabel by remember { mutableStateOf("Belum ada foto atau video dipilih") }
    var live by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            selectedMedia = uri
            mediaLabel = uri.lastPathSegment ?: "Media dipilih"
        }
    }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Cyan, secondary = Cyan, background = Color(0xFF080D4A),
        surface = Color(0xFF111A35), onSurface = Color.White
    )) {
        Column(
            Modifier.fillMaxSize().background(Navy).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("InfoKini", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    Text("NEWS STUDIO  •  infokini.news", color = Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Surface(color = if (live) Color.Red else Color(0xFF26334F), shape = RoundedCornerShape(20.dp)) {
                    Text(if (live) "● LIVE" else "STUDIO", Modifier.padding(horizontal = 14.dp, vertical = 8.dp), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Text("PRATINJAU SIARAN", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Column(
                Modifier.fillMaxWidth().height(220.dp).background(Color(0xFF101827), RoundedCornerShape(12.dp))
                    .border(1.dp, Cyan.copy(alpha = .55f), RoundedCornerShape(12.dp)).padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text("infokini.news", color = Color.White, fontWeight = FontWeight.Black,
                        modifier = Modifier.background(Navy.copy(alpha = .85f), RoundedCornerShape(4.dp)).padding(7.dp))
                }
                Spacer(Modifier.weight(1f))
                if (headline.isNotBlank()) {
                    Text(headline, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp,
                        modifier = Modifier.fillMaxWidth().background(Navy.copy(alpha = .92f)).padding(10.dp))
                }
                Row(Modifier.fillMaxWidth().background(Cyan).padding(7.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("INFO KINI", color = Navy, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(ticker.ifBlank { " " }, color = Navy, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                }
            }

            Text("EDITOR BERITA", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            StudioField("Judul berita", headline, { headline = it }, "Tulis judul berita...")
            StudioField("Teks berjalan", ticker, { ticker = it }, "Tulis running text...")
            StudioField("Lokasi berita", location, { location = it }, "Contoh: Cigombong, Kabupaten Bogor")
            OutlinedButton(
                onClick = { picker.launch(arrayOf("image/*", "video/*")) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Cyan)
            ) { Text("PILIH FOTO / VIDEO DARI HP") }
            Text(mediaLabel, color = Color.LightGray, fontSize = 12.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tampilkan label LIVE", color = Color.White, modifier = Modifier.weight(1f))
                Switch(checked = live, onCheckedChange = { live = it })
            }
            Button(
                onClick = { /* preview updates live from editor fields */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Navy)
            ) { Text("PERBARUI PRATINJAU", fontWeight = FontWeight.Black) }

            Text("Catatan versi 2.0: editor dasar dan pratinjau grafis sudah disiapkan. Ekspor MP4, penyuntingan video lanjutan, AI redaktur, TTS, dan siaran langsung masih memerlukan modul serta integrasi berikutnya.", color = Color(0xFFB8C4D9), fontSize = 11.sp)
        }
    }
}

@Composable
private fun StudioField(label: String, value: String, onValueChange: (String) -> Unit, hint: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)).padding(12.dp),
            textStyle = androidx.compose.ui.text.TextStyle(color = Ink, fontSize = 14.sp),
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) Text(hint, color = Color.Gray, fontSize = 14.sp)
                    inner()
                }
            }
        )
    }
}
