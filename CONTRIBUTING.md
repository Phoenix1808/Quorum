# Contributing to Quorum

Thanks for your interest! Quorum is a learning-focused, open-source project — beginners and first-time contributors are very welcome.

## New to the project?

1. Read the [README](README.md) for the big picture.
2. New to DAOs/Web3? Read [docs/CONCEPTS.md](docs/CONCEPTS.md) — it explains everything from scratch.
3. Understand the structure via [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Local setup

### Backend
```bash
cd backend
npm install
# create .env:  PORT=3000  and  REDIS_URL=rediss://<your-upstash-url>  (free at upstash.com)
npm run dev
```

### Android
```bash
cd android            # open in Android Studio
```
- Add a [Reown](https://dashboard.reown.com) Project ID to `android/local.properties`:
  `WALLET_PROJECT_ID=your_id`
- The app points at the live backend by default (`ApiClient.kt`), so you can skip running the backend for UI work.

## How to contribute

1. **Find/open an issue.** For anything non-trivial, comment first so we don't duplicate work.
2. **Branch** off `main`.
3. **Keep PRs focused** — one thing per PR.
4. **Match the existing style** — see below.
5. Open a PR describing *what* changed and *why*.

## Code style

**Backend (TypeScript)**
- Layered: `route → controller → service → client`. Keep controllers thin; logic goes in services.
- Conventional commits: `feat(backend): ...`, `fix(...): ...`, `chore: ...`.

**Android (Kotlin/Compose)**
- One screen = a `Screen` composable + a `ViewModel` exposing a `StateFlow<UiState>`.
- Model UI state with a sealed interface (`Loading / Success / Error`).
- Keep secrets in `local.properties` (never commit them).

## Good first contributions

- UI polish: empty/error states, loading indicators, better spacing.
- Add empty-state handling to more screens.
- Docs improvements — if something confused you, fix it for the next person.
- Add a new supported DAO in `backend/src/config/dao.ts` (verify the Snapshot space id first).

## Questions?

Open an issue. No question is too basic — this project exists partly to help people learn.
