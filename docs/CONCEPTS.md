# Concepts — DAO Governance Explained From Scratch

This doc explains everything Quorum touches, assuming **zero Web3 knowledge**. If you know what a DAO, Snapshot, and gasless voting are, you can skip this.

---

## 1. What is a DAO?

**DAO = Decentralized Autonomous Organization.**

Think of a normal company: a CEO/boss makes the decisions. A DAO is an internet community with **no single boss** — members make decisions together by **voting**.

Example: **Uniswap** (a large crypto exchange) is a DAO. It has a treasury worth millions. Who decides how that money is spent? The members — by voting.

**How do you get the right to vote?** By holding the DAO's **token**:
- Uniswap's token = `UNI`
- Aave's token = `AAVE`
- ENS's token = `ENS`

The more tokens you hold, the more **voting power** you have (like shares in a company).

---

## 2. Proposals & Voting

A **proposal** is a suggestion the community votes on. For example:

> "Take 100,000 USDC from the treasury and give it as a grant to developers."

Members pick one option:
- **For** ✅
- **Against** ❌
- **Abstain** 😐

When the deadline passes, the majority wins. That's **DAO governance**. Quorum shows you these proposals on your phone.

---

## 3. On-chain vs Off-chain (the key idea)

A **blockchain** (like Ethereum) is a public, permanent ledger. Writing to it has two flavours:

|  | **On-chain** | **Off-chain** |
|--|-------------|---------------|
| Where the vote lives | On the blockchain (a real transaction) | Off the chain (just a signed message) |
| Cost | **Gas fee** (real money 💸) | **Free** (gasless) 🎉 |
| Binding? | Yes — it auto-executes | No — it's a signal/poll |

**Gas** is the fee you pay to write to a blockchain. On-chain voting costs gas; off-chain voting is free.

---

## 4. Snapshot (what Quorum uses)

**Snapshot** is a platform for **off-chain, gasless voting**. Most DAOs use it to gauge community sentiment cheaply.

- Free — you just **sign a message** with your wallet (no gas).
- Each DAO has a "space" (e.g., `ens.eth`).
- Quorum reads all its data from Snapshot's public **GraphQL API**.

> There's also **Tally** (on-chain, binding voting) — that's on Quorum's v2 roadmap. Note: this "Tally" (tally.xyz) is unrelated to the Indian accounting software of the same name.

---

## 5. Wallets & WalletConnect

A **wallet** (like MetaMask) is your crypto identity. It holds your tokens and your **private key** (your secret signing key — never share it).

**WalletConnect** (now **Reown**) is how a mobile app connects to your wallet:
- You tap "Connect", pick your wallet (or scan a QR).
- A secure channel is set up via a **relay server**.
- Your wallet shares your **public address** with the app — but **never** your private key.

Quorum uses this so it can (eventually) ask your wallet to sign a vote — the private key stays inside your wallet the whole time.

---

## 6. Gasless Voting & EIP-712 (v1.1)

When you vote on Snapshot, your wallet signs a **structured message** using a standard called **EIP-712**.

- **EIP-712** = an Ethereum standard for signing **human-readable, structured data**.
- Instead of signing a scary blob of hex, your wallet shows you readable fields: `proposal`, `choice`, `space`, etc. — so you know exactly what you're approving.
- Signing is **not** a transaction, so it's **free** (gasless). Snapshot stores the signature off-chain.

> Voting is planned for Quorum **v1.1**. It needs the voter to hold the DAO's token (voting power), which is why it's a later milestone.

---

## 7. What Quorum does with all this

```
Snapshot (all the DAO data)
      ↓
Quorum backend  ── fetches + caches proposals/DAOs, stores your follows
      ↓
Quorum Android app  ── shows proposals, lets you follow DAOs, connect wallet
```

In one line: **Quorum takes the DAO governance that normally lives on desktop websites and brings it to a clean, mobile-first Android app.**

---

Still confused about a term? Open an issue — if it wasn't clear to you, it isn't clear to others either, and we should fix the docs.
