package id.infokini.news

import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun SportsNewsEditor() {
    val context = LocalContext.current
    var category by remember { mutableStateOf("Sepak Bola") }
    var headline by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var competition by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("") }
    var ticker by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Menunggu verifikasi") }
    var categoryOpen by remember { mutableStateOf(false) }
    var statusOpen by remember { mutableStateOf(false) }
    val categories = listOf("Sepak Bola", "Bulu Tangkis", "Basket", "Voli", "MotoGP", "Formula 1", "Tenis", "Atletik", "Olahraga Lainnya")
    val tts = remember { TextToSpeech(context) { } }
    DisposableEffect(tts) { onDispose { tts.stop(); tts.shutdown() } }

    Text("RUANG REDAKSI OLAHRAGA", color = Color(0xFF13BDEB), fontWeight = FontWeight.Bold)
    Text("Buat draf berita olahraga dan cantumkan sumber yang dapat diperiksa.", color = Color.LightGray, fontSize = 12.sp)
    Box {
        OutlinedButton(onClick = { categoryOpen = true }, modifier = Modifier.fillMaxWidth()) { Text("Cabang olahraga: $category") }
        DropdownMenu(expanded = categoryOpen, onDismissRequest = { categoryOpen = false }) {
            categories.forEach { item -> DropdownMenuItem(text = { Text(item) }, onClick = { category = item; categoryOpen = false }) }
        }
    }
    SportsField("Judul berita", headline, { headline = it }, "Tulis judul berita olahraga")
    SportsField("Kompetisi / lokasi", competition, { competition = it }, "Nama liga, turnamen, atau kota")
    Text("Isi berita / naskah", color = Color.White, fontWeight = FontWeight.SemiBold)
    BasicTextField(value = body, onValueChange = { body = it },
        modifier = Modifier.fillMaxWidth().heightIn(min = 130.dp).background(Color.White, RoundedCornerShape(8.dp)).padding(12.dp),
        textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFF172033), fontSize = 14.sp),
        decorationBox = { inner -> Box { if (body.isEmpty()) Text("Tulis isi berita...", color = Color.Gray); inner() } })
    SportsField("Sumber / tautan berita", source, { source = it }, "URL atau nama sumber")
    SportsField("Teks berjalan", ticker, { ticker = it }, "Ringkasan untuk running text")
    Box {
        OutlinedButton(onClick = { statusOpen = true }, modifier = Modifier.fillMaxWidth()) { Text(status) }
        DropdownMenu(expanded = statusOpen, onDismissRequest = { statusOpen = false }) {
            listOf("Menunggu verifikasi", "Sumber sudah diperiksa", "Belum terkonfirmasi").forEach {
                DropdownMenuItem(text = { Text(it) }, onClick = { status = it; statusOpen = false })
            }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = {
            context.getSharedPreferences("infokini_sports", 0).edit()
                .putString("category", category).putString("headline", headline)
                .putString("body", body).putString("competition", competition)
                .putString("source", source).putString("ticker", ticker)
                .putString("status", status).apply()
            android.widget.Toast.makeText(context, "Draf olahraga tersimpan", android.widget.Toast.LENGTH_SHORT).show()
        }, modifier = Modifier.weight(1f)) { Text("Simpan draf") }
        OutlinedButton(onClick = {
            val prefs = context.getSharedPreferences("infokini_sports", 0)
            category = prefs.getString("category", category) ?: category
            headline = prefs.getString("headline", "").orEmpty()
            body = prefs.getString("body", "").orEmpty()
            competition = prefs.getString("competition", "").orEmpty()
            source = prefs.getString("source", "").orEmpty()
            ticker = prefs.getString("ticker", "").orEmpty()
            status = prefs.getString("status", "Menunggu verifikasi") ?: "Menunggu verifikasi"
            android.widget.Toast.makeText(context, "Draf olahraga dimuat", android.widget.Toast.LENGTH_SHORT).show()
        }, modifier = Modifier.weight(1f)) { Text("Muat draf") }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = {
            val draft = "[$category] $headline\n\n$body\n\nKompetisi/Lokasi: $competition\nSumber: $source\nStatus: $status"
            val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, draft) }
            context.startActivity(Intent.createChooser(intent, "Bagikan draf berita"))
        }, modifier = Modifier.weight(1f)) { Text("Bagikan draf") }
        OutlinedButton(onClick = {
            tts.language = Locale("id", "ID")
            tts.speak("$headline. $body", TextToSpeech.QUEUE_FLUSH, null, "infokini-sports")
        }, modifier = Modifier.weight(1f)) { Text("Bacakan naskah") }
    }
    Text("Modul ini membuat draf manual dan menggunakan TTS bawaan Android. Pengumpulan berita otomatis, skor langsung, verifikasi AI, dan publikasi otomatis belum aktif. Draf harus diperiksa redaktur sebelum ditayangkan.", color = Color(0xFFB8C4D9), fontSize = 11.sp)
}

@Composable
private fun SportsField(label: String, value: String, onChange: (String) -> Unit, hint: String) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        BasicTextField(value = value, onValueChange = onChange,
            modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)).padding(12.dp),
            textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFF172033), fontSize = 14.sp),
            decorationBox = { inner -> Box { if (value.isEmpty()) Text(hint, color = Color.Gray); inner() } })
    }
}
