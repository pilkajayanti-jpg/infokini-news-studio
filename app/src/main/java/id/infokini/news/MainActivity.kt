package id.infokini.news

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import java.util.Locale
import java.net.URL
import java.net.URLEncoder
import android.util.Xml
import org.xmlpull.v1.XmlPullParser

private val Navy = Color(0xFF07142E)
private val Panel = Color(0xFF112544)
private val Accent = Color(0xFF16C5E8)
private val Muted = Color(0xFFB9C9DE)
private val Gold = Color(0xFFFFC857)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(7, 20, 46)
        window.navigationBarColor = android.graphics.Color.rgb(7, 20, 46)
        setContent { InfoKiniApp() }
    }
}

private enum class Page { HOME, VIDEO, PHOTO, TEMPLATE, EDITORIAL, PRESENTER, SPORTS, LIVE }

@Composable
private fun InfoKiniApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("infokini_v3", Context.MODE_PRIVATE) }
    var page by remember { mutableStateOf(Page.HOME) }
    var headline by remember { mutableStateOf(prefs.getString("headline", "").orEmpty()) }
    var ticker by remember { mutableStateOf(prefs.getString("ticker", "").orEmpty()) }
    var script by remember { mutableStateOf(prefs.getString("script", "").orEmpty()) }
    var topic by remember { mutableStateOf(prefs.getString("topic", "").orEmpty()) }
    var sourceA by remember { mutableStateOf(prefs.getString("source_a", "").orEmpty()) }
    var sourceB by remember { mutableStateOf(prefs.getString("source_b", "").orEmpty()) }
    var template by remember { mutableStateOf(prefs.getString("template", "InfoKini Utama").orEmpty()) }
    var video by remember { mutableStateOf(prefs.getString("video", null)?.let(Uri::parse)) }
    var photo by remember { mutableStateOf(prefs.getString("photo", null)?.let(Uri::parse)) }
    var music by remember { mutableStateOf<Uri?>(null) }
    var results by remember { mutableStateOf<List<String>>(emptyList()) }
    var searchStatus by remember { mutableStateOf("Cari judul berita dari Google News RSS. Baca sumber asli sebelum digunakan.") }
    var presenter by remember { mutableStateOf("Presenter perempuan") }
    var ttsStatus by remember { mutableStateOf("Gunakan mesin Text-to-Speech yang tersedia di Android.") }
    var homeTeam by remember { mutableStateOf("Tuan Rumah") }
    var awayTeam by remember { mutableStateOf("Tim Tamu") }
    var homeScore by remember { mutableIntStateOf(0) }
    var awayScore by remember { mutableIntStateOf(0) }
    val tts = remember { TextToSpeech(context) { status ->
        ttsStatus = if (status == TextToSpeech.SUCCESS) "Mesin TTS siap. Suara Indonesia bergantung pada paket suara perangkat." else "Mesin TTS tidak tersedia."
    } }
    DisposableEffect(tts) { onDispose { tts.stop(); tts.shutdown() } }
    LaunchedEffect(headline, ticker, script, topic, sourceA, sourceB, template, video, photo) {
        prefs.edit().putString("headline", headline).putString("ticker", ticker)
            .putString("script", script).putString("topic", topic).putString("source_a", sourceA)
            .putString("source_b", sourceB).putString("template", template)
            .putString("video", video?.toString()).putString("photo", photo?.toString()).apply()
    }
    val pickVideo = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { runCatching { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }; video = it }
    }
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { runCatching { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }; photo = it }
    }
    val pickMusic = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { runCatching { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }; music = it }
    }

    MaterialTheme(colorScheme = darkColorScheme(primary = Accent, background = Navy, surface = Panel)) {
        Scaffold(containerColor = Navy, bottomBar = {
            NavigationBar(containerColor = Panel) {
                listOf(Page.HOME, Page.VIDEO, Page.PHOTO, Page.EDITORIAL).forEach { item ->
                    NavigationBarItem(selected = page == item, onClick = { page = item },
                        icon = { Text(when (item) { Page.HOME -> "⌂"; Page.VIDEO -> "▶"; Page.PHOTO -> "▧"; else -> "✎" }, fontSize = 18.sp) },
                        label = { Text(when (item) { Page.HOME -> "Home"; Page.VIDEO -> "Video"; Page.PHOTO -> "Foto"; else -> "Redaktur" }, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Accent, selectedTextColor = Accent, indicatorColor = Color(0xFF244564), unselectedIconColor = Muted, unselectedTextColor = Muted))
                }
            }
        }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("INFO KINI", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black)
                        Text("NEWS STUDIO  •  infokini.news", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("V3.0", color = Gold, fontWeight = FontWeight.Bold)
                }
                when (page) {
                    Page.HOME -> HomePage { page = it }
                    Page.VIDEO -> VideoPage(headline, { headline = it }, ticker, { ticker = it }, script, { script = it }, video, { pickVideo.launch(arrayOf("video/*")) }, music, { pickMusic.launch(arrayOf("audio/*")) }, template, { page = Page.TEMPLATE })
                    Page.PHOTO -> PhotoPage(photo) { pickPhoto.launch(arrayOf("image/*")) }
                    Page.TEMPLATE -> TemplatePage(template) { template = it; page = Page.VIDEO }
                    Page.EDITORIAL -> EditorialPage(topic, { topic = it }, sourceA, { sourceA = it }, sourceB, { sourceB = it }, script, { script = it }, results, searchStatus, {
                        scope.launch {
                            searchStatus = "Mengambil hasil pencarian..."
                            try {
                                results = getNewsHeadlines(topic)
                                searchStatus = if (results.isEmpty()) "Tidak ada judul ditemukan." else "Ada " + results.size + " judul. Periksa isi dan sumber asli sebelum menulis."
                            } catch (e: Exception) { searchStatus = "Pencarian gagal: " + (e.message ?: "periksa koneksi") }
                        }
                    }, { page = Page.VIDEO })
                    Page.PRESENTER -> PresenterPage(presenter, { presenter = it }, script, { script = it }, ttsStatus) {
                        tts.setLanguage(Locale("id", "ID"))
                        tts.setSpeechRate(0.94f)
                        val result = tts.speak(script.ifBlank { "Selamat datang di InfoKini News Studio." }, TextToSpeech.QUEUE_FLUSH, null, "infokini-preview")
                        ttsStatus = if (result == TextToSpeech.SUCCESS) "Naskah dikirim ke mesin TTS perangkat." else "Gagal menjalankan TTS. Periksa pengaturan suara Android."
                    }
                    Page.SPORTS -> SportsPage(homeTeam, { homeTeam = it }, awayTeam, { awayTeam = it }, homeScore, { homeScore = it }, awayScore, { awayScore = it })
                    Page.LIVE -> LivePage(context)
                }
                Text("InfoKini News Studio • Redaksi memegang kendali atas verifikasi dan publikasi.", color = Muted, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun HomePage(go: (Page) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
        Surface(color = Panel, shape = RoundedCornerShape(22.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF29486B))) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("RUANG REDAKSI DIGITAL", color = Accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("Produksi berita. Dengan standar televisi.", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black)
                Text("Satu ruang kerja untuk naskah, visual, suara, dan video berita InfoKini.", color = Muted, fontSize = 12.sp)
                Button(onClick = { go(Page.VIDEO) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Navy)) { Text("MULAI PRODUKSI  →", fontWeight = FontWeight.Black) }
            }
        }
        Text("MENU STUDIO", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        val items = listOf(
            Triple("EDITOR VIDEO", "Klip, judul, ticker, audio dan ekspor", Page.VIDEO),
            Triple("EDITOR FOTO", "Materi visual, poster dan thumbnail", Page.PHOTO),
            Triple("GALERI TEMPLATE", "Identitas visual program berita", Page.TEMPLATE),
            Triple("AI REDAKTUR", "Pencarian sumber dan draf berita", Page.EDITORIAL),
            Triple("PRESENTER & NARATOR", "Karakter presenter dan suara TTS", Page.PRESENTER),
            Triple("GRAFIS OLAHRAGA", "Papan skor pertandingan", Page.SPORTS),
            Triple("SIARAN LANGSUNG", "Persiapan platform live", Page.LIVE)
        )
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { item ->
                    Surface(onClick = { go(item.third) }, modifier = Modifier.weight(1f), color = Panel, shape = RoundedCornerShape(15.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF29486B))) {
                        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text(item.first, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                            Text(item.second, color = Muted, fontSize = 10.sp)
                            Text("BUKA →", color = Accent, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        Surface(color = Color(0xFF30291A), shape = RoundedCornerShape(12.dp)) {
            Text("Integrasi avatar manusia realistis, layanan AI redaksi, dan siaran langsung langsung membutuhkan API resmi serta konfigurasi akun.", Modifier.padding(13.dp), color = Gold, fontSize = 10.sp)
        }
    }
}

@Composable
private fun PageHeading(kicker: String, title: String, detail: String) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(kicker, color = Accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(title, color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Black)
        Text(detail, color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun CardBlock(content: @Composable ColumnScope.() -> Unit) {
    Surface(color = Panel, shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF29486B))) {
        Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
private fun EditField(label: String, value: String, change: (String) -> Unit, hint: String = "") {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(label, color = Muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(value = value, onValueChange = change, modifier = Modifier.fillMaxWidth(), placeholder = { Text(hint, fontSize = 12.sp) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Accent, unfocusedBorderColor = Color(0xFF3A5272), cursorColor = Accent))
    }
}

@Composable
private fun ActionButton(label: String, click: () -> Unit, primary: Boolean = false) {
    if (primary) Button(onClick = click, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Navy)) { Text(label, fontWeight = FontWeight.Bold) }
    else OutlinedButton(onClick = click, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = Accent)) { Text(label) }
}

@Composable
private fun VideoPage(headline: String, onHeadline: (String) -> Unit, ticker: String, onTicker: (String) -> Unit, script: String, onScript: (String) -> Unit, video: Uri?, pickVideo: () -> Unit, music: Uri?, pickMusic: () -> Unit, template: String, openTemplates: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
        PageHeading("RUANG PRODUKSI / 01", "Editor Video", "Materi, naskah, identitas tayangan, dan audio.")
        CardBlock {
            Text("MATERI VIDEO", color = Accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            if (video == null) Text("Belum ada video dipilih", color = Muted, modifier = Modifier.fillMaxWidth().background(Navy, RoundedCornerShape(10.dp)).padding(24.dp))
            else AndroidView(factory = { VideoView(it).apply { setVideoURI(video); setOnPreparedListener { player -> player.isLooping = true; start() } } }, modifier = Modifier.fillMaxWidth().height(205.dp).background(Color.Black))
            ActionButton("＋ PILIH VIDEO", pickVideo, true)
        }
        CardBlock {
            Text("IDENTITAS BERITA", color = Accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            EditField("Judul berita", headline, onHeadline, "Masukkan judul utama")
            EditField("Running text / ticker", ticker, onTicker, "Informasi berjalan di bagian bawah")
            Text("Template aktif: " + template, color = Muted, fontSize = 11.sp)
            ActionButton("GANTI TEMPLATE", openTemplates)
        }
        CardBlock {
            Text("NASKAH PRESENTER", color = Accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            EditField("Isi naskah", script, onScript, "Tulis naskah berita untuk dibacakan")
        }
        CardBlock {
            Text("AUDIO LATAR", color = Accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(if (music == null) "Belum ada musik latar dipilih" else "Musik: " + music.lastPathSegment, color = Muted, fontSize = 11.sp)
            ActionButton("PILIH MUSIK", pickMusic)
        }
        CardBlock {
            Text("MESIN EKSPOR", color = Accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Ekspor MP4 dan timeline multi-klip akan dibangun setelah fondasi navigasi dan penyimpanan proyek ini lolos build. Pratinjau sekarang memutar klip sumber.", color = Gold, fontSize = 11.sp)
        }
    }
}

@Composable
private fun PhotoPage(photo: Uri?, pick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
        PageHeading("RUANG PRODUKSI / 02", "Editor Foto", "Pilih materi visual untuk poster dan thumbnail berita.")
        CardBlock {
            Text(if (photo == null) "Belum ada foto dipilih." else "Foto dipilih: " + photo.lastPathSegment, color = Muted, fontSize = 12.sp)
            ActionButton("＋ PILIH FOTO", pick, true)
            Text("Panel penyuntingan foto dan ekspor poster akan dibangun pada tahap editor berikutnya.", color = Gold, fontSize = 11.sp)
        }
    }
}

@Composable
private fun TemplatePage(selected: String, choose: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PageHeading("IDENTITAS VISUAL", "Galeri Template", "Pilih gaya visual untuk proyek berita.")
        listOf("InfoKini Utama", "Breaking News", "Liputan Desa", "Wawancara", "Olahraga", "Info Cuaca", "Ekonomi & UMKM", "Dokumenter", "Kriminal", "Vertikal Sosial Media").forEachIndexed { i, name ->
            Surface(onClick = { choose(name) }, color = if (selected == name) Color(0xFF183D57) else Panel, shape = RoundedCornerShape(13.dp), border = androidx.compose.foundation.BorderStroke(1.dp, if (selected == name) Accent else Color(0xFF29486B))) {
                Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp, 30.dp).background(listOf(Color(0xFFB92737), Color(0xFF315A44), Color(0xFF4A3978), Color(0xFF13617A))[i % 4], RoundedCornerShape(5.dp)))
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(if (selected == name) "Sedang dipilih" else "Pilih template", color = Muted, fontSize = 10.sp)
                    }
                    Text(if (selected == name) "✓" else "›", color = Accent, fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
private fun EditorialPage(topic: String, onTopic: (String) -> Unit, sourceA: String, onSourceA: (String) -> Unit, sourceB: String, onSourceB: (String) -> Unit, script: String, onScript: (String) -> Unit, results: List<String>, status: String, search: () -> Unit, toVideo: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
        PageHeading("RUANG REDAKSI / 03", "AI Redaktur", "Cari bahan awal, periksa sumber, lalu susun draf. Tidak ada publikasi otomatis.")
        CardBlock {
            EditField("Topik pencarian", topic, onTopic, "Contoh: banjir Cigombong")
            ActionButton("CARI JUDUL DARI GOOGLE NEWS", search, true)
            Text(status, color = Muted, fontSize = 11.sp)
            results.forEach { Text(it, color = Color.White, fontSize = 11.sp) }
        }
        CardBlock {
            Text("PEMERIKSAAN SUMBER", color = Accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            EditField("URL sumber utama", sourceA, onSourceA, "https://...")
            EditField("URL sumber pembanding", sourceB, onSourceB, "https://...")
            Text("Baca isi sumber asli, periksa tanggal, gunakan sumber independen dan konfirmasi fakta.", color = Gold, fontSize = 10.sp)
        }
        CardBlock {
            Text("DRAF NASKAH", color = Accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            EditField("Naskah redaksi", script, onScript, "Susun naskah dari fakta yang sudah diperiksa")
            ActionButton("LANJUTKAN KE EDITOR VIDEO", toVideo, true)
        }
    }
}

@Composable
private fun PresenterPage(presenter: String, onPresenter: (String) -> Unit, script: String, onScript: (String) -> Unit, status: String, speak: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
        PageHeading("STUDIO SUARA", "Presenter & Narator", "Pilihan suara TTS bergantung pada paket bahasa Android.")
        CardBlock {
            Text("KONSEP PRESENTER", color = Accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            listOf("Presenter perempuan", "Presenter laki-laki").forEach { item ->
                Row(Modifier.fillMaxWidth().clickable { onPresenter(item) }.padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = presenter == item, onClick = { onPresenter(item) }, colors = RadioButtonDefaults.colors(selectedColor = Accent))
                    Text(item, color = Color.White, fontSize = 12.sp)
                }
            }
            Text("Presenter manusia realistis dan sinkronisasi bibir memerlukan layanan avatar khusus; belum aktif di versi dasar ini.", color = Gold, fontSize = 10.sp)
        }
        CardBlock {
            EditField("Naskah untuk dibacakan", script, onScript, "Masukkan naskah")
            ActionButton("▶ DENGARKAN PRATINJAU", speak, true)
            Text(status, color = Muted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun SportsPage(home: String, setHome: (String) -> Unit, away: String, setAway: (String) -> Unit, hs: Int, setHs: (Int) -> Unit, ascore: Int, setAs: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
        PageHeading("GRAFIS SIARAN", "Grafis Olahraga", "Papan skor manual untuk grafis pertandingan.")
        CardBlock {
            EditField("Tim tuan rumah", home, setHome)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(hs.toString(), color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Black)
                    Row { TextButton(onClick = { setHs((hs - 1).coerceAtLeast(0)) }) { Text("−", color = Accent) }; TextButton(onClick = { setHs(hs + 1) }) { Text("+", color = Accent) } }
                }
                Text("VS", color = Gold, fontWeight = FontWeight.Black)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(ascore.toString(), color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Black)
                    Row { TextButton(onClick = { setAs((ascore - 1).coerceAtLeast(0)) }) { Text("−", color = Accent) }; TextButton(onClick = { setAs(ascore + 1) }) { Text("+", color = Accent) } }
                }
            }
            EditField("Tim tamu", away, setAway)
        }
    }
}

@Composable
private fun LivePage(context: Context) {
    Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
        PageHeading("DISTRIBUSI", "Siaran Langsung", "Pusat persiapan platform distribusi.")
        listOf(Triple("YOUTUBE LIVE", "Perlu otorisasi, konfigurasi encoder/RTMP dan pengujian.", "https://studio.youtube.com/channel/"),
            Triple("TIKTOK LIVE", "Perlu akses LIVE dan integrasi resmi.", "https://www.tiktok.com/live/creator-center"),
            Triple("FACEBOOK LIVE", "Perlu otorisasi akun dan konfigurasi siaran.", "https://www.facebook.com/live/producer/")).forEach { item ->
            CardBlock {
                Text(item.first, color = Color.White, fontWeight = FontWeight.Bold)
                Text(item.second, color = Gold, fontSize = 11.sp)
                ActionButton("BUKA " + item.first, { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.third))) } })
            }
        }
    }
}

private suspend fun getNewsHeadlines(query: String): List<String> = withContext(Dispatchers.IO) {
    require(query.isNotBlank()) { "Isi topik pencarian terlebih dahulu." }
    val encoded = URLEncoder.encode(query, "UTF-8")
    val connection = URL("https://news.google.com/rss/search?q=" + encoded + "&hl=id&gl=ID&ceid=ID:id").openConnection().apply {
        connectTimeout = 10000
        readTimeout = 10000
        setRequestProperty("User-Agent", "InfoKiniNewsStudio/3.0")
    }
    try {
        connection.getInputStream().use { stream ->
            val parser = Xml.newPullParser()
            parser.setInput(stream, "UTF-8")
            val items = mutableListOf<String>()
            var event = parser.eventType
            var insideItem = false
            var insideTitle = false
            var title = ""
            while (event != XmlPullParser.END_DOCUMENT && items.size < 10) {
                when (event) {
                    XmlPullParser.START_TAG -> when (parser.name) {
                        "item" -> { insideItem = true; title = "" }
                        "title" -> if (insideItem) insideTitle = true
                    }
                    XmlPullParser.TEXT -> if (insideItem && insideTitle) title += parser.text
                    XmlPullParser.END_TAG -> when (parser.name) {
                        "title" -> insideTitle = false
                        "item" -> { if (title.isNotBlank()) items.add(title.trim()); insideItem = false }
                    }
                }
                event = parser.next()
            }
            items
        }
    } finally { (connection as? java.net.HttpURLConnection)?.disconnect() }
}


internal fun isValidHttpUrl(value: String): Boolean {
    if (value.isBlank()) return false
    return try {
        val parsed = java.net.URI(value.trim()).toURL()
        (parsed.protocol.equals("http", true) || parsed.protocol.equals("https", true)) &&
            !parsed.host.isNullOrBlank()
    } catch (_: Exception) {
        false
    }
}

internal fun safeExportFileName(value: String): String {
    val cleaned = value.trim()
        .replace(Regex("[^A-Za-z0-9._-]+"), "_")
        .trim('_', '.', '-')
    return cleaned.ifBlank { "infokini_news" }.take(80)
}

internal fun isValidIndependentSourcePair(first: String, second: String): Boolean {
    if (!isValidHttpUrl(first) || !isValidHttpUrl(second)) return false
    return try {
        fun host(value: String): String =
            java.net.URI(value.trim()).host.orEmpty().lowercase(Locale.ROOT).removePrefix("www.")
        val a = host(first)
        val b = host(second)
        a.isNotBlank() && b.isNotBlank() && a != b
    } catch (_: Exception) {
        false
    }
}
