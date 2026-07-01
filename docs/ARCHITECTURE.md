# Architecture

How Quorum's pieces fit together, and why they're built this way.

---

## Big picture

```
┌─────────────────────────┐        ┌──────────────────────────┐        ┌─────────────┐
│   Android App            │  REST  │   Backend (Node/TS)       │ GraphQL│  Snapshot    │
│   (Kotlin + Compose)     │───────▶│   "thin proxy + cache"    │───────▶│  (DAO data)  │
│                          │◀───────│                           │◀───────│              │
└───────────┬─────────────┘        │  ├─ Redis: cache + users   │        └─────────────┘
            │                       │  └─ Cron: deadline checks  │
            │ WalletConnect         └──────────────────────────┘
            ▼
┌─────────────────────────┐
│  User's Wallet (MetaMask)│   ← app connects to it; signing/voting is client-side
└─────────────────────────┘
```

**Data flows one way for reads:** Snapshot → Backend → App.
**The wallet is separate:** the app connects to it directly (via WalletConnect); the backend never touches wallets or keys.

---

## Backend — "thin proxy + cache"

The backend does **not** own the DAO data — Snapshot does. Its jobs:

1. **Proxy + normalize** — fetch proposals/DAOs from Snapshot's GraphQL API and return clean, consistent JSON.
2. **Cache** — store responses in Redis (short TTL for active data) so we don't hammer Snapshot and stay under its rate limits.
3. **Own app data** — users and their DAO follows live in Redis (hash + set).
4. **Notifications** — a `node-cron` job scans followed DAOs' active proposals for 24h/1h deadline windows (delivery via FCM is wired in v1.1).

### Layered structure
```
routes/       → URL definitions
controllers/  → request/response handling (thin)
services/     → business logic (fetch, cache, normalize)
clients/      → external connections (Snapshot GraphQL, Redis)
jobs/         → cron (deadline reminders)
config/       → DAO list, constants
```
Each layer only calls the one directly below it. Adding a feature follows the same path.

### Why a backend at all?
The app *could* call Snapshot directly, but the backend gives us: caching (speed + rate-limit safety), a stable API contract, notifications (cron needs a server), and a place to store per-user data (follows).

---

## Android — MVVM + Compose

```
data/
  model/        → data classes (mirror the backend JSON)
  remote/       → Retrofit API + client
  repository/   → wraps the API, returns clean data to ViewModels
ui/
  feed/         → FeedScreen + FeedViewModel
  detail/       → ProposalDetailScreen + DetailViewModel
  discovery/    → DiscoveryScreen + DiscoveryViewModel
  wallet/       → WalletScreen (Reown AppKit)
  components/   → reusable composables (ProposalCard)
  navigation/   → bottom nav + routes (in MainActivity)
```

**Unidirectional data flow (per screen):**
```
Repository → ViewModel (holds StateFlow<UiState>) → Screen (@Composable observes state)
```
Each screen has a `UiState` sealed interface (`Loading / Success / Error`). The ViewModel fetches via the Repository and updates its `StateFlow`; the Composable observes it with `collectAsStateWithLifecycle()` and renders accordingly. State changes → recomposition → UI updates. No manual view manipulation.

### Wallet (Reown AppKit)
`WalletConnect` (Reown) is initialized once in `QuorumApplication`. The connect modal (`AppKitComponent`) lives in a bottom sheet; `appKitState.isConnected` and `AppKit.getAccount()` drive the wallet UI. Signing/voting (v1.1) happens client-side — the app builds an EIP-712 message, the wallet signs it, and the signed vote is submitted to Snapshot's sequencer.

---

## A read request, end to end (Proposal Feed)

```
1. FeedScreen opens → FeedViewModel.init → Repository.getProposals()
2. Repository → ApiClient → GET /api/proposals?state=active
3. Backend controller → service → checks Redis cache
      hit  → return cached JSON
      miss → query Snapshot GraphQL → normalize → cache in Redis → return
4. App receives JSON → Gson → data classes → ViewModel state = Success(proposals)
5. FeedScreen recomposes → LazyColumn of ProposalCards
```

---

## Key decisions

| Decision | Why |
|----------|-----|
| Redis for user data (not MongoDB) | Data is simple (follows = a set); avoids extra DB setup |
| Thin backend | Snapshot is the source of truth; we just proxy + cache + personalize |
| Compose | Modern, less boilerplate, reactive state fits this UI |
| Snapshot-first (voting deferred) | Off-chain reads work for everyone; voting needs token/voting power to test |
