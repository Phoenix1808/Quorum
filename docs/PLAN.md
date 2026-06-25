# Quorum — DAO Voting App (Project Plan & Concept Notes)

> **One-liner:** Pehla proper open-source **native Android** app jo DAO governance ko mobile-first banata hai — **Snapshot (off-chain, gasless) + Tally (on-chain, binding)** dono ek jagah.

This doc is the *understanding-first* reference. Code baad mein — pehle concepts aur scope locked.

---

## 1. Glossary — har concept simple words mein

### DAO (Decentralized Autonomous Organization)
Internet pe chalne wali community jiska koi single boss nahi. Members **token** hold karke **voting** se decisions lete hain.
- Token = voting power (zyada token = zyada votes). Uniswap ka token = `UNI`.
- Treasury = DAO ke funds (crypto mein), jinke baare mein voting hoti hai.

### Proposal & Vote
**Proposal** = ek suggestion jispe vote hota hai (e.g. "Treasury se 100k USDC grant do").
Members choose karte hain: **For** ✅ / **Against** ❌ / **Abstain** 😐. Deadline pe majority jeet jaati hai.

### On-chain vs Off-chain (CORE concept)
| | On-chain | Off-chain |
|---|---|---|
| Vote kahan store | Blockchain pe (real transaction) | IPFS pe (sirf signature) |
| Gas (fee) | **Lagta hai** 💸 (ETH mein) | **Free** (gasless) 🎉 |
| Binding? | Haan — auto execute hota hai | Nahi — sirf signal/opinion |
| Trust | Trustless (code khud chalta hai) | Multisig signers pe bharosa |

### Gas
Blockchain pe kuch bhi likhne ka "kiraya", ETH mein pay. Network busy = mehnga gas. Ye validators ko jaata hai.

### Wallet (MetaMask)
User ki crypto identity + tokens + ETH ka ghar. Private key isme. Isi se vote "sign" hota hai.

### WalletConnect
Tareeka jisse Android app, user ke MetaMask se QR code scan karke connect hota hai — bina private key dekhe (secure).

### EIP-712
Ek standard format jisme "sign karne wala message" readable banta hai (random hex ki jagah saaf-saaf dikhe user kya sign kar raha hai). Snapshot isi ko use karta hai gasless voting ke liye.

### Snapshot (off-chain tool)
Website + GraphQL API (free, no key). DAOs "space" banate hain (e.g. `uniswap.eth`). Members gasless vote dete hain. = "WhatsApp poll" (sentiment).

### Tally (on-chain tool) — NOTE
⚠️ Ye Indian accounting wala TallyPrime/Tally ERP **NAHI** hai. Naam same, cheez alag.
`tally.xyz` = on-chain Governor contracts ka interface. Binding votes, gas lagta hai. API: GraphQL (free API key chahiye). = "Official law pass + auto-enforce".

### Governor Contract
Blockchain pe chalne wala code (smart contract) jo on-chain votes count karta hai aur jeetne pe action execute karta hai. Tally inhi se baat karta hai.

### GraphQL
Data fetch karne ka modern style (REST ka alternative). **Client decide karta hai exactly kya data chahiye**, ek hi endpoint, ek hi call. Snapshot & Tally dono GraphQL dete hain.
- REST problem: over-fetching (zyada data) + N+1 calls. GraphQL dono solve karta hai.
- Hum apna GraphQL server nahi banate — bas Snapshot/Tally ke server se query maangte hain.

### FCM (Firebase Cloud Messaging)
Google ki free service — user ke phone pe push notification bheje, bina app khule.
Flow: App khulte hi FCM **token** (phone ka address) milta hai → backend ko bhejo → backend **Cron job** se deadline check karta hai → FCM ko bolta hai notification bhejne ko → Google phone tak deliver karta hai.

### Cron Job
Server pe timer/scheduler — automatically time-time pe code chalata hai ("har 10 min check karo"). Notifications ke liye must.

---

## 2. Why both Snapshot AND Tally? (Off-chain free hai toh on-chain paid kyun?)

**Off-chain vote sirf opinion hai — wo asli mein paisa move nahi kar sakta.** Sirf on-chain hi treasury funds nikaal sakta / contract change kar sakta hai.

4 reasons people pay gas for on-chain:
1. **Binding vs Signal** — Blockchain ko Snapshot votes ka pata hi nahi (alag store). Paisa hilane ke liye on-chain transaction zaroori.
2. **Trustless vs Trust** — Off-chain mein jeetne ke baad bhi insaan (multisig) manually execute karte hain → bharosa chahiye. On-chain mein code khud auto-execute karta hai.
3. **Permanent & censorship-proof** — On-chain permanent, edit/delete nahi. Snapshot servers band = data gaya.
4. **Stakes ka size** — Chhoti baat → Snapshot free. Crore ka decision → binding guarantee chahiye, gas ki fee uske saamne kuch nahi.

**Real funnel:** Snapshot (free filter/sentiment) → pass hua → Tally (on-chain binding execute).
DAOs sasta test pehle Snapshot pe karte hain, serious winners hi on-chain le jaate hain. Dono ka apna kaam — isi liye dono dikhana useful.
Aur: on-chain mostly bade **delegates** dete hain; aam user Snapshot pe. Isi liye **v1 = Snapshot-first**.

---

## 3. Scope — kya ship karenge

### v1 (MVP) — "beautiful read-mostly DAO companion + gasless voting" — LOCKED ✅

1. **Proposal Feed (Snapshot only)** — Uniswap/Aave/ENS proposals, status chip (Active/Closed/Pending), deadline countdown, vote bar, filter chips (All/Active/Closed), pagination.
2. **Proposal Detail** — full title+desc (markdown), choices, results as simple progress bars, "Connect Wallet to Vote".
3. **WalletConnect + Gasless Voting ⭐ (USP)** — wallet QR connect, EIP-712 sign → Snapshot gasless vote, success confirm + local save.
4. **DAO Discovery (basic)** — search + follow/unfollow (fixed list: Uniswap/Aave/ENS), followed DAOs priority in feed.
5. **Notifications (basic)** — deadline reminder (24h, 1h) via FCM for followed DAOs.
6. **Vote History (local)** — list of own votes (Room DB) + simple stats (total votes, participation count).

**Core loop:** app khole → followed DAOs ke active proposals dekhe → wallet connect → gaslessly vote → history track → deadline reminders. Bina paisa/risk real DAO voting mobile pe.

### Key v1 decisions (locked)
- **Backend = Thin proxy + cache.** Backend Snapshot GraphQL call karke Redis mein cache karke app ko deta hai + FCM bhejta hai. App bhi kaafi kaam khud karta hai. (Heavy DB-sync backend nahi.)
- **Wallet scope = sirf vote signing.** Voting power Snapshot API se read; on-chain balance read (Etherscan/Alchemy) v1 mein nahi.

### v2 (later)
- On-chain **Tally voting** (gas estimation, tx status tracking) — hard + real funds, isliye baad mein
- Full analytics/charts (MPAndroidChart), quorum tracker, advanced filters
- New-proposal alerts, real-time WebSocket, on-chain balance/voting-power display

### Non-goals (v1 mein OUT)
- Tally on-chain voting · Fancy charts · Quorum tracker · Proposal *create* · WebSocket real-time · New-proposal alerts (sirf deadline)
- Multi-chain beyond Ethereum mainnet

---

## 4. Target users
- **Primary:** Uniswap / Aave / ENS jaise bade DAOs ke existing token holders jo desktop pe vote karte hain aur mobile chahte hain. Android pe koi accha competitor nahi = ye wedge.
- **Secondary:** Crypto-curious devs jo repo star/contribute karenge (open-source).
- **v1 success metric:** "thousands of users" nahi — kuch real DAO members bolein *"main isse actually use karunga"* + clean open-source repo jo contributors laaye.

---

## 5. App flow (screen by screen)

```
Proposal Feed  →  Proposal Detail  →  WalletConnect  →  Sign & Vote (gasless)
(Snapshot data)   (choices+results)   (QR scan)         (EIP-712, Room DB save)

Bottom nav: [Feed] [DAO Discovery] [Vote History] [Profile/Settings]
```
Background (invisible): WorkManager (poll new proposals) + Backend Cron + FCM (deadline alerts) + Room DB (offline cache).

---

## 6. Tech stack

| Layer | Stack |
|---|---|
| **Android** | Kotlin + Jetpack Compose, WalletConnect v2, Web3j, Retrofit, Room DB, MPAndroidChart |
| **Backend** | Node.js / TypeScript, Express, MongoDB, Redis (cache), FCM (push), Cron jobs |
| **Web3** | Snapshot GraphQL, Tally API, EIP-712 signing, Etherscan API, Alchemy RPC, WalletConnect |

---

## 7. 8-week build plan
| Week | Focus | Deliverables |
|---|---|---|
| 1 | Setup + architecture | Repo structure, Android init, backend scaffold, Snapshot API explore, README draft |
| 2 | Proposal Feed (backend) | Snapshot GraphQL integration, proposal model, Redis cache, REST endpoints |
| 3 | Proposal Feed (Android) | Compose feed UI, detail screen, filter chips, pagination |
| 4 | Wallet + signing | WalletConnect v2 SDK, EIP-712 typed signing, off-chain Snapshot voting |
| 5 | On-chain voting (Tally) | Tally API, on-chain submit, gas estimation, tx status *(v2 — slip ok)* |
| 6 | Notifications + DAO discovery | FCM, WorkManager jobs, DAO search, follow/unfollow |
| 7 | Vote history + analytics | Personal stats, charts, Room DB cache, quorum tracker |
| 8 | Polish + OSS launch | Bug fixes, README, CONTRIBUTING.md, issues, demo APK, good-first-issue labels |

> **Note:** v1 ke liye Week 5 (Tally on-chain) ko v2 mein push karna theek hai — Snapshot-first ship karo.
