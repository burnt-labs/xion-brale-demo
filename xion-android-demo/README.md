# XION Android Demo

A reference Android app demonstrating XION blockchain integration using the real abstraxion flow:

**OAuth login -> Meta Account -> local session keypair -> authz grants -> gasless transactions via Treasury**

All transaction signing is done locally via the `mob` native library. The session keypair signs on behalf of the Meta Account via authorization grants, and the Treasury contract pays all gas fees.

## Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34
- For native `mob` library: Android NDK + Rust toolchain

## Quick Start

1. Clone this repo and open `xion-android-demo/` in Android Studio
2. Sync Gradle
3. Build the mob native library (see below)
4. Configure your OAuth client ID and Treasury address in `app/build.gradle.kts`
5. Run on an emulator or device (API 26+)

## Configuration

Before running, update the placeholder values in `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "XION_TREASURY_ADDRESS", "\"xion1...your-treasury\"")
buildConfigField("String", "XION_OAUTH_CLIENT_ID", "\"your-client-id\"")
buildConfigField("String", "XION_OAUTH_AUTHORIZATION_ENDPOINT", "\"https://accounts.xion-testnet-2.burnt.com/authorize\"")
```

## Project Structure

```
app/src/main/java/com/burnt/xiondemo/
├── XionDemoApp.kt              # Hilt Application
├── MainActivity.kt             # Single activity, OAuth callback handler
├── navigation/NavGraph.kt      # Compose Navigation (5 routes)
├── ui/
│   ├── theme/                  # Material3 theme (XION brand colors)
│   ├── screens/
│   │   ├── connect/            # Landing: "Sign in with XION" OAuth
│   │   ├── wallet/             # Dashboard: balance, actions, send bottom sheet
│   │   ├── send/               # Send sheet content (form + success)
│   │   ├── contract/           # Execute smart contracts
│   │   └── history/            # Session transaction history
│   └── components/             # BalanceCard, TransactionRow, etc.
├── data/
│   ├── repository/             # XionRepository (mob-first, all signing local)
│   ├── datasource/             # MobDataSource (UniFFI), OAuthDataSource (auth only)
│   ├── remote/                 # XionOAuthApi (Retrofit, auth endpoints only)
│   └── model/                  # WalletState, TransactionResult, etc.
├── auth/                       # OAuthManager, PKCE utilities
├── security/                   # Android Keystore encryption, SecureStorage
├── di/                         # Hilt modules
└── util/                       # Constants, CoinFormatter, Result
```

## Building the mob Native Library

```bash
# Requires: Android NDK, Rust toolchain
export ANDROID_NDK_HOME=/path/to/ndk

./scripts/build-mob-android.sh
```

This cross-compiles the `mob` library (from `burnt-labs/mob`) for arm64-v8a, armeabi-v7a, and x86_64, placing `.so` files in `jniLibs/`.

## OAuth2 Setup

1. Register an OAuth2 client at the [XION Developer Portal](https://developer.burnt.com)
2. Set redirect URI to `xiondemo://callback`
3. Configure your Treasury contract with authz/feegrant support
4. Update `XION_OAUTH_CLIENT_ID` and `XION_TREASURY_ADDRESS` in `build.gradle.kts`

## Network Configuration

| Setting | Value |
|---|---|
| RPC | `https://rpc.xion-testnet-2.burnt.com:443` |
| REST | `https://api.xion-testnet-2.burnt.com/` |
| Chain ID | `xion-testnet-2` |
| Denom | `uxion` (1 XION = 1,000,000 uxion) |
| Gas Price | `0.025` |
| Address Prefix | `xion` |

## Architecture

MVVM + Jetpack Compose, Hilt DI, Coroutines, JNA

```
UI (Compose) -> ViewModels -> XionRepository -> MobDataSource (all signing)
                                             -> OAuthDataSource (auth only)
                                                      |
                                                mob native lib (UniFFI/JNA)
                                                      |
                                                XION RPC node
```

**Auth flow**: OAuth -> Meta Account address -> generate session keypair via mob -> save encrypted session mnemonic -> sign transactions with session key (granter = Meta Account, feeGranter = Treasury)

## Key Dependencies

- Compose BOM 2024.02.00 + Material3
- Navigation Compose 2.7.7
- Hilt 2.50
- Retrofit 2.9.0 + kotlinx-serialization (OAuth endpoints only)
- JNA 5.14.0 (AAR)
- AndroidX Security Crypto 1.1.0-alpha06
- AndroidX Browser 1.7.0 (Custom Tabs)

**Min SDK**: 26 | **Target SDK**: 34 | **Kotlin**: 1.9+ | **JDK**: 17

## Screens

1. **Connect** — "Sign in with XION" OAuth button, session restore on launch
2. **Wallet** — Meta Account dashboard with XION, SBC, and vault balances (vault always shown), grant status, recent transactions (up to 3), and inline Send bottom sheet
3. **Send** — Token transfer bottom sheet (height-constrained, IME-aware) with gasless signing via Treasury
4. **Vault** — Deposit/withdraw tokens to non-custodial vault. iOS shows confirmation alert before each operation
5. **Link Bank** — Plaid-powered bank account linking for on/off-ramp
6. **Buy / Cash Out** — Stablecoin on-ramp (ACH → SBC) and off-ramp (SBC → bank)
7. **Contract** — Smart contract execution with JSON editor
8. **History** — Session transaction list with detail view
