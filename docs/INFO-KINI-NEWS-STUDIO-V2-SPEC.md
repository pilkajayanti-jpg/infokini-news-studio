# InfoKini News Studio V2 — Product & Technical Specification

## Product decision (confirmed 2026-11-27)
- Product: InfoKini News Studio
- Build: new-from-scratch V2 on branch `studio-v2`; preserve `main` as the previous project/archive.
- Platform: Android application, minimum Android 9 (API 28) as the primary supported floor; test on Vivo 1902 / Android 9.
- Operating mode: hybrid. Core editing, saved projects, templates, media library, TTS where device voices exist, and manual graphics work offline. News discovery, AI Redaktur, cloud features and platform streaming need internet.
- Visual direction: television newsroom / broadcast control room. Deep navy, red, white and cyan; clear high-contrast labels; touch-friendly mobile layout.
- Budget: free-first. Do not require a paid subscription for core local workflows. External AI, hosting, platform APIs and advanced presenter rendering must be optional and disclosed.
- Explicit exclusion: NO Reporter AI. Do not create an AI reporter/reporter agent feature.
- Editorial control: AI Redaktur may research and draft, but must not publish, export or broadcast a news item without explicit user approval.

## Main navigation
1. Beranda
2. Live Studio
3. Video Studio
4. Photo Studio
5. AI Redaktur
6. Template Berita
7. Narasi / Text-to-Speech
8. Grafis Olahraga
9. Promosi UMKM
10. Proyek Saya
11. Galeri Media
12. Pengaturan
13. Panduan & Diagnostik

## Feature requirements

### Live Studio / TV broadcast
- Broadcast preview and preflight checks for camera, microphone, network, and stream destination.
- Overlay composition: channel logo, headline, breaking-news label, running ticker, lower-third, reporter name, location, clock and optional sponsor panel.
- Scene presets: opening, main story, interview, field report, closing.
- Camera front/back, microphone controls, local video/photo/audio playback, full-screen preview.
- YouTube RTMP first. Keep a provider adapter for Facebook and TikTok; enable each only through official/authorized endpoints, account eligibility and platform rules.
- Protect stream keys; do not log or expose secrets. Prefer Android Keystore encrypted local storage. Explain that anyone with the key can broadcast.
- Streaming is online-only and depends on network/device encoder support.

### Video Studio
- Import multiple clips and images using Android document picker.
- Multi-clip timeline foundation: reorder, trim, split, delete, undo/redo.
- Transitions, crop/scale, 16:9, 9:16, 1:1.
- Headline, ticker, lower-third, captions/subtitles, logo/watermark, location and date overlays.
- Import/record voice-over and music; volume, mute and fade/mix where supported.
- Preview and export MP4/H.264, 720p and 1080p when hardware supports it; quality/frame-rate options; save/share.
- Never claim a feature is implemented until it works in a real build/device test.

### Photo Studio
- Crop, rotate, flip, brightness, contrast, saturation, text, logo, watermark, layers, poster and thumbnail templates.
- Export JPG/PNG to device gallery.

### InfoKini news templates
- Original InfoKini broadcast identity; do not imitate CNN, Kompas TV or another broadcaster's exact branding.
- Categories: headline, breaking news, local/regional, government/politics, economy/UMKM, society, education, sports, environment, investigation, interview, live report, advertisement/community announcement.
- Broadcast packages: lower-third, ticker, full-screen headline, quote card, map card, data chart, infographic, weather and score graphic.
- Export variants for 16:9, 9:16 and 1:1.

### AI Redaktur (no Reporter AI)
- Discover current news/topics from traceable sources, with date, publisher and original URL where available.
- Topic and regional filters; source comparison; distinguish fact, allegation, opinion and unverified claims.
- Draft headlines, article structure, summaries, language cleanup and broadcast scripts.
- Maintain source notes and a verification checklist; flag missing corroboration and stale/ambiguous information.
- Never fabricate events, quotes, numbers, sources or trends.
- No autonomous publication, export or broadcast. Require an explicit editorial approval action.
- AI provider must be configurable; API secrets belong on a backend, never in APK/source. Clearly disclose external data transmission and any usage costs.
- RSS/public search is discovery, not fact-checking or proof of virality.

### Narasi / Text-to-Speech
- Indonesian TTS using Android installed voices first; selectable available voices, speed/pitch preview and audio output.
- Optional remote voices only with clear consent and cost disclosure.

### Additional modules
- Sports scoreboards: manual input first; schedules/results/live data only when a reliable licensed source is configured.
- UMKM promotion: product photo, name, price, description and contact; poster/video templates.
- Optional digital presenter/avatar: original fictional presenter only, no impersonation; later phase and optional external service. This is not an AI reporter.
- Projects, autosave, duplicate, media gallery, local export management, backup/restore.
- Settings for channel identity, logo, watermark, language, quality, privacy and permissions.
- Help, tutorials, feature status and diagnostics.
- Monetization (ads/commissions) and cloud accounts are later optional phases, not prerequisites for core offline use.

## Technical direction
- Native Android Kotlin + Jetpack Compose.
- Minimum SDK 28 (Android 9); test on Vivo 1902.
- Keep UI responsive on low-memory devices; avoid heavy background work and provide export progress/cancel.
- Use Android document picker / scoped storage; request only permissions needed at point of use.
- Use Media3 for playback and evaluate Media3 Transformer for export; validate compatibility on Android 9.
- Use a modular package structure: core/designsystem, core/storage, feature/home, live, video, photo, editorial, templates, voice, sports, umkm, settings.
- Keep API clients behind interfaces; no embedded secrets.
- Unit tests for domain logic and build checks in GitHub Actions.
- Accessibility: Indonesian labels, large tap targets, contrast, clear empty/error/loading states.

## Delivery stages and acceptance gates
1. V2 foundation: app shell, navigation, theme, local project storage, media picker; builds and opens on Android 9.
2. Broadcast graphics: reusable InfoKini overlay/template editor and preview.
3. Video/photo production: real export and tested file output.
4. Live streaming: YouTube RTMP with preflight and protected key; device/network tests.
5. AI Redaktur: source discovery, grounded draft workflow, explicit approval; no Reporter AI.
6. TTS, sports, UMKM, optional presenter, diagnostics and final hardening.

A milestone is complete only when its APK is built successfully and the relevant feature is tested. A successful compilation alone does not prove every feature works.
