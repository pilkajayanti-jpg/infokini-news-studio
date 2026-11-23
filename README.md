# InfoKini News Studio

Paket proyek Android awal untuk aplikasi InfoKini News Studio.

## Fitur yang tersedia pada source ini
- Editor judul berita dan nama reporter
- Editor running text
- Memilih video dan foto dari galeri
- Pratinjau video dasar
- Identitas merek `infokini.news`

## Belum tersedia
- Ekspor MP4 final dengan ticker/logo/lower-third tertanam
- Siaran langsung langsung ke YouTube, TikTok, atau Facebook
- Intro otomatis pada video

Jadi paket ini **belum merupakan APK siap pasang**. APK perlu dibangun dan diuji terlebih dahulu.

## Membuat APK melalui GitHub
1. Buat repository baru di GitHub.
2. Unggah seluruh isi ZIP ini ke repository (termasuk folder `.github`).
3. Buka tab **Actions**, jalankan workflow **Build Android APK**.
4. Setelah selesai, unduh artifact `InfoKini-debug-APK`.
5. Ekstrak artifact, lalu pasang `app-debug.apk` di Android. Jika diminta, izinkan pemasangan aplikasi dari sumber tersebut.

Build otomatis membutuhkan koneksi internet dan akun GitHub.
