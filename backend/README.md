# InfoKini AI Backend

Backend minimal untuk tombol **Buat Naskah Video dengan AI** di aplikasi Android. Menggunakan OpenAI Responses API. API key hanya disimpan sebagai secret di Cloudflare Worker, tidak di APK atau Git.

## Deploy ke Cloudflare Workers

1. Buat akun Cloudflare dan buka Workers & Pages.
2. Buat Worker bernama \`infokini-ai-backend\`.
3. Salin isi \`worker.js\` ke editor Worker dan deploy.
4. Pada Settings → Variables and Secrets, tambahkan secret:
   - \`OPENAI_API_KEY\`: API key dari OpenAI Platform.
   - (Opsional) \`OPENAI_MODEL\`: model API yang tersedia pada akun Anda; default di kode adalah \`gpt-6.2-astra\`.
5. Tambahkan aturan rate limiting di Cloudflare untuk endpoint \`/api/news-script\` (misalnya batasi per IP) dan pantau penggunaan. Jangan publikasikan endpoint tanpa pembatasan.
6. Uji \`https://NAMA-WORKER.SUBDOMAIN.workers.dev/health\`; nilai \`configured\` harus \`true\`.
7. Di aplikasi InfoKini Studio, buka AI Reporter, isi alamat backend HTTPS, topik, dan fakta, lalu tekan **BUAT NASKAH VIDEO DENGAN AI**.

## Endpoint

\`POST /api/news-script\`

Request:
\`\`\`json
{
  "topic": "Topik berita",
  "facts": "Fakta dan hasil konfirmasi redaksi",
  "sources": ["https://sumber-utama.example", "https://sumber-pembanding.example"]
}
\`\`\`

Response:
\`\`\`json
{
  "headline": "Judul draf",
  "ticker": "Teks berjalan",
  "script": "Naskah narasi video",
  "verification_notes": "Catatan hal yang perlu diverifikasi",
  "status": "draft_only"
}
\`\`\`

## Batasan penting

- Backend hanya menyusun draf dari fakta yang dimasukkan redaksi. Ia tidak membuka URL sumber, tidak memantau platform sosial, dan tidak membuktikan kebenaran klaim.
- AI tidak dapat menyetujui berita. Verifikasi dan persetujuan redaksi tetap wajib dilakukan di aplikasi sebelum materi dipakai.
- API OpenAI ditagihkan terpisah dari langganan ChatGPT. Atur batas penggunaan dan pantau biaya di akun API.
- Jangan memasukkan data pribadi sensitif atau informasi rahasia ke bahan berita.
- Endpoint ini membatasi ukuran input dan metode, tetapi rate limiting harus dikonfigurasi pada Cloudflare.
