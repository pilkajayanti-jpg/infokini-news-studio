# InfoKini News Studio — Matriks Fitur

Dokumen ini memisahkan halaman/menu, fungsi yang sudah tersedia, pekerjaan implementasi, dan dependensi eksternal. Keberadaan menu tidak berarti semua fungsi di dalamnya sudah aktif.

## Produksi dan proyek
- Proyek Saya: buat/buka proyek, simpan otomatis, duplikasi, riwayat proyek.
- Galeri Media: impor dan kelola video, foto, logo, audio, serta hasil ekspor.
- Editor Video: pratinjau klip, judul, ticker infokini.news, naskah, musik.
- Timeline & Klip: multi-track, trim, split, susun ulang, transisi, undo/redo.
- Audio & Musik: impor/rekam voice-over, volume, fade, mixing, mute.
- Teks & Subtitle: headline, lower-third, ticker, subtitle bertiming, animasi teks.
- Ekspor Video: MP4/H.264, 720p/1080p, frame rate, kualitas, simpan/bagikan.

## Foto dan grafis
- Editor Foto: pilih foto, kecerahan, kontras, saturasi, rotasi, pratinjau, ekspor JPG.
- Desain Grafis: crop, flip, teks, logo/watermark, thumbnail/poster, lapisan.
- Ekspor Foto: JPG/PNG, kualitas, nama file, simpan Galeri.
- Template Studio: paket grafis acara, format berita, rasio layar.
- Grafis Olahraga: nama tim dan papan skor manual.
- Paket Grafis: breaking news, peta, grafik data, infografik, cuaca.

## Redaksi
- AI Redaktur: pencarian topik/berita terbaru dari sumber yang dapat ditelusuri.
- Sumber Berita: URL asli, metadata yang tersedia, pembandingan sumber independen.
- Draf & Verifikasi: draf berbantuan AI, pemisahan fakta/klaim, daftar periksa, persetujuan redaksi.
- Aturan: jangan mengarang berita atau tren; jangan terbitkan/ekspor otomatis tanpa persetujuan pengguna.

## Presenter dan audio
- Presenter Digital: presenter Indonesia dengan wajah orisinal, tidak meniru orang nyata.
- Avatar: gerak, ekspresi, sinkronisasi bibir, render video.
- Narator & Suara: TTS Bahasa Indonesia, pilihan suara yang tersedia, kecepatan, pratinjau, simpan audio.

## Siaran langsung dan sistem
- Siaran Langsung: ruang persiapan live dan pemeriksaan perangkat.
- Koneksi Live: YouTube, TikTok, Facebook, RTMP; autentikasi resmi dan pengamanan stream key.
- Pengaturan: identitas kanal, logo, watermark, bahasa, kualitas, penyimpanan, izin dan privasi.
- Panduan & Bantuan: tutorial, status fitur, kebutuhan internet/API, diagnostik.

## Status implementasi saat ini
- Sudah ada pada kode: navigasi utama, halaman menu, pemilihan/pratinjau video, judul dan ticker, pemilihan/pratinjau foto, penyesuaian dasar foto, ekspor JPG, pencarian headline RSS, TTS lokal, papan skor manual, tautan platform live.
- Halaman/menu baru yang ditambahkan: Proyek Saya, Galeri Media, Timeline & Klip, Audio & Musik, Teks & Subtitle, Ekspor Video, Desain Grafis, Ekspor Foto, Sumber Berita, Draf & Verifikasi, Presenter Digital, Narator & Suara, Paket Grafis, Koneksi Live, Pengaturan, Panduan & Bantuan. Sebagian masih berupa halaman rancangan/keterangan fitur.
- Belum dianggap selesai: editor timeline multi-klip, pemotongan/transisi, mixing audio, ekspor MP4, desain foto berlapis, AI generatif dan pemeriksaan fakta otomatis, avatar manusia dengan lip-sync, integrasi live streaming resmi.

## Dependensi yang perlu disiapkan
- AI Redaktur: penyedia model AI, backend aman, pencarian/sumber berita yang sah, kebijakan penyimpanan kunci API.
- Presenter avatar: layanan generasi avatar/video dan biaya render; jangan simpan API key di aplikasi Android.
- Live: OAuth/API resmi masing-masing platform dan izin akun; RTMP memerlukan endpoint dan stream key yang disimpan aman.
- Ekspor video: implementasi pipeline media Android, pengujian format, performa dan ruang penyimpanan pada perangkat sasaran.
