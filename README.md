# Quorum — Mobile-first DAO Governance

> A native Android app that brings **DAO governance to mobile** — browse proposals, follow DAOs, and vote **gaslessly** on Snapshot, all in one place. Supports **Snapshot (off-chain)** and (planned) **Tally (on-chain)**.

There's no good native Android app for DAO governance today. Quorum is an open-source attempt to fix that — starting with the people who actually vote: members of **Uniswap, Aave, and ENS**.

---

## Why Quorum?

- **Snapshot off-chain + (planned) Tally on-chain** — both in one place
- **Gasless voting** — vote on Snapshot by signing a message, no gas fee
- **Mobile-first** — track proposals, deadlines, and results from your phone
- **Open source** — built to learn from and contribute to

> **Snapshot vs Tally (quick mental model):** Snapshot = free, off-chain "opinion poll" (just sign). Tally = on-chain, binding vote that actually moves treasury funds (costs gas). Most big DAOs use both — Snapshot to gauge sentiment, Tally for the binding decision. See [docs/PLAN.md](docs/PLAN.md) for the full concept notes.

---

## Tech Stack

| Layer | Stack |
|-------|-------|
| **Backend** | Node.js · TypeScript · Express · Redis (Upstash) · node-cron |
| **Web3 data** | Snapshot GraphQL API |
| **Notifications** | FCM (Firebase Cloud Messaging) — *planned, wired with Android* |
| **Android** (planned) | Kotlin · Jetpack Compose · WalletConnect v2 · Retrofit · Room |

---

## Architecture

```
Android App ──► Backend (thin proxy + cache) ──► Snapshot GraphQL
                     │
                     ├── Redis: caching + users + follows
                     └── Cron: deadline checks ──► FCM (push)
```

The backend is a **thin proxy**: it fetches DAO data from Snapshot, caches it in Redis, and stores its own data (users, follows). Voting itself happens **client-side** (the app signs and submits directly to Snapshot — the backend never touches private keys).

---

## Backend — Setup

```bash
cd backend
npm install
```

Create a `backend/.env` file (see `.env.example`):

```
PORT=3000
REDIS_URL=rediss://<your-upstash-redis-url>
```

Run in dev (auto-reload):

```bash
npm run dev
```

Server starts on `http://localhost:3000`. Check `GET /health`.

---

## API Reference

### Proposals
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/proposals?spaces=ens.eth&state=active&page=1` | List proposals (filter by space + state, paginated) |
| `GET` | `/api/proposals/:id` | Full detail of a single proposal |

### DAOs
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/daos` | Supported DAOs + live metadata (followers, avatar, proposal count) |

### Users & Follows
| Method | Endpoint | Body | Description |
|--------|----------|------|-------------|
| `POST` | `/api/users` | `{ address, fcmToken }` | Register a user (wallet address) + FCM token |
| `POST` | `/api/users/:address/follow` | `{ daoId }` | Follow a DAO |
| `DELETE` | `/api/users/:address/follow/:daoId` | — | Unfollow a DAO |
| `GET` | `/api/users/:address/follows` | — | List a user's followed DAOs |

> Supported DAOs (`daoId`): `ens`, `aave`, `uniswap` — see [backend/src/config/dao.ts](backend/src/config/dao.ts).

---

## Project Status & Roadmap

### ✅ Backend v1 — Done
- [x] **Proposal Feed** — fetch from Snapshot, filter by space/state, pagination, Redis caching
- [x] **Proposal Detail** — single proposal with full body, cached
- [x] **DAO Discovery** — curated DAO list enriched with live Snapshot metadata
- [x] **Users & Follows** — registration + follow/unfollow stored in Redis (hash + set)
- [x] **Deadline Reminders** — `node-cron` job scans followed DAOs' active proposals, detects 24h/1h windows, dedupes — currently logs `WOULD SEND`

### 🚧 In Progress / Deferred
- [ ] **FCM push delivery** — actual notification send (wired alongside the Android app, since it needs a device to receive)

### 📱 Next — Android app (v1)
- [ ] Proposal Feed UI (Jetpack Compose) + filter chips + pagination
- [ ] Proposal Detail screen
- [ ] WalletConnect v2 — connect wallet, **gasless Snapshot voting** (EIP-712 signing)
- [ ] DAO Discovery + follow/unfollow
- [ ] Push notifications (FCM) + local vote history (Room)

### 🔮 v2 — Later
- [ ] **Tally on-chain voting** (gas estimation, transaction tracking)
- [ ] Charts / analytics, quorum tracker
- [ ] DAO list caching, more DAOs, multi-chain

---

## Repo Structure

```
Quorum/
├── backend/          # Node.js + TS API (this is built)
│   └── src/
│       ├── routes/         # URL definitions
│       ├── controllers/    # request/response handlers
│       ├── services/       # business logic (snapshot, proposals, users, notifications)
│       ├── clients/        # external connections (Snapshot, Redis)
│       ├── jobs/           # cron jobs (deadline reminders)
│       └── config/         # DAO list, constants
├── docs/
│   └── PLAN.md       # concept notes + detailed plan
└── README.md
```

---

## Contributing

This is a learning-focused open-source project — contributions and questions welcome. Good first issues will be labelled once the Android app lands.

## License

MIT (to be added)
