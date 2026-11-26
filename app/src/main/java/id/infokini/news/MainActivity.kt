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
import android.graphics.Matrix
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
import androidx.media3.transformer.Effects
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.TextOverlay
import androidx.media3.effect.TextureOverlay
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
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
import kotlinx.coroutines.delay

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

@OptIn(UnstableApi::class)
@Composable
fun InfoKiniStudio() {
    val context = LocalContext.current
    var ttsReady by remember { mutableStateOf(false) }
    var ttsVoiceNames by remember { mutableStateOf(listOf("Default Android")) }
    var selectedTtsVoice by remember { mutableStateOf("Default Android") }
    var ttsVoiceMenu by remember { mutableStateOf(false) }
    var aiPresenterEnabled by remember { mutableStateOf(false) }
    var presenterStyle by remember { mutableStateOf("Presenter Pria") }
    var presenterMenu by remember { mutableStateOf(false) }
    var deliveryStyle by remember { mutableStateOf("Formal / Berita") }
    var deliveryMenu by remember { mutableStateOf(false) }
    val deliveryStyles = listOf("Formal / Berita", "Santai / Akrab", "Breaking News", "Dokumenter", "Semangat / Olahraga")
    val tts = remember {
        TextToSpeech(context) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
        }
    }
    LaunchedEffect(ttsReady) {
        if (ttsReady) {
            val voices = tts.voices?.filter { it.locale.language == "id" }.orEmpty()
            val names = voices.map { it.name }.distinct()
            ttsVoiceNames = names.ifEmpty { listOf("Default Android") }
            if (selectedTtsVoice !in ttsVoiceNames) selectedTtsVoice = ttsVoiceNames.first()
        }
    }
    DisposableEffect(tts) { onDispose { tts.stop(); tts.shutdown() } }
    fun applyNarrationStyle() {
        when (deliveryStyle) {
            "Santai / Akrab" -> { tts.setSpeechRate(1.02f); tts.setPitch(1.04f) }
            "Breaking News" -> { tts.setSpeechRate(1.12f); tts.setPitch(1.02f) }
            "Dokumenter" -> { tts.setSpeechRate(0.88f); tts.setPitch(0.96f) }
            "Semangat / Olahraga" -> { tts.setSpeechRate(1.08f); tts.setPitch(1.10f) }
            else -> { tts.setSpeechRate(0.94f); tts.setPitch(1.0f) }
        }
    }
    var headline by remember { mutableStateOf("") }
    var ticker by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedMedia by remember { mutableStateOf<Uri?>(null) }
    var mediaLabel by remember { mutableStateOf("Belum ada foto atau video dipilih") }
    var photoBrightness by remember { mutableFloatStateOf(0f) }
    var photoContrast by remember { mutableFloatStateOf(1f) }
    var photoSaturation by remember { mutableFloatStateOf(1f) }
    var photoRotation by remember { mutableFloatStateOf(0f) }
    var photoFlipHorizontal by remember { mutableStateOf(false) }
    var photoFlipVertical by remember { mutableStateOf(false) }
    var photoCaption by remember { mutableStateOf("") }
    var photoBrandWatermark by remember { mutableStateOf(true) }
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
    var homeLogoUri by remember { mutableStateOf<Uri?>(null) }
    var awayLogoUri by remember { mutableStateOf<Uri?>(null) }
    var competitionLogoUri by remember { mutableStateOf<Uri?>(null) }
    var matchClockRunning by remember { mutableStateOf(false) }
    var matchElapsedSeconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(matchClockRunning) {
        while (matchClockRunning) {
            delay(1000)
            matchElapsedSeconds += 1
            matchTime = String.format(Locale.US, "%02d:%02d", matchElapsedSeconds / 60, matchElapsedSeconds % 60)
        }
    }
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


    fun keepLogoPermission(uri: Uri?) {
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {
                // Some document providers do not offer persistable permissions.
            }
        }
    }
    val homeLogoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        keepLogoPermission(it); homeLogoUri = it
    }
    val awayLogoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        keepLogoPermission(it); awayLogoUri = it
    }
    val competitionLogoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        keepLogoPermission(it); competitionLogoUri = it
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
                    val videoOverlays = mutableListOf<TextureOverlay>()
                    val title = headline.trim()
                    val crawl = ticker.trim()
                    if (title.isNotBlank() || crawl.isNotBlank()) {
                        val combined = buildString {
                            if (title.isNotBlank()) append(title)
                            if (title.isNotBlank() && crawl.isNotBlank()) append("\n")
                            if (crawl.isNotBlank()) append("INFO KINI  •  ").append(crawl)
                        }
                        val overlayText = SpannableString(combined)
                        if (title.isNotBlank()) {
                            overlayText.setSpan(ForegroundColorSpan(android.graphics.Color.WHITE), 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                            overlayText.setSpan(BackgroundColorSpan(android.graphics.Color.rgb(8, 13, 74)), 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                            overlayText.setSpan(StyleSpan(Typeface.BOLD), 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                        if (crawl.isNotBlank()) {
                            val crawlStart = combined.indexOf("INFO KINI")
                            overlayText.setSpan(ForegroundColorSpan(android.graphics.Color.rgb(8, 13, 74)), crawlStart, combined.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                            overlayText.setSpan(BackgroundColorSpan(android.graphics.Color.rgb(19, 189, 235)), crawlStart, combined.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                            overlayText.setSpan(StyleSpan(Typeface.BOLD), crawlStart, combined.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                        videoOverlays.add(TextOverlay.createStaticTextOverlay(overlayText))
                    }
                    val effects = if (videoOverlays.isNotEmpty()) {
                        Effects(emptyList(), listOf(OverlayEffect(videoOverlays)))
                    } else {
                        Effects(emptyList(), emptyList())
                    }
                    val editedItem = EditedMediaItem.Builder(mediaItem).setEffects(effects).build()
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

    val matchGraphicExporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { outputUri ->
        if (outputUri != null) {
            try {
                val graphic = Bitmap.createBitmap(1080, 560, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(graphic)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create("sans-serif", Typeface.BOLD) }
                paint.color = android.graphics.Color.rgb(8, 13, 74)
                canvas.drawRect(0f, 0f, 1080f, 560f, paint)
                paint.color = android.graphics.Color.rgb(19, 189, 235)
                canvas.drawRect(0f, 0f, 1080f, 12f, paint)
                paint.textSize = 38f
                canvas.drawText("INFO KINI  •  infokini.news", 48f, 78f, paint)
                if (competitionLogoUri != null) {
                    try {
                        val competitionLogo = context.contentResolver.openInputStream(competitionLogoUri!!)?.use { BitmapFactory.decodeStream(it) }
                        if (competitionLogo != null) {
                            canvas.drawBitmap(competitionLogo, null, RectF(930f, 28f, 1030f, 128f), Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
                            competitionLogo.recycle()
                        }
                    } catch (_: Exception) { }
                }
                paint.color = android.graphics.Color.WHITE
                paint.textSize = 30f
                canvas.drawText(competition.ifBlank { "PERTANDINGAN" }.take(42), 48f, 142f, paint)
                fun drawLogo(uri: Uri?, left: Float) {
                    if (uri == null) return
                    try {
                        val logo = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                        if (logo != null) {
                            val dst = RectF(left, 180f, left + 132f, 312f)
                            canvas.drawBitmap(logo, null, dst, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
                            logo.recycle()
                        }
                    } catch (_: Exception) { }
                }
                drawLogo(homeLogoUri, 80f)
                drawLogo(awayLogoUri, 868f)
                paint.color = android.graphics.Color.WHITE
                paint.textSize = 44f
                canvas.drawText(homeTeam.ifBlank { "TIM A" }.take(18), 235f, 238f, paint)
                canvas.drawText(awayTeam.ifBlank { "TIM B" }.take(18), 650f, 238f, paint)
                paint.typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
                paint.textSize = 100f
                canvas.drawText(homeScore, 390f, 360f, paint)
                canvas.drawText("-", 510f, 360f, paint)
                canvas.drawText(awayScore, 590f, 360f, paint)
                paint.color = android.graphics.Color.rgb(19, 189, 235)
                paint.textSize = 34f
                canvas.drawText("$matchTime  •  $matchStatus", 300f, 430f, paint)
                paint.color = android.graphics.Color.WHITE
                paint.textSize = 24f
                canvas.drawText("KUNING $homeYellowCards  MERAH $homeRedCards", 55f, 488f, paint)
                canvas.drawText("KUNING $awayYellowCards  MERAH $awayRedCards", 600f, 488f, paint)
                paint.color = android.graphics.Color.rgb(19, 189, 235)
                paint.textSize = 22f
                val eventLine = matchEvents.ifBlank { ticker }.take(78)
                if (eventLine.isNotBlank()) canvas.drawText(eventLine, 55f, 535f, paint)
                context.contentResolver.openOutputStream(outputUri)?.use {
                    if (!graphic.compress(Bitmap.CompressFormat.PNG, 100, it)) throw IllegalStateException("PNG gagal disimpan")
                } ?: throw IllegalStateException("Lokasi penyimpanan tidak dapat dibuka")
                graphic.recycle()
                Toast.makeText(context, "Grafis pertandingan berhasil diekspor", Toast.LENGTH_LONG).show()
            } catch (error: Exception) {
                Toast.makeText(context, "Ekspor grafis gagal: ${error.message}", Toast.LENGTH_LONG).show()
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
                    val rotateMatrix = Matrix().apply { postScale(if (photoFlipHorizontal) -1f else 1f, if (photoFlipVertical) -1f else 1f); postRotate(photoRotation) }
                    val rotated = Bitmap.createBitmap(source, 0, 0, source.width, source.height, rotateMatrix, true)
                    val edited = Bitmap.createBitmap(rotated.width, rotated.height, Bitmap.Config.ARGB_8888)
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                        colorFilter = ColorMatrixColorFilter(createPhotoColorMatrix(photoBrightness, photoContrast, photoSaturation))
                    }
                    val editCanvas = Canvas(edited)
                    editCanvas.drawBitmap(rotated, 0f, 0f, paint)
                    if (photoCaption.isNotBlank() || photoBrandWatermark) {
                        val overlay = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.argb(205, 8, 13, 74) }
                        val barHeight = (edited.height * 0.19f).coerceAtLeast(110f)
                        editCanvas.drawRect(0f, edited.height - barHeight, edited.width.toFloat(), edited.height.toFloat(), overlay)
                        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = android.graphics.Color.WHITE
                            typeface = Typeface.create("sans-serif", Typeface.BOLD)
                            textSize = (edited.width * 0.045f).coerceIn(22f, 64f)
                        }
                        if (photoCaption.isNotBlank()) {
                            val words = photoCaption.trim().split(Regex("\\s+"))
                            var line = ""
                            var y = edited.height - barHeight + labelPaint.textSize * 1.5f
                            val maxWidth = edited.width * 0.9f
                            for (word in words) {
                                val candidate = if (line.isEmpty()) word else "$line $word"
                                if (labelPaint.measureText(candidate) > maxWidth && line.isNotEmpty()) {
                                    editCanvas.drawText(line, edited.width * 0.05f, y, labelPaint)
                                    y += labelPaint.textSize * 1.2f
                                    line = word
                                } else line = candidate
                            }
                            if (line.isNotEmpty() && y < edited.height - 12f) editCanvas.drawText(line, edited.width * 0.05f, y, labelPaint)
                        }
                        if (photoBrandWatermark) {
                            labelPaint.color = android.graphics.Color.rgb(19, 189, 235)
                            labelPaint.textSize = (edited.width * 0.032f).coerceIn(18f, 42f)
                            editCanvas.drawText("INFO KINI • infokini.news", edited.width * 0.05f, edited.height - edited.height * 0.035f, labelPaint)
                        }
                    }
                    if (rotated !== source) rotated.recycle()
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
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            if (competitionLogoUri != null) {
                                AndroidView(factory = { ImageView(it).apply { setImageURI(competitionLogoUri); scaleType = ImageView.ScaleType.FIT_CENTER } },
                                    update = { it.setImageURI(competitionLogoUri) }, modifier = Modifier.size(28.dp))
                            }
                            Text(competition.ifBlank { "NAMA KOMPETISI" }, color = Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly) {
                            if (homeLogoUri != null) AndroidView(factory = { ImageView(it).apply { setImageURI(homeLogoUri); scaleType = ImageView.ScaleType.FIT_CENTER } },
                                update = { it.setImageURI(homeLogoUri) }, modifier = Modifier.size(34.dp))
                            Text(homeTeam.ifBlank { "TIM A" }, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("$homeScore - $awayScore", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                            Text(awayTeam.ifBlank { "TIM B" }, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            if (awayLogoUri != null) AndroidView(factory = { ImageView(it).apply { setImageURI(awayLogoUri); scaleType = ImageView.ScaleType.FIT_CENTER } },
                                update = { it.setImageURI(awayLogoUri) }, modifier = Modifier.size(34.dp))
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
                Text("JAM PERTANDINGAN", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(matchTime, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { matchClockRunning = !matchClockRunning }, modifier = Modifier.weight(1f)) {
                        Text(if (matchClockRunning) "JEDA JAM" else "MULAI JAM")
                    }
                    OutlinedButton(onClick = {
                        matchClockRunning = false
                        matchElapsedSeconds = 0
                        matchTime = "00:00"
                    }, modifier = Modifier.weight(1f)) { Text("RESET") }
                }
                StudioField("Koreksi waktu / tambahan", matchTime, { matchTime = it }, "Contoh: 45+2'")
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
                Text("LOGO PERTANDINGAN", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(onClick = { competitionLogoPicker.launch(arrayOf("image/*")) }, modifier = Modifier.weight(1f)) { Text("Logo kompetisi") }
                    OutlinedButton(onClick = { homeLogoPicker.launch(arrayOf("image/*")) }, modifier = Modifier.weight(1f)) { Text("Logo tim A") }
                    OutlinedButton(onClick = { awayLogoPicker.launch(arrayOf("image/*")) }, modifier = Modifier.weight(1f)) { Text("Logo tim B") }
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
                Button(onClick = { matchGraphicExporter.launch("infokini-skor-pertandingan.png") }, modifier = Modifier.fillMaxWidth()) {
                    Text("EKSPOR GRAFIS PERTANDINGAN PNG")
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        val prefs = context.getSharedPreferences("infokini_match", 0)
                        prefs.edit()
                            .putString("competition", competition)
                            .putString("homeLogoUri", homeLogoUri?.toString())
                            .putString("awayLogoUri", awayLogoUri?.toString())
                            .putString("competitionLogoUri", competitionLogoUri?.toString())
                            .putString("homeTeam", homeTeam).putString("awayTeam", awayTeam)
                            .putString("homeScore", homeScore).putString("awayScore", awayScore)
                            .putString("matchTime", matchTime).putString("matchStatus", matchStatus)
                            .putInt("elapsed", matchElapsedSeconds)
                            .putInt("homeYellow", homeYellowCards).putInt("awayYellow", awayYellowCards)
                            .putInt("homeRed", homeRedCards).putInt("awayRed", awayRedCards)
                            .putString("events", matchEvents).apply()
                        Toast.makeText(context, "Data pertandingan tersimpan", Toast.LENGTH_SHORT).show()
                    }, modifier = Modifier.weight(1f)) { Text("Simpan laga") }
                    OutlinedButton(onClick = {
                        val prefs = context.getSharedPreferences("infokini_match", 0)
                        competition = prefs.getString("competition", "").orEmpty()
                        homeLogoUri = prefs.getString("homeLogoUri", null)?.let { Uri.parse(it) }
                        awayLogoUri = prefs.getString("awayLogoUri", null)?.let { Uri.parse(it) }
                        competitionLogoUri = prefs.getString("competitionLogoUri", null)?.let { Uri.parse(it) }
                        homeTeam = prefs.getString("homeTeam", "").orEmpty()
                        awayTeam = prefs.getString("awayTeam", "").orEmpty()
                        homeScore = prefs.getString("homeScore", "0") ?: "0"
                        awayScore = prefs.getString("awayScore", "0") ?: "0"
                        matchTime = prefs.getString("matchTime", "00:00") ?: "00:00"
                        matchStatus = prefs.getString("matchStatus", "BELUM DIMULAI") ?: "BELUM DIMULAI"
                        matchElapsedSeconds = prefs.getInt("elapsed", 0)
                        homeYellowCards = prefs.getInt("homeYellow", 0)
                        awayYellowCards = prefs.getInt("awayYellow", 0)
                        homeRedCards = prefs.getInt("homeRed", 0)
                        awayRedCards = prefs.getInt("awayRed", 0)
                        matchEvents = prefs.getString("events", "").orEmpty()
                        matchClockRunning = false
                        Toast.makeText(context, "Data pertandingan dimuat", Toast.LENGTH_SHORT).show()
                    }, modifier = Modifier.weight(1f)) { Text("Muat laga") }
                }
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
                Text("Rotasi: ${photoRotation.toInt()}°", color = Color.White, fontSize = 12.sp)
                Slider(value = photoRotation, onValueChange = { photoRotation = it }, valueRange = -180f..180f)
                StudioField("Teks pada foto", photoCaption, { photoCaption = it }, "Tambahkan keterangan atau judul foto...")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = photoBrandWatermark, onCheckedChange = { photoBrandWatermark = it })
                    Text("Tambahkan watermark InfoKini pada foto", color = Color.White, fontSize = 12.sp)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        context.getSharedPreferences("infokini_photo_editor", 0).edit()
                            .putFloat("brightness", photoBrightness)
                            .putFloat("contrast", photoContrast)
                            .putFloat("saturation", photoSaturation)
                            .putFloat("rotation", photoRotation)
                            .putBoolean("flip_h", photoFlipHorizontal)
                            .putBoolean("flip_v", photoFlipVertical)
                            .putString("caption", photoCaption)
                            .putBoolean("watermark", photoBrandWatermark).apply()
                        Toast.makeText(context, "Pengaturan foto tersimpan", Toast.LENGTH_SHORT).show()
                    }, modifier = Modifier.weight(1f)) { Text("Simpan setelan") }
                    OutlinedButton(onClick = {
                        val prefs = context.getSharedPreferences("infokini_photo_editor", 0)
                        photoBrightness = prefs.getFloat("brightness", 0f)
                        photoContrast = prefs.getFloat("contrast", 1f)
                        photoSaturation = prefs.getFloat("saturation", 1f)
                        photoRotation = prefs.getFloat("rotation", 0f)
                        photoFlipHorizontal = prefs.getBoolean("flip_h", false)
                        photoFlipVertical = prefs.getBoolean("flip_v", false)
                        photoCaption = prefs.getString("caption", "").orEmpty()
                        photoBrandWatermark = prefs.getBoolean("watermark", true)
                        Toast.makeText(context, "Pengaturan foto dimuat", Toast.LENGTH_SHORT).show()
                    }, modifier = Modifier.weight(1f)) { Text("Muat setelan") }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { photoFlipHorizontal = !photoFlipHorizontal }, modifier = Modifier.weight(1f)) {
                        Text(if (photoFlipHorizontal) "Balik kiri-kanan: aktif" else "Balik kiri-kanan")
                    }
                    OutlinedButton(onClick = { photoFlipVertical = !photoFlipVertical }, modifier = Modifier.weight(1f)) {
                        Text(if (photoFlipVertical) "Balik atas-bawah: aktif" else "Balik atas-bawah")
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        photoBrightness = 0f
                        photoContrast = 1f
                        photoSaturation = 1f
                        photoRotation = 0f
                        photoFlipHorizontal = false
                        photoFlipVertical = false
                        photoCaption = ""
                        photoBrandWatermark = true
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
                                rotation = photoRotation
                                scaleX = if (photoFlipHorizontal) -1f else 1f
                                scaleY = if (photoFlipVertical) -1f else 1f
                            }
                        },
                        update = { view ->
                            view.setImageURI(selectedMedia)
                            view.colorFilter = ColorMatrixColorFilter(createPhotoColorMatrix(photoBrightness, photoContrast, photoSaturation))
                            view.rotation = photoRotation
                            view.scaleX = if (photoFlipHorizontal) -1f else 1f
                            view.scaleY = if (photoFlipVertical) -1f else 1f
                        },
                        modifier = Modifier.fillMaxWidth().height(220.dp)
                    )
                }
            }
            if (!footballMode && !sportsNewsMode) {
                Text("NARATOR & PEMBAWA ACARA AI", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Aktifkan mode pembawa acara", color = Color.White, modifier = Modifier.weight(1f))
                    Switch(checked = aiPresenterEnabled, onCheckedChange = { aiPresenterEnabled = it })
                }
                if (aiPresenterEnabled) {
                    Text("Karakter pembawa acara", color = Color.White, fontSize = 12.sp)
                    Box {
                        OutlinedButton(onClick = { presenterMenu = true }, modifier = Modifier.fillMaxWidth()) { Text(presenterStyle) }
                        DropdownMenu(expanded = presenterMenu, onDismissRequest = { presenterMenu = false }) {
                            listOf("Presenter Pria", "Presenter Wanita").forEach { option ->
                                DropdownMenuItem(text = { Text(option) }, onClick = { presenterStyle = option; presenterMenu = false })
                            }
                        }
                    }
                    Text("Gaya penyampaian narator", color = Color.White, fontSize = 12.sp)
                    Box {
                        OutlinedButton(onClick = { deliveryMenu = true }, modifier = Modifier.fillMaxWidth()) { Text(deliveryStyle) }
                        DropdownMenu(expanded = deliveryMenu, onDismissRequest = { deliveryMenu = false }) {
                            deliveryStyles.forEach { option ->
                                DropdownMenuItem(text = { Text(option) }, onClick = { deliveryStyle = option; deliveryMenu = false })
                            }
                        }
                    }
                    Text("Avatar realistis dan gerak bibir sinkron memerlukan integrasi layanan AI avatar; pilihan ini saat ini mengatur karakter dan gaya narasi.", color = Color(0xFFB8C4D9), fontSize = 11.sp)
                }
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
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { ttsVoiceMenu = true },
                            enabled = ttsReady,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Suara: ${selectedTtsVoice.take(18)}") }
                        DropdownMenu(expanded = ttsVoiceMenu, onDismissRequest = { ttsVoiceMenu = false }) {
                            ttsVoiceNames.forEach { voiceName ->
                                DropdownMenuItem(
                                    text = { Text(voiceName) },
                                    onClick = { selectedTtsVoice = voiceName; ttsVoiceMenu = false }
                                )
                            }
                        }
                    }
                    OutlinedButton(onClick = {
                        if (ttsReady) {
                            tts.language = Locale("id", "ID")
                            tts.voices?.firstOrNull { it.name == selectedTtsVoice }?.let { tts.voice = it }
                            applyNarrationStyle()
                            val presenterIntro = if (aiPresenterEnabled) {
                                if (presenterStyle == "Presenter Wanita") "Pemirsa, berikut informasi InfoKini. " else "Pemirsa, berikut informasi terkini InfoKini. "
                            } else ""
                            tts.speak(presenterIntro + "$headline. $ticker", TextToSpeech.QUEUE_FLUSH, null, "infokini-news-script")
                        } else {
                            Toast.makeText(context, "Mesin suara Android belum siap", Toast.LENGTH_SHORT).show()
                        }
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

            Text("InfoKini News Studio\nPowered by Wandi Azis", color = Color(0xFFB8C4D9), fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth())

            Text("Catatan: editor foto, pemotongan video, ekspor MP4 dengan judul dan running text, TTS perangkat, serta grafis skor manual tersedia pada tahap awal. Skor otomatis, pengumpulan berita AI dari sumber tepercaya, siaran langsung platform, dan fitur penyuntingan video lanjutan memerlukan integrasi serta pengujian tambahan.", color = Color(0xFFB8C4D9), fontSize = 11.sp)
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
