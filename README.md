# House Money — XION Demo Apps

Demo wallet applications for the [XION blockchain](https://xion.burnt.com/), showcasing session-key authentication via Abstraxion, native Rust signing via the [mob library](https://github.com/burnt-labs/mob), and stablecoin on/off-ramp via [Brale](https://brale.xyz).

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
                         ┌───────────────┐
                         │  XION Testnet │
                         │  (xion-testnet-2)
                         └───────┬───────┘
                                 │ RPC / REST
                    ┌────────────┼────────────┐
                    │            │             │
             ┌──────▼──────┐  ┌─▼──────┐  ┌──▼───────────┐
             │  Android App │  │iOS App │  │ Brale Proxy  │
             │  (mob FFI)   │  │(mob FFI)│  │ (Express.js) │
             └──────┬───────┘  └─┬──────┘  └──┬───────────┘
                    │            │             │
                    │   UniFFI   │             │ OAuth2 Client Credentials
                    ▼            ▼             ▼
             ┌────────────┐            ┌─────────────┐
             │  mob (Rust) │            │  Brale API  │
             │  Signing    │            │  Transfers  │
             └────────────┘            └─────────────┘
```

### Key Integrations

| Integration | Purpose | Docs |
|-------------|---------|------|
| **mob** | Rust signing library for Cosmos transactions via UniFFI | [github.com/burnt-labs/mob](https://github.com/burnt-labs/mob) |
| **Abstraxion** | XION account abstraction + OAuth-based session key grants | [docs.burnt.com](https://docs.burnt.com) |
| **Brale** | Stablecoin minting/burning + ACH on/off-ramp | [docs.brale.xyz](https://docs.brale.xyz) |
| **Plaid** | Bank account linking for ACH transfers (via Brale) | [plaid.com/docs](https://plaid.com/docs) |

---

## How It Works

### 1. Session Key Authentication (Abstraxion)

The apps use XION's account abstraction model where a **meta account** (the user's main account) grants temporary signing authority to a **session key** (a throwaway keypair controlled by the app).

**Flow:**

1. **Generate session key**: App creates a random keypair using mob's Rust crypto (BIP39 mnemonic → secp256k1 key derivation via `m/44'/118'/0'/0/0`)
2. **OAuth login**: App opens the Abstraxion dashboard in a web browser. The user authenticates (social login, email, etc.) and the dashboard creates authz grants from the user's meta account to the session key address
3. **Grant verification**: App polls the Cosmos LCD (`/cosmos/authz/v1beta1/grants`) to verify the grants are active
4. **Session storage**: Mnemonic + meta account address + session expiry are stored in encrypted storage (Android KeyStore / iOS Keychain)
5. **Transaction signing**: When the app sends tokens or executes contracts, mob wraps the message in `MsgExec` (authz), so the session key signs but the meta account is the logical sender. A treasury address pays gas via the fee grant module.

**Session expiry**: Grants last 24 hours. The app checks every 60 seconds and warns when < 5 minutes remain.

### 2. Native Signing via mob

[mob](https://github.com/burnt-labs/mob) is a Rust library that provides:
- Mnemonic-based key derivation (BIP39/BIP32)
- secp256k1 ECDSA signing
- Cosmos transaction building (MsgSend, MsgExecuteContract, MsgExec)
- RPC client for querying balances, broadcasting transactions, and fetching tx results
- Gas estimation and fee calculation

**UniFFI bindings** expose the Rust API to Kotlin (Android) and Swift (iOS) as native types.

**Key types:**
- `Client` — RPC client for chain interaction
- `Signer` — Key management and signing
- `ChainConfig` — Chain connection parameters
- `Coin` — Token amount with denomination
- `TxResponse` — Transaction result (hash, code, gas, height)

### 3. Stablecoin On/Off-Ramp via Brale

Brale provides stablecoin infrastructure on Xion, enabling conversion between USD and on-chain stablecoins.

**Onramp (Buy stablecoins):**
```
User's bank account → ACH Debit → Brale → Mint stablecoins → User's Xion wallet
```

**Offramp (Cash out):**
```
User sends stablecoins → Brale custodial address → Burn → ACH Credit → User's bank
```

The Brale API uses OAuth2 client credentials, so secrets are kept on the **backend proxy** — the mobile app never touches API keys.

**Transfer lifecycle:** `pending` → `processing` → `complete` (or `failed`/`canceled`)

---

## Setup & Running

### Prerequisites

- **Android**: Android Studio, JDK 17, Android SDK 34
- **iOS**: Xcode 15+, iOS 16+ simulator
- **Proxy**: Node.js 18+
- **mob build** (optional): Rust toolchain with iOS/Android targets

### 1. Brale Proxy

```bash
cd brale-proxy
cp .env.example .env
# Fill in your Brale credentials:
#   BRALE_CLIENT_ID, BRALE_CLIENT_SECRET, BRALE_ACCOUNT_ID
npm install
npm start
# Server runs on http://localhost:3000
```

**Verify:** `curl http://localhost:3000/health`

### 2. Android App

```bash
cd xion-android-demo

# The mob native library (.so files) are pre-built in the repo.
# If you need to rebuild from Rust source:
#   cd third_party/mob && cargo build --release --target aarch64-linux-android
#   (see scripts/ for full cross-compilation setup)

# Open in Android Studio and run, or:
./gradlew assembleDebug
```

**Configuration** (in `app/build.gradle.kts`):
- `XION_RPC_URL` — Testnet RPC endpoint
- `XION_REST_URL` — Testnet REST/LCD endpoint
- `XION_CHAIN_ID` — `xion-testnet-2`
- `XION_TREASURY_ADDRESS` — Fee granter treasury
- `XION_OAUTH_AUTHORIZATION_ENDPOINT` — Abstraxion dashboard URL
- `BRALE_PROXY_URL` — URL of the brale-proxy server

### 3. iOS App

```bash
cd xion-ios

# The mob xcframework is pre-built in the repo.
# If you need to rebuild from Rust source:
#   ./scripts/build-mob-ios.sh

# Build for simulator:
xcodebuild -project XionDemo.xcodeproj -scheme XionDemo \
  -destination 'platform=iOS Simulator,name=iPhone 16 Pro,OS=18.4' \
  -derivedDataPath build build

# Install and launch:
xcrun simctl install "iPhone 16 Pro" build/Build/Products/Debug-iphonesimulator/XionDemo.app
xcrun simctl launch "iPhone 16 Pro" com.burnt.xiondemo.ios
```

**Configuration** (in `XionDemo/Configuration/Constants.swift`): same fields as Android.

---

## Project Details

### Android App (`xion-android-demo/`)

**Tech stack:** Kotlin, Jetpack Compose, Hilt DI, Retrofit, OkHttp, Kotlinx Serialization

**Architecture:** MVVM with repository pattern

```
app/src/main/java/com/burnt/xiondemo/
├── navigation/NavGraph.kt          # Screen routing
├── ui/screens/
│   ├── connect/                    # OAuth login screen
│   ├── wallet/                     # Balance, send, buy/sell buttons
│   ├── send/                       # Token transfer (bottom sheet)
│   ├── contract/                   # Smart contract execution
│   ├── history/                    # Transaction history
│   └── brale/                      # Onramp + offramp screens
├── data/
│   ├── repository/                 # XionRepository, BraleRepository
│   ├── datasource/                 # MobDataSource (Rust FFI wrapper)
│   ├── remote/                     # XionOAuthApi, BraleProxyApi
│   └── model/                      # Data classes
├── di/                             # Hilt modules
├── security/                       # SecureStorage, KeyStore, BIP39
├── auth/                           # OAuthManager
└── util/                           # Constants, CoinFormatter
```

**Screens:**

| Screen | Purpose |
|--------|---------|
| ConnectScreen | OAuth login via Abstraxion dashboard |
| WalletScreen | Balance display, Send/Buy/Cash Out buttons, recent transactions |
| SendScreen | Token transfer bottom sheet (form → confirm → loading → success) |
| OnrampScreen | Buy stablecoins via ACH (Plaid bank linking → amount → transfer) |
| OfframpScreen | Cash out to bank (amount → on-chain deposit → ACH transfer) |
| ContractScreen | Smart contract execution demo |
| HistoryScreen | Full transaction history with detail view |

### iOS App (`xion-ios/`)

**Tech stack:** SwiftUI, Combine, UniFFI (mob bindings), BIP39 (vendor)

**Architecture:** MVVM with ObservableObject view models

```
XionDemo/
├── App/                            # Entry point, DI container
├── Navigation/                     # NavigationStack routing
├── UI/Screens/
│   ├── Connect/                    # OAuth login
│   ├── Wallet/                     # Balance, send sheet, transactions
│   ├── Send/                       # Bottom sheet (form → confirm → success)
│   ├── Contract/                   # Contract execution
│   └── History/                    # Transaction history
├── Services/
│   ├── MobSigningService.swift     # Rust FFI wrapper
│   ├── SessionManager.swift        # Session lifecycle
│   ├── OAuthService.swift          # ASWebAuthenticationSession
│   └── SecureStorage.swift         # Keychain wrapper
├── MobBindings/mob.swift           # Auto-generated UniFFI bindings
├── Frameworks/libmob.xcframework   # Pre-built Rust static library
└── vendor/                         # BIP39, UncommonCrypto, mob SPM packages
```

### Brale Proxy (`brale-proxy/`)

**Tech stack:** Node.js, Express

Single-file server that proxies mobile app requests to the Brale API with proper authentication.

**Key features:**
- OAuth2 client credentials token management with automatic refresh
- Idempotency-key generation for POST requests
- All routes scoped to a single Brale account (`BRALE_ACCOUNT_ID`)

**Endpoints:**

| Endpoint | Purpose |
|----------|---------|
| `POST /plaid/link-token` | Create Plaid Link token for bank account connection |
| `POST /plaid/register` | Exchange Plaid public token → register bank as address |
| `GET /addresses` | List Brale addresses (internal custodial + external) |
| `POST /addresses/external` | Register Xion wallet or bank account with Brale |
| `GET /addresses/:id/balance` | Query address balance |
| `POST /transfers` | Create onramp or offramp transfer |
| `GET /transfers/:id` | Poll transfer status |
| `GET /transfers` | List all transfers |
| `GET /health` | Health check |

---

## Chain Configuration

| Parameter | Value |
|-----------|-------|
| Chain ID | `xion-testnet-2` |
| RPC | `https://rpc.xion-testnet-2.burnt.com:443` |
| REST/LCD | `https://api.xion-testnet-2.burnt.com/` |
| Coin denom | `uxion` (display: `XION`, 6 decimals) |
| Address prefix | `xion` |
| Gas price | `0.025` |
| Brale transfer type | `xion_testnet` |

---

## User Flows

### Connect Wallet
1. App generates a session keypair (BIP39 mnemonic → secp256k1)
2. Opens Abstraxion OAuth dashboard with session key as grantee
3. User authenticates → dashboard grants authz + fee grants
4. App verifies grants on-chain, stores session in encrypted storage

### Send Tokens
1. Tap "Send Tokens" → bottom sheet opens
2. Enter recipient address, amount (XION), optional memo
3. Review → Confirm → mob broadcasts MsgExec(MsgSend) on-chain
4. Poll for tx confirmation → show success with tx details

### Buy Stablecoins (Onramp)
1. Tap "Buy" → Onramp screen
2. Link bank account via Plaid (first time only)
3. Enter USD amount
4. Proxy creates Brale transfer: ACH debit → stablecoin mint to Xion address
5. Poll transfer status: pending → processing → complete

### Cash Out (Offramp)
1. Tap "Cash Out" → Offramp screen
2. Enter stablecoin amount
3. App sends stablecoins to Brale custodial address (on-chain)
4. Proxy creates Brale transfer: stablecoin burn → ACH credit to bank
5. Poll transfer status: pending → processing → complete

### Restore Session
1. On app launch, check encrypted storage for saved session
2. If session exists and not expired, recreate mob Client/Signer from mnemonic
3. Resume connected state without re-authentication

---

## Building mob from Source

The mob Rust library is pre-built in the repo. To rebuild:

### Android
```bash
# Install Android NDK targets
rustup target add aarch64-linux-android armv7-linux-androideabi x86_64-linux-android

# Build (requires Android NDK configured)
cd xion-android-demo/third_party/mob
cargo build --release --target aarch64-linux-android
```

### iOS
```bash
cd xion-ios
./scripts/build-mob-ios.sh
# Builds for: aarch64-apple-ios, aarch64-apple-ios-sim, x86_64-apple-ios
# Creates: XionDemo/Frameworks/libmob.xcframework
# Generates: XionDemo/MobBindings/mob.swift
```

---

## Environment Variables

### Brale Proxy (`.env`)
```
BRALE_CLIENT_ID=       # Brale OAuth2 client ID
BRALE_CLIENT_SECRET=   # Brale OAuth2 client secret
BRALE_ACCOUNT_ID=      # Your KYB-verified Brale account ID
BRALE_API_URL=https://api.brale.xyz
BRALE_AUTH_URL=https://auth.brale.xyz
PORT=3000
```

### Android (`build.gradle.kts` buildConfigField)
```
XION_RPC_URL, XION_REST_URL, XION_CHAIN_ID
XION_TREASURY_ADDRESS, XION_OAUTH_CLIENT_ID
XION_OAUTH_AUTHORIZATION_ENDPOINT
BRALE_PROXY_URL
```

### iOS (`Constants.swift`)
Same parameters as Android, hardcoded in source for the demo.
