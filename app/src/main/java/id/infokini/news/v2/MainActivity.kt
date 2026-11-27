package id.infokini.news.v2

import android.os.Bundle
import android.speech.tts.TextToSpeech
import java.util.Locale
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Navy = Color(0xFF071426)
private val Panel = Color(0xFF10243D)
private val Cyan = Color(0xFF27C5E8)
private val Red = Color(0xFFE53945)
private val SoftWhite = Color(0xFFEAF2FA)
private val Muted = Color(0xFF9FB3C9)
private val Gold = Color(0xFFFFC857)

private data class StudioModule(val title: String, val subtitle: String, val status: String)

private val modules = listOf(
    StudioModule("Live Studio", "Studio siaran dan grafis televisi", "Tahap fondasi"),
    StudioModule("Video Studio", "Editor klip, teks, audio dan ekspor MP4", "Tahap fondasi"),
    StudioModule("Photo Studio", "Poster, thumbnail dan grafis berita", "Tahap fondasi"),
    StudioModule("AI Redaktur", "Riset sumber dan penyusunan draf berita", "Tahap fondasi"),
    StudioModule("Template Berita", "Paket grafis khas InfoKini", "Tahap fondasi"),
    StudioModule("Narasi / TTS", "Naskah menjadi suara Bahasa Indonesia", "Tahap fondasi"),
    StudioModule("Grafis Olahraga", "Papan skor dan grafis pertandingan", "Tahap fondasi"),
    StudioModule("Promosi UMKM", "Poster dan video promosi produk lokal", "Tahap fondasi"),
    StudioModule("Proyek Saya", "Simpan dan lanjutkan pekerjaan", "Tahap fondasi"),
    StudioModule("Galeri Media", "Kelola foto, video dan audio", "Tahap fondasi"),
    StudioModule("Pengaturan", "Identitas kanal, privasi dan kualitas", "Tahap fondasi"),
    StudioModule("Panduan & Diagnostik", "Bantuan dan pemeriksaan perangkat", "Tahap fondasi")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(7, 20, 38)
        window.navigationBarColor = android.graphics.Color.rgb(7, 20, 38)
        setContent { InfoKiniStudioV2() }
    }
}

@Composable
private fun InfoKiniStudioV2() {
    var selected by remember { mutableStateOf("Beranda") }
    var headline by remember { mutableStateOf("") }
    var ticker by remember { mutableStateOf("INFO KINI | BERPIKIR TAJAM, MENGUNGKAP KEBENARAN") }
    val navigation = listOf("Beranda", "Live", "Video", "Foto", "Redaktur", "Lainnya")

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Cyan,
            secondary = Red,
            background = Navy,
            surface = Panel,
            onPrimary = Navy,
            onBackground = SoftWhite,
            onSurface = SoftWhite
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Navy) {
            Column(modifier = Modifier.fillMaxSize()) {
                Header()
                Box(modifier = Modifier.weight(1f)) {
                    when (selected) {
                        "Beranda" -> HomeScreen(onOpen = { selected = it })
                        "Live" -> LiveScreen(headline, { headline = it }, ticker, { ticker = it })
                        "Video" -> SimpleScreen("Video Studio", "Ruang kerja editor video InfoKini. Impor klip, susun timeline, tambahkan grafis dan siapkan ekspor MP4.")
                        "Foto" -> SimpleScreen("Photo Studio", "Ruang desain poster, thumbnail, foto berita, logo dan watermark.")
                        "Redaktur" -> EditorialScreen()
                        "Narasi / TTS" -> TtsScreen()
                        else -> MoreScreen(onOpen = { selected = it })
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().background(Panel).padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    navigation.forEach { item ->
                        val label = when (item) {
                            "Lainnya" -> "Menu"
                            else -> item
                        }
                        if (selected == item) {
                            Button(onClick = { selected = item }, shape = RoundedCornerShape(12.dp)) {
                                Text(label, fontSize = 11.sp)
                            }
                        } else {
                            OutlinedButton(onClick = { selected = item }, shape = RoundedCornerShape(12.dp)) {
                                Text(label, fontSize = 11.sp, color = Muted)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Header() {
    Column(
        modifier = Modifier.fillMaxWidth().background(Panel).padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.background(Red, RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 8.dp)
            ) { Text("IK", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp) }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("INFOKINI", color = Color.White, fontWeight = FontWeight.Black, fontSize = 23.sp, letterSpacing = 1.sp)
                Text("NEWS STUDIO", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 2.sp)
            }
            Text("V2 • BETA", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))
        Text("BERPIKIR TAJAM, MENGUNGKAP KEBENARAN", color = Muted, fontSize = 10.sp)
    }
}

@Composable
private fun HomeScreen(onOpen: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF142E4B)), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(18.dp)) {
                    Text("NEWSROOM CONTROL", color = Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Selamat datang di studio produksi berita.", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Produksi, sunting, siapkan grafis, dan kelola berita InfoKini dari satu aplikasi.", color = Muted, fontSize = 13.sp)
                    Spacer(Modifier.height(14.dp))
                    Button(onClick = { onOpen("Live") }) { Text("Buka Live Studio") }
                }
            }
        }
        item { Text("MODUL PRODUKSI", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp) }
        items(modules.take(8)) { module ->
            ModuleCard(module) {
                val target = when (module.title) {
                    "Live Studio" -> "Live"
                    "Video Studio" -> "Video"
                    "Photo Studio" -> "Foto"
                    "AI Redaktur" -> "Redaktur"
                    else -> "Lainnya"
                }
                onOpen(target)
            }
        }
        item {
            Text("Catatan: modul ditampilkan sebagai kerangka V2. Fitur produksi akan diaktifkan dan diuji bertahap; belum semua fungsi tersedia pada build fondasi.", color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun ModuleCard(module: StudioModule, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(4.dp).height(42.dp).background(Cyan, RoundedCornerShape(4.dp)))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(module.title, color = SoftWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(module.subtitle, color = Muted, fontSize = 11.sp)
            }
            Text("›", color = Cyan, fontSize = 25.sp)
        }
    }
}

@Composable
private fun LiveScreen(
    headline: String,
    onHeadline: (String) -> Unit,
    ticker: String,
    onTicker: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("LIVE STUDIO", color = Cyan, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF02070D)), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("IK", color = Color.White, modifier = Modifier.background(Red, RoundedCornerShape(5.dp)).padding(7.dp), fontWeight = FontWeight.Black)
                    Spacer(Modifier.width(8.dp))
                    Text("INFO KINI NEWS", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text("PREVIEW", color = Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(48.dp))
                Text(if (headline.isBlank()) "JUDUL BERITA ANDA" else headline.uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text(ticker, color = Color.White, fontSize = 10.sp, modifier = Modifier.fillMaxWidth().background(Red).padding(8.dp))
            }
        }
        OutlinedTextField(value = headline, onValueChange = onHeadline, modifier = Modifier.fillMaxWidth(), label = { Text("Judul berita") }, singleLine = true)
        OutlinedTextField(value = ticker, onValueChange = onTicker, modifier = Modifier.fillMaxWidth(), label = { Text("Running text") })
        Text("Pratinjau grafis ini belum mengirim siaran. Koneksi YouTube/RTMP, kamera dan encoder akan menjadi tahap implementasi Live Studio berikutnya.", color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun EditorialScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("infokini_editorial_v2", 0) }
    var topic by remember { mutableStateOf(prefs.getString("topic", "").orEmpty()) }
    var draft by remember { mutableStateOf(prefs.getString("draft", "").orEmpty()) }
    var sourceUrl by remember { mutableStateOf(prefs.getString("source_url", "").orEmpty()) }
    var status by remember { mutableStateOf(prefs.getString("status", "DRAF — BELUM DISETUJUI").orEmpty()) }
    val sourceValid = sourceUrl.startsWith("https://") || sourceUrl.startsWith("http://")
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("AI REDAKTUR", color = Cyan, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Text("Ruang kerja redaksi. Catatan dan sumber disimpan lokal. AI belum tersambung pada build ini; jangan anggap draf sebagai berita terverifikasi.", color = Muted, fontSize = 12.sp)
        OutlinedTextField(value = topic, onValueChange = { topic = it; status = "DRAF — BELUM DISETUJUI" }, modifier = Modifier.fillMaxWidth(), label = { Text("Topik / isu berita") })
        OutlinedTextField(value = sourceUrl, onValueChange = { sourceUrl = it; status = "DRAF — BELUM DISETUJUI" }, modifier = Modifier.fillMaxWidth(), label = { Text("URL sumber asli (http/https)") })
        OutlinedTextField(value = draft, onValueChange = { draft = it; status = "DRAF — BELUM DISETUJUI" }, modifier = Modifier.fillMaxWidth().height(190.dp), label = { Text("Catatan fakta dan draf redaksi") })
        Text("Status: $status", color = if (status.startsWith("DISETUJUI")) Cyan else Gold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Button(onClick = {
            status = "DRAF — BELUM DISETUJUI"
            prefs.edit().putString("topic", topic).putString("draft", draft)
                .putString("source_url", sourceUrl).putString("status", status).apply()
        }, modifier = Modifier.fillMaxWidth()) { Text("Simpan draf") }
        OutlinedButton(onClick = {
            if (topic.isNotBlank() && draft.isNotBlank() && sourceValid) {
                status = "DISETUJUI REDAKTUR — ${System.currentTimeMillis()}"
                prefs.edit().putString("topic", topic).putString("draft", draft)
                    .putString("source_url", sourceUrl).putString("status", status).apply()
            } else {
                status = "BELUM DISETUJUI — lengkapi topik, draf, dan URL sumber"
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Saya menyetujui draf ini") }
        Text("Persetujuan hanya mencatat keputusan pengguna di perangkat. Ini bukan verifikasi fakta, bukan publikasi, dan bukan integrasi AI. Periksa sumber asli serta konfirmasi independen sebelum menayangkan berita.", color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun TtsScreen() {
    val context = LocalContext.current
    var script by remember { mutableStateOf("") }
    var engine by remember { mutableStateOf<TextToSpeech?>(null) }
    var status by remember { mutableStateOf("Menyiapkan mesin suara perangkat…") }

    DisposableEffect(context) {
        val tts = TextToSpeech(context) { result ->
            if (result == TextToSpeech.SUCCESS) {
                val languageResult = ttsLanguageSetup(engine = null)
                status = if (languageResult) "Mesin TTS siap" else "Bahasa Indonesia belum tersedia; periksa paket suara di pengaturan Android"
            } else {
                status = "Mesin TTS tidak dapat diinisialisasi"
            }
        }
        engine = tts
        onDispose {
            tts.stop()
            tts.shutdown()
            engine = null
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("NARASI / TEXT TO SPEECH", color = Cyan, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Text("Ubah naskah menjadi suara menggunakan mesin TTS yang tersedia di perangkat Android.", color = Muted, fontSize = 12.sp)
        OutlinedTextField(value = script, onValueChange = { script = it }, modifier = Modifier.fillMaxWidth().weight(1f), label = { Text("Tempel naskah berita di sini") })
        Text(status, color = Muted, fontSize = 11.sp)
        Button(onClick = {
            val tts = engine
            if (tts != null && script.isNotBlank()) {
                val result = tts.setLanguage(Locale("id", "ID"))
                if (result >= TextToSpeech.LANG_AVAILABLE) {
                    tts.speak(script, TextToSpeech.QUEUE_FLUSH, null, "infokini-narration")
                    status = "Sedang membacakan naskah"
                } else {
                    status = "Paket suara Bahasa Indonesia belum tersedia di perangkat"
                }
            } else {
                status = if (script.isBlank()) "Masukkan naskah terlebih dahulu" else "Mesin suara belum siap"
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Bacakan naskah") }
        OutlinedButton(onClick = { engine?.stop(); status = "Pembacaan dihentikan" }, modifier = Modifier.fillMaxWidth()) { Text("Hentikan suara") }
        Text("Catatan: fitur ini memutar suara melalui TTS Android. Ekspor audio menjadi MP3/WAV belum tersedia.", color = Muted, fontSize = 11.sp)
    }
}

private fun ttsLanguageSetup(engine: TextToSpeech?): Boolean {
    if (engine == null) return false
    return engine.setLanguage(Locale("id", "ID")) >= TextToSpeech.LANG_AVAILABLE
}

@Composable
private fun MoreScreen(onOpen: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(modules) { module -> ModuleCard(module) { onOpen(module.title) } }
        item { Text("Pilih modul untuk membuka ruang kerjanya.", color = Muted, fontSize = 11.sp) }
    }
}

@Composable
private fun SimpleScreen(title: String, description: String) {
    Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title.uppercase(), color = Cyan, fontSize = 20.sp, fontWeight = FontWeight.Black)
        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(18.dp)) {
                Text(description, color = SoftWhite, fontSize = 14.sp)
                Spacer(Modifier.height(12.dp))
                Text("Status: fondasi antarmuka. Fungsi penuh akan masuk melalui milestone teruji.", color = Muted, fontSize = 11.sp)
            }
        }
    }
}
