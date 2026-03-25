# House Money — XION Demo Apps

Demo wallet applications for the [XION blockchain](https://xion.burnt.com/), showcasing session-key authentication via Abstraxion, native Rust signing via the [mob library](https://github.com/burnt-labs/mob), and stablecoin on/off-ramp via [Brale](https://brale.xyz).

---

## Table of Contents

- [Repository Structure](#repository-structure)
- [Architecture Overview](#architecture-overview)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Configuration Reference](#configuration-reference)
- [Session Key Authentication (Abstraxion)](#session-key-authentication-abstraxion)
- [Native Signing via mob](#native-signing-via-mob)
- [Stablecoin On/Off-Ramp (Brale)](#stablecoin-onoff-ramp-brale)
- [Brale Proxy API Reference](#brale-proxy-api-reference)
- [Wallet State Machine](#wallet-state-machine)
- [Data Models](#data-models)
- [Security Model](#security-model)
- [Project Structure](#project-structure)
- [Building mob from Source](#building-mob-from-source)
- [Troubleshooting](#troubleshooting)

---

## Repository Structure

```
apps/
├── xion-android-demo/     # Android (Kotlin/Compose) wallet app
├── xion-ios/              # iOS (SwiftUI) wallet app
├── brale-proxy/           # Node.js proxy for Brale API (holds API secrets)
└── README.md              # This file
```

---

## Architecture Overview

```
┌───────────────────────────────────────────────────────────────────────┐
│                          XION Testnet                                │
│                        (xion-testnet-2)                              │
│                                                                       │
│   RPC: rpc.xion-testnet-2.burnt.com:443                              │
│   REST: api.xion-testnet-2.burnt.com                                 │
│   Abstraxion: auth.testnet.burnt.com                                 │
└──────────┬────────────────────────┬───────────────────────────────────┘
           │                        │
     RPC/REST                  On-chain stablecoin
     (Tendermint JSON-RPC,     mint/burn via Brale
      Cosmos LCD REST)         custodial address
           │                        │
    ┌──────▼──────────┐     ┌───────▼───────────┐
    │  Mobile Apps    │     │  Brale Proxy       │
    │                 │────▶│  (Express.js)      │
    │  Android: Kotlin│     │                    │
    │  iOS: SwiftUI   │◀────│  Holds API secrets │
    │                 │     │  Token caching     │
    │  mob (Rust FFI) │     │  Transfer guard    │
    └─────────────────┘     └───────┬────────────┘
                                    │ OAuth2 Client Credentials
                                    ▼
                            ┌───────────────┐
                            │  Brale API    │
                            │  Transfers    │
                            │  Plaid Link   │
                            │  Addresses    │
                            └───────────────┘
```

**Three components:**

1. **Mobile apps** — connect directly to XION RPC/REST for blockchain operations (balance, send, contract execution) via the mob Rust library (compiled as native code via UniFFI)
2. **Brale proxy** — sits between mobile apps and Brale's API. Holds OAuth2 client credentials, manages bearer token lifecycle, enforces a transfer-type allowlist to prevent accidental mainnet usage
3. **XION testnet** — Cosmos SDK chain with account abstraction. The Abstraxion dashboard handles OAuth login and authz grant creation

---

## Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| Android Studio | 2024+ | Android build and emulator |
| JDK | 17 | Kotlin compilation |
| Android SDK | API 34 | Target SDK |
| Xcode | 15+ | iOS build and simulator |
| Node.js | 18+ | Brale proxy server |
| Rust | 1.75+ | Only if rebuilding mob from source |

---

## Quick Start

### 1. Start the Brale Proxy

```bash
cd brale-proxy
cp .env.example .env
# Edit .env with your Brale credentials (see Configuration Reference below)
npm install
npm start
```

Verify: `curl http://localhost:3000/health` should return `{"status":"ok","account_id":"configured"}`.

### 2. Run the Android App

```bash
cd xion-android-demo
# Open in Android Studio → Run on emulator
# Or from command line:
./gradlew assembleDebug
```

The emulator proxy URL defaults to `http://10.0.2.2:3000/` (Android emulator's host loopback). For physical device testing, update `BRALE_PROXY_URL` in `app/build.gradle.kts` to your machine's LAN IP.

### 3. Run the iOS App

```bash
cd xion-ios
xcodebuild -project XionDemo.xcodeproj -scheme XionDemo \
  -destination 'platform=iOS Simulator,name=iPhone 16 Pro' \
  -derivedDataPath build build
xcrun simctl install booted build/Build/Products/Debug-iphonesimulator/XionDemo.app
xcrun simctl launch booted com.burnt.xiondemo.ios
```

---

## Configuration Reference

### Brale Proxy Environment Variables (`brale-proxy/.env`)

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `BRALE_CLIENT_ID` | Yes | — | OAuth2 client ID from Brale dashboard |
| `BRALE_CLIENT_SECRET` | Yes | — | OAuth2 client secret from Brale dashboard |
| `BRALE_ACCOUNT_ID` | Yes | — | Your KYB-verified Brale account ID (KSUID, 26 chars). Find via `GET /accounts` after authenticating |
| `BRALE_API_URL` | No | `https://api.brale.xyz` | Brale API base URL |
| `BRALE_AUTH_URL` | No | `https://auth.brale.xyz` | Brale OAuth2 token endpoint base |
| `PORT` | No | `3000` | Port the proxy server listens on |
| `ALLOWED_TRANSFER_TYPES` | No | `xion_testnet,ach_debit,ach_credit,same_day_ach_credit,rtp_credit` | Comma-separated allowlist. The proxy rejects any `POST /transfers` request whose source or destination `transfer_type` is not in this list. Set to `*` to disable (not recommended). This prevents accidental mainnet transfers since the same Brale credentials access both environments |

**How to get your Account ID:**
```bash
# Authenticate
TOKEN=$(curl -s -X POST "https://auth.brale.xyz/oauth2/token" \
  -H "Authorization: Basic $(echo -n 'CLIENT_ID:CLIENT_SECRET' | base64)" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" | jq -r '.access_token')

# List accounts
curl -s "https://api.brale.xyz/accounts" -H "Authorization: Bearer $TOKEN"
# → {"accounts":[{"id":"YOUR_ACCOUNT_ID","name":"...","status":"complete",...}]}
```

### Android BuildConfig Fields (`app/build.gradle.kts`)

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `XION_RPC_URL` | String | `https://rpc.xion-testnet-2.burnt.com:443` | Tendermint RPC endpoint. Used by mob for broadcasting transactions and querying chain state |
| `XION_REST_URL` | String | `https://api.xion-testnet-2.burnt.com/` | Cosmos LCD REST endpoint. Used for grant verification, transaction history fetching, and contract queries |
| `XION_CHAIN_ID` | String | `xion-testnet-2` | Chain ID passed to mob for transaction signing. Must match the chain the RPC/REST endpoints point to |
| `XION_TREASURY_ADDRESS` | String | `xion1sm3qp...hqa0mj` | Address that pays gas fees via the Cosmos fee grant module. Set by the Abstraxion dashboard during grant creation |
| `XION_OAUTH_CLIENT_ID` | String | `""` (empty) | OAuth2 client ID for the Abstraxion dashboard. Leave empty if using the default public client |
| `XION_OAUTH_AUTHORIZATION_ENDPOINT` | String | `https://auth.testnet.burnt.com/` | Base URL of the Abstraxion authentication dashboard |
| `BRALE_PROXY_URL` | String | `http://10.0.2.2:3000/` | URL of the Brale proxy server. `10.0.2.2` is Android emulator's host loopback. For physical devices, use your machine's LAN IP (e.g., `http://192.168.1.100:3000/`) |
| `BRALE_TRANSFER_TYPE` | String | `xion_testnet` | Brale transfer type for on-chain stablecoin operations. Use `xion_testnet` for testnet, `xion` for mainnet |
| `BRALE_STABLECOIN_DENOM` | String | `SBC` | Brale stablecoin denomination identifier used in transfer `value_type` fields |

### Android Constants (`util/Constants.kt`)

These are compile-time constants not exposed as BuildConfig (change by editing source):

| Constant | Value | Description |
|----------|-------|-------------|
| `COIN_DENOM` | `uxion` | On-chain denomination (micro-units). 1 XION = 1,000,000 uxion |
| `DISPLAY_DENOM` | `XION` | Human-readable denomination shown in UI |
| `GAS_PRICE` | `0.025` | Minimum gas price for transaction fee estimation |
| `ADDRESS_PREFIX` | `xion` | Bech32 address prefix for XION addresses |
| `DECIMALS` | `6` | Decimal places for uxion → XION conversion |
| `SESSION_GRANT_DURATION_SECONDS` | `86400` | Session key grant duration (24 hours) |
| `OAUTH_REDIRECT_URI` | `xiondemo://callback` | Deep link URI for OAuth callback. Must match the URL scheme registered in `AndroidManifest.xml` |
| `BRALE_ACH_DEBIT_TYPE` | `ach_debit` | Brale transfer type for pulling funds from a bank account |
| `BRALE_ACH_CREDIT_TYPE` | `same_day_ach_credit` | Brale transfer type for pushing funds to a bank account (same-day settlement) |
| `BRALE_FIAT_VALUE_TYPE` | `USD` | Fiat value type used in Brale transfer endpoints |

### iOS Configuration (`Constants.swift`)

iOS reads configuration from `Info.plist` first (settable via xcconfig), falling back to hardcoded defaults.

**To override via xcconfig:**
1. Create `XionDemo/Configuration/Debug.xcconfig`
2. Add entries: `XION_RPC_URL = https://rpc.xion-testnet-2.burnt.com:443`
3. In `Info.plist`, add: `<key>XION_RPC_URL</key><string>$(XION_RPC_URL)</string>`
4. `Constants.swift` reads it via `Bundle.main.infoDictionary`

| Constant | Default | Override Key |
|----------|---------|-------------|
| `rpcUrl` | `https://rpc.xion-testnet-2.burnt.com:443` | `XION_RPC_URL` |
| `restUrl` | `https://api.xion-testnet-2.burnt.com/` | `XION_REST_URL` |
| `chainId` | `xion-testnet-2` | `XION_CHAIN_ID` |
| `treasuryAddress` | `xion1sm3qp...hqa0mj` | `XION_TREASURY_ADDRESS` |
| `oauthAuthorizationEndpoint` | `https://auth.testnet.burnt.com/` | `XION_OAUTH_AUTHORIZATION_ENDPOINT` |
| `braleProxyUrl` | `http://localhost:3000/` | `BRALE_PROXY_URL` |
| `coinType` | `118` | — (edit source) |
| `derivationPath` | `m/44'/118'/0'/0/0` | — (edit source) |
| `sessionGrantDurationSeconds` | `86400` | — (edit source) |

---

## Session Key Authentication (Abstraxion)

XION uses account abstraction where users have **meta accounts** (smart contract accounts) that can delegate signing authority. The demo apps use **session keys** — temporary keypairs that are granted limited authority via the Cosmos `authz` module.

### Detailed Flow

```
┌──────────┐     ┌──────────────┐     ┌───────────────┐     ┌──────────┐
│  Mobile   │     │  Abstraxion  │     │  XION Chain   │     │   LCD    │
│  App      │     │  Dashboard   │     │  (Validators) │     │   REST   │
└─────┬─────┘     └──────┬───────┘     └───────┬───────┘     └────┬─────┘
      │                   │                     │                   │
      │ 1. Generate BIP39 │                     │                   │
      │    mnemonic        │                     │                   │
      │ 2. Derive secp256k1│                     │                   │
      │    keypair via     │                     │                   │
      │    m/44'/118'/0'/0/0                     │                   │
      │ 3. Get session key │                     │                   │
      │    address (xion1...)                    │                   │
      │                   │                     │                   │
      │ 4. Open browser ──▶│                     │                   │
      │    with grantee=   │                     │                   │
      │    session_address │                     │                   │
      │                   │ 5. User authenticates│                   │
      │                   │    (social login,    │                   │
      │                   │     email, etc.)     │                   │
      │                   │                     │                   │
      │                   │ 6. Create authz ────▶│                   │
      │                   │    grants:           │                   │
      │                   │    - GenericAuth for  │                   │
      │                   │      MsgSend         │                   │
      │                   │    - GenericAuth for  │                   │
      │                   │      MsgExecContract │                   │
      │                   │    + fee grant from  │                   │
      │                   │      treasury        │                   │
      │                   │                     │                   │
      │◀── 7. Callback ───│                     │                   │
      │    xiondemo://     │                     │                   │
      │    callback?addr=  │                     │                   │
      │    xion1meta...    │                     │                   │
      │                   │                     │                   │
      │ 8. Poll grants ───────────────────────────────────────────▶│
      │    GET /cosmos/authz/v1beta1/grants                        │
      │    ?granter=meta&grantee=session                           │
      │◀───────────────────────────────────────── 9. grants[] ─────│
      │                   │                     │                   │
      │ 10. Save to encrypted storage:          │                   │
      │     - session mnemonic (AES-256-GCM)    │                   │
      │     - meta account address              │                   │
      │     - session key address               │                   │
      │     - treasury address                  │                   │
      │     - session expiry timestamp          │                   │
      │                   │                     │                   │
      │ 11. State → Connected                   │                   │
```

### How Transactions Are Signed

When the app sends tokens or executes a contract, the actual message flow is:

1. App builds the inner message (e.g., `MsgSend` from meta account to recipient)
2. mob wraps it in `MsgExec` with the session key as the executor (grantee)
3. The transaction `fee` field includes `granter: treasury_address` (fee grant)
4. mob signs the outer transaction with the session key's private key
5. The signed transaction is broadcast via RPC
6. Validators verify: session key has authz grant from meta account, treasury has fee grant for session key
7. The meta account's balance is debited, treasury pays gas

```
Transaction Envelope:
├── body:
│   └── messages:
│       └── MsgExec:
│           ├── grantee: xion1session... (session key signs)
│           └── msgs:
│               └── MsgSend:
│                   ├── from_address: xion1meta... (meta account pays)
│                   ├── to_address: xion1recip...
│                   └── amount: [{denom: "uxion", amount: "1000000"}]
├── auth_info:
│   ├── signer_infos: [session key pubkey + signature]
│   └── fee:
│       ├── amount: [{denom: "uxion", amount: "25000"}]
│       ├── gas_limit: 1000000
│       └── granter: xion1treasury... (treasury pays gas)
```

### Session Expiry

- Grants are created with a 24-hour expiry (`SESSION_GRANT_DURATION_SECONDS`)
- The app runs a 60-second timer checking `sessionExpiresAt - now`
- When < 300 seconds remain: shows "Session expiring soon" warning
- When expired: auto-disconnects and clears session storage
- User must re-authenticate via Abstraxion to get new grants

---

## Native Signing via mob

[mob](https://github.com/burnt-labs/mob) is a Rust library compiled for each platform:

- **Android**: `.so` shared libraries for `arm64-v8a`, `armeabi-v7a`, `x86_64` (loaded via JNI)
- **iOS**: `libmob.xcframework` static library for `arm64` (device) and `arm64+x86_64` (simulator)

UniFFI generates idiomatic bindings — Kotlin classes for Android, Swift classes for iOS — from the Rust API.

### Key Types

| Type | Description |
|------|-------------|
| `ChainConfig` | Connection parameters: `chainId`, `rpcEndpoint`, `grpcEndpoint?`, `addressPrefix`, `coinType`, `gasPrice` |
| `Client` | RPC client. Methods: `getBalance()`, `getHeight()`, `send()`, `executeContract()`, `getTx()`, `getAccount()`, `getAllBalances()`, `isSynced()`, `getChainId()` |
| `Signer` | Key manager. Created from mnemonic via `Signer.fromMnemonic(mnemonic, addressPrefix, derivationPath?)`. Methods: `address()`, `publicKeyHex()`, `signBytes()` |
| `Coin` | `{denom: String, amount: String}` — represents a token amount |
| `TxResponse` | `{txhash, code, rawLog, gasWanted, gasUsed, height}` — code=0 means success |
| `MobError` | Rich error enum: `Rpc`, `Transaction`, `Signing`, `KeyDerivation`, `Address`, `Serialization`, `InvalidInput`, `Account`, `Network`, `GasEstimation`, `InsufficientFunds`, `Timeout`, `Generic` |

### App Integration Layer

The apps don't call mob directly. A service layer wraps mob for thread safety and type mapping:

**Android**: `RealMobDataSource` (implements `MobDataSource`) — uses `DispatchQueue`-equivalent background thread pool, maps `TxResponse` → `TransactionResult`, handles retry on cold-start failures

**iOS**: `MobSigningService` (implements `MobSigningServiceProtocol`) — uses `DispatchQueue(label: "com.burnt.xiondemo.mob")` for serial execution, maps mob types to app models via `withCheckedThrowingContinuation`, implements deferred cleanup (2-second delay) to prevent races with in-flight RPCs on disconnect

---

## Stablecoin On/Off-Ramp (Brale)

Brale provides stablecoin infrastructure on XION. The same Brale account and API credentials access **both testnet and mainnet** — the environment is determined by the `transfer_type` field in each transfer (`xion_testnet` vs `xion`).

### Onramp Flow (Buy Stablecoins)

```
User taps "Buy"
        │
        ▼
┌─ Bank linked? ─── No ──▶ Plaid Link SDK ──▶ Register bank ──▶ Save address_id ─┐
│       │                                                                          │
│      Yes                                                                         │
│       │◀─────────────────────────────────────────────────────────────────────────┘
│       ▼
│  Xion address registered with Brale?
│       │
│      No ──▶ POST /addresses/external {wallet_address, transfer_types: ["xion_testnet"]}
│       │
│      Yes
│       ▼
│  User enters USD amount
│       │
│       ▼
│  POST /transfers
│  {
│    amount: {value: "100", currency: "USD"},
│    source: {address_id: "<bank>", value_type: "USD", transfer_type: "ach_debit"},
│    destination: {address_id: "<xion_wallet>", value_type: "SBC", transfer_type: "xion_testnet"}
│  }
│       │
│       ▼
│  Brale pulls ACH from bank, mints stablecoins to user's Xion address
│       │
│       ▼
│  Poll GET /transfers/:id every 3s (with exponential backoff up to 30s)
│  Status: pending → processing → complete
```

### Offramp Flow (Cash Out)

```
User taps "Cash Out"
        │
        ▼
┌─ Bank linked? ─── No ──▶ Must link bank first (use Buy flow)
│       │
│      Yes
│       ▼
│  Fetch Brale internal (custodial) address for xion_testnet
│  GET /addresses?type=internal → find address with transfer_type "xion_testnet"
│       │
│       ▼
│  User enters stablecoin amount
│       │
│       ▼
│  STEP 1: On-chain transaction
│  App calls mob.send() to transfer stablecoins from user's wallet
│  to Brale's custodial address (xion1amma4dg7przheey2svzsxx6uwtfj7eq6cwpfr4)
│       │
│       ▼
│  STEP 2: Brale offramp
│  POST /transfers
│  {
│    amount: {value: "100", currency: "USD"},
│    source: {address_id: "<custodial>", value_type: "SBC", transfer_type: "xion_testnet"},
│    destination: {address_id: "<bank>", value_type: "USD", transfer_type: "same_day_ach_credit"}
│  }
│       │
│       ▼
│  Brale burns stablecoins, initiates ACH credit to bank
│  Poll status: pending → processing → complete
```

### Transfer Lifecycle

| Status | Meaning | What to do |
|--------|---------|------------|
| `pending` | Submitted, awaiting funds or review | Keep polling |
| `processing` | Funds are moving (ACH initiated or stablecoin minting) | Keep polling |
| `complete` | Finalized — funds arrived at destination | Show success, refresh balance |
| `canceled` | Manually canceled | Show error state |
| `failed` | Something went wrong | Show error, allow retry |

### ACH Limits

- **Inbound (onramp)**: $50,000 per ACH debit transaction
- **Outbound (offramp)**: No per-transaction limit for same-day ACH
- **Wire transfers**: No limit

---

## Brale Proxy API Reference

Base URL: `http://localhost:3000` (or wherever the proxy is deployed)

All requests and responses use `Content-Type: application/json`.

### `GET /health`

Health check.

**Response:**
```json
{"status": "ok", "account_id": "configured"}
```

### `POST /plaid/link-token`

Create a Plaid Link token for bank account linking.

**Request:**
```json
{
  "legal_name": "John Doe",
  "email_address": "john@example.com",
  "phone_number": "+15551234567",
  "date_of_birth": "1990-01-15"
}
```

**Response:**
```json
{
  "link_token": "link-sandbox-abc123...",
  "expiration": "2026-03-25T03:22:34.086Z"
}
```

Use the `link_token` with the Plaid Link SDK (Android/iOS) to let the user select their bank.

### `POST /plaid/register`

Exchange a Plaid public token (received from the Plaid Link SDK callback) for a Brale bank address.

**Request:**
```json
{
  "public_token": "public-sandbox-xyz789...",
  "transfer_types": ["ach_debit", "ach_credit", "same_day_ach_credit"]
}
```

**Response:**
```json
{
  "address_id": "36nftTpbLIOwLZwEC7UXjwUGrcc"
}
```

Store this `address_id` — it's used as the source (onramp) or destination (offramp) in transfers.

### `GET /addresses`

List all addresses on the Brale account.

**Query parameters:**
- `type` (optional): `"internal"` or `"external"`

**Response:**
```json
{
  "addresses": [
    {
      "id": "361A6HEwLLnJQuHIiWV8teFMcO1",
      "name": "on-chain custodial address",
      "status": "active",
      "type": "internal",
      "address": "xion1amma4dg7przheey2svzsxx6uwtfj7eq6cwpfr4",
      "transfer_types": ["xion_testnet"]
    },
    {
      "id": "36nftTpbLIOwLZwEC7UXjwUGrcc",
      "name": "BUS COMPLETE CHK",
      "status": "active",
      "type": "external",
      "transfer_types": ["ach_debit", "rtp_credit", "same_day_ach_debit"],
      "account_number": "****1039",
      "account_type": "checking"
    }
  ]
}
```

**Address types:**
- `internal`: Brale-managed custodial wallet (for deposit-before-offramp)
- `external`: User's blockchain wallet or bank account

### `POST /addresses/external`

Register a new blockchain wallet or bank account.

**Request (blockchain wallet):**
```json
{
  "name": "XION Wallet xion1abc...",
  "wallet_address": "xion1abc123def456...",
  "transfer_types": ["xion_testnet"]
}
```

**Request (bank account, direct entry):**
```json
{
  "owner": "Jane Doe",
  "account_number": "1234567890",
  "routing_number": "021000021",
  "name": "Chase Checking",
  "transfer_types": ["ach_credit", "same_day_ach_credit"],
  "account_type": "checking",
  "beneficiary_address": {
    "street_line_1": "100 Main St",
    "city": "New York",
    "state": "NY",
    "zip": "10001"
  },
  "bank_address": {
    "street_line_1": "270 Park Ave",
    "city": "New York",
    "state": "NY",
    "zip": "10017"
  }
}
```

### `GET /addresses/:id/balance`

Query the balance of an address.

**Query parameters:**
- `transfer_type` (optional): e.g., `"xion_testnet"`
- `value_type` (optional): e.g., `"SBC"`

**Response:**
```json
{
  "available": "1500.00",
  "pending": "0.00"
}
```

### `POST /transfers`

Create an onramp or offramp transfer.

**The proxy validates transfer types** against `ALLOWED_TRANSFER_TYPES` before forwarding to Brale. If a transfer type is not in the allowlist, the proxy returns:
```json
{
  "error": "Transfer type \"xion\" is not allowed. Allowed: xion_testnet, ach_debit, ..."
}
```

**Onramp request (ACH → stablecoin):**
```json
{
  "amount": {"value": "100", "currency": "USD"},
  "source": {
    "address_id": "36nftTpbLIOwLZwEC7UXjwUGrcc",
    "value_type": "USD",
    "transfer_type": "ach_debit"
  },
  "destination": {
    "address_id": "36nbkiAueXmgsFLSQHdEVJT1dP9",
    "value_type": "SBC",
    "transfer_type": "xion_testnet"
  }
}
```

**Offramp request (stablecoin → bank):**
```json
{
  "amount": {"value": "100", "currency": "USD"},
  "source": {
    "address_id": "361A6HEwLLnJQuHIiWV8teFMcO1",
    "value_type": "SBC",
    "transfer_type": "xion_testnet"
  },
  "destination": {
    "address_id": "36nftTpbLIOwLZwEC7UXjwUGrcc",
    "value_type": "USD",
    "transfer_type": "same_day_ach_credit"
  }
}
```

**Response:**
```json
{
  "id": "2xNL6PAF0cbcQHyjMQJ2RKRfbD9",
  "status": "pending",
  "amount": {"value": "100", "currency": "USD"},
  "source": {
    "address_id": "36nftTpbLIOwLZwEC7UXjwUGrcc",
    "value_type": "USD",
    "transfer_type": "ach_debit"
  },
  "destination": {
    "address_id": "36nbkiAueXmgsFLSQHdEVJT1dP9",
    "value_type": "SBC",
    "transfer_type": "xion_testnet"
  },
  "created_at": "2026-03-24T20:36:48.147Z",
  "updated_at": "2026-03-24T20:36:48.147Z"
}
```

### `GET /transfers/:id`

Poll transfer status.

**Response:** Same structure as `POST /transfers` response, with updated `status` and `updated_at`.

When complete, `source` and `destination` may include `transaction_id` (on-chain tx hash or payment reference).

### `GET /transfers`

List all transfers. Supports query parameters: `value_type`, `transfer_type`, `page[size]` (1-100, default 25).

### Error Format

All errors return:
```json
{
  "error": "Human-readable error message",
  "details": { ... }  // Brale API error body, if available
}
```

HTTP status codes mirror Brale's (400, 401, 404, 500, etc.).

### Token Lifecycle

The proxy manages Brale's OAuth2 bearer token automatically:
1. On first request, fetches a token via `POST /oauth2/token` (client credentials flow)
2. Caches in memory with a 55-minute TTL (Brale tokens expire after 60 minutes)
3. Refreshes automatically when the cached token is within 5 minutes of expiry
4. On 401 from Brale, the next request will re-authenticate

---

## Wallet State Machine

The app uses a sealed class / enum to track wallet connection state:

```
                            ┌──────────────┐
                            │ Disconnected │◀──────────────────────┐
                            └──────┬───────┘                       │
                                   │ authenticate()                │
                                   ▼                               │
                      ┌────────────────────────┐                   │
                      │ Connecting              │                   │
                      │                        │                   │
                      │  Steps:                │                   │
                      │  1. GENERATING_SESSION_KEY                 │
                      │  2. AUTHENTICATING      │                   │
                      │  3. SETTING_UP_GRANTS   │     disconnect() │
                      │  4. VERIFYING_GRANTS    │     or error     │
                      └────────────┬───────────┘                   │
                                   │ success                       │
                                   ▼                               │
                      ┌────────────────────────┐                   │
                      │ Connected               │───────────────────┘
                      │                        │   session expired
                      │  metaAccountAddress    │   or user disconnect
                      │  sessionKeyAddress     │
                      │  treasuryAddress       │
                      │  grantsActive: Bool    │
                      │  sessionExpiresAt: Long│
                      └────────────────────────┘
```

**State fields in Connected:**
- `metaAccountAddress` — the user's main XION account (displayed in UI, used as `granter` in authz)
- `sessionKeyAddress` — the temporary signing key (used as `grantee`, signs transactions)
- `treasuryAddress` — pays gas fees via fee grant module
- `grantsActive` — `false` if authz grants were revoked or expired mid-session
- `sessionExpiresAt` — Unix timestamp when the session expires

---

## Data Models

### TransactionResult (App model)

| Field | Type | Description |
|-------|------|-------------|
| `txHash` | String | Transaction hash (hex, 64 chars) |
| `success` | Boolean | `true` if `code == 0` |
| `gasUsed` | String | Actual gas consumed |
| `gasWanted` | String | Gas limit requested |
| `height` | Int64 | Block height where tx was included |
| `rawLog` | String | Validator response log (error details on failure) |
| `timestamp` | String | Block timestamp (ISO 8601, e.g., `2026-03-24T14:30:00Z`) |
| `fee` | String | Fee amount in micro-units (e.g., `"5000"` = 0.005 XION) |
| `txType` | String | Message type (e.g., `MsgExec`, `MsgSend`) |
| `amount` | String | Transfer amount in micro-units |
| `recipient` | String | Destination address |

### BraleTransfer

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | KSUID (26 chars, time-sortable) |
| `status` | String | `pending`, `processing`, `complete`, `failed`, `canceled` |
| `amount.value` | String | Dollar amount (e.g., `"100.00"`) |
| `amount.currency` | String | Always `"USD"` |
| `source.address_id` | String | Source address KSUID |
| `source.value_type` | String | `"USD"` (fiat) or `"SBC"` (stablecoin) |
| `source.transfer_type` | String | `"ach_debit"`, `"xion_testnet"`, etc. |
| `source.transaction_id` | String? | On-chain tx hash or payment reference (populated on completion) |
| `destination` | — | Same structure as `source` |
| `created_at` | String | ISO 8601 timestamp |
| `updated_at` | String | ISO 8601 timestamp (changes on each status transition) |

### BraleAddress

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | KSUID address identifier |
| `name` | String | Display name |
| `type` | String | `"internal"` (Brale custodial) or `"external"` (user-owned) |
| `address` | String? | Blockchain address or `null` for bank accounts |
| `status` | String | `"active"`, `"pending"`, `"archived"` |
| `transfer_types` | String[] | Supported operations (e.g., `["xion_testnet"]`, `["ach_debit", "ach_credit"]`) |
| `account_number` | String? | Masked bank account number (e.g., `"****1039"`) |
| `account_type` | String? | `"checking"` or `"savings"` |

---

## Security Model

### Secret Isolation

| Secret | Where it lives | Never exposed to |
|--------|---------------|-----------------|
| Brale `client_id` + `client_secret` | Proxy `.env` file | Mobile apps |
| Brale bearer token | Proxy memory (55-min cache) | Mobile apps, disk |
| Session mnemonic (Android) | EncryptedSharedPreferences + Android KeyStore AES-256-GCM | Plaintext on disk, other apps |
| Session mnemonic (iOS) | iOS Keychain (`kSecClassGenericPassword`) | Plaintext on disk, other apps |
| Session private key | In-memory only (mob Rust `Signer` object) | Disk, network |

### Transfer Type Allowlist

The proxy's `ALLOWED_TRANSFER_TYPES` environment variable prevents accidental mainnet transfers. Since the same Brale credentials access both testnet and mainnet, a misconfigured `transfer_type` could move real money. The proxy rejects any `POST /transfers` request containing a type not in the allowlist before it reaches Brale.

Default allowlist: `xion_testnet, ach_debit, ach_credit, same_day_ach_credit, rtp_credit`

To enable mainnet, add `xion` to the allowlist (after confirming all other config points to mainnet).

### Session Key Trust Model

- The session key can only execute operations that the meta account has explicitly granted via authz
- Grants have a time-limited expiry (24 hours by default)
- The treasury only pays gas for transactions signed by the session key (fee grant is scoped)
- If the session key is compromised, the attacker can only perform the granted operations until expiry
- The meta account holder can revoke grants at any time via the Abstraxion dashboard

---

## Project Structure

### Android App (`xion-android-demo/`)

```
app/src/main/java/com/burnt/xiondemo/
├── MainActivity.kt                     # Single-activity entry point
├── XionDemoApp.kt                      # Hilt application class
│
├── navigation/
│   └── NavGraph.kt                     # Compose Navigation host
│                                        # Routes: CONNECT, WALLET, CONTRACT,
│                                        #         HISTORY, ONRAMP, OFFRAMP
├── ui/
│   ├── screens/
│   │   ├── connect/
│   │   │   ├── ConnectScreen.kt        # OAuth login via ASWebAuth
│   │   │   └── ConnectViewModel.kt     # Session key generation + OAuth flow
│   │   ├── wallet/
│   │   │   ├── WalletScreen.kt         # Balance, Send/Buy/CashOut buttons,
│   │   │   │                            # recent transactions, Mintscan link
│   │   │   ├── WalletViewModel.kt      # Balance/height/tx loading, expiry timer
│   │   │   └── WalletUiState.kt        # UI state data class
│   │   ├── send/
│   │   │   ├── SendScreen.kt           # Bottom sheet: form → confirm → success
│   │   │   └── SendViewModel.kt        # Validation, micro conversion, broadcast
│   │   ├── brale/
│   │   │   ├── OnrampScreen.kt         # Buy stablecoins UI (4-step flow)
│   │   │   ├── OnrampViewModel.kt      # Plaid link, transfer creation, polling
│   │   │   ├── OfframpScreen.kt        # Cash out UI (4-step flow)
│   │   │   └── OfframpViewModel.kt     # On-chain deposit, transfer, polling
│   │   ├── contract/                   # Smart contract execution demo
│   │   └── history/                    # Full transaction history
│   ├── components/
│   │   └── SharedComponents.kt         # BalanceCard, CompactTransactionRow,
│   │                                    # ErrorBanner, LoadingOverlay,
│   │                                    # AddressDisplay
│   └── theme/
│       ├── Color.kt                    # XionOrange, XionGreen, MintscanBlue, etc.
│       ├── Type.kt                     # Inter font family
│       └── Theme.kt                    # Material3 theme
│
├── data/
│   ├── repository/
│   │   ├── XionRepository.kt           # Interface: balance, send, contract, tx
│   │   ├── XionRepositoryImpl.kt       # Implementation with grant recovery
│   │   ├── BraleRepository.kt          # Interface: Plaid, addresses, transfers
│   │   └── BraleRepositoryImpl.kt      # Implementation calling proxy API
│   ├── datasource/
│   │   ├── MobDataSource.kt            # Interface for mob FFI
│   │   └── RealMobDataSource.kt        # Rust FFI wrapper (thread-safe)
│   ├── remote/
│   │   ├── XionOAuthApi.kt             # Retrofit: OAuth token exchange
│   │   ├── BraleProxyApi.kt            # Retrofit: all proxy endpoints
│   │   └── GrantVerifier.kt            # LCD grant polling
│   └── model/
│       ├── WalletState.kt              # Sealed class: Disconnected/Connecting/Connected
│       ├── TransactionResult.kt        # On-chain tx result
│       ├── BraleModels.kt              # Transfer, Address, Plaid DTOs
│       ├── BalanceInfo.kt              # Balance amount + denom
│       └── OAuthTokens.kt             # OAuth response
│
├── di/
│   ├── DataModule.kt                   # Binds XionRepository
│   ├── NetworkModule.kt                # OkHttp, Retrofit (XION REST)
│   ├── AppModule.kt                    # MobDataSource singleton
│   └── BraleModule.kt                  # BraleRetrofit, BraleProxyApi, BraleRepository
│
├── security/
│   ├── SecureStorage.kt                # EncryptedSharedPreferences wrapper
│   ├── KeyStoreManager.kt             # Android KeyStore AES encryption
│   └── Bip39.kt                        # BIP39 mnemonic generation
│
├── auth/
│   └── OAuthManager.kt                 # Chrome Custom Tabs OAuth flow
│
└── util/
    ├── Constants.kt                    # All configuration (from BuildConfig + compile-time)
    ├── CoinFormatter.kt               # uxion ↔ XION conversion with comma formatting
    └── Result.kt                       # Result<T> sealed class (Success/Error/Loading)
```

### iOS App (`xion-ios/`)

```
XionDemo/
├── App/
│   ├── XionDemoApp.swift               # @main entry, light color scheme
│   └── AppContainer.swift              # Manual DI container
├── Navigation/
│   └── AppNavigation.swift             # NavigationStack with Route enum
├── Configuration/
│   └── Constants.swift                 # All config (with Info.plist override support)
├── Models/
│   ├── WalletState.swift               # Enum: disconnected/connecting/connected
│   ├── TransactionResult.swift         # Codable tx result
│   ├── BalanceInfo.swift
│   └── OAuthTokens.swift
├── Services/
│   ├── MobSigningService.swift         # Rust FFI wrapper (DispatchQueue serial)
│   ├── SessionManager.swift            # ObservableObject: auth, restore, disconnect
│   ├── OAuthService.swift              # ASWebAuthenticationSession
│   └── SecureStorage.swift             # Keychain wrapper (SecItem API)
├── Data/
│   ├── Repository/
│   │   ├── XionRepository.swift        # Protocol
│   │   └── XionRepositoryImpl.swift    # Implementation with grant recovery + REST tx history
│   └── Remote/
│       └── XionOAuthAPI.swift
├── UI/
│   ├── Theme/
│   │   ├── XionColors.swift            # Color extensions (screenBackground, cardBackground, etc.)
│   │   └── XionTypography.swift        # Font extensions
│   ├── Components/
│   │   ├── TransactionRow.swift        # CompactTransactionRow with detail rows
│   │   ├── ErrorBanner.swift           # Dismiss + retry
│   │   ├── BalanceCard.swift
│   │   ├── AddressDisplay.swift
│   │   ├── LoadingOverlay.swift
│   │   └── ActionCardView.swift
│   └── Screens/
│       ├── Connect/                    # ConnectView + ConnectViewModel
│       ├── Wallet/                     # WalletView + WalletViewModel (send sheet)
│       ├── Send/                       # SendSheetContent (4-step bottom sheet)
│       ├── Contract/                   # ContractView + ContractViewModel
│       └── History/                    # HistoryView + HistoryViewModel
├── Utilities/
│   ├── CoinFormatter.swift             # NumberFormatter with comma grouping, 6 decimals
│   └── PkceUtil.swift                  # PKCE challenge generation for OAuth
├── MobBindings/
│   └── mob.swift                       # Auto-generated UniFFI bindings (1803 lines)
├── Frameworks/
│   └── libmob.xcframework/            # Pre-built Rust static library
│       ├── ios-arm64/                  # Device
│       └── ios-arm64_x86_64-simulator/ # Simulator
└── vendor/
    ├── bip39/                          # BIP39 SPM package
    ├── uncommon-crypto/                # HMAC, PBKDF2, SHA
    └── mob/                            # SPM package wrapping xcframework
```

### Brale Proxy (`brale-proxy/`)

```
brale-proxy/
├── index.js          # All routes, auth, transfer guard (single file for demo)
├── package.json      # Express, uuid, dotenv
├── .env.example      # Documented env var template
├── .env              # Actual credentials (gitignored)
├── .gitignore        # node_modules/, .env
└── node_modules/     # Installed dependencies
```

---

## Building mob from Source

The mob Rust library is **pre-built** in the repo. Rebuild only if you need to modify the Rust code or update to a newer version.

### Prerequisites

```bash
# Install Rust
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh

# iOS targets
rustup target add aarch64-apple-ios aarch64-apple-ios-sim x86_64-apple-ios

# Android targets (requires Android NDK)
rustup target add aarch64-linux-android armv7-linux-androideabi x86_64-linux-android
```

### iOS Build

```bash
cd xion-ios
./scripts/build-mob-ios.sh
```

This script:
1. Builds `libmob.a` for 3 iOS targets (device arm64, simulator arm64, simulator x86_64)
2. Creates a universal simulator binary via `lipo`
3. Generates Swift bindings via `uniffi-bindgen`
4. Packages as `libmob.xcframework`

Output:
- `XionDemo/Frameworks/libmob.xcframework` — static library
- `XionDemo/MobBindings/mob.swift` — generated Swift bindings

### Android Build

```bash
cd xion-android-demo/third_party/mob
# Requires ANDROID_NDK_HOME set and proper linker configs
cargo build --release --target aarch64-linux-android
# Repeat for armv7-linux-androideabi, x86_64-linux-android
```

The built `.so` files go into `app/src/main/jniLibs/<abi>/libmob.so`.

---

## Troubleshooting

### Simulator/Emulator Can't Connect to Network

**iOS Simulator**: If `ASWebAuthenticationSession` shows "Safari can't open the page because the network connection was lost":
1. Quit Simulator completely
2. Run: `xcrun simctl shutdown all && xcrun simctl erase all`
3. Restart Simulator and reinstall the app

**Android Emulator**: If the app can't reach the proxy at `10.0.2.2:3000`:
1. Verify the proxy is running: `curl http://localhost:3000/health`
2. Check the emulator can reach the host: `adb shell curl http://10.0.2.2:3000/health`
3. If using a physical device, change `BRALE_PROXY_URL` to your LAN IP

### "Session grants expired" Warning

The authz grants lasted 24 hours. Tap "Disconnect" and re-authenticate via Abstraxion. The app will generate a new session key and get fresh grants.

### Brale Transfer Stuck in "Pending"

- **Onramp**: ACH debits can take 1-3 business days to clear. On testnet, Brale may process faster
- **Offramp**: Ensure the on-chain deposit to the custodial address actually confirmed. Check the `depositTxHash` in the offramp status screen
- Poll `GET /transfers/:id` to see if the status has changed
- Check Brale's dashboard for the transfer details

### "No signer attached" Error

This means the mob `Client` lost its `Signer` reference (usually due to a race condition during reconnect). The app has deferred cleanup logic to prevent this. If it persists:
1. Disconnect and reconnect
2. If the issue is reproducible, check that `MobSigningService.disconnect()` waits for in-flight RPCs

### Proxy Returns 401

The Brale bearer token expired and auto-refresh failed. Check:
1. `BRALE_CLIENT_ID` and `BRALE_CLIENT_SECRET` are correct in `.env`
2. The Brale auth endpoint is reachable: `curl https://auth.brale.xyz/oauth2/token`
3. Restart the proxy to clear the cached token

### "Transfer type X is not allowed"

The proxy's `ALLOWED_TRANSFER_TYPES` is blocking the request. This is a safety feature. If you intentionally need a type not in the list, add it to `.env` and restart the proxy.

### iOS Build Fails with "No such module 'Mob'"

The vendor SPM package can't find the xcframework. Ensure:
1. `XionDemo/Frameworks/libmob.xcframework/` exists and contains both `ios-arm64/` and `ios-arm64_x86_64-simulator/` slices
2. In Xcode, check that libmob.xcframework is listed under "Frameworks, Libraries, and Embedded Content" as "Do Not Embed" (it's a static library)
3. Linker flags include `-lresolv`

### Balance Shows "0" After Onramp

Onramp mints stablecoins (SBC denomination), not XION. The wallet screen shows XION (uxion) balance. The stablecoin balance is tracked by Brale — query it via `GET /addresses/:id/balance?transfer_type=xion_testnet&value_type=SBC`.
