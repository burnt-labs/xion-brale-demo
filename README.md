# House Money — XION Demo Apps

Demo wallet applications for the [XION blockchain](https://xion.burnt.com/), showcasing session-key authentication via Abstraxion, native Rust signing via the [mob library](https://github.com/burnt-labs/mob), stablecoin on/off-ramp via [Brale](https://brale.xyz), and a non-custodial vault smart contract.

---

## Table of Contents

- [Repository Structure](#repository-structure)
- [Architecture Overview](#architecture-overview)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Configuration Reference](#configuration-reference)
- [Session Key Authentication (Abstraxion)](#session-key-authentication-abstraxion)
- [Native Signing via mob](#native-signing-via-mob)
- [Non-Custodial Vault Contract](#non-custodial-vault-contract)
- [Stablecoin On/Off-Ramp (Brale)](#stablecoin-onoff-ramp-brale)
- [Brale Proxy API Reference](#brale-proxy-api-reference)
- [Wallet State Machine](#wallet-state-machine)
- [Data Models](#data-models)
- [Security Model](#security-model)
- [Project Structure](#project-structure)
- [Building mob from Source](#building-mob-from-source)
- [Troubleshooting](#troubleshooting)
- [Developer Setup: Brale + Plaid Integration](#developer-setup-brale--plaid-integration)

---

## Repository Structure

```
apps/
├── xion-android-demo/     # Android (Kotlin/Compose) wallet app
├── xion-ios/              # iOS (SwiftUI) wallet app
├── brale-proxy/           # Node.js proxy for Brale API (holds API secrets)
├── contracts/             # CosmWasm smart contracts
│   └── hm-vault/          # Non-custodial per-user vault contract
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
    │                 │     │  Per-user accounts │
    │  X-Wallet-Addr  │     │  (SQLite)          │
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

**Four components:**

1. **Mobile apps** — connect directly to XION RPC/REST for blockchain operations (balance, send, contract execution, vault deposit/withdraw) via the mob Rust library (compiled as native code via UniFFI)
2. **Brale proxy** — sits between mobile apps and Brale's API. Holds OAuth2 client credentials, manages bearer token lifecycle, enforces a transfer-type allowlist to prevent accidental mainnet usage. Routes requests to per-user Brale managed accounts via a local SQLite database, using the user's XION wallet address (sent as `X-Wallet-Address` header) as the lookup key
3. **Vault contract** (`hm-vault`) — CosmWasm smart contract deployed on XION. Provides non-custodial per-user storage: each user's funds are isolated, only the depositor can withdraw, no admin override. Mobile apps call it via `executeContract` (deposit/withdraw) and `queryContractSmart` (balance)
4. **XION testnet** — Cosmos SDK chain with account abstraction. The Abstraxion dashboard handles OAuth login and authz grant creation

---

## Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| Android Studio | 2024+ | Android build and emulator |
| JDK | 17 | Kotlin compilation |
| Android SDK | API 34 | Target SDK |
| Xcode | 15+ | iOS build and simulator |
| Node.js | 18+ | Brale proxy server |
| Rust | 1.75+ | Only if rebuilding mob from source or vault contract |
| Docker | 20+ | Only if optimizing vault contract wasm binary |

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
| `BRALE_ACCOUNT_ID` | Yes | — | Partner (parent) Brale account ID (KSUID, 26 chars). Managed sub-accounts for end users are created under this account. Also used for the `brand` field on ACH transfers (bank statement name). Find via `GET /accounts` after authenticating |
| `BRALE_API_URL` | No | `https://api.brale.xyz` | Brale API base URL |
| `BRALE_AUTH_URL` | No | `https://auth.brale.xyz` | Brale OAuth2 token endpoint base |
| `PORT` | No | `3000` | Port the proxy server listens on |
| `ALLOWED_TRANSFER_TYPES` | No | `xion_testnet,ach_debit,ach_credit,same_day_ach_credit,rtp_credit` | Comma-separated allowlist. The proxy rejects any `POST /transfers` request whose source or destination `transfer_type` is not in this list. Set to `*` to disable (not recommended). This prevents accidental mainnet transfers since the same Brale credentials access both environments |
| `DB_PATH` | No | `./data/accounts.db` | SQLite database file path for per-user wallet-to-Brale-account mappings |

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
| `BRALE_PROXY_URL` | String | `http://192.168.100.199:3000/` | URL of the Brale proxy server. Set this to your machine's LAN IP for physical device testing, or `http://10.0.2.2:3000/` for Android emulator (host loopback). Must match a domain in `network_security_config.xml` |
| `BRALE_TRANSFER_TYPE` | String | `xion_testnet` | Brale transfer type for on-chain stablecoin operations. Use `xion_testnet` for testnet, `xion` for mainnet |
| `BRALE_STABLECOIN_DENOM` | String | `SBC` | Brale stablecoin denomination identifier used in transfer `value_type` fields |
| `BRALE_SBC_ON_CHAIN_DENOM` | String | `factory/xion17grq736740r70awldugfs3mls3stu9haewctv2/sbc` | Full on-chain denomination for the SBC token (Cosmos token factory format). Used to query on-chain SBC balance via the LCD REST API |

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
| `VAULT_CONTRACT_ADDRESS` | `xion1waen5muj0g5p76t35apjnje43t795478lmnpcxvcm7flmlry5szq0dzvlc` | Non-custodial vault contract on xion-testnet-2 (code_id: 2107) |
| `BRALE_ACH_DEBIT_TYPE` | `ach_debit` | Brale transfer type for pulling funds from a bank account |
| `BRALE_ACH_CREDIT_TYPE` | `same_day_ach_credit` | Brale transfer type for pushing funds to a bank account (same-day settlement) |
| `BRALE_FIAT_VALUE_TYPE` | `USD` | Fiat value type used in Brale transfer endpoints |

### Android Network Security Config (`app/src/main/res/xml/network_security_config.xml`)

The Android app includes a network security config that allows cleartext HTTP traffic to the local Brale proxy during development. By default, Android blocks all cleartext (non-HTTPS) traffic.

**Pre-configured domains:**

| Domain | Purpose |
|--------|---------|
| `10.0.2.2` | Android emulator host loopback |
| `192.168.100.199` | Developer LAN IP (change to yours) |
| `localhost` | Local testing |
| `127.0.0.1` | Local testing |

**For physical device testing:** Add your machine's LAN IP to the `<domain-config>` block in this file. The domain must match the IP in `BRALE_PROXY_URL` in `build.gradle.kts`.

Referenced from `AndroidManifest.xml` via `android:networkSecurityConfig="@xml/network_security_config"`.

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
| `vaultContractAddress` | `xion1waen5muj0g5p76t35apjnje43t795478lmnpcxvcm7flmlry5szq0dzvlc` | — (edit source) |
| `coinType` | `118` | — (edit source) |
| `derivationPath` | `m/44'/118'/0'/0/0` | — (edit source) |
| `sessionGrantDurationSeconds` | `86400` | — (edit source) |
| `defaultSendGasLimit` | `200000` | — (edit source) |
| `defaultExecuteGasLimit` | `400000` | — (edit source) |

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

**iOS**: `MobSigningService` (implements `MobSigningServiceProtocol`) — uses `DispatchQueue(label: "com.burnt.xiondemo.mob")` for serial execution, maps mob types to app models via `withCheckedThrowingContinuation`, implements deferred cleanup (2-second delay) to prevent races with in-flight RPCs on disconnect. Supports optional `gasLimit` parameter on `send()` and `executeContract()` — when provided, bypasses gas simulation entirely (see [iOS Simulator QUIC Issue](#ios-simulator-quic-issue) below). For `send()` with a gas limit, uses `buildSendMessage` + `signAndBroadcastMulti` since the `client.send()` FFI has no gas limit parameter

---

## Non-Custodial Vault Contract

The vault contract (`contracts/hm-vault/`) implements a non-custodial per-user savings vault on XION. It is part of a hybrid custody model where each user has two account types:

- **Checking Account** (custodial) — House Money controls operations: card spending, payroll, AI auto-allocation, reversals, compliance holds. Implemented via XION Meta Accounts with authz grants.
- **Vault** (non-custodial) — User-only control for long-term storage. Excluded from card spending, automated actions, and reversals. Funds sit in the vault contract and only the depositor can withdraw.

### Testnet Deployment

| Field | Value |
|-------|-------|
| Chain | `xion-testnet-2` |
| Code ID | `2107` |
| Contract Address | `xion1waen5muj0g5p76t35apjnje43t795478lmnpcxvcm7flmlry5szq0dzvlc` |
| Allowed Denoms | `uxion`, `factory/xion17grq736740r70awldugfs3mls3stu9haewctv2/sbc` |
| Admin | Deployer — can update allowed denoms only, cannot move user funds |

### Admin & Token Management

The contract has an **admin** address (set at instantiation, defaults to the deployer). The admin can add or remove accepted token denominations via `UpdateAllowedDenoms`. This is the admin's **only** power — they cannot move, freeze, or access user funds.

**Key safety guarantee:** If a denom is removed from `allowed_denoms`, users who already deposited that token can still withdraw it. Only new deposits of the removed denom are blocked.

### Contract Messages

**Execute:**

| Message | Caller | Behavior |
|---------|--------|----------|
| `Deposit {}` | Any user | Accepts native tokens sent with the tx. Credits to sender's balance. Rejects disallowed denoms. |
| `Withdraw { coins }` | Depositor only | Deducts coins from caller's balance, sends back via `BankMsg::Send`. Always works regardless of current `allowed_denoms`. |
| `WithdrawAll {}` | Depositor only | Sends all of the caller's vault balance back to them. Always works regardless of current `allowed_denoms`. |
| `UpdateAllowedDenoms { add, remove }` | Admin only | Adds and/or removes token denominations from the accepted list. Does not affect existing deposits. |

**Query:**

| Message | Returns |
|---------|---------|
| `Balance { address }` | `{ coins: [{ denom, amount }] }` |
| `Config {}` | `{ admin: "xion1...", allowed_denoms: [...] }` |
| `TotalDeposits {}` | `{ coins: [{ denom, amount }] }` |

### CLI Usage

```bash
# Query config
xiond query wasm contract-state smart \
  xion1waen5muj0g5p76t35apjnje43t795478lmnpcxvcm7flmlry5szq0dzvlc \
  '{"config":{}}' --node https://rpc.xion-testnet-2.burnt.com:443

# Deposit 1 XION
xiond tx wasm execute \
  xion1waen5muj0g5p76t35apjnje43t795478lmnpcxvcm7flmlry5szq0dzvlc \
  '{"deposit":{}}' --amount 1000000uxion \
  --from <key> --chain-id xion-testnet-2 \
  --node https://rpc.xion-testnet-2.burnt.com:443 \
  --gas auto --gas-adjustment 1.3 --fees 50000uxion -y

# Query balance
xiond query wasm contract-state smart \
  xion1waen5muj0g5p76t35apjnje43t795478lmnpcxvcm7flmlry5szq0dzvlc \
  '{"balance":{"address":"xion1your..."}}' \
  --node https://rpc.xion-testnet-2.burnt.com:443

# Withdraw all
xiond tx wasm execute \
  xion1waen5muj0g5p76t35apjnje43t795478lmnpcxvcm7flmlry5szq0dzvlc \
  '{"withdraw_all":{}}' \
  --from <key> --chain-id xion-testnet-2 \
  --node https://rpc.xion-testnet-2.burnt.com:443 \
  --gas auto --gas-adjustment 1.3 --fees 50000uxion -y

# Update allowed denoms (admin only) — add uatom, remove usbc
xiond tx wasm execute \
  xion1waen5muj0g5p76t35apjnje43t795478lmnpcxvcm7flmlry5szq0dzvlc \
  '{"update_allowed_denoms":{"add":["uatom"],"remove":["usbc"]}}' \
  --from <admin-key> --chain-id xion-testnet-2 \
  --node https://rpc.xion-testnet-2.burnt.com:443 \
  --gas auto --gas-adjustment 1.3 --fees 50000uxion -y
```

### Building the Contract

```bash
cd contracts/hm-vault

# Run tests
cargo test

# Optimize for deployment (requires Docker)
docker run --rm -v "$(pwd)":/code \
  --mount type=volume,source="hm_vault_cache",target=/target \
  --mount type=volume,source=registry_cache,target=/usr/local/cargo/registry \
  cosmwasm/optimizer:0.17.0

# Output: artifacts/hm_vault.wasm
```

### Mobile Integration

Both apps interact with the vault via the mob library's `executeContract` (for deposit/withdraw) and `queryContractSmart` (for balance queries). The vault balance is always displayed on the home wallet screen alongside XION and SBC balances (even when zero), with a dedicated Vault screen for deposit/withdraw operations.

**Repository methods:**
- `getVaultBalance()` — queries the contract for the user's vault balance (read-only)
- `vaultDeposit(amount, denom)` — deposits tokens into the vault
- `vaultWithdraw(amount, denom)` — withdraws specific amount from the vault
- `vaultWithdrawAll()` — withdraws entire vault balance

**iOS vault operations** use explicit gas limits (400,000) to bypass gas simulation, which fails on the iOS simulator due to the QUIC transport issue. Deposit, withdraw, and withdraw-all show a native confirmation alert before submitting. After a successful vault transaction, the balance reload is delayed by 3 seconds to allow the block to be committed.

**Gas limits:** Send operations use 200,000 gas, contract execute operations use 400,000 gas. These are defined in `Constants.swift` (`defaultSendGasLimit`, `defaultExecuteGasLimit`) and `Constants.kt`. The fee granter (treasury) pays all gas fees, so over-estimation has no cost to the user.

---

## Stablecoin On/Off-Ramp (Brale)

Brale provides stablecoin infrastructure on XION. The same Brale account and API credentials access **both testnet and mainnet** — the environment is determined by the `transfer_type` field in each transfer (`xion_testnet` vs `xion`).

### How the ACH Onramp Works (Plain English)

**The goal:** User gives USD from their bank, gets stablecoin tokens (SBC) in their crypto wallet.

1. **User links their bank** — They go through Plaid (the bank-linking widget you've seen in Venmo, Robinhood, etc.). They pick their bank, log in, select an account. This is the *only* way to do ACH debit — you can't just type in an account number.

2. **Your backend registers that bank** — Plaid gives you a temporary `public_token`. You send it to Brale, and Brale gives you back a permanent `address_id` representing that bank account. Think of it like a nickname for "Chase checking ****1039".

3. **User's crypto wallet gets registered too** — You tell Brale "here's the XION wallet address where tokens should go" and get another `address_id` for it.

4. **User says "buy $100 of SBC"** — You create a transfer that says:
   - **Pull $100 from** the bank `address_id` (via ACH debit)
   - **Send SBC tokens to** the wallet `address_id` (via xion_testnet)

5. **Brale handles the rest** — They debit the bank, mint the stablecoins on-chain, and deliver them to the wallet.

**Key constraints:**
- **Plaid is mandatory** for pulling money from banks (ACH debit). No Plaid = no bank pulls.
- **$50k max** per transaction.
- **The `brand` field** is optional — it controls what name appears on the user's bank statement (e.g., "HOUSE MONEY" instead of "BRALE"). This is a premium feature.
- **Idempotency-Key header** — prevents duplicate transfers if a request is accidentally sent twice.

**What this looks like in our app:**

```
User fills form (name, email, phone, DOB)
        |
Taps "Link" -> Plaid opens -> picks bank -> done
        |
Enters "$100" -> taps "Buy"
        |
App tells proxy: pull $100 from bank, send SBC to wallet
        |
Poll until tokens arrive on-chain -> show confirmation
```

Brale is the bridge between the traditional banking system (ACH) and the blockchain (XION). Plaid is the secure way to connect a user's bank account without you ever touching their credentials.

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

All endpoints (except `/health`) accept an optional `X-Wallet-Address` header containing the user's XION wallet address (e.g., `xion1abc...`). When present, the proxy routes the request to that user's managed Brale sub-account. When absent, falls back to the partner account.

### `GET /health`

Health check.

**Response:**
```json
{"status": "ok", "account_id": "configured", "managed_accounts": 0}
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

### Per-User Account Routing

The proxy maps each user's XION wallet address to their own Brale managed sub-account. This provides data isolation — each user's bank links, wallet registrations, and transfer history are scoped to their own account.

**How it works:**
1. Mobile apps send an `X-Wallet-Address` header with every proxy request (injected automatically by an OkHttp interceptor on Android, or in `BraleProxyService` on iOS)
2. The proxy looks up the wallet address in a local SQLite database (`data/accounts.db`)
3. If found, routes the request to that user's Brale account
4. If not found, creates a new managed sub-account under the partner account, stores the mapping, then routes
5. A race-condition guard deduplicates concurrent first-requests from the same wallet

**Backward compatibility:** If the `X-Wallet-Address` header is missing, the proxy falls back to `BRALE_ACCOUNT_ID` (the partner account). This allows older app versions to continue working during rollout.

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
│                                        #         HISTORY, ONRAMP, OFFRAMP,
│                                        #         LINK_BANK, VAULT
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
│   │   ├── vault/
│   │   │   ├── VaultScreen.kt          # Vault deposit/withdraw UI
│   │   │   └── VaultViewModel.kt       # Vault balance, deposit, withdraw logic
│   │   ├── linkbank/
│   │   │   ├── LinkBankScreen.kt       # Standalone bank linking UI
│   │   │   └── LinkBankViewModel.kt    # Plaid link flow
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
│   │   ├── XionRepository.kt           # Interface: balance, send, contract, vault, tx
│   │   ├── XionRepositoryImpl.kt       # Implementation with grant recovery + vault ops
│   │   ├── BraleRepository.kt          # Interface: Plaid, addresses, transfers
│   │   └── BraleRepositoryImpl.kt      # Implementation calling proxy API
│   ├── datasource/
│   │   ├── MobDataSource.kt            # Interface for mob FFI (incl. queryContractSmart)
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
│   ├── BraleModule.kt                  # BraleRetrofit, BraleProxyApi, BraleRepository,
│   │                                    # @BraleClient OkHttpClient with X-Wallet-Address
│   └── WalletAddressProvider.kt        # Interface + impl for dynamic wallet address
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
│   ├── MobSigningService.swift         # Rust FFI wrapper (serial DispatchQueue, gasLimit support)
│   ├── NativeHttpTransport.swift       # HTTP/1.1 via NWConnection (avoids QUIC),
│   │                                    # POST for mob FFI, GET for REST, chunked decoder
│   ├── SessionManager.swift            # ObservableObject: auth, restore, disconnect
│   ├── OAuthService.swift              # ASWebAuthenticationSession
│   ├── BraleProxyService.swift         # Brale proxy HTTP client
│   └── SecureStorage.swift             # Keychain wrapper (SecItem API)
├── Data/
│   ├── Repository/
│   │   ├── XionRepository.swift        # Protocol (incl. vault methods)
│   │   └── XionRepositoryImpl.swift    # Implementation with grant recovery, vault ops, REST tx history
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
│       ├── Vault/                      # VaultView + VaultViewModel (deposit/withdraw)
│       ├── LinkBank/                   # LinkBankView + LinkBankViewModel
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
├── index.js          # Routes, auth, transfer guard, per-user account middleware
├── db.js             # SQLite database layer (wallet → Brale account mapping)
├── package.json      # Express, uuid, dotenv, better-sqlite3
├── .env.example      # Documented env var template
├── .env              # Actual credentials (gitignored)
├── .gitignore        # node_modules/, .env, data/
├── data/             # SQLite database files (gitignored)
│   └── accounts.db   # Per-user account mappings
└── node_modules/     # Installed dependencies
```

### Vault Contract (`contracts/hm-vault/`)

```
contracts/hm-vault/
├── Cargo.toml          # cosmwasm-std 2.1, cw-storage-plus 2.0, cw-multi-test
├── Cargo.lock
├── .gitignore          # /target excluded
├── src/
│   ├── lib.rs          # Module declarations
│   ├── contract.rs     # Entry points: instantiate, execute, query
│   ├── msg.rs          # InstantiateMsg, ExecuteMsg, QueryMsg, response types
│   ├── state.rs        # Config, BALANCES (per-user Map), CONFIG (Item)
│   ├── error.rs        # ContractError enum
│   └── tests.rs        # 13 unit tests (cw-multi-test)
└── artifacts/
    └── hm_vault.wasm   # Optimized binary (206KB, built with cosmwasm/optimizer:0.17.0)
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

### iOS Simulator QUIC Issue

The iOS simulator has a known bug with HTTP/3 (QUIC) connections to Cloudflare-fronted endpoints (including `rpc.xion-testnet-2.burnt.com` and `api.xion-testnet-2.burnt.com`). Symptoms include "Simulate query failed", "The request timed out", "cannot parse response", or transactions/balances failing to load.

**How it's handled in the codebase:**

| Component | Problem | Solution |
|-----------|---------|----------|
| mob RPC calls (balance, contract queries, broadcast) | QUIC connections fail silently | `NativeHttpTransport` uses `NWConnection` with TCP+TLS, explicitly negotiating `http/1.1` via ALPN. Avoids QUIC entirely. |
| Gas estimation (simulate) | ABCI query for `/cosmos.tx.v1beta1.Service/Simulate` fails via NativeHttpTransport | Bypassed entirely — explicit gas limits are passed for all `send()` and `executeContract()` calls (200k and 400k respectively) |
| REST transaction history | `URLSession.shared` uses HTTP/3 by default, times out or returns unparseable responses | Replaced with `NativeHttpTransport.get()` — a static async helper that makes GET requests via `NWConnection` + HTTP/1.1 |
| Grant polling (SessionManager) | Same URLSession QUIC issue | Also uses `NativeHttpTransport.get()` |
| Chunked transfer encoding | REST API responses use `Transfer-Encoding: chunked` (no Content-Length), raw chunk markers corrupt JSON parsing | `NativeHttpTransport.extractResponseBody()` detects chunked encoding via headers and decodes chunk frames before returning the body |

**Key files:**
- `NativeHttpTransport.swift` — HTTP/1.1 transport via NWConnection, with `post()` (for mob FFI), `get()` (for REST calls), and chunked encoding decoder
- `Constants.swift` — `defaultSendGasLimit` (200,000) and `defaultExecuteGasLimit` (400,000)
- `MobSigningService.swift` — `gasLimit` parameter on `send()` and `executeContract()`

**Note:** This is an iOS simulator-specific issue. On physical devices, QUIC works correctly. The NativeHttpTransport approach works on both simulator and device.

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

### Vault Balance Shows "0" After Deposit (iOS)

If vault deposits show as successful but the vault balance remains 0, check that `getVaultBalance()` in `XionRepositoryImpl.swift` properly unwraps the optional `metaAccountAddress`. A previous bug interpolated `Optional("xion1...")` into the contract query JSON instead of the unwrapped address, causing the vault contract to return no coins. The fix: use `guard let address = sessionManager.walletState.metaAccountAddress` instead of `state.metaAccountAddress` directly in string interpolation.

### "Could not connect to the server" on Link Bank (iOS)

The Brale proxy server at `localhost:3000` is not running. Start it with `cd brale-proxy && npm start`. This is not a QUIC issue — the proxy runs locally over HTTP.

### Recent Transactions Not Loading (iOS Simulator)

If the home screen shows "No transactions yet" despite having on-chain transactions, this is the QUIC issue. The REST API calls via `URLSession` time out or return unparseable HTTP/3 responses. The fix replaces `URLSession.shared` with `NativeHttpTransport.get()` which forces HTTP/1.1. See [iOS Simulator QUIC Issue](#ios-simulator-quic-issue).

---

## Developer Setup: Brale + Plaid Integration

This section covers everything a developer needs to integrate Brale's stablecoin on/off-ramp into their own XION app.

**Official Brale guide:** [ACH On-Ramp](https://docs.brale.xyz/guides/ach-on-ramp) | [Stablecoin to Fiat Offramp](https://docs.brale.xyz/guides/stablecoin-to-fiat-offramp) | [Transfers](https://docs.brale.xyz/key-concepts/transfers)

### Prerequisites

1. **Brale account** — You need a KYB-verified business account on Brale. Apply at [brale.xyz](https://brale.xyz). The account requires: business name, EIN, business address, phone number, email, website, business controller details (SSN, DOB), and beneficial owners (25%+ ownership). Account status progresses from `pending` → `complete` (approved).

2. **Brale API credentials** — After approval, create OAuth2 client credentials in the Brale dashboard. You'll receive a `client_id` and `client_secret`. **Important:** Credentials are scoped to either testnet or production at creation time — the same API base URL (`https://api.brale.xyz`) is used for both, but the credentials determine which environment you access.

3. **Plaid** — Brale handles Plaid configuration on their end. You do NOT need a separate Plaid account. The Plaid Link token is created through Brale's API, and Brale manages the Plaid integration entirely. However, your **mobile app** needs the Plaid Link SDK to render the bank selection UI:
   - **Android:** `com.plaid.link:sdk-core:4.5.1` (see `build.gradle.kts`)
   - **iOS:** `LinkKit` from `github.com/plaid/plaid-link-ios`, version 5.6.0+ (see `project.yml`)
   - Register your Android package name and/or iOS redirect URI in the Brale dashboard under **Plaid OAuth Configuration** (Step 2 below)

### Step-by-Step Integration

The full flow, matching [Brale's ACH On-Ramp guide](https://docs.brale.xyz/guides/ach-on-ramp):

#### Step 1: Get Your Account ID

```bash
# Authenticate
TOKEN=$(curl -s -X POST "https://auth.brale.xyz/oauth2/token" \
  -H "Authorization: Basic $(echo -n 'CLIENT_ID:CLIENT_SECRET' | base64)" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" | jq -r '.access_token')

# List accounts
curl -s "https://api.brale.xyz/accounts" -H "Authorization: Bearer $TOKEN"
# → {"accounts":[{"id":"YOUR_ACCOUNT_ID","name":"Your Business","status":"complete"}]}
```

Store the `account_id` in your proxy's `.env` file as `BRALE_ACCOUNT_ID`.

#### Step 2: Configure Plaid OAuth for Mobile (Brale Dashboard)

Before integrating Plaid Link in your mobile apps, you must register your app's identity in the Brale dashboard so that OAuth-based bank connections can redirect back to your app.

In the Brale dashboard, navigate to **Plaid OAuth Configuration**:

**Android Package Names:**
Add your Android app's `applicationId` (from `build.gradle.kts`). For this demo: `com.burnt.xiondemo`.

Without this, Plaid Link will fail to redirect back to your app after OAuth-based bank login flows (where the user is taken to their bank's app or website to authenticate).

**iOS Redirect URIs:**
Add HTTPS universal link URLs for iOS if your app uses OAuth-based bank connections. These must be HTTPS URLs that your app has registered as universal links (e.g., `https://app.example.com/plaid-oauth`). If your app only uses non-OAuth institutions (like sandbox test banks), this can be deferred.

**Note:** Plaid OAuth configuration is per API key. If you have separate testnet and production keys, configure both.

#### Step 3: Create Plaid Link Token (Server-Side)

Your **backend** (not the mobile app) calls Brale to create a Plaid link token. The mobile app never talks to Brale directly — it gets the `link_token` from your backend and uses it to initialize the Plaid Link SDK.

**In our app:** The proxy handles this at `POST /plaid/link-token`, which forwards to Brale. `OnrampViewModel` calls this when the user taps "Link", then passes the returned `link_token` to the Plaid Link SDK.

```
POST /accounts/{account_id}/plaid/link_token
```

```json
{
  "legal_name": "John Doe",
  "email_address": "john@example.com",
  "phone_number": "+15551234567",
  "date_of_birth": "1990-01-15"
}
```

**Identity fields and Plaid verification:**
- All four fields (`legal_name`, `email_address`, `phone_number`, `date_of_birth`) are technically optional per Brale's API
- However, **Plaid uses these for identity verification**. If `phone_number` is provided, Plaid will send an SMS verification code to that number. The user must enter the code before they can select their bank
- If `phone_number` is omitted, Plaid may prompt the user to enter their phone number inline
- **The phone number must be in E.164 format** (e.g., `+15551234567`). Invalid format will cause Plaid to fail silently
- **These should be the end user's real details**, not hardcoded test values. The demo app collects legal name and email via input fields on the Buy Stablecoins screen before launching Plaid Link

**Response:**
```json
{
  "link_token": "link-sandbox-abc123...",
  "expiration": "2026-03-25T05:37:05Z",
  "callback_url": "http://api.brale.xyz:80/accounts/.../plaid/register-account"
}
```

**Key fields in the response:**
- `link_token` — Pass this to the Plaid Link SDK on the mobile app. This is the only field you need for the next step.
- `expiration` — The token is short-lived (typically 4 hours). Create a new one if it expires before the user completes the flow.
- `callback_url` — This is Brale's register-account endpoint. You don't need to call this URL directly — instead, your backend calls `POST /plaid/register` (Step 5) with the `public_token` that Plaid returns after the user finishes. The demo app handles Step 5 explicitly rather than relying on the callback.

#### Step 4: Launch Plaid Link (Mobile App)

Pass the `link_token` to the Plaid Link SDK. The SDK opens a webview where the user links their bank. When finished, it returns a `public_token` to your app.

**Redirect URI requirement:** For banks that use OAuth (where the user is redirected to their bank's app or website), Plaid needs a way to return the user to your app. This is configured in Step 2 via the Brale dashboard:
- **Android:** Register your package name (`com.burnt.xiondemo`). Plaid uses Android App Links to redirect back.
- **iOS:** Register an HTTPS universal link as a redirect URI. For sandbox test banks this isn't needed (they don't use OAuth), but production banks will require it.
- The redirect URI configured in the Brale dashboard **must exactly match** what your app is registered to handle, otherwise the handoff back to the app will fail silently.

**In our app:** `OnrampViewModel.requestPlaidLinkToken()` calls the proxy, gets the `link_token`, and sets it in UI state. `OnrampScreen` observes this and launches Plaid Link. On success, `onPlaidSuccess(publicToken)` is called, which triggers Step 5.

Pass the `link_token` to the Plaid Link SDK:

**Android (Kotlin/Compose):**
```kotlin
// build.gradle.kts
implementation("com.plaid.link:sdk-core:4.5.1")

// In your Composable
val plaidLauncher = rememberLauncherForActivityResult(OpenPlaidLink()) { result ->
    when (result) {
        is LinkSuccess -> viewModel.onPlaidSuccess(result.publicToken)
        is LinkExit -> viewModel.onPlaidCancelled()
    }
}

// When link_token is ready:
val config = LinkTokenConfiguration.Builder().token(linkToken).build()
plaidLauncher.launch(config)
```

**iOS (SwiftUI):**
```swift
// project.yml dependency:
//   LinkKit: { url: "https://github.com/plaid/plaid-link-ios", from: "5.6.0" }

import LinkKit

// The demo wraps LinkKit's callback API in an async/await service:
let result = try await plaidLinkService.openLink(token: linkToken)
switch result {
case .success(let publicToken):
    let addressId = try await braleRepository.registerBankAccount(publicToken: publicToken)
    secureStorage.saveBraleBankAddressId(addressId)
case .cancelled:
    break
}

// Under the hood, PlaidLinkService creates a handler and presents it:
var config = LinkTokenConfiguration(token: token) { success in
    // success.publicToken — exchange this via your backend
}
config.onExit = { exit in
    // User cancelled or Plaid error (exit.error)
}
let result = Plaid.create(config) // Returns Result<Handler, PlaidError>
handler.open(presentUsing: .viewController(topViewController))
```

The user will:
1. Verify their identity (SMS code to their phone number)
2. Search for and select their bank
3. Log in to their bank
4. Select a checking/savings account
5. Plaid returns a `public_token` to your app

#### Step 5: Register the Bank Account

After the user completes Plaid Link, the SDK returns a `public_token` to your app. Your **backend** exchanges this token with Brale to register the bank account. This creates an external address in Brale representing the user's bank account and returns an `address_id` you'll use in all future transfers.

**How this relates to the `callback_url`:** The link token response (Step 3) includes a `callback_url` pointing to Brale's register-account endpoint. You have two options:
1. **Explicit call (what we do):** Your backend calls `POST /plaid/register` with the `public_token` from the Plaid SDK callback. This gives you full control over the flow.
2. **Callback-based:** Plaid can call the `callback_url` directly if configured. We don't use this approach because our proxy handles the exchange explicitly.

**In our app:** `OnrampViewModel.onPlaidSuccess(publicToken)` calls the proxy at `POST /plaid/register`, which forwards to Brale. The returned `address_id` is saved to secure storage for future sessions.

```
POST /accounts/{account_id}/plaid/register-account
```

```json
{
  "public_token": "public-sandbox-xyz789...",
  "transfer_types": ["ach_debit", "ach_credit", "same_day_ach_credit"]
}
```

**Fields:**
- `public_token` (required) — The token from the Plaid Link SDK callback. This is a one-time-use token that must be exchanged promptly.
- `transfer_types` (required) — Determines which payment rails are enabled for this bank account. `ach_debit` is required for onramp (pulling money from the bank). `ach_credit` and `same_day_ach_credit` are needed for offramp (pushing money to the bank).
- `customer_webhook_url` (optional) — If provided, Brale forwards Plaid events to this URL, such as `ITEM_LOGIN_REQUIRED` (the user needs to re-authenticate with their bank due to a password change or MFA update). The demo app omits this. **For production:** consider adding a webhook endpoint so you can prompt users to re-link when their bank connection breaks.
- **ACH debit is ONLY available through the Plaid-linked bank account flow.** You cannot enable `ach_debit` via direct bank entry — this is a Plaid/regulatory requirement.

**Response:**
```json
{
  "address_id": "36nftTpbLIOwLZwEC7UXjwUGrcc"
}
```

Store this `address_id` — it's the user's bank account identifier for all future onramp and offramp transfers. The demo app saves it to encrypted local storage (`EncryptedSharedPreferences` on Android, Keychain on iOS) so returning users don't need to re-link.

#### Step 6: Setup Addresses (Funding)

Before you can create a transfer, both the **source** (where money comes from) and **destination** (where tokens go) must be registered as addresses on the Brale account. There are two types of external addresses:

**On-chain wallet (destination for onramp, source for offramp):**

Register the user's XION wallet so Brale knows where to mint stablecoins:

```
POST /accounts/{account_id}/addresses/external
Idempotency-Key: <uuid>
```

```json
{
  "name": "XION Wallet xion1abc123...",
  "address": "xion1abc123...",
  "transfer_types": ["xion_testnet"]
}
```

Use `xion_testnet` for testnet or `xion` for mainnet. The returned `address_id` is the destination for onramp transfers and the identifier Brale uses to track this wallet.

**In our app:** `OnrampViewModel.submitOnramp()` checks if the user's wallet is already registered (cached `xionAddressId` in secure storage). If not, it calls `braleRepository.registerXionAddress(walletAddress)` which sends the request above through the proxy. This happens automatically — the user doesn't need to do anything.

**Off-chain bank account (source for onramp, destination for offramp):**

There are two ways to register a bank account:

1. **Via Plaid (what we use):** Steps 4-5 above handle this. Plaid Link collects the bank credentials securely, and the `public_token` exchange creates the bank address. **This is the only way to enable ACH debit** (pulling money from a bank).

2. **Via direct bank entry (not used in this app):** You can also register a bank account by collecting routing number, account number, and beneficiary details directly:
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
     }
   }
   ```
   **Important:** Direct bank entry only supports ACH credit (pushing money *to* a bank). It does **not** support ACH debit (pulling money *from* a bank). If your app needs to pull funds from users' bank accounts (onramp), you must use the Plaid flow.

**How address IDs work:** Once registered, Brale returns an `address_id` (KSUID, 26 chars) for each address. This ID is permanent and used in all future transfer requests to reference that bank account or wallet. The demo app caches these IDs in encrypted local storage so they persist across sessions.

**Idempotency:** All `POST /addresses/external` requests must include an `Idempotency-Key` header (UUID). The proxy handles this automatically. This prevents duplicate address creation if a request is retried.

#### Step 7: Create the Onramp Transfer

```
POST /accounts/{account_id}/transfers
Idempotency-Key: <uuid>
```

```json
{
  "amount": { "value": "100", "currency": "USD" },
  "source": {
    "address_id": "<bank_address_id>",
    "value_type": "USD",
    "transfer_type": "ach_debit"
  },
  "destination": {
    "address_id": "<wallet_address_id>",
    "value_type": "SBC",
    "transfer_type": "xion_testnet"
  },
  "brand": {
    "account_id": "<your_brale_account_id>"
  }
}
```

- The `brand` field is optional and controls the name on the user's bank statement (ACH only)
- Always include an `Idempotency-Key` header on POST requests (the proxy handles this automatically)
- Our proxy injects the `brand` field server-side so the mobile app doesn't need the account ID

#### Step 8: Poll Transfer Status

```
GET /accounts/{account_id}/transfers/{transfer_id}
```

Status progression: `pending` → `processing` → `complete` (or `failed`/`canceled`)

Poll with exponential backoff (3s → 6s → 12s → ... up to 30s). ACH debits can take 1-3 business days to settle in production.

### Returning Users: Local Cache

Once a user has linked their bank via Plaid, the demo app stores the `bank_address_id` in local secure storage (EncryptedSharedPreferences on Android, Keychain on iOS). On subsequent app launches, the cached ID is restored — no Plaid flow needed.

The app does **not** auto-detect bank accounts from the Brale API. Each user must link their own bank via Plaid at least once. This is intentional — with per-user managed accounts, querying all addresses could only return that user's own addresses, but the Plaid flow ensures proper bank verification and KYC.

### Common Pitfalls

| Issue | Cause | Fix |
|-------|-------|-----|
| Plaid Link shows "Sending SMS" then closes without bank selection | A `phone_number` was included in the link token request, triggering Plaid's Returning User phone verification — real phone numbers don't work in Sandbox | **Do not send `phone_number`** to Brale's link token endpoint. It's optional, and omitting it skips the SMS step entirely. If you must test phone verification in Sandbox, use Plaid's test numbers (`+14155550010` through `+14155550015`) with OTP `123456` |
| HTTP 400 "null value where string expected" | Kotlinx serialization sends `null` for optional fields | Set `explicitNulls = false` in your Json config |
| HTTP 400 "No idempotency key found" | Missing `Idempotency-Key` header on POST requests | Ensure all POST requests include a UUID idempotency key |
| "CLEARTEXT communication not permitted" (Android) | Android blocks HTTP by default | A `network_security_config.xml` is already included. Add your machine's LAN IP to the `<domain-config>` block in `app/src/main/res/xml/network_security_config.xml` and rebuild |
| Transfer type not in allowlist | Proxy's `ALLOWED_TRANSFER_TYPES` is blocking the request | Add the type to `.env` and restart proxy |
| Brale returns `{"addresses": [...]}` but model expects `{"data": [...]}` | Brale's response keys don't match generic `data` field | Use the exact field names from Brale's API (`addresses`, `transfers`, etc.) |

### Sandbox Testing with Plaid

Brale manages the Plaid integration on their end. When using Brale's testnet credentials, Plaid Link will connect to Plaid's Sandbox environment. No setup is needed in the Plaid dashboard — test bank accounts with pre-populated balances are provided automatically.

**Test credentials for Plaid Sandbox:**

| Field | Value |
|-------|-------|
| Institution | Select any sandbox bank (e.g. "First Platypus Bank") |
| Username | `user_good` |
| Password | `pass_good` |
| MFA code (if prompted) | `1234` |

These credentials work across all sandbox test institutions and provide checking/savings accounts with balances.

**Custom test data:** If you need specific balances or account types, use Plaid Dashboard → Developers → Sandbox → "Sandbox Users" to create a custom test user. Alternatively, log in with username `user_custom` and pass a JSON configuration as the password.

**Note:** On Brale testnet, the fiat leg of on-ramp/off-ramp transfers is simulated — tokens mint/burn on-chain without actual ACH movement. The Plaid bank linking step primarily tests the UI/UX flow of connecting a bank account.

### Testnet vs Production

| Aspect | Testnet | Production |
|--------|---------|------------|
| API credentials | Testnet-scoped from Brale dashboard | Production-scoped |
| API URL | Same: `https://api.brale.xyz` | Same |
| Transfer type | `xion_testnet` | `xion` |
| Plaid environment | Plaid Sandbox (managed by Brale) | Plaid Production |
| ACH settlement | Simulated (immediate on-chain) | 1-3 business days |
| Real money | No | Yes |
| Proxy `ALLOWED_TRANSFER_TYPES` | Include `xion_testnet` | Include `xion` |
