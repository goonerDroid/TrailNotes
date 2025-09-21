# TrailNotes — Offline‑First Sample


An Android sample that proves the "DB as source of truth" approach: Room + Paging3 for UI, WorkManager to keep the DB fresh, Koin for DI, DataStore for knobs.


## Why
Offline‑first UX builds trust, reduces latency, and saves battery/data. This app shows a pragmatic way to do it.


## Stack
Compose • Room • Paging3 • WorkManager • DataStore • Koin • Retrofit


## Key ideas
- UI pages from Room; background workers sync with server.
- Write locally first, mark as `PENDING`, batch upload.
- Periodic pull for freshness; conflict policy pluggable.


## Run
1. Open in Android Studio Giraffe+.
2. Replace `baseUrl` in `NetworkModule`.
3. Run the app; use the stubbed UI; inspect WorkManager logs.