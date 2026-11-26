package id.infokini.news

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.RectF
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.speech.tts.TextToSpeech
import android.widget.Toast
import android.widget.ImageView
import android.widget.VideoView
import android.media.MediaMetadataRetriever
import java.io.File
import androidx.media3.common.MediaItem
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import java.util.Locale

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
    val context = LocalContext.current
    val tts = remember { TextToSpeech(context) { } }
    DisposableEffect(tts) { onDispose { tts.stop(); tts.shutdown() } }
    var headline by remember { mutableStateOf("") }
    var ticker by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedMedia by remember { mutableStateOf<Uri?>(null) }
    var mediaLabel by remember { mutableStateOf("Belum ada foto atau video dipilih") }
    var photoBrightness by remember { mutableFloatStateOf(0f) }
    var photoContrast by remember { mutableFloatStateOf(1f) }
    var photoSaturation by remember { mutableFloatStateOf(1f) }
    var videoDurationMs by remember { mutableLongStateOf(0L) }
    var videoStartMs by remember { mutableFloatStateOf(0f) }
    var videoEndMs by remember { mutableFloatStateOf(0f) }
    var videoExporting by remember { mutableStateOf(false) }
    var pendingVideoOutput by remember { mutableStateOf<Uri?>(null) }
    var live by remember { mutableStateOf(false) }
    var footballMode by remember { mutableStateOf(false) }
    var sportsNewsMode by remember { mutableStateOf(false) }
    var homeTeam by remember { mutableStateOf("") }
    var awayTeam by remember { mutableStateOf("") }
    var homeScore by remember { mutableStateOf("0") }
    var awayScore by remember { mutableStateOf("0") }
    var competition by remember { mutableStateOf("") }
    var matchTime by remember { mutableStateOf("00:00") }
    var matchStatus by remember { mutableStateOf("BELUM DIMULAI") }
    var homeYellowCards by remember { mutableIntStateOf(0) }
    var awayYellowCards by remember { mutableIntStateOf(0) }
    var homeRedCards by remember { mutableIntStateOf(0) }
    var awayRedCards by remember { mutableIntStateOf(0) }
    var matchEvents by remember { mutableStateOf("") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            selectedMedia = uri
            mediaLabel = uri.lastPathSegment ?: "Media dipilih"
            videoDurationMs = 0L
            videoStartMs = 0f
            videoEndMs = 0f
            if (context.contentResolver.getType(uri)?.startsWith("video/") == true) {
                try {
                    val metadata = MediaMetadataRetriever()
                    metadata.setDataSource(context, uri)
                    videoDurationMs = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                    videoEndMs = videoDurationMs.toFloat()
                    metadata.release()
                } catch (_: Exception) {
                    Toast.makeText(context, "Durasi video tidak dapat dibaca", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val videoExporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("video/mp4")) { outputUri ->
        if (outputUri != null) {
            val sourceUri = selectedMedia
            if (sourceUri == null || videoDurationMs <= 0L) {
                Toast.makeText(context, "Pilih video yang valid terlebih dahulu", Toast.LENGTH_LONG).show()
            } else if (videoEndMs.toLong() <= videoStartMs.toLong()) {
                Toast.makeText(context, "Waktu akhir harus lebih besar dari waktu awal", Toast.LENGTH_LONG).show()
            } else {
                try {
                    videoExporting = true
                    pendingVideoOutput = outputUri
                    val clipping = MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(videoStartMs.toLong())
                        .setEndPositionMs(videoEndMs.toLong())
                        .build()
                    val mediaItem = MediaItem.Builder()
                        .setUri(sourceUri)
                        .setClippingConfiguration(clipping)
                        .build()
                    val editedItem = EditedMediaItem.Builder(mediaItem).build()
                    val outputFile = File(context.cacheDir, "infokini-export-${System.currentTimeMillis()}.mp4")
                    val transformer = Transformer.Builder(context)
                        .addListener(object : Transformer.Listener {
                            override fun onCompleted(composition: androidx.media3.transformer.Composition, exportResult: ExportResult) {
                                try {
                                    val destination = pendingVideoOutput ?: throw IllegalStateException("Lokasi simpan hilang")
                                    context.contentResolver.openOutputStream(destination)?.use { out ->
                                        outputFile.inputStream().use { input -> input.copyTo(out) }
                                    } ?: throw IllegalStateException("File tujuan tidak dapat dibuka")
                                    outputFile.delete()
                                    videoExporting = false
                                    pendingVideoOutput = null
                                    Toast.makeText(context, "Video MP4 berhasil diekspor", Toast.LENGTH_LONG).show()
                                } catch (error: Exception) {
                                    outputFile.delete()
                                    videoExporting = false
                                    pendingVideoOutput = null
                                    Toast.makeText(context, "Gagal menyimpan MP4: ${error.message}", Toast.LENGTH_LONG).show()
                                }
                            }

                            override fun onError(composition: androidx.media3.transformer.Composition, exportResult: ExportResult, exportException: ExportException) {
                                outputFile.delete()
                                videoExporting = false
                                pendingVideoOutput = null
                                Toast.makeText(context, "Ekspor video gagal: ${exportException.message}", Toast.LENGTH_LONG).show()
                            }
                        })
                        .build()
                    transformer.start(editedItem, outputFile.absolutePath)
                } catch (error: Exception) {
                    videoExporting = false
                    pendingVideoOutput = null
                    Toast.makeText(context, "Tidak dapat memulai ekspor: ${error.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val posterExporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { outputUri ->
        if (outputUri != null) {
            val sourceUri = selectedMedia
            if (sourceUri == null || context.contentResolver.getType(sourceUri)?.startsWith("image/") != true) {
                Toast.makeText(context, "Pilih foto terlebih dahulu untuk membuat poster", Toast.LENGTH_LONG).show()
            } else {
                try {
                    val source = context.contentResolver.openInputStream(sourceUri)?.use { BitmapFactory.decodeStream(it) }
                        ?: throw IllegalStateException("Foto tidak dapat dibaca")
                    val poster = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(poster)
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                    val scale = maxOf(1080f / source.width, 1920f / source.height)
                    val drawWidth = source.width * scale
                    val drawHeight = source.height * scale
                    val imageRect = RectF((1080f - drawWidth) / 2f, (1920f - drawHeight) / 2f, (1080f + drawWidth) / 2f, (1920f + drawHeight) / 2f)
                    canvas.drawBitmap(source, null, imageRect, paint)
                    paint.color = android.graphics.Color.argb(225, 8, 13, 74)
                    canvas.drawRect(0f, 1320f, 1080f, 1920f, paint)
                    paint.color = android.graphics.Color.rgb(19, 189, 235)
                    paint.typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
                    paint.textSize = 48f
                    canvas.drawText("INFO KINI  •  infokini.news", 58f, 1400f, paint)
                    paint.color = android.graphics.Color.WHITE
                    paint.textSize = 58f
                    paint.typeface = Typeface.create("sans-serif", Typeface.BOLD)
                    val words = headline.ifBlank { "INFO TERKINI" }.split(" ")
                    var line = ""
                    var y = 1500f
                    for (word in words) {
                        val candidate = if (line.isEmpty()) word else "$line $word"
                        if (paint.measureText(candidate) > 960f && line.isNotEmpty()) {
                            canvas.drawText(line, 58f, y, paint)
                            y += 72f
                            line = word
                        } else line = candidate
                    }
                    if (line.isNotEmpty() && y < 1705f) canvas.drawText(line, 58f, y, paint)
                    paint.color = android.graphics.Color.rgb(19, 189, 235)
                    paint.textSize = 34f
                    paint.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                    canvas.drawText(location.ifBlank { "Berita terkini" }, 58f, 1770f, paint)
                    paint.color = android.graphics.Color.WHITE
                    paint.textSize = 30f
                    canvas.drawText(ticker.take(62), 58f, 1830f, paint)
                    context.contentResolver.openOutputStream(outputUri)?.use { poster.compress(Bitmap.CompressFormat.PNG, 100, it) }
                        ?: throw IllegalStateException("File tujuan tidak dapat dibuka")
                    source.recycle()
                    poster.recycle()
                    Toast.makeText(context, "Poster InfoKini berhasil diekspor", Toast.LENGTH_LONG).show()
                } catch (error: Exception) {
                    Toast.makeText(context, "Ekspor gagal: ${error.message ?: "kesalahan file"}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    val photoExporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/jpeg")) { outputUri ->
        if (outputUri != null) {
            val sourceUri = selectedMedia
            if (sourceUri == null || context.contentResolver.getType(sourceUri)?.startsWith("image/") != true) {
                Toast.makeText(context, "Pilih foto terlebih dahulu", Toast.LENGTH_LONG).show()
            } else {
                try {
                    val source = context.contentResolver.openInputStream(sourceUri)?.use { BitmapFactory.decodeStream(it) }
                        ?: throw IllegalStateException("Foto tidak dapat dibaca")
                    val edited = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                        colorFilter = ColorMatrixColorFilter(createPhotoColorMatrix(photoBrightness, photoContrast, photoSaturation))
                    }
                    Canvas(edited).drawBitmap(source, 0f, 0f, paint)
                    val stream = context.contentResolver.openOutputStream(outputUri)
                        ?: throw IllegalStateException("Lokasi penyimpanan tidak dapat dibuka")
                    stream.use {
                        if (!edited.compress(Bitmap.CompressFormat.JPEG, 92, it)) {
                            throw IllegalStateException("Foto gagal disimpan")
                        }
                    }
                    source.recycle()
                    edited.recycle()
                    Toast.makeText(context, "Foto hasil edit berhasil disimpan", Toast.LENGTH_LONG).show()
                } catch (error: Exception) {
                    Toast.makeText(context, "Edit foto gagal: ${error.message ?: "kesalahan file"}", Toast.LENGTH_LONG).show()
                }
            }
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

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = !footballMode && !sportsNewsMode, onClick = { footballMode = false; sportsNewsMode = false }, label = { Text("Studio Berita") })
                FilterChip(selected = footballMode, onClick = { footballMode = true; sportsNewsMode = false }, label = { Text("Skor Bola") })
                FilterChip(selected = sportsNewsMode, onClick = { sportsNewsMode = true; footballMode = false }, label = { Text("Berita Olahraga") })
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
                if (footballMode) {
                    Column(Modifier.fillMaxWidth().background(Color(0xFF07152B), RoundedCornerShape(8.dp)).padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(competition.ifBlank { "NAMA KOMPETISI" }, color = Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly) {
                            Text(homeTeam.ifBlank { "TIM A" }, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("$homeScore - $awayScore", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                            Text(awayTeam.ifBlank { "TIM B" }, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        }
                        Text("$matchTime  •  $matchStatus", color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("KUNING $homeYellowCards | MERAH $homeRedCards     KUNING $awayYellowCards | MERAH $awayRedCards", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        if (matchEvents.isNotBlank()) Text(matchEvents, color = Color.White, fontSize = 9.sp, maxLines = 1)
                    }
                } else if (headline.isNotBlank()) {
                    Column(Modifier.fillMaxWidth().background(Navy.copy(alpha = .92f)).padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(headline, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        if (location.isNotBlank()) Text(location, color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Row(Modifier.fillMaxWidth().background(Cyan).padding(7.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("INFO KINI", color = Navy, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(ticker.ifBlank { " " }, color = Navy, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                }
            }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("MODE SIARAN SEPAK BOLA", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Aktifkan hanya saat menayangkan pertandingan", color = Color.LightGray, fontSize = 11.sp)
                }
                Switch(checked = footballMode, onCheckedChange = { footballMode = it })
            }
            if (footballMode) {
                Text("PENGATURAN SKOR PERTANDINGAN", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                StudioField("Kompetisi / nama pertandingan", competition, { competition = it }, "Contoh: Liga 1 Indonesia")
                StudioField("Nama tim kandang", homeTeam, { homeTeam = it }, "Nama tim A")
                StudioField("Skor tim kandang", homeScore, { homeScore = it.filter { ch -> ch.isDigit() }.take(2) }, "0")
                StudioField("Nama tim tandang", awayTeam, { awayTeam = it }, "Nama tim B")
                StudioField("Skor tim tandang", awayScore, { awayScore = it.filter { ch -> ch.isDigit() }.take(2) }, "0")
                StudioField("Waktu pertandingan", matchTime, { matchTime = it }, "Contoh: 45+2'")
                var expandedStatus by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { expandedStatus = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(matchStatus, color = Cyan)
                    }
                    DropdownMenu(expanded = expandedStatus, onDismissRequest = { expandedStatus = false }) {
                        listOf("BELUM DIMULAI", "BABAK PERTAMA", "ISTIRAHAT", "BABAK KEDUA", "TAMBAHAN WAKTU", "ADU PENALTI", "SELESAI").forEach { status ->
                            DropdownMenuItem(text = { Text(status) }, onClick = { matchStatus = status; expandedStatus = false })
                        }
                    }
                }
                Text("KARTU PERTANDINGAN", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(homeTeam.ifBlank { "Tim kandang" }, color = Color.White, fontSize = 11.sp)
                        Text("Kuning: $homeYellowCards  Merah: $homeRedCards", color = Color.LightGray, fontSize = 10.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedButton(onClick = { homeYellowCards = (homeYellowCards + 1).coerceAtMost(9) }, modifier = Modifier.weight(1f)) { Text("+ Kuning") }
                            OutlinedButton(onClick = { homeRedCards = (homeRedCards + 1).coerceAtMost(9) }, modifier = Modifier.weight(1f)) { Text("+ Merah") }
                        }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(awayTeam.ifBlank { "Tim tandang" }, color = Color.White, fontSize = 11.sp)
                        Text("Kuning: $awayYellowCards  Merah: $awayRedCards", color = Color.LightGray, fontSize = 10.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedButton(onClick = { awayYellowCards = (awayYellowCards + 1).coerceAtMost(9) }, modifier = Modifier.weight(1f)) { Text("+ Kuning") }
                            OutlinedButton(onClick = { awayRedCards = (awayRedCards + 1).coerceAtMost(9) }, modifier = Modifier.weight(1f)) { Text("+ Merah") }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { homeYellowCards = (homeYellowCards - 1).coerceAtLeast(0); homeRedCards = (homeRedCards - 1).coerceAtLeast(0) }, modifier = Modifier.weight(1f)) { Text("Koreksi kandang") }
                    OutlinedButton(onClick = { awayYellowCards = (awayYellowCards - 1).coerceAtLeast(0); awayRedCards = (awayRedCards - 1).coerceAtLeast(0) }, modifier = Modifier.weight(1f)) { Text("Koreksi tandang") }
                }
                StudioField("Catatan kejadian (gol / pergantian)", matchEvents, { matchEvents = it }, "Contoh: 32' Gol Tim A • 60' Pergantian pemain")
                Text("Logo tim dan logo kompetisi serta timer otomatis masih memerlukan pengembangan lanjutan.", color = Color(0xFFB8C4D9), fontSize = 11.sp)
            } else if (sportsNewsMode) {
                SportsNewsEditor()
            } else {
            Text("EDITOR BERITA", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            StudioField("Judul berita", headline, { headline = it }, "Tulis judul berita...")
            StudioField("Teks berjalan", ticker, { ticker = it }, "Tulis running text...")
            StudioField("Lokasi berita", location, { location = it }, "Contoh: Cigombong, Kabupaten Bogor")
            }
            OutlinedButton(
                onClick = { picker.launch(arrayOf("image/*", "video/*")) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Cyan)
            ) { Text("PILIH FOTO / VIDEO DARI HP") }
            Text(mediaLabel, color = Color.LightGray, fontSize = 12.sp)

            if (selectedMedia != null && context.contentResolver.getType(selectedMedia!!)?.startsWith("image/") == true) {
                Text("EDITOR FOTO", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Kecerahan: ${photoBrightness.toInt()}", color = Color.White, fontSize = 12.sp)
                Slider(value = photoBrightness, onValueChange = { photoBrightness = it }, valueRange = -100f..100f)
                Text("Kontras: ${"%.1f".format(Locale.US, photoContrast)}", color = Color.White, fontSize = 12.sp)
                Slider(value = photoContrast, onValueChange = { photoContrast = it }, valueRange = 0.5f..1.8f)
                Text("Saturasi: ${"%.1f".format(Locale.US, photoSaturation)}", color = Color.White, fontSize = 12.sp)
                Slider(value = photoSaturation, onValueChange = { photoSaturation = it }, valueRange = 0f..2f)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        photoBrightness = 0f
                        photoContrast = 1f
                        photoSaturation = 1f
                    }, modifier = Modifier.weight(1f)) { Text("Atur ulang") }
                    Button(onClick = { photoExporter.launch("InfoKini-Foto-Edit.jpg") }, modifier = Modifier.weight(1f)) {
                        Text("SIMPAN FOTO EDIT")
                    }
                }
            }
            if (selectedMedia != null && context.contentResolver.getType(selectedMedia!!)?.startsWith("image/") == true) {
                Button(
                    onClick = { posterExporter.launch("InfoKini-Poster.png") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Navy)
                ) { Text("EKSPOR POSTER BERITA PNG", fontWeight = FontWeight.Black) }
                Text("Membuat poster vertikal 1080 × 1920 dengan identitas InfoKini, judul, lokasi, dan running text.", color = Color(0xFFB8C4D9), fontSize = 11.sp)
            }
            if (selectedMedia != null) {
                val isVideo = context.contentResolver.getType(selectedMedia!!)?.startsWith("video/") == true
                Text(if (isVideo) "PRATINJAU VIDEO" else "PRATINJAU FOTO", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                if (isVideo) {
                    Text("EDITOR VIDEO", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    if (videoDurationMs > 0L) {
                        Text("Durasi: ${videoDurationMs / 1000}s", color = Color.White, fontSize = 12.sp)
                        Text("Mulai: ${(videoStartMs / 1000f).toInt()} detik", color = Color.White, fontSize = 12.sp)
                        Slider(
                            value = videoStartMs,
                            onValueChange = { videoStartMs = it.coerceAtMost(videoEndMs - 1000f) },
                            valueRange = 0f..(videoDurationMs - 1000L).coerceAtLeast(0L).toFloat()
                        )
                        Text("Akhir: ${(videoEndMs / 1000f).toInt()} detik", color = Color.White, fontSize = 12.sp)
                        Slider(
                            value = videoEndMs,
                            onValueChange = { videoEndMs = it.coerceAtLeast(videoStartMs + 1000f) },
                            valueRange = 1000f..videoDurationMs.toFloat()
                        )
                        Button(
                            onClick = { videoExporter.launch("InfoKini-Video-Edit.mp4") },
                            enabled = !videoExporting,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Navy)
                        ) { Text(if (videoExporting) "SEDANG EKSPOR..." else "POTONG & EKSPOR MP4", fontWeight = FontWeight.Black) }
                        Text("Video diproses di perangkat. Lama proses bergantung pada durasi, resolusi, dan kemampuan HP.", color = Color(0xFFB8C4D9), fontSize = 11.sp)
                    } else {
                        Text("Durasi video belum terbaca. Coba pilih file video lain.", color = Color.LightGray, fontSize = 11.sp)
                    }
                    AndroidView(
                        factory = { viewContext ->
                            VideoView(viewContext).apply {
                                setVideoURI(selectedMedia)
                                setOnPreparedListener { player -> player.isLooping = true; start() }
                            }
                        },
                        update = { view ->
                            if (view.tag != selectedMedia.toString()) {
                                view.tag = selectedMedia.toString()
                                view.setVideoURI(selectedMedia)
                                view.setOnPreparedListener { player -> player.isLooping = true; view.start() }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(220.dp)
                    )
                } else {
                    AndroidView(
                        factory = { viewContext ->
                            ImageView(viewContext).apply {
                                scaleType = ImageView.ScaleType.FIT_CENTER
                                setBackgroundColor(android.graphics.Color.BLACK)
                                setImageURI(selectedMedia)
                                colorFilter = ColorMatrixColorFilter(createPhotoColorMatrix(photoBrightness, photoContrast, photoSaturation))
                            }
                        },
                        update = { view ->
                            view.setImageURI(selectedMedia)
                            view.colorFilter = ColorMatrixColorFilter(createPhotoColorMatrix(photoBrightness, photoContrast, photoSaturation))
                        },
                        modifier = Modifier.fillMaxWidth().height(220.dp)
                    )
                }
            }
            if (!footballMode && !sportsNewsMode) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        context.getSharedPreferences("infokini_studio", 0).edit()
                            .putString("headline", headline).putString("ticker", ticker)
                            .putString("location", location).apply()
                        Toast.makeText(context, "Draf InfoKini tersimpan di perangkat", Toast.LENGTH_SHORT).show()
                    }, modifier = Modifier.weight(1f)) { Text("Simpan draf") }
                    OutlinedButton(onClick = {
                        val prefs = context.getSharedPreferences("infokini_studio", 0)
                        headline = prefs.getString("headline", "").orEmpty()
                        ticker = prefs.getString("ticker", "").orEmpty()
                        location = prefs.getString("location", "").orEmpty()
                        Toast.makeText(context, "Draf dimuat", Toast.LENGTH_SHORT).show()
                    }, modifier = Modifier.weight(1f)) { Text("Muat draf") }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        val shareText = "INFO KINI | infokini.news\n" + headline + "\n" + location + "\n\n" + ticker
                        val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, shareText) }
                        context.startActivity(Intent.createChooser(send, "Bagikan konten InfoKini"))
                    }, modifier = Modifier.weight(1f)) { Text("Bagikan") }
                    OutlinedButton(onClick = {
                        tts.language = Locale("id", "ID")
                        tts.speak("$headline. $ticker", TextToSpeech.QUEUE_FLUSH, null, "infokini-news-script")
                    }, modifier = Modifier.weight(1f)) { Text("Bacakan naskah") }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tampilkan label LIVE", color = Color.White, modifier = Modifier.weight(1f))
                Switch(checked = live, onCheckedChange = { live = it })
            }
            Button(
                onClick = { /* preview updates live from editor fields */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Navy)
            ) { Text("PERBARUI PRATINJAU", fontWeight = FontWeight.Black) }

            Text("Tentang aplikasi: InfoKini News Studio. Penggagas: Wandi Azis.", color = Color(0xFFB8C4D9), fontSize = 11.sp)

            Text("Mode skor sepak bola masih tahap dasar. Logo tim/kompetisi, jam pertandingan otomatis, statistik laga, dan ekspor grafis video memerlukan pengembangan lanjutan. Ekspor MP4, editor video penuh, AI redaktur, TTS, dan siaran langsung juga belum selesai.", color = Color(0xFFB8C4D9), fontSize = 11.sp)
        }
    }
}


private fun createPhotoColorMatrix(brightness: Float, contrast: Float, saturation: Float): ColorMatrix {
    val inverse = 1f - saturation
    val red = 0.213f * inverse
    val green = 0.715f * inverse
    val blue = 0.072f * inverse
    return ColorMatrix(floatArrayOf(
        (red + saturation) * contrast, green * contrast, blue * contrast, 0f, brightness,
        red * contrast, (green + saturation) * contrast, blue * contrast, 0f, brightness,
        red * contrast, green * contrast, (blue + saturation) * contrast, 0f, brightness,
        0f, 0f, 0f, 1f, 0f
    ))
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
