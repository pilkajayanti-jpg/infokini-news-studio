# InfoKini Studio

**Nama aplikasi:** InfoKini Studio  
**Kredit di dalam aplikasi:** Powered by Wandi Azis  
**Platform:** Android  
**Package:** `id.infokini.news`

InfoKini Studio adalah studio produksi konten berita untuk membantu pengguna menyusun materi berita, mengolah foto/video, membuat grafis skor, dan menyiapkan narasi. Kredit penggagas hanya ditampilkan pada antarmuka aplikasi dan tidak dimaksudkan sebagai watermark pada video/foto hasil ekspor.

## Fitur yang sudah tersedia pada source

- Studio video manual dengan pilihan template berita, judul, lokasi, teks berjalan, pemilihan video, pratinjau, pemotongan awal/akhir, pilihan rasio 16:9 atau 9:16, dan ekspor MP4 melalui Android Media3 Transformer.
- Pemilihan musik latar dari berkas perangkat dan pengulangan musik saat ekspor jika komposisi didukung oleh format media.
- Editor foto dengan kontrol kecerahan, kontras, saturasi, rotasi, flip horizontal/vertikal, teks/caption, opsi watermark InfoKini, simpan/muat setelan, dan ekspor foto/poster.
- Text-to-Speech Android untuk membacakan naskah berbahasa Indonesia, dengan pilihan suara yang tersedia di perangkat dan gaya penyampaian.
- Modul Reporter/Redaktur berbasis alur kerja manual: pencarian topik melalui tautan publik, pencatatan dua URL sumber, fakta/naskah, penandaan verifikasi, penyimpanan draf, dan persetujuan redaksi sebelum materi Reporter disiapkan untuk video. Persetujuan memerlukan dua URL HTTP(S) berbeda, tetapi aplikasi belum memeriksa apakah penerbitnya benar-benar independen atau isi laporannya saling menguatkan.
- Pencarian berita otomatis berdasarkan topik melalui Google News RSS, menampilkan hingga 15 judul, tanggal terbit, penerbit, dan tautan sumber; pengguna dapat membuka sumber asli atau memasukkan judul/tautan ke formulir redaksi. Ini bukan pemantauan semua platform atau verifikasi fakta otomatis.\n- Pintasan penelusuran topik ke Bing News, YouTube, TikTok, Facebook, X, Instagram melalui pencarian web, Reddit, dan Google Trends.
- Grafis skor pertandingan dengan pengisian data manual dan ekspor grafis.
- Kredit “Powered by Wandi Azis” pada antarmuka aplikasi.

## Belum tersedia / membutuhkan pekerjaan lanjutan

- Pemantauan berita viral otomatis lintas platform, pengambilan data terjadwal, dan analisis tren berbasis API. Pencarian Google News RSS per topik sudah tersedia, tetapi bukan pemantauan terjadwal.
- AI Redaktur yang benar-benar menghasilkan naskah/video secara otomatis, pemeriksaan fakta otomatis, dan integrasi model AI.
- Presenter virtual dengan karakter fiktif orisinal, animasi, dan sinkronisasi gerak bibir.
- Paket musik original InfoKini yang diproduksi khusus dan disertai dokumentasi hak penggunaan. Musik yang dipilih sendiri oleh pengguna harus memiliki lisensi yang sesuai.
- Integrasi siaran langsung langsung dari aplikasi ke YouTube, TikTok, dan Facebook.
- Penggabungan beberapa klip, transisi lanjutan, subtitle otomatis, pengaturan timeline multi-track, dan sistem proyek video penuh.
- Data skor olahraga langsung dari penyedia resmi.
- Sistem akun, cloud sync, backend, monetisasi iklan, dan komisi marketplace.

## Catatan editorial dan hak cipta

Pencarian melalui tautan publik bukan verifikasi. Redaksi harus membuka sumber asli, memeriksa tanggal dan konteks, membandingkan laporan independen, meminta konfirmasi pihak terkait bila relevan, dan membedakan fakta dari klaim. Jangan mengarang kejadian, angka, kutipan, atau tren. Buat narasi sendiri; jangan menyalin artikel, foto, video, musik, atau logo pihak lain tanpa izin/lisensi yang sesuai. Simpan tautan sumber dalam catatan produksi.

## Privasi dan keamanan

Aplikasi menggunakan pemilih dokumen Android untuk media yang dipilih pengguna. Periksa `app/src/main/AndroidManifest.xml` dan konfigurasi Gradle untuk daftar izin serta target Android terbaru. Pengaturan keamanan dasar yang diterapkan termasuk menonaktifkan Android backup dan cleartext HTTP traffic. Ini bukan audit keamanan menyeluruh. Sebelum rilis, lakukan pengujian pada perangkat sasaran, tinjau seluruh dependensi, siapkan kebijakan privasi, dan pastikan setiap integrasi eksternal menjelaskan data yang dikirim.

Jangan menyimpan API key rahasia di source code atau APK. Gunakan backend dengan pengelolaan rahasia untuk integrasi API yang memerlukannya.

## Build Android

Workflow GitHub Actions: `.github/workflows/android-apk.yml`. Buka tab **Actions** pada repositori untuk melihat hasil build dan mengunduh artifact APK setelah workflow berhasil.

Build yang sukses hanya membuktikan source berhasil dikompilasi dan artifact diunggah. Itu tidak membuktikan semua fitur telah berjalan baik pada semua perangkat. Uji instalasi, pemilihan media, pratinjau, ekspor MP4/foto, audio, dan perilaku aplikasi pada perangkat Android sasaran sebelum distribusi.

Repository: https://github.com/pilkajayanti-jpg/infokini-news-studio
