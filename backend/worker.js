const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type",
  "Content-Type": "application/json; charset=utf-8",
  "Cache-Control": "no-store"
};

function json(data, status = 200) {
  return new Response(JSON.stringify(data), { status, headers: corsHeaders });
}

function textFromResponse(data) {
  if (typeof data.output_text === "string" && data.output_text.trim()) return data.output_text;
  for (const item of data.output || []) {
    for (const part of item.content || []) {
      if (part.type === "output_text" && part.text) return part.text;
    }
  }
  return "";
}

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    if (request.method === "OPTIONS") return new Response(null, { status: 204, headers: corsHeaders });
    if (url.pathname === "/health" && request.method === "GET") {
      return json({ ok: true, service: "InfoKini AI backend", configured: Boolean(env.OPENAI_API_KEY) });
    }
    if (url.pathname !== "/api/news-script") return json({ error: "Not found" }, 404);
    if (request.method !== "POST") return json({ error: "Method not allowed" }, 405);
    if (!env.OPENAI_API_KEY) return json({ error: "Backend belum memiliki OPENAI_API_KEY." }, 503);
    if (!env.OPENAI_MODEL) return json({ error: "Backend belum memiliki OPENAI_MODEL yang valid. Atur model API yang tersedia di akun OpenAI Anda." }, 503);

    const declaredLength = Number(request.headers.get("content-length") || 0);
    if (declaredLength > 24000) return json({ error: "Permintaan terlalu besar." }, 413);

    let body;
    try { body = await request.json(); }
    catch { return json({ error: "JSON tidak valid." }, 400); }

    const topic = typeof body.topic === "string" ? body.topic.trim().slice(0, 300) : "";
    const facts = typeof body.facts === "string" ? body.facts.trim().slice(0, 12000) : "";
    const sources = Array.isArray(body.sources)
      ? body.sources.filter(x => typeof x === "string").slice(0, 2).map(x => x.slice(0, 1000))
      : [];
    if (!topic || !facts) return json({ error: "Topik dan fakta wajib diisi." }, 400);

    const instructions = [
      "Anda adalah asisten penulis naskah video berita untuk redaksi InfoKini di Indonesia.",
      "Tugas hanya menyusun DRAF naskah video berdasarkan fakta yang diberikan pengguna.",
      "Jangan mengarang fakta, angka, kutipan, kronologi, identitas, lokasi, atau tanggapan.",
      "Jika informasi tidak cukup, nyatakan kekurangan itu di field verification_notes dan gunakan bahasa bersyarat.",
      "URL sumber yang diberikan hanya metadata rujukan; Anda tidak membuka atau memverifikasi isi URL tersebut.",
      "Jangan menyatakan berita telah terverifikasi. Jangan menyalin artikel sumber.",
      "Tulis Bahasa Indonesia yang lugas, berimbang, tidak menghakimi, dan cocok dibacakan presenter.",
      "Kembalikan hanya JSON valid dengan field headline, ticker, script, verification_notes."
    ].join(" ");

    const input = [
      "TOPIK: " + topic,
      "FAKTA / CATATAN REDAKSI:\n" + facts,
      "URL RUJUKAN (belum dibaca/diverifikasi oleh backend):\n" + sources.join("\n"),
      "Buat judul singkat, running text singkat, dan naskah video orisinal 45-90 detik. Jangan menambah fakta di luar bahan."
    ].join("\n\n");

    let upstream;
    try {
      upstream = await fetch("https://api.openai.com/v1/responses", {
        method: "POST",
        headers: {
          "Authorization": "Bearer " + env.OPENAI_API_KEY,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          model: env.OPENAI_MODEL,
          instructions,
          input,
          max_output_tokens: 1200,
          text: { format: { type: "json_object" } }
        })
      });
    } catch {
      return json({ error: "Tidak dapat terhubung ke penyedia AI." }, 502);
    }

    const raw = await upstream.text();
    if (!upstream.ok) {
      return json({ error: "Penyedia AI menolak permintaan.", provider_status: upstream.status }, 502);
    }
    let result;
    try { result = JSON.parse(raw); }
    catch { return json({ error: "Respons penyedia AI tidak valid." }, 502); }

    const output = textFromResponse(result);
    let draft;
    try { draft = JSON.parse(output); }
    catch { return json({ error: "AI tidak mengembalikan format draf yang benar." }, 502); }

    const headline = typeof draft.headline === "string" ? draft.headline.trim().slice(0, 180) : "";
    const ticker = typeof draft.ticker === "string" ? draft.ticker.trim().slice(0, 500) : "";
    const script = typeof draft.script === "string" ? draft.script.trim().slice(0, 6000) : "";
    const verification_notes = typeof draft.verification_notes === "string"
      ? draft.verification_notes.trim().slice(0, 2000) : "";
    if (!script) return json({ error: "Naskah kosong dari penyedia AI." }, 502);

    return json({ headline, ticker, script, verification_notes, status: "draft_only" });
  }
};
