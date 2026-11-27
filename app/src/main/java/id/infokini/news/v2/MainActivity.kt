package id.infokini.news.v2

import android.os.Bundle
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.media3.common.MediaItem
import androidx.media3.common.Effects
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.EditedMediaItem
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
                        "Video" -> VideoStudioScreen()
                        "Foto" -> PhotoStudioScreen()
                        "Redaktur", "AI Redaktur" -> EditorialScreen()
                        "Narasi / TTS" -> TtsScreen()
                        "Grafis Olahraga", "Berita Olahraga" -> SportsScreen()
                        "Lainnya" -> MoreScreen(onOpen = { selected = it })
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
    var cameraGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var frontCamera by remember { mutableStateOf(false) }
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var showSports by remember { mutableStateOf(false) }
    var homeName by remember { mutableStateOf("TIM A") }
    var awayName by remember { mutableStateOf("TIM B") }
    var homeScore by remember { mutableStateOf(0) }
    var awayScore by remember { mutableStateOf(0) }
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        cameraGranted = granted
    }
    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { logoUri = it }
    val dateLabel = java.text.SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(java.util.Date())

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("LIVE STUDIO", color = Cyan, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Text("KAMERA DAN GRAFIS SIARAN", color = Muted, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        if (cameraGranted) {
            Box(Modifier.fillMaxWidth().height(235.dp)) {
                CameraPreview(frontCamera = frontCamera, modifier = Modifier.fillMaxSize())
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
                OutlinedButton(onClick = { frontCamera = !frontCamera }, modifier = Modifier.weight(1f)) {
                    Text(if (frontCamera) "Kamera belakang" else "Kamera depan")
                }
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
private fun VideoStudioScreen() {
    val context = LocalContext.current
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    var headline by remember { mutableStateOf("") }
    var ticker by remember { mutableStateOf("INFO KINI | BERPIKIR TAJAM, MENGUNGKAP KEBENARAN") }
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var showSports by remember { mutableStateOf(false) }
    var homeName by remember { mutableStateOf("TIM A") }
    var awayName by remember { mutableStateOf("TIM B") }
    var homeScore by remember { mutableStateOf(0) }
    var awayScore by remember { mutableStateOf(0) }
    var exportStatus by remember { mutableStateOf("Pilih video untuk mulai mengedit.") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { videoUri = it }
    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { logoUri = it }
    val exportPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("video/mp4")) { destination ->
        val source = videoUri
        if (destination != null && source != null) {
            exportVideoWithNewsOverlay(context, source, destination, headline, ticker, logoUri, if (showSports) "$homeName  $homeScore - $awayScore  $awayName" else null) { exportStatus = it }
        }
    }
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("VIDEO STUDIO", color = Cyan, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Text("Pilih video dari perangkat untuk dipratinjau dan siapkan teks grafis berita.", color = Muted, fontSize = 12.sp)
        Button(onClick = { picker.launch("video/*") }, modifier = Modifier.fillMaxWidth()) { Text("Pilih video dari HP") }
        if (videoUri != null) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        setVideoURI(videoUri)
                        setMediaController(MediaController(ctx).also { it.setAnchorView(this) })
                    }
                },
                update = { view ->
                    if (view.tag != videoUri.toString()) {
                        view.tag = videoUri.toString()
                        view.setVideoURI(videoUri)
                        view.setMediaController(MediaController(context).also { it.setAnchorView(view) })
                    }
                },
                modifier = Modifier.fillMaxWidth().height(220.dp)
            )
        } else {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF02070D)), shape = RoundedCornerShape(14.dp)) {
                Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                    Text("PRATINJAU VIDEO", color = Muted, fontWeight = FontWeight.Bold)
                }
            }
        }
        OutlinedTextField(value = headline, onValueChange = { headline = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Judul / lower third") })
        OutlinedTextField(value = ticker, onValueChange = { ticker = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Running text") })
        OutlinedButton(onClick = { logoPicker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
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
            onClick = { if (videoUri != null) exportPicker.launch("InfoKini-Berita.mp4") else exportStatus = "Pilih video terlebih dahulu." },
            modifier = Modifier.fillMaxWidth(),
            enabled = videoUri != null
        ) { Text("Ekspor MP4 dengan grafis berita") }
        Text(exportStatus, color = Cyan, fontSize = 11.sp)
        Text("Ekspor membakar identitas kanal, tanggal, headline, running text, logo pilihan, dan papan skor opsional ke video. Timeline potong/gabung klip masih tahap berikutnya.", color = Muted, fontSize = 11.sp)
    }
}

@OptIn(UnstableApi::class)
private fun exportVideoWithNewsOverlay(
    context: android.content.Context,
    source: Uri,
    destination: Uri,
    headline: String,
    ticker: String,
    logoUri: Uri?,
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
        canvas.drawRect(0f, 570f, 1280f, 720f, panel)
        canvas.drawRect(0f, 625f, 1280f, 680f, red)
        canvas.drawText("INFOKINI NEWS  •  " + java.text.SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(java.util.Date()), 34f, 610f, small)
        val safeHeadline = headline.ifBlank { "INFO TERKINI" }.take(62)
        canvas.drawText(safeHeadline, 34f, 662f, white)
        canvas.drawText(ticker.ifBlank { "INFO KINI | BERPIKIR TAJAM, MENGUNGKAP KEBENARAN" }.take(105), 34f, 705f, small)
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
        val editedItem = EditedMediaItem.Builder(MediaItem.fromUri(source)).setEffects(effects).build()
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
        transformer.start(editedItem, outputFile.absolutePath)
    } catch (error: Exception) {
        onStatus("Tidak dapat memulai ekspor: ${error.message}")
    }
}

@Composable
private fun PhotoStudioScreen() {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { imageUri = it }
    var caption by remember { mutableStateOf("INFO KINI") }
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("PHOTO STUDIO", color = Cyan, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Text("Pilih foto untuk pratinjau dan siapkan teks poster berita.", color = Muted, fontSize = 12.sp)
        Button(onClick = { picker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) { Text("Pilih foto dari HP") }
        if (imageUri != null) {
            AndroidView(
                factory = { ctx -> ImageView(ctx).apply { scaleType = ImageView.ScaleType.FIT_CENTER; setImageURI(imageUri) } },
                update = { it.setImageURI(imageUri) },
                modifier = Modifier.fillMaxWidth().height(260.dp)
            )
        } else {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF02070D)), shape = RoundedCornerShape(14.dp)) {
                Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) { Text("PRATINJAU FOTO", color = Muted) }
            }
        }
        OutlinedTextField(value = caption, onValueChange = { caption = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Judul poster / watermark") })
        Text("INFO KINI", color = Cyan, fontWeight = FontWeight.Black)
        Text(caption, color = SoftWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Yang sudah berfungsi: memilih dan melihat foto serta menyiapkan teks poster. Crop, filter, penghapus latar, layer desain, dan ekspor gambar belum tersedia.", color = Muted, fontSize = 11.sp)
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
    var script by remember { mutableStateOf("") }
    var engine by remember { mutableStateOf<TextToSpeech?>(null) }
    var status by remember { mutableStateOf("Menyiapkan mesin suara perangkat…") }

    DisposableEffect(context) {
        val tts = TextToSpeech(context) { result ->
            if (result == TextToSpeech.SUCCESS) {
                status = "Mesin TTS siap; ketersediaan suara Indonesia akan diperiksa saat mulai membaca"
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
