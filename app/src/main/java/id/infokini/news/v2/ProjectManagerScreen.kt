package id.infokini.news.v2

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ProjectManagerScreen() {
    val context = LocalContext.current
    val repository = remember(context) { StudioProjectRepository(context) }
    val projects = remember(context) { mutableStateListOf<StudioProject>().apply { addAll(repository.load()) } }
    var editingId by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Berita") }
    var search by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("Proyek tersimpan lokal. Buat cadangan agar data dapat dipindahkan.") }
    var deleteTarget by remember { mutableStateOf<StudioProject?>(null) }

    val exportBackup = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use {
                    it.write(repository.exportJson())
                } ?: error("Tidak dapat membuka lokasi penyimpanan.")
                message = "Cadangan proyek berhasil diekspor."
            } catch (error: Exception) {
                message = "Gagal mengekspor: ${error.localizedMessage ?: "kesalahan file"}"
            }
        }
    }
    val importBackup = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                val raw = context.contentResolver.openInputStream(uri)?.use { input ->
                    val bytes = input.readBytes()
                    require(bytes.size <= 1_000_000) { "File cadangan melebihi 1 MB." }
                    bytes.toString(Charsets.UTF_8)
                } ?: error("Tidak dapat membaca file.")
                val count = repository.importJson(raw)
                projects.clear()
                projects.addAll(repository.load())
                message = "$count proyek berhasil dipulihkan/digabungkan."
            } catch (error: Exception) {
                message = "Gagal mengimpor: ${error.localizedMessage ?: "format cadangan tidak valid"}"
            }
        }
    }

    fun resetForm() {
        editingId = ""
        title = ""
        description = ""
        category = "Berita"
    }
    fun saveProject() {
        if (title.isBlank()) {
            message = "Nama proyek wajib diisi."
        } else {
            try {
                val saved = repository.save(StudioProject(editingId, title, description, category, 0L))
                val index = projects.indexOfFirst { it.id == saved.id }
                if (index >= 0) projects[index] = saved else projects.add(0, saved)
                projects.sortByDescending { it.updatedAt }
                resetForm()
                message = "Proyek berhasil disimpan."
            } catch (error: Exception) {
                message = error.localizedMessage ?: "Tidak dapat menyimpan proyek."
            }
        }
    }

    val visibleProjects = projects.filter {
        it.title.contains(search, true) || it.category.contains(search, true) || it.description.contains(search, true)
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("PROYEK SAYA", color = Color(0xFF27C5E8), fontWeight = FontWeight.Black, fontSize = 20.sp)
        Text("Kelola naskah dan bahan produksi. Data tersimpan di perangkat; ekspor cadangan JSON untuk memindahkannya ke HP lain.", color = Color(0xFF9FB3C9), fontSize = 12.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { exportBackup.launch("InfoKini-Proyek-Cadangan.json") }, modifier = Modifier.weight(1f)) { Text("Ekspor cadangan") }
            OutlinedButton(onClick = { importBackup.launch(arrayOf("application/json", "text/*")) }, modifier = Modifier.weight(1f)) { Text("Pulihkan cadangan") }
        }
        OutlinedTextField(value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Nama proyek") }, singleLine = true)
        OutlinedTextField(value = category, onValueChange = { category = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Kategori: Berita / Olahraga / UMKM / Lainnya") }, singleLine = true)
        OutlinedTextField(value = description, onValueChange = { description = it }, modifier = Modifier.fillMaxWidth().height(90.dp), label = { Text("Catatan dan bahan produksi") })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { saveProject() }, modifier = Modifier.weight(1f), enabled = title.isNotBlank()) {
                Text(if (editingId.isBlank()) "Simpan proyek" else "Perbarui proyek")
            }
            OutlinedButton(onClick = { resetForm(); message = "Formulir proyek baru." }) { Text("Baru") }
        }
        Text(message, color = Color(0xFFFFC857), fontSize = 11.sp)
        OutlinedTextField(value = search, onValueChange = { search = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Cari nama, kategori, atau isi proyek") }, singleLine = true)
        Text("DAFTAR PROYEK • ${visibleProjects.size} / ${projects.size}", color = Color(0xFF27C5E8), fontWeight = FontWeight.Bold, fontSize = 11.sp)
        if (visibleProjects.isEmpty()) {
            Text("Tidak ada proyek yang cocok. Buat proyek baru atau ubah kata pencarian.", color = Color(0xFF9FB3C9))
        } else {
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(visibleProjects, key = { it.id }) { project ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF10243D))) {
                        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(project.title, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(project.category, color = Color(0xFF27C5E8), fontSize = 10.sp)
                            if (project.description.isNotBlank()) Text(project.description, color = Color(0xFF9FB3C9), fontSize = 12.sp)
                            Text("Diperbarui: ${java.text.DateFormat.getDateTimeInstance().format(java.util.Date(project.updatedAt))}", color = Color(0xFF9FB3C9), fontSize = 9.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(onClick = {
                                    editingId = project.id
                                    title = project.title
                                    description = project.description
                                    category = project.category
                                    message = "Mengedit: ${project.title}"
                                }) { Text("Edit") }
                                OutlinedButton(onClick = {
                                    try {
                                        val copy = repository.duplicate(project.id)
                                        if (copy != null) {
                                            projects.add(0, copy)
                                            message = "Salinan proyek berhasil dibuat."
                                        } else {
                                            message = "Proyek sumber tidak ditemukan."
                                        }
                                    } catch (error: Exception) {
                                        message = error.localizedMessage ?: "Gagal menggandakan proyek."
                                    }
                                }) { Text("Duplikat") }
                                OutlinedButton(onClick = { deleteTarget = project }) { Text("Hapus") }
                            }
                        }
                    }
                }
            }
        }
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Hapus proyek?") },
            text = { Text("Proyek '${target.title}' akan dihapus dari penyimpanan perangkat. Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(onClick = {
                    try {
                        repository.delete(target.id)
                        projects.removeAll { it.id == target.id }
                        if (editingId == target.id) resetForm()
                        message = "Proyek berhasil dihapus."
                    } catch (error: Exception) {
                        message = error.localizedMessage ?: "Gagal menghapus proyek."
                    } finally {
                        deleteTarget = null
                    }
                }) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Batal") }
            }
        )
    }
}
