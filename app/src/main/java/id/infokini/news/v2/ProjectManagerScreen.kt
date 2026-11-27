package id.infokini.news.v2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
    var message by remember { mutableStateOf("Proyek tersimpan secara lokal di perangkat.") }

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
            val saved = repository.save(StudioProject(editingId, title, description, category, 0L))
            val index = projects.indexOfFirst { it.id == saved.id }
            if (index >= 0) projects[index] = saved else projects.add(0, saved)
            projects.sortByDescending { it.updatedAt }
            resetForm()
            message = "Proyek berhasil disimpan."
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("PROYEK SAYA", color = Color(0xFF27C5E8), fontWeight = FontWeight.Black, fontSize = 20.sp)
        Text("Buat, edit, lanjutkan, dan hapus proyek produksi berita tanpa akun cloud.", color = Color(0xFF9FB3C9), fontSize = 12.sp)
        OutlinedTextField(value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Nama proyek") }, singleLine = true)
        OutlinedTextField(value = category, onValueChange = { category = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Kategori: Berita / Olahraga / UMKM / Lainnya") }, singleLine = true)
        OutlinedTextField(value = description, onValueChange = { description = it }, modifier = Modifier.fillMaxWidth().height(100.dp), label = { Text("Catatan dan bahan produksi") })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { saveProject() }, modifier = Modifier.weight(1f), enabled = title.isNotBlank()) {
                Text(if (editingId.isBlank()) "Simpan proyek" else "Perbarui proyek")
            }
            OutlinedButton(onClick = { resetForm(); message = "Formulir proyek baru." }) { Text("Baru") }
        }
        Text(message, color = Color(0xFFFFC857), fontSize = 11.sp)
        Text("DAFTAR PROYEK • ${projects.size}", color = Color(0xFF27C5E8), fontWeight = FontWeight.Bold, fontSize = 11.sp)
        if (projects.isEmpty()) {
            Text("Belum ada proyek. Isi nama proyek lalu tekan Simpan proyek.", color = Color(0xFF9FB3C9))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(projects, key = { it.id }) { project ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF10243D))) {
                        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(project.title, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(project.category, color = Color(0xFF27C5E8), fontSize = 10.sp)
                            if (project.description.isNotBlank()) Text(project.description, color = Color(0xFF9FB3C9), fontSize = 12.sp)
                            Text("Diperbarui: ${java.text.DateFormat.getDateTimeInstance().format(java.util.Date(project.updatedAt))}", color = Color(0xFF9FB3C9), fontSize = 9.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = {
                                    editingId = project.id
                                    title = project.title
                                    description = project.description
                                    category = project.category
                                    message = "Mengedit: ${project.title}"
                                }) { Text("Edit") }
                                OutlinedButton(onClick = {
                                    repository.delete(project.id)
                                    projects.removeAll { it.id == project.id }
                                    if (editingId == project.id) resetForm()
                                    message = "Proyek dihapus."
                                }) { Text("Hapus") }
                            }
                        }
                    }
                }
            }
        }
    }
}
