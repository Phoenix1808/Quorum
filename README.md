<p align="center">
  <img src="docs/logo.svg" width="120" alt="Quorum logo" />
</p>

<h1 align="center">Quorum</h1>

<p align="center"><b>Mobile-first DAO Governance</b></p>

<p align="center">
  Browse DAO proposals, follow your favourite DAOs, and connect your wallet —<br/>
  all from a native Android app. An open-source attempt to bring <b>DAO governance to mobile</b>.
</p>

---

Quorum aggregates governance proposals from real DAOs (**Uniswap, Aave, ENS**) into one clean Android app, powered by [Snapshot](https://snapshot.box). Today there's no good native Android app for DAO governance — Quorum is here to fix that.

> 📸 _Screenshots coming soon._

<!-- Screenshots: add images to docs/screenshots/ then uncomment
<p align="center">
  <img src="docs/screenshots/feed.png" width="240" alt="Proposal Feed" />
  <img src="docs/screenshots/discovery.png" width="240" alt="DAO Discovery" />
  <img src="docs/screenshots/wallet.png" width="240" alt="Wallet Connect" />
</p>
-->

---

## 🤔 New to DAOs? Start here

A **DAO** (Decentralized Autonomous Organization) is an internet community with no single boss — members make decisions by **voting**. Big protocols like Uniswap, Aave, and ENS are run this way.

**Snapshot** is where most of this voting happens — it's free and "gasless" (you just sign a message with your wallet, no transaction fee).

Quorum brings all of this to your phone: see what's being voted on, follow the DAOs you care about, and connect your wallet.

👉 **Full beginner-friendly explainer:** [docs/CONCEPTS.md](docs/CONCEPTS.md)

---

## ✨ Features

| Feature | Status |
|---------|--------|
| 📋 **Proposal Feed** — browse proposals from Uniswap/Aave/ENS, filter by All/Active/Closed | ✅ |
| 📄 **Proposal Detail** — full description, live results per choice, deadline | ✅ |
| 🔍 **DAO Discovery** — browse DAOs (followers, proposal counts) + follow/unfollow | ✅ |
| 👛 **Wallet Connect** — connect any wallet via WalletConnect/Reown, see your address | ✅ |
| 🔔 **Deadline notifications** — backend cron detects 24h/1h windows *(delivery wiring in v1.1)* | 🚧 |
| 🗳️ **Gasless voting** — sign & submit votes to Snapshot (EIP-712) | 🔮 v1.1 |
| 📊 **Vote history** — your past votes + stats | 🔮 v1.1 |

---

## 🧱 Tech Stack

| Layer | Stack |
|-------|-------|
| **Android** | Kotlin · Jetpack Compose · MVVM · Retrofit · Coil · Navigation · Reown AppKit (WalletConnect) |
| **Backend** | Node.js · TypeScript · Express · Redis (Upstash) · node-cron · deployed on Render |
| **Web3 data** | Snapshot GraphQL API |

The backend is a **thin proxy**: it fetches DAO data from Snapshot, caches it in Redis, and stores app data (users, follows). The Android app talks only to the backend (+ the wallet for connecting).

👉 **How it all fits together:** [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)

---

## 📁 Repo Structure

```
Quorum/
├── android/          # Native Android app (Kotlin + Compose)
├── backend/          # Node.js + TypeScript API (deployed on Render)
├── docs/
│   ├── CONCEPTS.md       # DAO / Snapshot / gasless voting — explained from scratch
│   ├── ARCHITECTURE.md   # How the app + backend + Snapshot fit together
│   └── PLAN.md           # Original project plan & notes
├── CONTRIBUTING.md
└── README.md
```

---

## 🚀 Getting Started

### Backend
```bash
cd backend
npm install
# create .env with:  PORT=3000  and  REDIS_URL=rediss://<your-upstash-url>
npm run dev
```
Server runs on `http://localhost:3000` (health check: `GET /health`).
Live instance: `https://quorum-t5uv.onrender.com`

### Android
```bash
cd android
```
1. Open in Android Studio.
2. Add your [Reown](https://dashboard.reown.com) Project ID to `android/local.properties`:
   ```
   WALLET_PROJECT_ID=your_project_id_here
   ```
3. Set the backend URL in `data/remote/ApiClient.kt` (defaults to the live instance).
4. Run on a device/emulator.

---

## 🔌 API Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/proposals?spaces=ens.eth&state=active&page=1` | List proposals (filter + paginate) |
| `GET` | `/api/proposals/:id` | Single proposal detail |
| `GET` | `/api/daos` | Supported DAOs + live metadata |
| `POST` | `/api/users` | Register user + FCM token |
| `POST` | `/api/users/:address/follow` | Follow a DAO |
| `DELETE` | `/api/users/:address/follow/:daoId` | Unfollow |
| `GET` | `/api/users/:address/follows` | List follows |

---

## 🗺️ Roadmap

- **v1 (current):** Browse + filter proposals, DAO discovery + follow, wallet connect
- **v1.1:** Gasless voting (EIP-712 → Snapshot), push notification delivery (FCM), vote history
- **v2:** On-chain voting (Tally), analytics/charts, more DAOs, multi-chain

---

## 🤝 Contributing

Contributions welcome! See [CONTRIBUTING.md](CONTRIBUTING.md). This is a learning-focused, open-source project — issues, ideas, and PRs are all appreciated.

## 📄 License

MIT — see [LICENSE](LICENSE).
