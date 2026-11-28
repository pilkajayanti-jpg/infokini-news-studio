package id.infokini.news.v2

import android.os.Bundle
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Typeface
import androidx.media3.common.MediaItem
import androidx.media3.common.C
import androidx.media3.transformer.Effects
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.Composition
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ExportException
import androidx.media3.effect.BitmapOverlay
import androidx.media3.effect.OverlayEffect
import java.io.File
import androidx.lifecycle.LifecycleOwner
import androidx.camera.view.PreviewView
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.core.Preview
import androidx.camera.core.CameraSelector
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.Manifest
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import android.media.MediaPlayer
import android.widget.MediaController
import android.widget.VideoView
import android.widget.ImageView
import android.net.Uri
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
import androidx.compose.runtime.LaunchedEffect
import android.view.SurfaceView
import com.pedro.library.generic.GenericStream
import com.pedro.encoder.input.sources.video.Camera2Source
import com.pedro.encoder.input.sources.audio.MicrophoneSource
import com.pedro.common.StreamingStatsReport
import com.pedro.common.ConnectChecker
import com.pedro.encoder.input.gl.render.filters.`object`.ImageFilterRender
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.verticalScroll
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
    StudioModule("Berita Olahraga", "Ruang draf dan catatan liputan olahraga", "Tahap fondasi"),
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
    val context = LocalContext.current
    val studioPrefs = remember(context) {
        context.getSharedPreferences("infokini_studio_preferences_v2", android.content.Context.MODE_PRIVATE)
    }
    var selected by remember { mutableStateOf("Beranda") }
    var headline by remember { mutableStateOf(studioPrefs.getString("headline", "") ?: "") }
    var ticker by remember { mutableStateOf(studioPrefs.getString("ticker", "INFO KINI | BERPIKIR TAJAM, MENGUNGKAP KEBENARAN") ?: "INFO KINI | BERPIKIR TAJAM, MENGUNGKAP KEBENARAN") }
    var videoTemplate by remember { mutableStateOf(studioPrefs.getString("video_template", "Breaking News") ?: "Breaking News") }
    LaunchedEffect(headline, ticker, videoTemplate) {
        studioPrefs.edit()
            .putString("headline", headline)
            .putString("ticker", ticker)
            .putString("video_template", videoTemplate)
            .apply()
    }
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
                        "Video", "Template Berita" -> VideoStudioScreen(headline, { headline = it }, ticker, { ticker = it }, videoTemplate, { videoTemplate = it }, showTemplateLibraryInitially = selected == "Template Berita")
                        "Foto" -> PhotoStudioScreen()
                        "Proyek Saya" -> ProjectManagerScreen()
                        "Galeri Media" -> MediaLibraryScreen()
                        "Redaktur", "AI Redaktur" -> EditorialScreen()
                        "Narasi / TTS" -> TtsScreen()
                        "Grafis Olahraga", "Berita Olahraga" -> SportsScreen()
                        "Template Berita" -> { selected = "Video" }
                        "Lainnya" -> MoreScreen(onOpen = { module ->
                            selected = when (module) {
                                "Live Studio" -> "Live"
                                "Video Studio" -> "Video"
                                "Photo Studio" -> "Foto"
                                "AI Redaktur" -> "Redaktur"
                                "Narasi / TTS" -> "Narasi / TTS"
                                "Grafis Olahraga", "Berita Olahraga" -> "Grafis Olahraga"
                                "Template Berita" -> "Template Berita"
                                "Proyek Saya" -> "Proyek Saya"
                                "Galeri Media" -> "Galeri Media"
                                "Promosi UMKM" -> "Promosi UMKM"
                                else -> module
                            }
                        })
                        else -> ModuleWorkspaceScreen(selected)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().background(Panel).horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
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
    val context = LocalContext.current
    var cameraGranted by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    var audioGranted by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) }
    var frontCamera by remember { mutableStateOf(false) }
    var streamStatus by remember { mutableStateOf("Siapkan kamera dan mikrofon untuk siaran.") }
    var streamPrepared by remember { mutableStateOf(false) }
    var isStreaming by remember { mutableStateOf(false) }
    val stream = remember {
        GenericStream(context.applicationContext, object : ConnectChecker {
            override fun onConnectionStarted(url: String) { streamStatus = "Menghubungkan ke server RTMP…" }
            override fun onConnectionSuccess() { isStreaming = true; streamStatus = "Terhubung — siaran sedang berlangsung." }
            override fun onConnectionFailed(reason: String) { isStreaming = false; streamStatus = "Koneksi gagal: $reason" }
            override fun onDisconnect() { isStreaming = false; streamStatus = "Siaran terputus." }
            override fun onAuthError() { isStreaming = false; streamStatus = "Autentikasi ditolak. Periksa stream key." }
            override fun onAuthSuccess() { streamStatus = "Autentikasi server berhasil." }
            override fun onStreamingStats(report: StreamingStatsReport) { }
        })
    }
    val audioPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { audioGranted = it }
    DisposableEffect(stream) {
        onDispose { try { if (stream.isStreaming) stream.stopStream(); if (stream.isOnPreview) stream.stopPreview(); stream.release() } catch (_: Exception) { } }
    }
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var showSports by remember { mutableStateOf(false) }
    var homeName by remember { mutableStateOf("TIM A") }
    var awayName by remember { mutableStateOf("TIM B") }
    var homeScore by remember { mutableStateOf(0) }
    var awayScore by remember { mutableStateOf(0) }
    var rtmpAddress by remember { mutableStateOf("") }
    var rtmpStreamKey by remember { mutableStateOf("") }
    var rtmpCheck by remember { mutableStateOf("Belum diperiksa. Kunci siaran hanya disimpan sementara di layar ini.") }
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        cameraGranted = granted
    }
    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { logoUri = it }
    val dateLabel = java.text.SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(java.util.Date())
    val liveOverlayFilter = remember { ImageFilterRender() }
    var liveOverlayAttached by remember { mutableStateOf(false) }
    LaunchedEffect(streamPrepared, headline, ticker, dateLabel, showSports, homeName, awayName, homeScore, awayScore, logoUri) {
        if (streamPrepared) {
            try {
                val overlayBitmap = buildLiveOverlayBitmap(
                    context, headline, ticker, dateLabel, showSports,
                    homeName, awayName, homeScore, awayScore, logoUri
                )
                liveOverlayFilter.setImage(overlayBitmap)
                liveOverlayFilter.setScale(100f, 100f)
                if (!liveOverlayAttached) {
                    stream.getGlInterface().addFilter(liveOverlayFilter)
                    liveOverlayAttached = true
                }
            } catch (error: Exception) {
                streamStatus = "Grafis encoder gagal diperbarui: ${error.message}"
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("LIVE STUDIO", color = Cyan, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Text("KAMERA DAN GRAFIS SIARAN", color = Muted, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        if (cameraGranted) {
            Box(Modifier.fillMaxWidth().height(235.dp)) {
                AndroidView(modifier = Modifier.fillMaxSize(), factory = { ctx ->
                    SurfaceView(ctx).also { surface ->
                        if (!streamPrepared) {
                            try {
                                stream.changeVideoSource(Camera2Source(context.applicationContext))
                                stream.changeAudioSource(MicrophoneSource())
                                streamPrepared = stream.prepareVideo(1280, 720, 1_800_000) && stream.prepareAudio(32000, true, 128000)
                                if (streamPrepared) {
                                    stream.startPreview(surface)
                                    streamStatus = "Pratinjau encoder siap. Isi server untuk mulai siaran."
                                } else streamStatus = "Perangkat tidak mendukung konfigurasi encoder ini."
                            } catch (error: Exception) { streamStatus = "Gagal menyiapkan encoder: ${error.message}" }
                        } else if (!stream.isOnPreview) stream.startPreview(surface)
                    }
                })
                Column(
                    Modifier.align(Alignment.BottomStart).fillMaxWidth()
                        .background(Color(0xCC071426)).padding(9.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (logoUri != null) {
                            AndroidView(
                                factory = { ctx -> ImageView(ctx).apply { scaleType = ImageView.ScaleType.FIT_CENTER; setImageURI(logoUri) } },
                                update = { it.setImageURI(logoUri) },
                                modifier = Modifier.width(42.dp).height(32.dp)
                            )
                        } else {
                            Text("IK", color = Color.White, modifier = Modifier.background(Red, RoundedCornerShape(4.dp)).padding(horizontal = 7.dp, vertical = 4.dp), fontWeight = FontWeight.Black)
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("INFOKINI NEWS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                            Text(dateLabel, color = Muted, fontSize = 9.sp)
                        }
                        Text("LIVE PREVIEW", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                    }
                    if (headline.isNotBlank()) {
                        Text(headline.uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp,
                            modifier = Modifier.fillMaxWidth().background(Red).padding(7.dp))
                    }
                    if (showSports) {
                        Text("$homeName  $homeScore  -  $awayScore  $awayName", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth().background(Color(0xFF123B59)).padding(6.dp))
                    }
                    Text(ticker, color = Color.White, fontSize = 9.sp, modifier = Modifier.fillMaxWidth().background(Color(0xFFB51F2A)).padding(6.dp))
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    try { (stream.videoSource as? Camera2Source)?.switchCamera(); frontCamera = !frontCamera }
                    catch (error: Exception) { streamStatus = "Gagal mengganti kamera: ${error.message}" }
                }, modifier = Modifier.weight(1f)) { Text(if (frontCamera) "Kamera belakang" else "Kamera depan") }
                OutlinedButton(onClick = { logoPicker.launch("image/*") }, modifier = Modifier.weight(1f)) {
                    Text(if (logoUri == null) "Pilih logo" else "Ganti logo")
                }
            }
        } else {
            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(14.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Izin kamera diperlukan untuk menampilkan pratinjau grafis di atas kamera.", color = SoftWhite, fontSize = 12.sp)
                    Button(onClick = { cameraPermission.launch(Manifest.permission.CAMERA) }) { Text("Izinkan kamera") }
                    OutlinedButton(onClick = { logoPicker.launch("image/*") }) { Text("Pilih logo kanal") }
                }
            }
        }
        if (!audioGranted) OutlinedButton(onClick = { audioPermission.launch(Manifest.permission.RECORD_AUDIO) }, modifier = Modifier.fillMaxWidth()) { Text("Izinkan mikrofon untuk siaran") }
        Button(onClick = {
            val validAddress = rtmpAddress.startsWith("rtmp://") || rtmpAddress.startsWith("rtmps://")
            when {
                !cameraGranted -> streamStatus = "Izin kamera belum diberikan."
                !audioGranted -> streamStatus = "Izin mikrofon belum diberikan."
                !streamPrepared -> streamStatus = "Encoder belum siap. Periksa kamera dan perangkat."
                !validAddress || rtmpStreamKey.isBlank() -> streamStatus = "Isi alamat server RTMP/RTMPS dan stream key."
                isStreaming -> { stream.stopStream(); isStreaming = false; streamStatus = "Menghentikan siaran…"; }
                else -> {
                    val fullUrl = rtmpAddress.trim().trimEnd('/') + "/" + rtmpStreamKey.trim().trimStart('/')
                    streamStatus = "Memulai siaran…"
                    stream.startStream(fullUrl)
                }
            }
        }, modifier = Modifier.fillMaxWidth(), enabled = streamPrepared && cameraGranted && audioGranted) { Text(if (isStreaming) "HENTIKAN SIARAN" else "MULAI SIARAN RTMP") }
        Text(streamStatus, color = if (isStreaming) Cyan else Gold, fontSize = 11.sp)
        Text("Encoder mengirim kamera dan mikrofon. Grafis headline/logo/ticker/skor belum dirender ke frame siaran; masih tahap integrasi berikutnya.", color = Muted, fontSize = 10.sp)
        Text("KONFIGURASI SERVER LIVE (RTMP)", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        OutlinedTextField(value = rtmpAddress, onValueChange = { rtmpAddress = it.trim(); rtmpCheck = "Konfigurasi berubah — periksa kembali." }, modifier = Modifier.fillMaxWidth(), label = { Text("Alamat server RTMP / RTMPS") }, singleLine = true)
        OutlinedTextField(value = rtmpStreamKey, onValueChange = { rtmpStreamKey = it; rtmpCheck = "Konfigurasi berubah — periksa kembali." }, modifier = Modifier.fillMaxWidth(), label = { Text("Stream key (rahasia)") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
        Button(onClick = {
            val validAddress = rtmpAddress.startsWith("rtmp://") || rtmpAddress.startsWith("rtmps://")
            rtmpCheck = when {
                !validAddress -> "Alamat harus diawali rtmp:// atau rtmps://."
                rtmpStreamKey.isBlank() -> "Stream key belum diisi."
                else -> "Format konfigurasi terisi. Belum terhubung atau mengirim siaran; encoder RTMP masih tahap integrasi."
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Periksa konfigurasi RTMP") }
        Text(rtmpCheck, color = Gold, fontSize = 10.sp)
        Text("Jangan bagikan stream key. Kolom ini hanya sementara di memori layar; jangan gunakan perangkat bersama. Tombol ini tidak memulai siaran.", color = Muted, fontSize = 10.sp)
        OutlinedTextField(value = headline, onValueChange = onHeadline, modifier = Modifier.fillMaxWidth(), label = { Text("Judul berita / headline TV") })
        OutlinedTextField(value = ticker, onValueChange = onTicker, modifier = Modifier.fillMaxWidth(), label = { Text("Running text / berita berjalan") })
        OutlinedButton(onClick = { showSports = !showSports }, modifier = Modifier.fillMaxWidth()) {
            Text(if (showSports) "Nonaktifkan grafis olahraga" else "Tambahkan grafis olahraga")
        }
        if (showSports) {
            Text("PAPAN SKOR SIARAN", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            OutlinedTextField(value = homeName, onValueChange = { homeName = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Nama tim kandang") }, singleLine = true)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { if (homeScore > 0) homeScore-- }) { Text("−") }
                Text("$homeScore", color = SoftWhite, fontWeight = FontWeight.Black, fontSize = 20.sp)
                Button(onClick = { homeScore++ }) { Text("+") }
                Text("SKOR", color = Muted, fontSize = 10.sp)
            }
            OutlinedTextField(value = awayName, onValueChange = { awayName = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Nama tim tandang") }, singleLine = true)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { if (awayScore > 0) awayScore-- }) { Text("−") }
                Text("$awayScore", color = SoftWhite, fontWeight = FontWeight.Black, fontSize = 20.sp)
                Button(onClick = { awayScore++ }) { Text("+") }
            }
        }
        Text("Logo, tanggal, headline, running text, dan papan skor opsional tampil pada pratinjau kamera. Ini masih komposisi grafis lokal; belum dikirim ke encoder atau siaran RTMP/YouTube.", color = Muted, fontSize = 11.sp)
    }
}

private fun buildLiveOverlayBitmap(
    context: android.content.Context,
    headline: String,
    ticker: String,
    dateLabel: String,
    showSports: Boolean,
    homeName: String,
    awayName: String,
    homeScore: Int,
    awayScore: Int,
    logoUri: Uri?
): Bitmap {
    val width = 1280
    val height = 720
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }
    val navy = android.graphics.Color.rgb(7, 20, 38)
    val red = android.graphics.Color.rgb(181, 31, 42)
    val white = android.graphics.Color.WHITE
    val cyan = android.graphics.Color.rgb(39, 197, 232)

    paint.color = 0xCC071426.toInt()
    canvas.drawRect(0f, 500f, width.toFloat(), height.toFloat(), paint)
    paint.color = red
    canvas.drawRect(0f, 610f, width.toFloat(), 675f, paint)

    var logoDrawn = false
    if (logoUri != null) {
        try {
            context.contentResolver.openInputStream(logoUri)?.use { input ->
                val logo = android.graphics.BitmapFactory.decodeStream(input)
                if (logo != null) {
                    val dest = android.graphics.Rect(38, 520, 150, 590)
                    canvas.drawBitmap(logo, null, dest, paint)
                    logoDrawn = true
                    logo.recycle()
                }
            }
        } catch (_: Exception) { }
    }
    if (!logoDrawn) {
        paint.color = red
        canvas.drawRoundRect(38f, 520f, 145f, 590f, 12f, 12f, paint)
        paint.color = white
        paint.textSize = 48f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("IK", 62f, 570f, paint)
    }

    paint.color = white
    paint.textSize = 34f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("INFOKINI NEWS", 180f, 548f, paint)
    paint.color = cyan
    paint.textSize = 22f
    paint.typeface = Typeface.DEFAULT
    canvas.drawText(dateLabel, 180f, 582f, paint)

    if (headline.isNotBlank()) {
        paint.color = red
        canvas.drawRect(0f, 610f, width.toFloat(), 675f, paint)
        paint.color = white
        paint.textSize = 34f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val safeHeadline = headline.uppercase().take(68)
        canvas.drawText(safeHeadline, 38f, 655f, paint)
    }

    if (showSports) {
        paint.color = android.graphics.Color.rgb(18, 59, 89)
        canvas.drawRect(760f, 515f, 1240f, 590f, paint)
        paint.color = white
        paint.textSize = 25f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val scoreText = "$homeName  $homeScore - $awayScore  $awayName".take(36)
        canvas.drawText(scoreText, 780f, 560f, paint)
    }

    paint.color = red
    canvas.drawRect(0f, 675f, width.toFloat(), 720f, paint)
    paint.color = white
    paint.textSize = 22f
    paint.typeface = Typeface.DEFAULT
    canvas.drawText(ticker.take(105), 30f, 706f, paint)
    return bitmap
}

@Composable
private fun CameraPreview(frontCamera: Boolean, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = context as LifecycleOwner
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val providerFuture = ProcessCameraProvider.getInstance(ctx)
            providerFuture.addListener({
                try {
                    val provider = providerFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    val selector = if (frontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                    provider.unbindAll()
                    provider.bindToLifecycle(lifecycleOwner, selector, preview)
                } catch (_: Exception) {
                    // Camera unavailable: keep the preview surface empty without crashing the studio.
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        update = { view ->
            val providerFuture = ProcessCameraProvider.getInstance(context)
            providerFuture.addListener({
                try {
                    val provider = providerFuture.get()
                    val preview = Preview.Builder().build().also { it.surfaceProvider = view.surfaceProvider }
                    val selector = if (frontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                    provider.unbindAll()
                    provider.bindToLifecycle(lifecycleOwner, selector, preview)
                } catch (_: Exception) { }
            }, ContextCompat.getMainExecutor(context))
        }
    )
}

@OptIn(UnstableApi::class)
@Composable
private fun VideoStudioScreen(
    headline: String,
    onHeadline: (String) -> Unit,
    ticker: String,
    onTicker: (String) -> Unit,
    templateStyle: String,
    onTemplateStyle: (String) -> Unit,
    showTemplateLibraryInitially: Boolean = false
) {
    val context = LocalContext.current
    var showTemplateLibrary by remember { mutableStateOf(showTemplateLibraryInitially) }
    var videoSegments by remember(context) {
        mutableStateOf(
            VideoDraftStore.load(context).map { saved ->
                VideoSegment(
                    uri = Uri.parse(saved.uri),
                    startSeconds = saved.startSeconds,
                    endSeconds = saved.endSeconds,
                    durationMs = saved.durationMs
                )
            }
        )
    }
    LaunchedEffect(videoSegments) {
        VideoDraftStore.save(
            context,
            videoSegments.map { segment ->
                SavedVideoClip(
                    uri = segment.uri.toString(),
                    startSeconds = segment.startSeconds,
                    endSeconds = segment.endSeconds,
                    durationMs = segment.durationMs
                )
            }
        )
    }
    var selectedClipIndex by remember { mutableStateOf(0) }
    val previewUri = videoSegments.getOrNull(selectedClipIndex)?.uri
    val previewStartMs = (videoSegments.getOrNull(selectedClipIndex)?.startSeconds?.toLongOrNull() ?: 0L).coerceAtLeast(0L) * 1000L
    val previewKey = "${previewUri}|${previewStartMs}"
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var showSports by remember { mutableStateOf(false) }
    var homeName by remember { mutableStateOf("TIM A") }
    var awayName by remember { mutableStateOf("TIM B") }
    var homeScore by remember { mutableStateOf(0) }
    var awayScore by remember { mutableStateOf(0) }
    var exportStatus by remember { mutableStateOf("Pilih video untuk mulai mengedit.") }
    var trimStartSeconds by remember { mutableStateOf("0") }
    var trimEndSeconds by remember { mutableStateOf("0") }
    var splitAtSeconds by remember { mutableStateOf("") }
    var reporterName by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { picked ->
        picked.forEach { uri ->
            try { context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) { }
        }
        videoSegments = picked.map { uri -> VideoSegment(uri = uri, durationMs = readVideoDurationMs(context, uri)) }
        selectedClipIndex = 0
        trimStartSeconds = "0"
        trimEndSeconds = "0"
        splitAtSeconds = ""
        exportStatus = if (picked.isEmpty()) "Tidak ada video yang dipilih." else "${picked.size} video berhasil dimasukkan ke timeline."
    }
    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { pickedLogo ->
        logoUri = pickedLogo
        if (pickedLogo != null) try { context.contentResolver.takePersistableUriPermission(pickedLogo, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) { }
    }
    val exportPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("video/mp4")) { destination ->
        val sources = videoSegments
        if (destination != null && sources.isNotEmpty()) {
            val edits = sources.map { segment ->
                val start = (segment.startSeconds.toLongOrNull() ?: 0L).coerceAtLeast(0L)
                val end = (segment.endSeconds.toLongOrNull() ?: 0L).coerceAtLeast(0L)
                VideoClipEdit(segment.uri, start * 1000L, end * 1000L)
            }
            val invalidIndex = sources.indexOfFirst { segment ->
                val start = segment.startSeconds.toLongOrNull() ?: 0L
                val end = segment.endSeconds.toLongOrNull() ?: 0L
                start < 0L || (end > 0L && end <= start) ||
                    (segment.durationMs > 0L && (start * 1000L >= segment.durationMs || (end > 0L && end * 1000L > segment.durationMs)))
            }
            if (invalidIndex >= 0) {
                val bad = sources[invalidIndex]
                exportStatus = "Rentang klip ${invalidIndex + 1} melewati durasi video atau waktu akhirnya tidak valid. Durasi: ${formatVideoDuration(bad.durationMs)}."
            } else {
                exportVideoWithNewsOverlay(
                    context, edits, destination, headline, ticker, logoUri, templateStyle,
                    reporterName, locationName,
                    if (showSports) "$homeName  $homeScore - $awayScore  $awayName" else null
                ) { exportStatus = it }
            }
        }
    }
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("VIDEO STUDIO", color = Cyan, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Text("Pilih klip, susun urutannya, tentukan gaya grafis berita, lalu ekspor menjadi MP4.", color = Muted, fontSize = 12.sp)
        Button(onClick = { picker.launch(arrayOf("video/*")) }, modifier = Modifier.fillMaxWidth()) { Text("Tambah video ke timeline") }
        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(14.dp)) {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("TEMPLATE DI DALAM EDITOR", color = Cyan, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        Text("Pilih desain, lalu langsung edit judul, video, logo, dan teks berjalan.", color = Muted, fontSize = 10.sp)
                    }
                    OutlinedButton(onClick = { showTemplateLibrary = !showTemplateLibrary }) { Text(if (showTemplateLibrary) "Tutup" else "Pilih template") }
                }
                Text("Desain aktif: $templateStyle", color = Gold, fontSize = 10.sp)
                if (showTemplateLibrary) {
                    val presets = listOf(
                        Triple("Breaking News", "BREAKING NEWS", "INFO TERKINI | PERKEMBANGAN BERITA TERBARU"),
                        Triple("Berita Daerah", "KABAR DAERAH", "INFO WILAYAH | FAKTA, DATA, DAN KONFIRMASI"),
                        Triple("Olahraga", "INFO OLAHRAGA", "HASIL PERTANDINGAN | SKOR DAN KABAR TERKINI"),
                        Triple("Wawancara", "DIALOG / WAWANCARA", "INFO KINI | SUARA DAN PENDAPAT NARASUMBER"),
                        Triple("Promosi UMKM", "PRODUK LOKAL UNGGULAN", "DUKUNG UMKM DAERAH | PRODUK ASLI INDONESIA")
                    )
                    presets.forEach { preset ->
                        val accent = when (preset.first) {
                            "Berita Daerah" -> Color(0xFF087D91)
                            "Olahraga" -> Color(0xFF17608A)
                            "Wawancara" -> Color(0xFF263D55)
                            "Promosi UMKM" -> Color(0xFF9B7020)
                            else -> Color(0xFFB51F2A)
                        }
                        Card(
                            onClick = {
                                onTemplateStyle(preset.first)
                                onHeadline(preset.second)
                                onTicker(preset.third)
                                showTemplateLibrary = false
                                exportStatus = "Template ${preset.first} diterapkan. Judul dan running text sudah dimasukkan; lanjutkan mengedit video."
                            },
                            colors = CardDefaults.cardColors(containerColor = if (templateStyle == preset.first) Color(0xFF173B5C) else Color(0xFF09182A)),
                            border = if (templateStyle == preset.first) androidx.compose.foundation.BorderStroke(2.dp, Cyan) else null,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(Modifier.fillMaxWidth().padding(9.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(preset.first.uppercase(), color = SoftWhite, fontWeight = FontWeight.Black, fontSize = 10.sp)
                                    Text(if (templateStyle == preset.first) "AKTIF" else "PAKAI DESAIN", color = Cyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(Modifier.fillMaxWidth().height(76.dp).background(Color(0xFF02070D)), contentAlignment = Alignment.BottomStart) {
                                    Column(Modifier.fillMaxWidth().background(Color(0xE6071426)).padding(6.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text("INFOKINI.NEWS  •  ${preset.first.uppercase()}", color = Cyan, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                        Text(preset.second, Modifier.fillMaxWidth().background(accent).padding(horizontal = 6.dp, vertical = 4.dp), color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp, maxLines = 1)
                                        Text(preset.third, color = Color.White, fontSize = 7.sp, maxLines = 1)
                                    }
                                }
                                Text("Ketuk untuk menerapkan ke proyek editor", color = Muted, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }
        if (videoSegments.isNotEmpty()) {
            Text("${videoSegments.size} segmen dalam daftar edit. Setiap segmen dapat dipotong dan diatur urutannya secara mandiri.", color = Cyan, fontSize = 11.sp)
            videoSegments.forEachIndexed { index, segment ->
                Card(colors = CardDefaults.cardColors(containerColor = if (selectedClipIndex == index) Color(0xFF173B5C) else Panel), shape = RoundedCornerShape(10.dp)) {
                    Row(Modifier.fillMaxWidth().padding(7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text("KLIP ${index + 1}", color = if (selectedClipIndex == index) Cyan else SoftWhite, fontWeight = FontWeight.Bold)
                            Text("Durasi: ${formatVideoDuration(segment.durationMs)}  •  ${segment.startSeconds}s–${if (segment.endSeconds == "0") "akhir" else segment.endSeconds + "s"}", color = Muted, fontSize = 9.sp)
                        }
                        OutlinedButton(onClick = {
                            selectedClipIndex = index
                            trimStartSeconds = segment.startSeconds
                            trimEndSeconds = segment.endSeconds
                            splitAtSeconds = ""
                        }) { Text("Pratinjau") }
                        OutlinedButton(onClick = { if (index > 0) { val list = videoSegments.toMutableList(); val item = list.removeAt(index); list.add(index - 1, item); videoSegments = list; selectedClipIndex = index - 1 } }, enabled = index > 0) { Text("↑") }
                        OutlinedButton(onClick = { if (index < videoSegments.lastIndex) { val list = videoSegments.toMutableList(); val item = list.removeAt(index); list.add(index + 1, item); videoSegments = list; selectedClipIndex = index + 1 } }, enabled = index < videoSegments.lastIndex) { Text("↓") }
                        OutlinedButton(onClick = {
                            val updated = videoSegments.toMutableList().also { it.removeAt(index) }
                            videoSegments = updated
                            selectedClipIndex = selectedClipIndex.coerceAtMost((updated.size - 1).coerceAtLeast(0))
                            val next = updated.getOrNull(selectedClipIndex)
                            trimStartSeconds = next?.startSeconds ?: "0"
                            trimEndSeconds = next?.endSeconds ?: "0"
                            splitAtSeconds = ""
                        }) { Text("Hapus") }
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(230.dp)) {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            tag = previewKey
                            setMediaController(MediaController(ctx).also { it.setAnchorView(this) })
                            setOnPreparedListener { player: MediaPlayer ->
                                player.isLooping = false
                                if (previewStartMs > 0L) seekTo(previewStartMs.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
                                start()
                                exportStatus = "Pratinjau segmen dimulai dari detik ${previewStartMs / 1000L}."
                            }
                            setOnErrorListener { _, what, extra ->
                                exportStatus = "Video tidak dapat diputar (kode $what/$extra). Pilih berkas video MP4 lain."
                                true
                            }
                            setVideoURI(previewUri)
                        }
                    },
                    update = { view ->
                        if (view.tag != previewKey) {
                            view.tag = previewKey
                            view.stopPlayback()
                            view.setOnPreparedListener { player: MediaPlayer ->
                                player.isLooping = false
                                if (previewStartMs > 0L) view.seekTo(previewStartMs.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
                                view.start()
                                exportStatus = "Pratinjau segmen dimulai dari detik ${previewStartMs / 1000L}."
                            }
                            view.setVideoURI(previewUri)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                val previewAccent = when (templateStyle) {
                    "Olahraga" -> Color(0xFF17608A)
                    "Promosi UMKM" -> Color(0xFF8B651C)
                    "Berita Daerah" -> Color(0xFF087D91)
                    "Wawancara" -> Color(0xFF263D55)
                    else -> Color(0xFFB51F2A)
                }
                Column(
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                        .background(Color(0xE6071426)).padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text("INFOKINI.NEWS  •  $templateStyle", color = Cyan, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text(headline.ifBlank { "JUDUL BERITA ANDA" }, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, maxLines = 2)
                    val bylinePreview = listOf(reporterName, locationName).filter { it.isNotBlank() }.joinToString("  •  ")
                    if (bylinePreview.isNotBlank()) Text(bylinePreview, color = Cyan, fontSize = 9.sp, maxLines = 1)
                    Text(ticker.ifBlank { "INFO KINI | BERPIKIR TAJAM, MENGUNGKAP KEBENARAN" }, Modifier.fillMaxWidth().background(previewAccent).padding(3.dp), color = Color.White, fontSize = 8.sp, maxLines = 1)
                }
            }
        } else {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF02070D)), shape = RoundedCornerShape(14.dp)) {
                Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                    Text("PRATINJAU VIDEO", color = Muted, fontWeight = FontWeight.Bold)
                }
            }
        }
        OutlinedTextField(value = headline, onValueChange = onHeadline, modifier = Modifier.fillMaxWidth(), label = { Text("Judul berita utama") })
        OutlinedTextField(value = reporterName, onValueChange = { reporterName = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Nama reporter / narasumber (opsional)") }, singleLine = true)
        OutlinedTextField(value = locationName, onValueChange = { locationName = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Lokasi berita (opsional)") }, singleLine = true)
        OutlinedTextField(value = ticker, onValueChange = onTicker, modifier = Modifier.fillMaxWidth(), label = { Text("Running text / teks berjalan") })
        Text("POTONG VIDEO (DETIK)", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = trimStartSeconds,
                onValueChange = {
                    trimStartSeconds = it.filter(Char::isDigit)
                    val current = videoSegments.getOrNull(selectedClipIndex)
                    if (current != null) videoSegments = videoSegments.toMutableList().also { it[selectedClipIndex] = current.copy(startSeconds = trimStartSeconds) }
                },
                modifier = Modifier.weight(1f),
                label = { Text("Mulai dari detik") },
                singleLine = true
            )
            OutlinedTextField(
                value = trimEndSeconds,
                onValueChange = {
                    trimEndSeconds = it.filter(Char::isDigit)
                    val current = videoSegments.getOrNull(selectedClipIndex)
                    if (current != null) videoSegments = videoSegments.toMutableList().also { it[selectedClipIndex] = current.copy(endSeconds = trimEndSeconds) }
                },
                modifier = Modifier.weight(1f),
                label = { Text("Sampai detik (0 = akhir)") },
                singleLine = true
            )
        }
        Text("Atur rentang waktu segmen terpilih. Waktu akhir 0 berarti sampai akhir video. Untuk memisahkan segmen, isi waktu akhir terlebih dahulu.", color = Muted, fontSize = 10.sp)
        OutlinedTextField(value = splitAtSeconds, onValueChange = { splitAtSeconds = it.filter(Char::isDigit) }, modifier = Modifier.fillMaxWidth(), label = { Text("Pisahkan pada detik") }, singleLine = true)
        OutlinedButton(
            onClick = {
                val current = videoSegments.getOrNull(selectedClipIndex)
                val start = current?.startSeconds?.toLongOrNull() ?: 0L
                val end = current?.endSeconds?.toLongOrNull() ?: 0L
                val split = splitAtSeconds.toLongOrNull()
                if (current == null) {
                    exportStatus = "Pilih segmen yang akan dipisah."
                } else if (end <= 0L) {
                    exportStatus = "Isi waktu akhir segmen terlebih dahulu agar titik pisah dapat dihitung."
                } else if (split == null || split <= start || split >= end) {
                    exportStatus = "Titik pisah harus berada di antara waktu mulai dan waktu akhir."
                } else {
                    val first = current.copy(endSeconds = split.toString())
                    val second = VideoSegment(uri = current.uri, startSeconds = split.toString(), endSeconds = end.toString())
                    videoSegments = videoSegments.toMutableList().also { it[selectedClipIndex] = first; it.add(selectedClipIndex + 1, second) }
                    trimStartSeconds = first.startSeconds
                    trimEndSeconds = first.endSeconds
                    splitAtSeconds = ""
                    exportStatus = "Segmen berhasil dipisah menjadi dua bagian independen."
                }
            },
            enabled = videoSegments.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Pisahkan klip pada titik tersebut") }
        OutlinedButton(onClick = { logoPicker.launch(arrayOf("image/*")) }, modifier = Modifier.fillMaxWidth()) {
            Text(if (logoUri == null) "Pilih logo kanal dari HP" else "Ganti logo kanal")
        }
        OutlinedButton(onClick = { showSports = !showSports }, modifier = Modifier.fillMaxWidth()) {
            Text(if (showSports) "Sembunyikan grafis olahraga" else "Tambahkan grafis olahraga")
        }
        if (showSports) {
            OutlinedTextField(value = homeName, onValueChange = { homeName = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Tim kandang") }, singleLine = true)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { if (homeScore > 0) homeScore-- }) { Text("−") }
                Text("$homeScore", color = SoftWhite, fontWeight = FontWeight.Bold)
                Button(onClick = { homeScore++ }) { Text("+ Gol") }
            }
            OutlinedTextField(value = awayName, onValueChange = { awayName = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Tim tandang") }, singleLine = true)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { if (awayScore > 0) awayScore-- }) { Text("−") }
                Text("$awayScore", color = SoftWhite, fontWeight = FontWeight.Bold)
                Button(onClick = { awayScore++ }) { Text("+ Gol") }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Grafis yang disiapkan", color = Cyan, fontWeight = FontWeight.Bold)
                Text(if (headline.isBlank()) "Judul berita akan tampil di sini" else headline, color = SoftWhite, fontWeight = FontWeight.Bold)
                Text(ticker, color = SoftWhite, fontSize = 11.sp)
            }
        }
        Button(
            onClick = { if (videoSegments.isNotEmpty()) exportPicker.launch("InfoKini-Berita.mp4") else exportStatus = "Pilih video terlebih dahulu." },
            modifier = Modifier.fillMaxWidth(),
            enabled = videoSegments.isNotEmpty()
        ) { Text("Ekspor MP4 dengan grafis berita") }
        Text(exportStatus, color = Cyan, fontSize = 11.sp)
        Text("Ekspor menggabungkan klip sesuai urutan pilihan, memakai rentang potong masing-masing klip, lalu membakar grafis InfoKini, logo, dan skor opsional. Uji pada perangkat tetap diperlukan.", color = Muted, fontSize = 11.sp)
    }
}

private data class VideoSegment(
    val id: String = java.util.UUID.randomUUID().toString(),
    val uri: Uri,
    val startSeconds: String = "0",
    val endSeconds: String = "0",
    val durationMs: Long = 0L
)

private fun readVideoDurationMs(context: android.content.Context, uri: Uri): Long {
    val retriever = android.media.MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, uri)
        retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
    } catch (_: Exception) {
        0L
    } finally {
        try { retriever.release() } catch (_: Exception) { }
    }
}

private fun formatVideoDuration(durationMs: Long): String {
    if (durationMs <= 0L) return "tidak diketahui"
    val totalSeconds = durationMs / 1000L
    return "%d:%02d".format(totalSeconds / 60L, totalSeconds % 60L)
}

private data class VideoClipEdit(
    val uri: Uri,
    val startPositionMs: Long,
    val endPositionMs: Long
)

@OptIn(UnstableApi::class)
private fun exportVideoWithNewsOverlay(
    context: android.content.Context,
    sources: List<VideoClipEdit>,
    destination: Uri,
    headline: String,
    ticker: String,
    logoUri: Uri?,
    templateStyle: String,
    reporterName: String,
    locationName: String,
    sportsScore: String?,
    onStatus: (String) -> Unit
) {
    try {
        val overlay = Bitmap.createBitmap(1280, 720, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(overlay)
        val panel = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.argb(220, 7, 20, 38) }
        val red = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb(190, 35, 48) }
        val white = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE; textSize = 30f; typeface = Typeface.create("sans-serif", Typeface.BOLD) }
        val small = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.LTGRAY; textSize = 20f }
        val dateText = "INFOKINI.NEWS  •  " + java.text.SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(java.util.Date())
        val safeHeadline = headline.ifBlank { "INFO TERKINI" }.take(62)
        val safeTicker = ticker.ifBlank { "INFO KINI | BERPIKIR TAJAM, MENGUNGKAP KEBENARAN" }.take(105)
        val byline = listOf(reporterName.trim(), locationName.trim()).filter { it.isNotBlank() }.joinToString("  •  ").take(72)
        val bylinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb(39, 197, 232); textSize = 21f; typeface = Typeface.create("sans-serif", Typeface.BOLD) }
        when (templateStyle) {
            "Berita Daerah" -> {
                canvas.drawRect(0f, 565f, 1280f, 720f, panel)
                canvas.drawRect(0f, 565f, 14f, 720f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb(19, 189, 235) })
                canvas.drawText(dateText, 38f, 603f, small)
                canvas.drawText(safeHeadline, 38f, 646f, white)
                if (byline.isNotBlank()) canvas.drawText(byline, 38f, 675f, bylinePaint)
                canvas.drawText(safeTicker, 38f, 705f, small)
            }
            "Olahraga" -> {
                canvas.drawRect(0f, 570f, 1280f, 720f, panel)
                canvas.drawRect(0f, 570f, 1280f, 610f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb(18, 59, 89) })
                canvas.drawText("INFOKINI SPORT  •  $dateText", 34f, 598f, small)
                canvas.drawText(safeHeadline, 34f, 650f, white)
                if (byline.isNotBlank()) canvas.drawText(byline, 34f, 678f, bylinePaint)
                canvas.drawText(safeTicker, 34f, 705f, small)
            }
            "Wawancara" -> {
                canvas.drawRect(0f, 590f, 1280f, 720f, panel)
                canvas.drawRect(0f, 590f, 1280f, 600f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb(19, 189, 235) })
                canvas.drawText(dateText, 38f, 630f, small)
                canvas.drawText(safeHeadline, 38f, 660f, white)
                if (byline.isNotBlank()) canvas.drawText(byline, 38f, 685f, bylinePaint)
                canvas.drawText(safeTicker, 38f, 710f, small)
            }
            "Promosi UMKM" -> {
                canvas.drawRect(0f, 570f, 1280f, 720f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.argb(225, 28, 24, 12) })
                canvas.drawRect(0f, 570f, 1280f, 580f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb(218, 164, 55) })
                canvas.drawText("INFO KINI  •  PROMOSI UMKM", 34f, 610f, small)
                canvas.drawText(safeHeadline, 34f, 650f, white)
                if (byline.isNotBlank()) canvas.drawText(byline, 34f, 678f, bylinePaint)
                canvas.drawText(safeTicker, 34f, 705f, small)
            }
            else -> {
                canvas.drawRect(0f, 570f, 1280f, 720f, panel)
                canvas.drawRect(0f, 625f, 1280f, 680f, red)
                canvas.drawText(dateText, 34f, 610f, small)
                canvas.drawText(safeHeadline, 34f, 650f, white)
                if (byline.isNotBlank()) canvas.drawText(byline, 34f, 678f, bylinePaint)
                canvas.drawText(safeTicker, 34f, 705f, small)
            }
        }
        if (!sportsScore.isNullOrBlank()) {
            val sportPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                textSize = 27f
                typeface = Typeface.create("sans-serif", Typeface.BOLD)
            }
            canvas.drawRoundRect(760f, 500f, 1250f, 555f, 12f, 12f, panel)
            canvas.drawText(sportsScore.take(34), 780f, 537f, sportPaint)
        }
        if (logoUri != null) {
            try {
                context.contentResolver.openInputStream(logoUri)?.use { stream ->
                    val logo = android.graphics.BitmapFactory.decodeStream(stream)
                    if (logo != null) {
                        val maxW = 150f
                        val maxH = 85f
                        val scale = minOf(maxW / logo.width, maxH / logo.height)
                        val w = logo.width * scale
                        val h = logo.height * scale
                        val dst = android.graphics.RectF(1080f - w, 475f - h, 1080f, 475f)
                        canvas.drawBitmap(logo, null, dst, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
                        logo.recycle()
                    }
                }
            } catch (_: Exception) { }
        }
        val bitmapOverlay = BitmapOverlay.createStaticBitmapOverlay(overlay)
        val effects = Effects(emptyList(), listOf(OverlayEffect(listOf(bitmapOverlay))))
        val editedItems = sources.map { clip ->
            val clipStartMs = clip.startPositionMs.coerceAtLeast(0L)
            val clipEndMs = if (clip.endPositionMs > clipStartMs) clip.endPositionMs else C.TIME_END_OF_SOURCE
            val clipConfiguration = MediaItem.ClippingConfiguration.Builder()
                .setStartPositionMs(clipStartMs)
                .setEndPositionMs(clipEndMs)
                .build()
            val clipMediaItem = MediaItem.Builder()
                .setUri(clip.uri)
                .setClippingConfiguration(clipConfiguration)
                .build()
            EditedMediaItem.Builder(clipMediaItem).setEffects(effects).build()
        }
        if (editedItems.isEmpty()) throw IllegalArgumentException("Pilih minimal satu klip video.")
        val sequence = EditedMediaItemSequence.Builder(editedItems).build()
        val composition = Composition.Builder(listOf(sequence)).build()
        val outputFile = File(context.cacheDir, "infokini-export-${System.currentTimeMillis()}.mp4")
        val transformer = Transformer.Builder(context)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    try {
                        context.contentResolver.openOutputStream(destination, "w")?.use { output ->
                            outputFile.inputStream().use { input -> input.copyTo(output) }
                        } ?: throw IllegalStateException("Tidak dapat membuka lokasi penyimpanan.")
                        outputFile.delete()
                        onStatus("Ekspor selesai. Video MP4 dengan grafis InfoKini sudah disimpan.")
                    } catch (error: Exception) {
                        onStatus("Video selesai diproses, tetapi gagal disimpan: ${error.message}")
                    }
                }
                override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {
                    outputFile.delete()
                    onStatus("Ekspor gagal: ${exportException.message}")
                }
            })
            .build()
        onStatus("Sedang merender video dan menempelkan grafis InfoKini…")
        transformer.start(composition, outputFile.absolutePath)
    } catch (error: Exception) {
        onStatus("Tidak dapat memulai ekspor: ${error.message}")
    }
}

@Composable
private fun PhotoStudioScreen() {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var caption by remember { mutableStateOf("INFO KINI") }
    var subheading by remember { mutableStateOf("BERPIKIR TAJAM, MENGUNGKAP KEBENARAN") }
    var format by remember { mutableStateOf("4:5") }
    var photoFilter by remember { mutableStateOf("Asli") }
    var status by remember { mutableStateOf("Pilih foto untuk membuat poster berita.") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { imageUri = it }
    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { logoUri = it }
    val exportPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { destination ->
        val source = imageUri
        if (source != null && destination != null) exportNewsPoster(context, source, destination, caption, subheading, logoUri, format, photoFilter) { status = it }
    }
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("PHOTO STUDIO", color = Cyan, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Text("Buat poster berita InfoKini dari foto galeri dengan judul, subjudul, logo, dan format media sosial.", color = Muted, fontSize = 12.sp)
        Button(onClick = { picker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) { Text(if (imageUri == null) "Pilih foto dari HP" else "Ganti foto") }
        if (imageUri != null) {
            AndroidView(
                factory = { ctx -> ImageView(ctx).apply { scaleType = ImageView.ScaleType.FIT_CENTER; setImageURI(imageUri) } },
                update = { it.setImageURI(imageUri) },
                modifier = Modifier.fillMaxWidth().height(230.dp)
            )
        } else {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF02070D)), shape = RoundedCornerShape(14.dp)) {
                Box(Modifier.fillMaxWidth().height(190.dp), contentAlignment = Alignment.Center) { Text("PRATINJAU FOTO", color = Muted) }
            }
        }
        Text("FORMAT POSTER", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("4:5", "1:1", "16:9").forEach { item ->
                if (format == item) Button(onClick = { format = item }, modifier = Modifier.weight(1f)) { Text(item) }
                else OutlinedButton(onClick = { format = item }, modifier = Modifier.weight(1f)) { Text(item) }
            }
        }
        Text("FILTER FOTO", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Asli", "Hitam Putih", "Sepia", "Vivid").forEach { item ->
                if (photoFilter == item) Button(onClick = { photoFilter = item }, modifier = Modifier.weight(1f)) {
                    Text(item, fontSize = 9.sp)
                } else OutlinedButton(onClick = { photoFilter = item }, modifier = Modifier.weight(1f)) {
                    Text(item, fontSize = 9.sp)
                }
            }
        }
        OutlinedTextField(value = caption, onValueChange = { caption = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Judul utama poster") })
        OutlinedTextField(value = subheading, onValueChange = { subheading = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Subjudul / keterangan") })
        OutlinedButton(onClick = { logoPicker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) { Text(if (logoUri == null) "Tambahkan logo kanal (opsional)" else "Ganti logo kanal") }
        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(14.dp)) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("CONTOH IDENTITAS POSTER", color = Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("INFOKINI.NEWS", color = SoftWhite, fontWeight = FontWeight.Black, fontSize = 13.sp)
                Text(caption.ifBlank { "Judul berita akan tampil di sini" }, color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                Text(subheading, color = Muted, fontSize = 11.sp)
                Text("Format $format • tanggal otomatis saat ekspor", color = Gold, fontSize = 10.sp)
            }
        }
        Button(onClick = { if (imageUri != null) exportPicker.launch("InfoKini-Poster.png") else status = "Pilih foto terlebih dahulu." }, modifier = Modifier.fillMaxWidth(), enabled = imageUri != null) { Text("Ekspor poster PNG") }
        Text(status, color = Cyan, fontSize = 11.sp)
        Text("Ekspor menghasilkan PNG dengan pemotongan otomatis sesuai rasio, judul, subjudul, tanggal, identitas InfoKini, logo opsional, dan filter warna dasar. Layer bebas serta penghapus latar belum tersedia.", color = Muted, fontSize = 11.sp)
    }
}

private fun exportNewsPoster(
    context: android.content.Context, source: Uri, destination: Uri,
    headline: String, subheading: String, logoUri: Uri?, format: String, photoFilter: String, onStatus: (String) -> Unit
) {
    var sourceBitmap: Bitmap? = null
    var outputBitmap: Bitmap? = null
    try {
        val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(source)?.use { android.graphics.BitmapFactory.decodeStream(it, null, bounds) }
            ?: throw IllegalStateException("Foto sumber tidak dapat dibuka.")
        var sample = 1
        while (bounds.outWidth / sample > 1800 || bounds.outHeight / sample > 1800) sample *= 2
        val options = android.graphics.BitmapFactory.Options().apply { inSampleSize = sample; inPreferredConfig = Bitmap.Config.ARGB_8888 }
        sourceBitmap = context.contentResolver.openInputStream(source)?.use { android.graphics.BitmapFactory.decodeStream(it, null, options) }
            ?: throw IllegalStateException("Gagal membaca foto.")
        val ratio = when (format) { "1:1" -> 1f; "16:9" -> 16f / 9f; else -> 4f / 5f }
        val width = 1080
        val height = (width / ratio).toInt()
        val posterBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        outputBitmap = posterBitmap
        val canvas = Canvas(posterBitmap)
        val src = sourceBitmap ?: throw IllegalStateException("Foto sumber tidak dapat dibaca.")
        val scale = maxOf(width.toFloat() / src.width, height.toFloat() / src.height)
        val scaledWidth = src.width * scale
        val scaledHeight = src.height * scale
        val left = (width - scaledWidth) / 2f
        val top = (height - scaledHeight) / 2f
        val photoPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        photoPaint.colorFilter = when (photoFilter) {
            "Hitam Putih" -> ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
            "Sepia" -> ColorMatrixColorFilter(ColorMatrix(floatArrayOf(
                0.393f, 0.769f, 0.189f, 0f, 0f,
                0.349f, 0.686f, 0.168f, 0f, 0f,
                0.272f, 0.534f, 0.131f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )))
            "Vivid" -> ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(1.35f) })
            else -> null
        }
        canvas.drawBitmap(src, null, android.graphics.RectF(left, top, left + scaledWidth, top + scaledHeight), photoPaint)
        val shade = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.argb(210, 7, 20, 38) }
        val red = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb(190, 35, 48) }
        val white = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE; textSize = width * 0.052f; typeface = Typeface.create("sans-serif", Typeface.BOLD) }
        val muted = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.LTGRAY; textSize = width * 0.026f }
        val panelTop = height * 0.67f
        canvas.drawRect(0f, panelTop, width.toFloat(), height.toFloat(), shade)
        canvas.drawRect(0f, panelTop, width.toFloat(), panelTop + height * 0.012f, red)
        canvas.drawText("INFOKINI.NEWS", width * 0.055f, panelTop + height * 0.075f, muted)
        val date = java.text.SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(java.util.Date())
        canvas.drawText(date, width * 0.055f, panelTop + height * 0.112f, muted)
        val lines = headline.ifBlank { "INFO TERKINI" }.take(90).chunked(30).take(3)
        var baseline = panelTop + height * 0.19f
        lines.forEach { line -> canvas.drawText(line, width * 0.055f, baseline, white); baseline += height * 0.065f }
        canvas.drawText(subheading.ifBlank { "BERPIKIR TAJAM, MENGUNGKAP KEBENARAN" }.take(72), width * 0.055f, height * 0.94f, muted)
        if (logoUri != null) {
            try {
                context.contentResolver.openInputStream(logoUri)?.use { stream ->
                    val logo = android.graphics.BitmapFactory.decodeStream(stream)
                    if (logo != null) {
                        val maxW = width * 0.20f; val maxH = height * 0.10f
                        val logoScale = minOf(maxW / logo.width, maxH / logo.height)
                        val w = logo.width * logoScale; val h = logo.height * logoScale
                        canvas.drawBitmap(logo, null, android.graphics.RectF(width - w - width * 0.05f, height * 0.05f, width - width * 0.05f, height * 0.05f + h), Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
                        logo.recycle()
                    }
                }
            } catch (_: Exception) { }
        }
        context.contentResolver.openOutputStream(destination, "w")?.use { output ->
            if (!posterBitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) throw IllegalStateException("Gagal menyimpan PNG.")
        } ?: throw IllegalStateException("Lokasi penyimpanan tidak dapat dibuka.")
        onStatus("Poster berhasil diekspor sebagai PNG.")
    } catch (error: Exception) {
        onStatus("Ekspor poster gagal: ${error.message}")
    } finally {
        sourceBitmap?.recycle()
        outputBitmap?.recycle()
    }
}
@Composable
private fun ModuleWorkspaceScreen(module: String) {
    val context = LocalContext.current
    val prefs = remember(module) { context.getSharedPreferences("infokini_module_" + module.replace(" ", "_"), 0) }
    var title by remember(module) { mutableStateOf(prefs.getString("title", "").orEmpty()) }
    var notes by remember(module) { mutableStateOf(prefs.getString("notes", "").orEmpty()) }
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(module.uppercase(), color = Cyan, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Text("Ruang kerja $module. Catatan yang Anda simpan akan tersimpan di perangkat ini.", color = Muted, fontSize = 12.sp)
        OutlinedTextField(value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Judul / nama pekerjaan") })
        OutlinedTextField(value = notes, onValueChange = { notes = it }, modifier = Modifier.fillMaxWidth().height(180.dp), label = { Text("Catatan / materi kerja") })
        Button(onClick = {
            prefs.edit().putString("title", title).putString("notes", notes).apply()
        }, modifier = Modifier.fillMaxWidth()) { Text("Simpan pekerjaan") }
        Text("Status: ruang kerja dasar dengan penyimpanan lokal. Integrasi cloud, kolaborasi, dan fungsi khusus modul ini belum aktif.", color = Muted, fontSize = 11.sp)
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
    val prefs = remember(context) { context.getSharedPreferences("infokini_tts_v2", 0) }
    var script by remember { mutableStateOf(prefs.getString("script", "").orEmpty()) }
    var engine by remember { mutableStateOf<TextToSpeech?>(null) }
    var engineReady by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Menyiapkan mesin suara perangkat…") }

    LaunchedEffect(script) {
        prefs.edit().putString("script", script).apply()
    }

    DisposableEffect(context) {
        val tts = TextToSpeech(context) { result ->
            engineReady = result == TextToSpeech.SUCCESS
            status = if (engineReady) {
                "Mesin TTS siap; ketersediaan suara Indonesia akan diperiksa saat mulai membaca"
            } else {
                "Mesin TTS tidak dapat diinisialisasi"
            }
        }
        engine = tts
        onDispose {
            tts.stop()
            tts.shutdown()
            engine = null
            engineReady = false
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("NARASI / TEXT TO SPEECH", color = Cyan, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Text("Ubah naskah menjadi suara menggunakan mesin TTS yang tersedia di perangkat Android.", color = Muted, fontSize = 12.sp)
        OutlinedTextField(value = script, onValueChange = { script = it }, modifier = Modifier.fillMaxWidth().weight(1f), label = { Text("Tempel naskah berita di sini") })
        Text(status, color = Muted, fontSize = 11.sp)
        Button(onClick = {
            val tts = engine
            if (tts != null && engineReady && script.isNotBlank()) {
                val result = tts.setLanguage(Locale("id", "ID"))
                if (result >= TextToSpeech.LANG_AVAILABLE) {
                    tts.speak(script, TextToSpeech.QUEUE_FLUSH, null, "infokini-narration")
                    status = "Sedang membacakan naskah"
                } else {
                    status = "Paket suara Bahasa Indonesia belum tersedia di perangkat"
                }
            } else {
                status = when {
                    script.isBlank() -> "Masukkan naskah terlebih dahulu"
                    !engineReady -> "Mesin suara masih disiapkan. Coba lagi sebentar."
                    else -> "Mesin suara belum siap"
                }
            }
        }, modifier = Modifier.fillMaxWidth(), enabled = engineReady && script.isNotBlank()) { Text("Bacakan naskah") }
        OutlinedButton(onClick = { engine?.stop(); status = "Pembacaan dihentikan" }, modifier = Modifier.fillMaxWidth()) { Text("Hentikan suara") }
        Text("Catatan: fitur ini memutar suara melalui TTS Android. Ekspor audio menjadi MP3/WAV belum tersedia.", color = Muted, fontSize = 11.sp)
    }
}


@Composable
private fun SportsScreen() {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("infokini_sports_v2", 0) }
    var homeName by remember { mutableStateOf(prefs.getString("home_name", "TIM A").orEmpty()) }
    var awayName by remember { mutableStateOf(prefs.getString("away_name", "TIM B").orEmpty()) }
    var homeScore by remember { mutableStateOf(prefs.getInt("home_score", 0)) }
    var awayScore by remember { mutableStateOf(prefs.getInt("away_score", 0)) }
    var period by remember { mutableStateOf(prefs.getString("period", "BABAK 1").orEmpty()) }
    var elapsedSeconds by remember { mutableStateOf(prefs.getInt("elapsed_seconds", 0)) }
    var timerRunning by remember { mutableStateOf(false) }
    var homeYellow by remember { mutableStateOf(prefs.getInt("home_yellow", 0)) }
    var awayYellow by remember { mutableStateOf(prefs.getInt("away_yellow", 0)) }
    var homeRed by remember { mutableStateOf(prefs.getInt("home_red", 0)) }
    var awayRed by remember { mutableStateOf(prefs.getInt("away_red", 0)) }

    androidx.compose.runtime.LaunchedEffect(timerRunning) {
        while (timerRunning) {
            kotlinx.coroutines.delay(1000)
            elapsedSeconds++
        }
    }

    androidx.compose.runtime.LaunchedEffect(homeName, awayName, homeScore, awayScore, period, elapsedSeconds, homeYellow, awayYellow, homeRed, awayRed) {
        prefs.edit()
            .putString("home_name", homeName)
            .putString("away_name", awayName)
            .putInt("home_score", homeScore)
            .putInt("away_score", awayScore)
            .putString("period", period)
            .putInt("elapsed_seconds", elapsedSeconds)
            .putInt("home_yellow", homeYellow).putInt("away_yellow", awayYellow)
            .putInt("home_red", homeRed).putInt("away_red", awayRed)
            .apply()
    }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("GRAFIS PERTANDINGAN", color = Cyan, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(14.dp)) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val clockText = "%02d:%02d".format(elapsedSeconds / 60, elapsedSeconds % 60)
                Text("WAKTU PERTANDINGAN", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text(clockText, color = SoftWhite, fontWeight = FontWeight.Black, fontSize = 34.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { timerRunning = !timerRunning }) { Text(if (timerRunning) "Jeda" else "Mulai") }
                    OutlinedButton(onClick = { timerRunning = false; elapsedSeconds = 0 }) { Text("Reset waktu") }
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF02070D)), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("INFOKINI SPORT  •  $period", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(homeName.uppercase(), color = SoftWhite, fontWeight = FontWeight.Bold)
                        Text(homeScore.toString(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 44.sp)
                        Row {
                            OutlinedButton(onClick = { if (homeScore > 0) homeScore-- }) { Text("−") }
                            Button(onClick = { homeScore++ }) { Text("+ Gol") }
                        }
                    }
                    Text("VS", color = Cyan, fontWeight = FontWeight.Black)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(awayName.uppercase(), color = SoftWhite, fontWeight = FontWeight.Bold)
                        Text(awayScore.toString(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 44.sp)
                        Row {
                            OutlinedButton(onClick = { if (awayScore > 0) awayScore-- }) { Text("−") }
                            Button(onClick = { awayScore++ }) { Text("+ Gol") }
                        }
                    }
                }
            }
        }
        OutlinedTextField(value = homeName, onValueChange = { homeName = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Nama tim kandang") }, singleLine = true)
        OutlinedTextField(value = awayName, onValueChange = { awayName = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Nama tim tandang") }, singleLine = true)
        OutlinedTextField(value = period, onValueChange = { period = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Babak / status pertandingan") }, singleLine = true)
        Text("KARTU PERTANDINGAN", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(homeName, color = SoftWhite, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🟨 $homeYellow", color = SoftWhite)
                    Spacer(Modifier.weight(1f))
                    OutlinedButton(onClick = { homeYellow++ }) { Text("+") }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🟥 $homeRed", color = SoftWhite)
                    Spacer(Modifier.weight(1f))
                    OutlinedButton(onClick = { homeRed++ }) { Text("+") }
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(awayName, color = SoftWhite, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🟨 $awayYellow", color = SoftWhite)
                    Spacer(Modifier.weight(1f))
                    OutlinedButton(onClick = { awayYellow++ }) { Text("+") }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🟥 $awayRed", color = SoftWhite)
                    Spacer(Modifier.weight(1f))
                    OutlinedButton(onClick = { awayRed++ }) { Text("+") }
                }
            }
        }
        OutlinedButton(onClick = {
            homeScore = 0; awayScore = 0; period = "BABAK 1"
            elapsedSeconds = 0; timerRunning = false
            homeYellow = 0; awayYellow = 0; homeRed = 0; awayRed = 0
        }, modifier = Modifier.fillMaxWidth()) { Text("Reset seluruh pertandingan") }
        Text("Nama tim, skor, waktu pertandingan, babak, dan kartu tersimpan otomatis di perangkat. Timer dan kartu masih manual; grafis ini belum masuk ke encoder streaming.", color = Muted, fontSize = 11.sp)
    }
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
private fun TemplateScreen(onApply: (String, String, String) -> Unit) {
    val presets = listOf(
        Triple("Breaking News", "BREAKING NEWS", "INFO TERKINI | PERKEMBANGAN BERITA TERBARU"),
        Triple("Berita Daerah", "KABAR DAERAH", "INFO WILAYAH | FAKTA, DATA, DAN KONFIRMASI"),
        Triple("Olahraga", "INFO OLAHRAGA", "HASIL PERTANDINGAN | SKOR DAN KABAR TERKINI"),
        Triple("Wawancara", "DIALOG / WAWANCARA", "INFO KINI | SUARA DAN PENDAPAT NARASUMBER"),
        Triple("Promosi UMKM", "PRODUK LOKAL UNGGULAN", "DUKUNG UMKM DAERAH | PRODUK ASLI INDONESIA")
    )
    var chosen by remember { mutableStateOf(0) }
    Column(
        Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("TEMPLATE BERITA INFOKINI", color = Cyan, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Text("Pilih paket judul dan running text. Setelah diterapkan, Anda akan dibawa ke Video Studio untuk melanjutkan penyuntingan.", color = Muted, fontSize = 12.sp)
        presets.forEachIndexed { index, preset ->
            Card(
                onClick = { chosen = index },
                colors = CardDefaults.cardColors(containerColor = if (chosen == index) Color(0xFF173B5C) else Panel),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val accent = when (preset.first) {
                        "Berita Daerah" -> Color(0xFF13BDEB)
                        "Olahraga" -> Color(0xFF176B45)
                        "Wawancara" -> Color(0xFF13BDEB)
                        "Promosi UMKM" -> Color(0xFFD6A437)
                        else -> Color(0xFFB51F2A)
                    }
                    Box(
                        Modifier.fillMaxWidth().height(126.dp).background(Color(0xFF071426)),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Column(
                            Modifier.fillMaxWidth().background(Color(0xDD071426)).padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("INFOKINI.NEWS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp)
                                Text(preset.first.uppercase(), color = Cyan, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                            }
                            Text(
                                preset.second,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                modifier = Modifier.fillMaxWidth().background(accent).padding(horizontal = 7.dp, vertical = 5.dp)
                            )
                            Text(preset.third, color = Color.White, fontSize = 9.sp, maxLines = 1)
                        }
                    }
                    Text(preset.first, color = if (chosen == index) Cyan else SoftWhite, fontWeight = FontWeight.Bold)
                    Text("Paket grafis siap diterapkan ke Video Studio", color = Muted, fontSize = 10.sp)
                }
            }
        }
        Button(onClick = { onApply(presets[chosen].first, presets[chosen].second, presets[chosen].third) }, modifier = Modifier.fillMaxWidth()) {
            Text("Gunakan template di Video Studio")
        }
        Text("Template ini mengisi judul dan running text awal. Logo, gambar, warna lanjutan, dan animasi dapat disesuaikan pada tahap pengembangan berikutnya.", color = Muted, fontSize = 11.sp)
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
