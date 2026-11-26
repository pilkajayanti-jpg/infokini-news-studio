package id.infokini.news

import android.content.Context
import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.VideoView
import android.widget.FrameLayout
import android.widget.TextView
import android.view.Gravity
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

private enum class Page { HOME, PROJECTS, MEDIA, VIDEO, VIDEO_TIMELINE, VIDEO_AUDIO, VIDEO_SUBTITLE, VIDEO_EXPORT, PHOTO, PHOTO_DESIGN, PHOTO_EXPORT, TEMPLATE, EDITORIAL, NEWS_SOURCES, NEWS_DRAFTS, PRESENTER, PRESENTER_AVATAR, VOICE_LIBRARY, SPORTS, GRAPHICS, LIVE, LIVE_SETUP, SETTINGS, HELP }

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
    var photoBrightness by remember { mutableFloatStateOf(prefs.getFloat("photo_brightness", 0f)) }
    var photoContrast by remember { mutableFloatStateOf(prefs.getFloat("photo_contrast", 1f)) }
    var photoSaturation by remember { mutableFloatStateOf(prefs.getFloat("photo_saturation", 1f)) }
    var photoRotation by remember { mutableFloatStateOf(prefs.getFloat("photo_rotation", 0f)) }
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
            .putString("video", video?.toString()).putString("photo", photo?.toString())
            .putFloat("photo_brightness", photoBrightness).putFloat("photo_contrast", photoContrast)
            .putFloat("photo_saturation", photoSaturation).putFloat("photo_rotation", photoRotation).apply()
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
                    Page.PHOTO -> PhotoPage(photo, photoBrightness, { photoBrightness = it }, photoContrast, { photoContrast = it }, photoSaturation, { photoSaturation = it }, photoRotation, { photoRotation = it }) { pickPhoto.launch(arrayOf("image/*")) }
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
                    else -> StudioModulePage(page)
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
            Triple("PROYEK SAYA", "Buat, buka, dan lanjutkan proyek berita", Page.PROJECTS),
            Triple("GALERI MEDIA", "Kelola video, foto, audio, dan hasil ekspor", Page.MEDIA),
            Triple("EDITOR VIDEO", "Ruang kerja utama penyuntingan video", Page.VIDEO),
            Triple("TIMELINE & KLIP", "Susun banyak klip, potong, urutkan, dan transisi", Page.VIDEO_TIMELINE),
            Triple("AUDIO & MUSIK", "Musik latar, rekaman suara, dan mixing", Page.VIDEO_AUDIO),
            Triple("TEKS & SUBTITLE", "Judul, lower third, ticker, dan subtitle", Page.VIDEO_SUBTITLE),
            Triple("EKSPOR VIDEO", "Pengaturan kualitas dan hasil MP4", Page.VIDEO_EXPORT),
            Triple("EDITOR FOTO", "Penyuntingan foto dan materi visual", Page.PHOTO),
            Triple("DESAIN GRAFIS", "Poster, thumbnail, crop, dan teks foto", Page.PHOTO_DESIGN),
            Triple("EKSPOR FOTO", "Simpan hasil desain ke perangkat", Page.PHOTO_EXPORT),
            Triple("TEMPLATE STUDIO", "Paket grafis dan format acara berita", Page.TEMPLATE),
            Triple("AI REDAKTUR", "Pantau topik dan kumpulkan berita", Page.EDITORIAL),
            Triple("SUMBER BERITA", "Daftar sumber, tautan, dan verifikasi", Page.NEWS_SOURCES),
            Triple("DRAF & VERIFIKASI", "Naskah AI, cek fakta, dan persetujuan", Page.NEWS_DRAFTS),
            Triple("PRESENTER DIGITAL", "Presenter manusia orisinal dan avatar", Page.PRESENTER_AVATAR),
            Triple("NARATOR & SUARA", "Pilihan suara TTS Bahasa Indonesia", Page.VOICE_LIBRARY),
            Triple("GRAFIS OLAHRAGA", "Skor, papan pertandingan, dan lower third", Page.SPORTS),
            Triple("PAKET GRAFIS", "Peta, cuaca, breaking news, dan infografik", Page.GRAPHICS),
            Triple("SIARAN LANGSUNG", "Pusat persiapan live streaming", Page.LIVE),
            Triple("KONEKSI LIVE", "YouTube, TikTok, Facebook, dan RTMP", Page.LIVE_SETUP),
            Triple("PENGATURAN", "Identitas kanal, ekspor, dan penyimpanan", Page.SETTINGS),
            Triple("PANDUAN & BANTUAN", "Petunjuk penggunaan dan status fitur", Page.HELP)
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
private fun StudioModulePage(page: Page) {
    val module = when (page) {
        Page.PROJECTS -> Triple("PROYEK SAYA", "Kelola pekerjaan berita dari ide sampai video final.", listOf("Buat proyek baru per berita atau program", "Buka dan lanjutkan proyek terakhir", "Simpan otomatis naskah dan pengaturan", "Duplikasi proyek sebagai template"))
        Page.MEDIA -> Triple("GALERI MEDIA", "Pusat aset yang dipakai di seluruh ruang produksi.", listOf("Impor video, foto, logo, dan audio", "Kelompokkan aset berdasarkan proyek", "Pratinjau dan pilih beberapa media", "Lihat file hasil ekspor"))
        Page.VIDEO_TIMELINE -> Triple("TIMELINE & KLIP", "Rancangan ruang penyuntingan multi-klip.", listOf("Beberapa track video dan overlay", "Potong, trim, split, dan susun klip", "Transisi antar-klip", "Atur rasio 16:9, 9:16, dan 1:1", "Undo dan redo riwayat edit"))
        Page.VIDEO_AUDIO -> Triple("AUDIO & MUSIK", "Pengaturan suara untuk paket berita.", listOf("Impor musik latar dan efek suara", "Rekam atau impor voice-over", "Atur volume, fade in, dan fade out", "Mixing suara narator dengan musik", "Kontrol mute dan level audio"))
        Page.VIDEO_SUBTITLE -> Triple("TEKS & SUBTITLE", "Perangkat grafis teks bergaya siaran televisi.", listOf("Judul utama dan subjudul berita", "Running text / ticker infokini.news", "Lower third nama presenter dan narasumber", "Subtitle manual dan sinkronisasi waktu", "Font, warna, posisi, dan animasi teks"))
        Page.VIDEO_EXPORT -> Triple("EKSPOR VIDEO", "Halaman keluaran akhir untuk video berita.", listOf("Ekspor MP4 dengan H.264", "Pilihan resolusi 720p dan 1080p", "Pilihan frame rate dan kualitas", "Pratinjau sebelum ekspor", "Simpan ke Galeri dan bagikan"))
        Page.PHOTO_DESIGN -> Triple("DESAIN GRAFIS FOTO", "Ruang desain visual untuk kebutuhan redaksi.", listOf("Crop, rotate, dan flip", "Kecerahan, kontras, dan saturasi", "Teks judul, logo, dan watermark", "Template thumbnail dan poster", "Lapisan elemen grafis"))
        Page.PHOTO_EXPORT -> Triple("EKSPOR FOTO", "Simpan materi visual hasil penyuntingan.", listOf("Ekspor JPG dan PNG", "Atur kualitas gambar", "Simpan ke Galeri", "Nama file dan folder InfoKini"))
        Page.NEWS_SOURCES -> Triple("SUMBER BERITA", "Daftar sumber yang dapat ditelusuri dan diperiksa.", listOf("Pencarian RSS dan kanal berita", "Simpan tautan sumber asli", "Catat waktu publikasi dan penulis bila tersedia", "Bandingkan minimal dua sumber independen", "Tandai sumber yang belum diverifikasi"))
        Page.NEWS_DRAFTS -> Triple("DRAF & VERIFIKASI", "AI membantu menyusun, redaksi tetap memegang keputusan.", listOf("Susun draf dari bahan dan sumber terpilih", "Pisahkan fakta, klaim, dan informasi belum pasti", "Tampilkan tautan sumber pada setiap draf", "Checklist verifikasi dan penyuntingan", "Persetujuan manual sebelum ekspor atau publikasi"))
        Page.PRESENTER_AVATAR -> Triple("PRESENTER DIGITAL", "Konsep presenter Indonesia dengan wajah orisinal, bukan tiruan orang nyata.", listOf("Pilihan presenter pria dan wanita", "Gaya busana dan latar studio", "Masukkan naskah presenter", "Render gerak dan sinkronisasi bibir melalui layanan avatar", "Pratinjau sebelum digunakan"))
        Page.VOICE_LIBRARY -> Triple("NARATOR & SUARA", "Pengaturan pembacaan naskah Bahasa Indonesia.", listOf("Pilih suara TTS yang tersedia di perangkat", "Atur kecepatan dan intonasi", "Pratinjau pembacaan naskah", "Simpan audio narasi ke proyek"))
        Page.GRAPHICS -> Triple("PAKET GRAFIS", "Elemen visual pendukung siaran dan video berita.", listOf("Breaking news dan headline strap", "Papan skor olahraga", "Peta lokasi dan penanda", "Grafik data dan infografik", "Cuaca dan informasi layanan publik"))
        Page.LIVE_SETUP -> Triple("KONEKSI SIARAN LANGSUNG", "Persiapan integrasi resmi platform streaming.", listOf("Hubungkan kanal YouTube melalui OAuth", "Siapkan tujuan live TikTok sesuai akses akun", "Siapkan Facebook Live", "Konfigurasi RTMP/stream key secara aman", "Uji koneksi, audio, dan video sebelum tayang"))
        Page.SETTINGS -> Triple("PENGATURAN STUDIO", "Identitas kanal dan preferensi produksi.", listOf("Nama kanal dan logo InfoKini", "Watermark infokini.news", "Folder penyimpanan dan kualitas ekspor", "Bahasa aplikasi dan preferensi suara", "Privasi, izin media, dan pengelolaan data"))
        Page.HELP -> Triple("PANDUAN & BANTUAN", "Petunjuk penggunaan serta keterangan kesiapan fitur.", listOf("Panduan membuat proyek berita", "Panduan editor foto dan video", "Panduan verifikasi AI Redaktur", "Daftar fitur yang memerlukan internet atau API", "Informasi versi dan diagnostik aplikasi"))
        else -> Triple("RUANG STUDIO", "Halaman fitur InfoKini News Studio.", listOf("Fitur sedang disiapkan"))
    }
    PageHeading("INFO KINI • RUANG PRODUKSI", module.first, module.second)
    CardBlock {
        Text("DAFTAR FITUR", color = Accent, fontWeight = FontWeight.Black, fontSize = 12.sp)
        module.third.forEachIndexed { index, item ->
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("\${index + 1}.", color = Accent, fontWeight = FontWeight.Bold)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(item, color = Color.White, fontSize = 12.sp)
                    Text("Ruang fitur dirancang • implementasi dan pengujian bertahap", color = Gold, fontSize = 9.sp)
                }
            }
            if (index != module.third.lastIndex) HorizontalDivider(color = Color(0xFF29486B))
        }
    }
    CardBlock {
        Text("CATATAN KESIAPAN", color = Accent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        Text("Halaman dan alur menu ini sudah dimasukkan ke struktur aplikasi. Daftar di atas bukan klaim bahwa seluruh mesin fitur telah aktif. Fungsi seperti render avatar, AI generatif, ekspor MP4, dan siaran langsung memerlukan implementasi teknis serta, untuk layanan tertentu, API atau akun resmi.", color = Muted, fontSize = 11.sp)
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
            else AndroidView(
                factory = { ctx ->
                    val frame = FrameLayout(ctx).apply { setBackgroundColor(android.graphics.Color.BLACK) }
                    val player = VideoView(ctx).apply {
                        setVideoURI(video)
                        setOnPreparedListener { media -> media.isLooping = true; start() }
                    }
                    frame.addView(player, FrameLayout.LayoutParams(-1, -1))
                    val titleView = TextView(ctx).apply {
                        setTextColor(android.graphics.Color.WHITE)
                        textSize = 15f
                        setTypeface(null, android.graphics.Typeface.BOLD)
                        setPadding(12, 9, 12, 9)
                        setBackgroundColor(android.graphics.Color.argb(210, 150, 20, 35))
                    }
                    frame.addView(titleView, FrameLayout.LayoutParams(-1, -2, Gravity.TOP))
                    val tickerView = TextView(ctx).apply {
                        setTextColor(android.graphics.Color.WHITE)
                        textSize = 12f
                        setPadding(12, 10, 12, 10)
                        setBackgroundColor(android.graphics.Color.argb(225, 7, 20, 46))
                        gravity = Gravity.CENTER_VERTICAL
                    }
                    frame.addView(tickerView, FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM))
                    frame.tag = Pair(titleView, tickerView)
                    frame
                },
                update = { frame ->
                    val labels = frame.tag as? Pair<*, *>
                    (labels?.first as? TextView)?.text = headline.ifBlank { "INFO KINI • infokini.news" }
                    (labels?.second as? TextView)?.text = ticker.ifBlank { "INFO TERKINI • infokini.news" }
                },
                modifier = Modifier.fillMaxWidth().height(205.dp).background(Color.Black)
            )
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
private fun VideoExportPage(video: Uri?) {
    val context = LocalContext.current
    var durationMs by remember(video) { mutableLongStateOf(0L) }
    var startMs by remember(video) { mutableFloatStateOf(0f) }
    var endMs by remember(video) { mutableFloatStateOf(0f) }
    var status by remember(video) { mutableStateOf("Pilih video dari Editor Video untuk mulai.") }
    var exporting by remember { mutableStateOf(false) }

    LaunchedEffect(video) {
        if (video != null) {
            status = "Membaca durasi video..."
            val length = withContext(Dispatchers.IO) {
                runCatching {
                    val retriever = MediaMetadataRetriever()
                    try {
                        retriever.setDataSource(context, video)
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                    } finally { retriever.release() }
                }.getOrDefault(0L)
            }
            durationMs = length
            startMs = 0f
            endMs = length.toFloat()
            status = if (length > 0L) "Atur titik mulai dan akhir, lalu ekspor." else "Durasi tidak terbaca. Coba pilih file video lain."
        }
    }

    fun formatTime(ms: Long): String {
        val seconds = (ms / 1000L).coerceAtLeast(0L)
        return "%02d:%02d".format(seconds / 60L, seconds % 60L)
    }

    Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
        PageHeading("RUANG PRODUKSI / 03", "Potong & Ekspor MP4", "Pilih rentang klip sebelum membuat file video.")
        CardBlock {
            Text("SUMBER VIDEO", color = Accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(video?.lastPathSegment ?: "Belum ada video dipilih", color = Muted, fontSize = 12.sp)
            if (durationMs > 0L) {
                Text("Durasi sumber: " + formatTime(durationMs), color = Color.White, fontWeight = FontWeight.Bold)
                Text("Titik mulai: " + formatTime(startMs.toLong()), color = Muted, fontSize = 12.sp)
                Slider(value = startMs, onValueChange = { startMs = it.coerceAtMost(endMs - 1000f) }, valueRange = 0f..(durationMs - 1000L).coerceAtLeast(0L).toFloat())
                Text("Titik akhir: " + formatTime(endMs.toLong()), color = Muted, fontSize = 12.sp)
                Slider(value = endMs, onValueChange = { endMs = it.coerceAtLeast(startMs + 1000f) }, valueRange = 1000f..durationMs.toFloat())
                Text("Durasi hasil: " + formatTime((endMs - startMs).toLong()), color = Accent, fontWeight = FontWeight.Bold)
            }
        }
        CardBlock {
            Text("EKSPOR VIDEO", color = Accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(status, color = if (status.contains("gagal", true) || status.contains("tidak")) Gold else Muted, fontSize = 12.sp)
            ActionButton(if (exporting) "SEDANG MENGEKSPOR..." else "EKSPOR MP4 KE GALERI", {
                if (video == null || durationMs <= 0L || endMs <= startMs || exporting) {
                    status = "Pilih video valid dan atur rentang minimal 1 detik."
                } else {
                    exporting = true
                    status = "Ekspor dimulai. Jangan tutup aplikasi sampai selesai."
                    val output = File(context.cacheDir, "infokini_" + System.currentTimeMillis() + ".mp4")
                    val clipping = MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(startMs.toLong())
                        .setEndPositionMs(endMs.toLong())
                        .build()
                    val item = MediaItem.Builder().setUri(video).setClippingConfiguration(clipping).build()
                    val edited = EditedMediaItem.Builder(item).build()
                    val listener = object : Transformer.Listener {
                        override fun onCompleted(composition: androidx.media3.transformer.Composition, exportResult: androidx.media3.transformer.ExportResult) {
                            try {
                                val values = ContentValues().apply {
                                    put(MediaStore.Video.Media.DISPLAY_NAME, "InfoKini_" + System.currentTimeMillis() + ".mp4")
                                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                                    if (Build.VERSION.SDK_INT >= 29) put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/InfoKini")
                                }
                                val saved = context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                                    ?: throw IllegalStateException("Tidak dapat membuat file Galeri")
                                context.contentResolver.openOutputStream(saved)?.use { destination ->
                                    output.inputStream().use { source -> source.copyTo(destination) }
                                } ?: throw IllegalStateException("Tidak dapat menyimpan hasil")
                                output.delete()
                                status = "Berhasil! MP4 tersimpan di Galeri / Movies / InfoKini."
                            } catch (error: Exception) {
                                status = "Ekspor selesai, tetapi penyimpanan ke Galeri gagal: " + error.message
                            }
                            exporting = false
                        }
                        override fun onError(composition: androidx.media3.transformer.Composition, exportResult: androidx.media3.transformer.ExportResult, exception: androidx.media3.transformer.ExportException) {
                            output.delete()
                            status = "Ekspor gagal: " + (exception.message ?: "format video tidak didukung")
                            exporting = false
                        }
                    }
                    try {
                        Transformer.Builder(context)
                            .setVideoMimeType(MimeTypes.VIDEO_H264)
                            .setAudioMimeType(MimeTypes.AUDIO_AAC)
                            .addListener(listener)
                            .build()
                            .start(edited, output.absolutePath)
                    } catch (error: Exception) {
                        output.delete()
                        status = "Tidak dapat memulai ekspor: " + error.message
                        exporting = false
                    }
                }
            }, true)
        }
        Text("Versi ini mendukung pemotongan satu video dan ekspor MP4. Penggabungan banyak klip, transisi, overlay ticker permanen, dan mixing musik masih tahap pengembangan.", color = Gold, fontSize = 11.sp)
    }
}

@Composable
private fun PhotoPage(
    photo: Uri?,
    brightness: Float, onBrightness: (Float) -> Unit,
    contrast: Float, onContrast: (Float) -> Unit,
    saturation: Float, onSaturation: (Float) -> Unit,
    rotation: Float, onRotation: (Float) -> Unit,
    pick: () -> Unit
) {
    val context = LocalContext.current
    val exportPhotoNow = {
        try {
            val source = context.contentResolver.openInputStream(photo!!)
            val original = source?.use { BitmapFactory.decodeStream(it) } ?: throw IllegalStateException("Foto tidak dapat dibaca")
            val color = android.graphics.ColorMatrix().apply { setSaturation(saturation.coerceIn(0f, 2f)) }
            val cv = contrast.coerceIn(0f, 2f)
            val bv = brightness.coerceIn(-1f, 1f) * 255f
            val cm = android.graphics.ColorMatrix(floatArrayOf(cv,0f,0f,0f,128f*(1f-cv)+bv, 0f,cv,0f,0f,128f*(1f-cv)+bv, 0f,0f,cv,0f,128f*(1f-cv)+bv, 0f,0f,0f,1f,0f))
            color.postConcat(cm)
            val matrix = Matrix().apply { postRotate(rotation) }
            val rotated = Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
            val output = Bitmap.createBitmap(rotated.width, rotated.height, Bitmap.Config.ARGB_8888)
            Canvas(output).drawBitmap(rotated, 0f, 0f, Paint().apply { colorFilter = android.graphics.ColorMatrixColorFilter(color) })
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "InfoKini_Foto_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= 29) put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/InfoKini")
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: throw IllegalStateException("Tidak dapat membuat file")
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                if (!output.compress(Bitmap.CompressFormat.JPEG, 92, stream)) throw IllegalStateException("Gagal menyimpan gambar")
            } ?: throw IllegalStateException("Tidak dapat menulis file")
            Toast.makeText(context, "Foto hasil edit tersimpan di Galeri / Pictures / InfoKini", Toast.LENGTH_LONG).show()
            original.recycle()
            if (rotated !== original) rotated.recycle()
            output.recycle()
        } catch (error: Exception) {
            Toast.makeText(context, "Ekspor gagal: ${error.message}", Toast.LENGTH_LONG).show()
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) exportPhotoNow() else Toast.makeText(context, "Izin penyimpanan diperlukan di Android 9", Toast.LENGTH_LONG).show()
    }
    Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
        PageHeading("RUANG PRODUKSI / 02", "Editor Foto", "Atur tampilan dasar foto untuk materi berita.")
        CardBlock {
            if (photo == null) {
                Text("Belum ada foto dipilih.", color = Muted, fontSize = 12.sp)
            } else {
                Text("PRATINJAU HASIL PENYESUAIAN", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                AndroidView(
                    factory = { ctx -> android.widget.ImageView(ctx).apply {
                        scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                        setBackgroundColor(android.graphics.Color.BLACK)
                    } },
                    update = { image ->
                        image.setImageURI(photo)
                        val matrix = android.graphics.ColorMatrix().apply { setSaturation(saturation.coerceIn(0f, 2f)) }
                        val cValue = contrast.coerceIn(0f, 2f)
                        val bValue = brightness.coerceIn(-1f, 1f) * 255f
                        val contrastMatrix = android.graphics.ColorMatrix(floatArrayOf(
                            cValue, 0f, 0f, 0f, 128f * (1f - cValue) + bValue,
                            0f, cValue, 0f, 0f, 128f * (1f - cValue) + bValue,
                            0f, 0f, cValue, 0f, 128f * (1f - cValue) + bValue,
                            0f, 0f, 0f, 1f, 0f
                        ))
                        matrix.postConcat(contrastMatrix)
                        image.colorFilter = android.graphics.ColorMatrixColorFilter(matrix)
                        image.rotation = rotation
                    },
                    modifier = Modifier.fillMaxWidth().height(245.dp).background(Color.Black)
                )
                Text("Kecerahan", color = Muted, fontSize = 11.sp)
                Slider(value = brightness, onValueChange = onBrightness, valueRange = -1f..1f)
                Text("Kontras", color = Muted, fontSize = 11.sp)
                Slider(value = contrast, onValueChange = onContrast, valueRange = 0f..2f)
                Text("Saturasi", color = Muted, fontSize = 11.sp)
                Slider(value = saturation, onValueChange = onSaturation, valueRange = 0f..2f)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onRotation((rotation - 90f + 360f) % 360f) }, modifier = Modifier.weight(1f)) { Text("PUTAR KIRI") }
                    OutlinedButton(onClick = { onRotation((rotation + 90f) % 360f) }, modifier = Modifier.weight(1f)) { Text("PUTAR KANAN") }
                }
                ActionButton("RESET PENYESUAIAN", { onBrightness(0f); onContrast(1f); onSaturation(1f); onRotation(0f) })
                ActionButton("EKSPOR FOTO HASIL EDIT", {
                    if (Build.VERSION.SDK_INT <= 28 && context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    } else exportPhotoNow()
                }, true)
            }
            ActionButton("＋ PILIH FOTO", pick, true)
            Text("Penyesuaian tampil di pratinjau dan tersimpan pada proyek. Ekspor poster final belum diaktifkan.", color = Gold, fontSize = 11.sp)
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
