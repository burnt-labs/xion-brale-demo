# Vault Contract Deploy & Mobile Integration Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deploy the hm-vault contract to xion-testnet-2, then integrate vault deposit/withdraw/balance into both the Android and iOS demo apps.

**Architecture:** The vault contract is already built and tested (`contracts/hm-vault/`). We need to: (1) optimize the wasm binary with cosmwasm/optimizer Docker, (2) deploy via the mob library's `instantiateContract` method from within the app OR via wasmd CLI, (3) add `queryContractSmart` wrappers to both MobDataSource (Android) and MobSigningService (iOS) since they exist in the mob FFI bindings but aren't exposed yet, (4) add vault repository methods, (5) add vault UI screens for deposit/withdraw, (6) display vault balance on the wallet screen.

**Tech Stack:** CosmWasm 2.1, Rust, Docker (cosmwasm/optimizer), Kotlin/Jetpack Compose (Android), Swift/SwiftUI (iOS), mob FFI (UniFFI)

---

### Task 1: Optimize and Deploy Contract to Testnet

**Files:**
- Read: `contracts/hm-vault/Cargo.toml`
- Create: `contracts/hm-vault/deploy.sh` (helper script)

- [ ] **Step 1: Optimize the wasm binary with Docker**

```bash
cd contracts/hm-vault
docker run --rm -v "$(pwd)":/code \
  --mount type=volume,source="$(basename "$(pwd)")_cache",target=/target \
  --mount type=volume,source=registry_cache,target=/usr/local/cargo/registry \
  cosmwasm/optimizer:0.16.1
```

This produces `artifacts/hm_vault.wasm` — the optimized binary for deployment.

- [ ] **Step 2: Verify the optimized binary exists**

```bash
ls -la artifacts/hm_vault.wasm
```

Expected: File exists, ~150-300KB.

- [ ] **Step 3: Deploy to xion-testnet-2**

We have two options. If `wasmd` is not installed, we can use the mob CLI example app or write a small deploy script. The simplest path is:

**Option A — via wasmd (if installable):**
```bash
# Install wasmd if needed
go install github.com/CosmWasm/wasmd@latest

# Store code
wasmd tx wasm store artifacts/hm_vault.wasm \
  --from <key-name> \
  --chain-id xion-testnet-2 \
  --node https://rpc.xion-testnet-2.burnt.com:443 \
  --gas auto --gas-adjustment 1.3 \
  --fees 5000uxion \
  -y

# Note the code_id from the response

# Instantiate
wasmd tx wasm instantiate <code_id> \
  '{"allowed_denoms":["uxion","factory/xion17grq736740r70awldugfs3mls3stu9haewctv2/sbc"]}' \
  --label "hm-vault" \
  --no-admin \
  --from <key-name> \
  --chain-id xion-testnet-2 \
  --node https://rpc.xion-testnet-2.burnt.com:443 \
  --gas auto --gas-adjustment 1.3 \
  --fees 5000uxion \
  -y
```

**Option B — via mob library from the app:**
The mob FFI has `instantiateContract(admin, codeId, label, msg, funds, memo, gasLimit)`. If the wasm binary is already stored on-chain (code uploaded by someone else), any connected session can instantiate. If we need to upload code first, we'll need wasmd or a REST upload.

- [ ] **Step 4: Record the contract address**

After instantiation, note the contract address (e.g., `xion1vault...`). This will go into both Android and iOS Constants.

- [ ] **Step 5: Verify deployment by querying config**

```bash
wasmd query wasm contract-state smart <contract_address> \
  '{"config":{}}' \
  --node https://rpc.xion-testnet-2.burnt.com:443
```

Expected: `{"data":{"allowed_denoms":["uxion","factory/xion17grq736740r70awldugfs3mls3stu9haewctv2/sbc"]}}`

- [ ] **Step 6: Commit**

```bash
git add contracts/hm-vault/artifacts/
git commit -m "feat: add optimized hm-vault wasm binary for testnet deployment"
```

---

### Task 2: Add queryContractSmart to Android MobDataSource

**Files:**
- Modify: `xion-android-demo/app/src/main/java/com/burnt/xiondemo/data/datasource/MobDataSource.kt`

The mob FFI bindings already expose `Client.queryContractSmart(contractAddress, queryMsg)` in `uniffi/mob/mob.kt`, but `MobDataSource` doesn't wrap it. We need it for vault balance queries (read-only, no transaction).

- [ ] **Step 1: Add queryContractSmart to MobDataSource interface**

Add to the `MobDataSource` interface (after the `executeContract` method):

```kotlin
suspend fun queryContractSmart(contractAddress: String, queryMsg: ByteArray): ByteArray
```

- [ ] **Step 2: Implement in RealMobDataSource**

Add to `RealMobDataSource`:

```kotlin
override suspend fun queryContractSmart(
    contractAddress: String,
    queryMsg: ByteArray
): ByteArray = withContext(Dispatchers.IO) {
    val c = client ?: throw IllegalStateException("Client not initialized")
    c.queryContractSmart(contractAddress, queryMsg)
}
```

- [ ] **Step 3: Commit**

```bash
git add xion-android-demo/app/src/main/java/com/burnt/xiondemo/data/datasource/MobDataSource.kt
git commit -m "feat: expose queryContractSmart in Android MobDataSource"
```

---

### Task 3: Add queryContractSmart to iOS MobSigningService

**Files:**
- Modify: `xion-ios/XionDemo/Services/MobSigningService.swift`

Same situation — mob FFI has `queryContractSmart(contractAddress:queryMsg:)` but `MobSigningService` doesn't expose it.

- [ ] **Step 1: Add to MobSigningServiceProtocol**

Add to the protocol:

```swift
func queryContractSmart(contractAddress: String, queryMsg: Data) async throws -> Data
```

- [ ] **Step 2: Implement in MobSigningService**

Add to the class:

```swift
func queryContractSmart(contractAddress: String, queryMsg: Data) async throws -> Data {
    try await withCheckedThrowingContinuation { continuation in
        queue.async {
            do {
                guard let client = self.client else {
                    throw MobServiceError.clientNotInitialized
                }
                let result = try client.queryContractSmart(contractAddress: contractAddress, queryMsg: queryMsg)
                continuation.resume(returning: result)
            } catch {
                continuation.resume(throwing: error)
            }
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add xion-ios/XionDemo/Services/MobSigningService.swift
git commit -m "feat: expose queryContractSmart in iOS MobSigningService"
```

---

### Task 4: Add Vault Repository Methods — Android

**Files:**
- Modify: `xion-android-demo/app/src/main/java/com/burnt/xiondemo/util/Constants.kt`
- Modify: `xion-android-demo/app/src/main/java/com/burnt/xiondemo/data/repository/XionRepository.kt`
- Modify: `xion-android-demo/app/src/main/java/com/burnt/xiondemo/data/repository/XionRepositoryImpl.kt`

- [ ] **Step 1: Add vault contract address to Constants**

Add to `Constants.kt` after the sample contract lines:

```kotlin
// Vault contract
const val VAULT_CONTRACT_ADDRESS = "<deployed_contract_address>"
```

Replace `<deployed_contract_address>` with the actual address from Task 1.

- [ ] **Step 2: Add vault methods to XionRepository interface**

Add to the interface after `executeContract`:

```kotlin
suspend fun getVaultBalance(): Result<BalanceInfo>
suspend fun vaultDeposit(amount: String, denom: String = Constants.COIN_DENOM): Result<TransactionResult>
suspend fun vaultWithdraw(amount: String, denom: String = Constants.COIN_DENOM): Result<TransactionResult>
suspend fun vaultWithdrawAll(): Result<TransactionResult>
```

- [ ] **Step 3: Implement in XionRepositoryImpl**

Add the implementations. `getVaultBalance` uses `queryContractSmart` (read-only). The others use `executeContract`.

```kotlin
override suspend fun getVaultBalance(): Result<BalanceInfo> = Result.runCatching {
    val state = _walletState.value as? WalletState.Connected
        ?: throw IllegalStateException("Wallet not connected")
    val queryMsg = """{"balance":{"address":"${state.metaAccountAddress}"}}"""
    val responseBytes = mobDataSource.queryContractSmart(
        Constants.VAULT_CONTRACT_ADDRESS,
        queryMsg.toByteArray()
    )
    val responseStr = String(responseBytes)
    // Parse {"coins":[{"denom":"uxion","amount":"500000"}]}
    val json = org.json.JSONObject(responseStr)
    val coins = json.getJSONArray("coins")
    if (coins.length() == 0) {
        BalanceInfo(amount = "0", denom = Constants.COIN_DENOM)
    } else {
        // Sum all coin amounts for display (primary denom)
        val firstCoin = coins.getJSONObject(0)
        BalanceInfo(amount = firstCoin.getString("amount"), denom = firstCoin.getString("denom"))
    }
}

override suspend fun vaultDeposit(amount: String, denom: String): Result<TransactionResult> = withGrantRecovery {
    _walletState.value as? WalletState.Connected
        ?: throw IllegalStateException("Wallet not connected")
    val msg = """{"deposit":{}}"""
    val fundsCoins = listOf(Coin(denom = denom, amount = amount))
    val result = mobDataSource.executeContract(
        contractAddress = Constants.VAULT_CONTRACT_ADDRESS,
        msg = msg.toByteArray(),
        funds = fundsCoins,
        memo = null
    )
    val confirmed = if (result.success) {
        awaitTxConfirmation(result.txHash) ?: result
    } else {
        result
    }
    _transactionHistory.value = _transactionHistory.value + confirmed
    confirmed
}

override suspend fun vaultWithdraw(amount: String, denom: String): Result<TransactionResult> = withGrantRecovery {
    _walletState.value as? WalletState.Connected
        ?: throw IllegalStateException("Wallet not connected")
    val msg = """{"withdraw":{"coins":[{"denom":"$denom","amount":"$amount"}]}}"""
    val result = mobDataSource.executeContract(
        contractAddress = Constants.VAULT_CONTRACT_ADDRESS,
        msg = msg.toByteArray(),
        funds = emptyList(),
        memo = null
    )
    val confirmed = if (result.success) {
        awaitTxConfirmation(result.txHash) ?: result
    } else {
        result
    }
    _transactionHistory.value = _transactionHistory.value + confirmed
    confirmed
}

override suspend fun vaultWithdrawAll(): Result<TransactionResult> = withGrantRecovery {
    _walletState.value as? WalletState.Connected
        ?: throw IllegalStateException("Wallet not connected")
    val msg = """{"withdraw_all":{}}"""
    val result = mobDataSource.executeContract(
        contractAddress = Constants.VAULT_CONTRACT_ADDRESS,
        msg = msg.toByteArray(),
        funds = emptyList(),
        memo = null
    )
    val confirmed = if (result.success) {
        awaitTxConfirmation(result.txHash) ?: result
    } else {
        result
    }
    _transactionHistory.value = _transactionHistory.value + confirmed
    confirmed
}
```

- [ ] **Step 4: Commit**

```bash
git add xion-android-demo/app/src/main/java/com/burnt/xiondemo/util/Constants.kt \
       xion-android-demo/app/src/main/java/com/burnt/xiondemo/data/repository/XionRepository.kt \
       xion-android-demo/app/src/main/java/com/burnt/xiondemo/data/repository/XionRepositoryImpl.kt
git commit -m "feat: add vault deposit/withdraw/balance repository methods (Android)"
```

---

### Task 5: Add Vault Repository Methods — iOS

**Files:**
- Modify: `xion-ios/XionDemo/Configuration/Constants.swift`
- Modify: `xion-ios/XionDemo/Data/Repository/XionRepository.swift`
- Modify: `xion-ios/XionDemo/Data/Repository/XionRepositoryImpl.swift`

- [ ] **Step 1: Add vault contract address to Constants**

Add after `sampleContractMsg`:

```swift
// Vault contract
static let vaultContractAddress = "<deployed_contract_address>"
```

- [ ] **Step 2: Add vault methods to XionRepositoryProtocol**

Add after `executeContract`:

```swift
func getVaultBalance() async throws -> BalanceInfo
func vaultDeposit(amount: String, denom: String) async throws -> TransactionResult
func vaultWithdraw(amount: String, denom: String) async throws -> TransactionResult
func vaultWithdrawAll() async throws -> TransactionResult
```

Add default denom extension:

```swift
extension XionRepositoryProtocol {
    func vaultDeposit(amount: String) async throws -> TransactionResult {
        try await vaultDeposit(amount: amount, denom: Constants.coinDenom)
    }
    func vaultWithdraw(amount: String) async throws -> TransactionResult {
        try await vaultWithdraw(amount: amount, denom: Constants.coinDenom)
    }
}
```

- [ ] **Step 3: Implement in XionRepositoryImpl**

```swift
func getVaultBalance() async throws -> BalanceInfo {
    guard let state = sessionManager.walletState.connectedState else {
        throw XionRepositoryError.notConnected
    }
    let query = #"{"balance":{"address":"\#(state.metaAccountAddress)"}}"#
    let queryData = Data(query.utf8)
    let responseData = try await mobService.queryContractSmart(
        contractAddress: Constants.vaultContractAddress,
        queryMsg: queryData
    )
    // Parse {"coins":[{"denom":"uxion","amount":"500000"}]}
    let json = try JSONSerialization.jsonObject(with: responseData) as? [String: Any]
    let coins = json?["coins"] as? [[String: Any]] ?? []
    guard let first = coins.first,
          let amount = first["amount"] as? String,
          let denom = first["denom"] as? String else {
        return BalanceInfo(amount: "0", denom: Constants.coinDenom)
    }
    return BalanceInfo(amount: amount, denom: denom)
}

func vaultDeposit(amount: String, denom: String) async throws -> TransactionResult {
    let msg = #"{"deposit":{}}"#
    let funds = [Coin(denom: denom, amount: amount)]
    let result = try await withGrantRecovery {
        try await self.mobService.executeContract(
            contractAddress: Constants.vaultContractAddress,
            msg: Data(msg.utf8),
            funds: funds,
            memo: nil
        )
    }
    let confirmed = result.success ? (await awaitTxConfirmation(txHash: result.txHash) ?? result) : result
    sessionManager.appendTransaction(confirmed)
    return confirmed
}

func vaultWithdraw(amount: String, denom: String) async throws -> TransactionResult {
    let msg = #"{"withdraw":{"coins":[{"denom":"\#(denom)","amount":"\#(amount)"}]}}"#
    let result = try await withGrantRecovery {
        try await self.mobService.executeContract(
            contractAddress: Constants.vaultContractAddress,
            msg: Data(msg.utf8),
            funds: [],
            memo: nil
        )
    }
    let confirmed = result.success ? (await awaitTxConfirmation(txHash: result.txHash) ?? result) : result
    sessionManager.appendTransaction(confirmed)
    return confirmed
}

func vaultWithdrawAll() async throws -> TransactionResult {
    let msg = #"{"withdraw_all":{}}"#
    let result = try await withGrantRecovery {
        try await self.mobService.executeContract(
            contractAddress: Constants.vaultContractAddress,
            msg: Data(msg.utf8),
            funds: [],
            memo: nil
        )
    }
    let confirmed = result.success ? (await awaitTxConfirmation(txHash: result.txHash) ?? result) : result
    sessionManager.appendTransaction(confirmed)
    return confirmed
}
```

- [ ] **Step 4: Commit**

```bash
git add xion-ios/XionDemo/Configuration/Constants.swift \
       xion-ios/XionDemo/Data/Repository/XionRepository.swift \
       xion-ios/XionDemo/Data/Repository/XionRepositoryImpl.swift
git commit -m "feat: add vault deposit/withdraw/balance repository methods (iOS)"
```

---

### Task 6: Add Vault Balance to Android Wallet Screen

**Files:**
- Modify: `xion-android-demo/app/src/main/java/com/burnt/xiondemo/ui/screens/wallet/WalletViewModel.kt`
- Modify: `xion-android-demo/app/src/main/java/com/burnt/xiondemo/ui/screens/wallet/WalletScreen.kt`

- [ ] **Step 1: Add vault balance to WalletUiState and WalletViewModel**

Add to `WalletUiState` data class:

```kotlin
val vaultBalance: String? = null,
```

Add to `WalletViewModel`:

```kotlin
private fun loadVaultBalance() {
    viewModelScope.launch {
        when (val result = repository.getVaultBalance()) {
            is Result.Success -> {
                _uiState.update { it.copy(vaultBalance = result.data.amount) }
            }
            is Result.Error -> {} // Non-critical
            is Result.Loading -> {}
        }
    }
}
```

Add `loadVaultBalance()` call to `refresh()`:

```kotlin
fun refresh() {
    loadBalance()
    loadSbcBalance()
    loadVaultBalance()
    loadBlockHeight()
    loadRecentTransactions()
    checkBankLinked()
}
```

- [ ] **Step 2: Add vault balance display to WalletScreen**

In `WalletScreen.kt`, add a vault balance section inside the balance Card, after the SBC balance block (after line 253's closing `}`):

```kotlin
if (uiState.vaultBalance != null && uiState.vaultBalance != "0") {
    Spacer(modifier = Modifier.height(16.dp))
    HorizontalDivider(color = SubtitleText.copy(alpha = 0.2f))
    Spacer(modifier = Modifier.height(16.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Vault Balance",
                fontSize = 14.sp,
                color = SubtitleText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = CoinFormatter.formatWithDenom(uiState.vaultBalance!!),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = GreetingText,
                maxLines = 1
            )
        }
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Vault",
            tint = SubtitleText,
            modifier = Modifier.size(20.dp)
        )
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add xion-android-demo/app/src/main/java/com/burnt/xiondemo/ui/screens/wallet/WalletViewModel.kt \
       xion-android-demo/app/src/main/java/com/burnt/xiondemo/ui/screens/wallet/WalletScreen.kt
git commit -m "feat: display vault balance on Android wallet screen"
```

---

### Task 7: Add Vault Balance to iOS Wallet Screen

**Files:**
- Modify: `xion-ios/XionDemo/UI/Screens/Wallet/WalletViewModel.swift`
- Modify: `xion-ios/XionDemo/UI/Screens/Wallet/WalletView.swift`

- [ ] **Step 1: Add vault balance to WalletViewModel**

Add property:

```swift
@Published var vaultBalance: String?
```

Add method:

```swift
private func loadVaultBalance() {
    Task {
        do {
            let info = try await repository.getVaultBalance()
            vaultBalance = info.amount
        } catch {
            // Non-critical
        }
    }
}
```

Add `loadVaultBalance()` to `refresh()`:

```swift
func refresh() {
    checkBankLinked()
    loadBalance()
    loadSbcBalance()
    loadVaultBalance()
    loadBlockHeight()
    loadTransactions()
}
```

- [ ] **Step 2: Add vault balance display to WalletView**

In `WalletView.swift`, add after the SBC balance section inside the balance VStack (after line 139's closing `}`):

```swift
if let vaultBal = viewModel.vaultBalance, vaultBal != "0" {
    Divider()
        .padding(.vertical, 8)

    HStack {
        VStack(alignment: .leading, spacing: 4) {
            Text("Vault Balance")
                .font(.system(size: 14))
                .foregroundStyle(Color.subtitleText)

            Text(CoinFormatter.formatWithDenom(vaultBal))
                .font(.system(size: 22, weight: .bold))
                .foregroundStyle(Color.greetingText)
                .lineLimit(1)
        }
        Spacer()
        Image(systemName: "lock.fill")
            .font(.system(size: 16))
            .foregroundStyle(Color.subtitleText)
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add xion-ios/XionDemo/UI/Screens/Wallet/WalletViewModel.swift \
       xion-ios/XionDemo/UI/Screens/Wallet/WalletView.swift
git commit -m "feat: display vault balance on iOS wallet screen"
```

---

### Task 8: Create Android Vault Screen (Deposit/Withdraw)

**Files:**
- Create: `xion-android-demo/app/src/main/java/com/burnt/xiondemo/ui/screens/vault/VaultViewModel.kt`
- Create: `xion-android-demo/app/src/main/java/com/burnt/xiondemo/ui/screens/vault/VaultScreen.kt`
- Modify: `xion-android-demo/app/src/main/java/com/burnt/xiondemo/navigation/NavGraph.kt`
- Modify: `xion-android-demo/app/src/main/java/com/burnt/xiondemo/ui/screens/wallet/WalletScreen.kt`

- [ ] **Step 1: Create VaultViewModel**

```kotlin
package com.burnt.xiondemo.ui.screens.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burnt.xiondemo.data.repository.XionRepository
import com.burnt.xiondemo.util.CoinFormatter
import com.burnt.xiondemo.util.Constants
import com.burnt.xiondemo.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VaultUiState(
    val vaultBalance: String? = null,
    val amount: String = "",
    val isLoading: Boolean = false,
    val isBalanceLoading: Boolean = false,
    val txHash: String? = null,
    val error: String? = null
)

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val repository: XionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VaultUiState())
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    init {
        loadVaultBalance()
    }

    fun updateAmount(value: String) {
        _uiState.update { it.copy(amount = value, error = null) }
    }

    fun loadVaultBalance() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBalanceLoading = true) }
            when (val result = repository.getVaultBalance()) {
                is Result.Success -> {
                    _uiState.update { it.copy(vaultBalance = result.data.amount, isBalanceLoading = false) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isBalanceLoading = false) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun deposit() {
        val amount = _uiState.value.amount
        if (amount.isBlank()) return
        val microAmount = CoinFormatter.displayToMicro(amount, Constants.DECIMALS)

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, txHash = null) }
            when (val result = repository.vaultDeposit(microAmount)) {
                is Result.Success -> {
                    if (result.data.success) {
                        _uiState.update { it.copy(isLoading = false, txHash = result.data.txHash, amount = "") }
                        loadVaultBalance()
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = result.data.rawLog) }
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun withdraw() {
        val amount = _uiState.value.amount
        if (amount.isBlank()) return
        val microAmount = CoinFormatter.displayToMicro(amount, Constants.DECIMALS)

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, txHash = null) }
            when (val result = repository.vaultWithdraw(microAmount)) {
                is Result.Success -> {
                    if (result.data.success) {
                        _uiState.update { it.copy(isLoading = false, txHash = result.data.txHash, amount = "") }
                        loadVaultBalance()
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = result.data.rawLog) }
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun withdrawAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, txHash = null) }
            when (val result = repository.vaultWithdrawAll()) {
                is Result.Success -> {
                    if (result.data.success) {
                        _uiState.update { it.copy(isLoading = false, txHash = result.data.txHash, amount = "") }
                        loadVaultBalance()
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = result.data.rawLog) }
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetState() {
        _uiState.update { it.copy(txHash = null, error = null, amount = "") }
    }
}
```

- [ ] **Step 2: Create VaultScreen**

```kotlin
package com.burnt.xiondemo.ui.screens.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.burnt.xiondemo.ui.components.ErrorBanner
import com.burnt.xiondemo.ui.theme.*
import com.burnt.xiondemo.util.CoinFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    onBack: () -> Unit,
    viewModel: VaultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadVaultBalance()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vault", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ScreenBackground)
            )
        },
        containerColor = ScreenBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            // Vault balance card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Vault Balance", fontSize = 14.sp, color = SubtitleText)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (uiState.isBalanceLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = XionOrange
                            )
                        } else {
                            Text(
                                text = if (uiState.vaultBalance != null) CoinFormatter.formatWithDenom(uiState.vaultBalance!!) else "0 XION",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreetingText
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Vault",
                        tint = SubtitleText,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Success state
            if (uiState.txHash != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = XionGreen.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Transaction Successful", fontWeight = FontWeight.SemiBold, color = XionGreen)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.txHash!!,
                            fontSize = 12.sp,
                            color = SubtitleText,
                            maxLines = 1
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.resetState() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = XionOrange)
                ) {
                    Text("New Transaction", fontWeight = FontWeight.SemiBold)
                }
            } else {
                // Amount input
                OutlinedTextField(
                    value = uiState.amount,
                    onValueChange = { viewModel.updateAmount(it) },
                    label = { Text("Amount (XION)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !uiState.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Deposit / Withdraw buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.deposit() },
                        enabled = uiState.amount.isNotBlank() && !uiState.isLoading,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = XionGreen)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Deposit", fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Button(
                        onClick = { viewModel.withdraw() },
                        enabled = uiState.amount.isNotBlank() && !uiState.isLoading,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MintscanBlue)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Withdraw", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Withdraw All button
                OutlinedButton(
                    onClick = { viewModel.withdrawAll() },
                    enabled = !uiState.isLoading && uiState.vaultBalance != null && uiState.vaultBalance != "0",
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Withdraw All", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ErrorBanner(
                message = uiState.error,
                onDismiss = { viewModel.clearError() },
                onRetry = null
            )
        }
    }
}
```

- [ ] **Step 3: Add vault route to NavGraph**

In `NavGraph.kt`, add to `Routes`:

```kotlin
const val VAULT = "vault"
```

Add composable:

```kotlin
composable(Routes.VAULT) {
    VaultScreen(onBack = { navController.popBackStack() })
}
```

- [ ] **Step 4: Add vault navigation button to WalletScreen**

In `WalletScreen.kt`, add `onNavigateToVault: () -> Unit` parameter to `WalletScreen` composable and add a "Vault" button below the "Send Tokens" button, before the bank link section:

```kotlin
Spacer(modifier = Modifier.height(8.dp))
OutlinedButton(
    onClick = onNavigateToVault,
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    border = BorderStroke(1.dp, SubtitleText.copy(alpha = 0.3f))
) {
    Icon(
        imageVector = Icons.Default.Lock,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
        tint = GreetingText
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
        text = "Vault",
        fontWeight = FontWeight.SemiBold,
        color = GreetingText
    )
}
```

Update NavGraph to pass the callback:

```kotlin
composable(Routes.WALLET) {
    WalletScreen(
        onNavigateToContract = { navController.navigate(Routes.CONTRACT) },
        onNavigateToHistory = { navController.navigate(Routes.HISTORY) },
        onNavigateToOnramp = { navController.navigate(Routes.ONRAMP) },
        onNavigateToOfframp = { navController.navigate(Routes.OFFRAMP) },
        onNavigateToLinkBank = { navController.navigate(Routes.LINK_BANK) },
        onNavigateToVault = { navController.navigate(Routes.VAULT) },
        onDisconnected = { ... }
    )
}
```

- [ ] **Step 5: Commit**

```bash
git add xion-android-demo/app/src/main/java/com/burnt/xiondemo/ui/screens/vault/ \
       xion-android-demo/app/src/main/java/com/burnt/xiondemo/navigation/NavGraph.kt \
       xion-android-demo/app/src/main/java/com/burnt/xiondemo/ui/screens/wallet/WalletScreen.kt
git commit -m "feat: add vault screen with deposit/withdraw on Android"
```

---

### Task 9: Create iOS Vault Screen (Deposit/Withdraw)

**Files:**
- Create: `xion-ios/XionDemo/UI/Screens/Vault/VaultViewModel.swift`
- Create: `xion-ios/XionDemo/UI/Screens/Vault/VaultView.swift`
- Modify: `xion-ios/XionDemo/Navigation/AppNavigation.swift`
- Modify: `xion-ios/XionDemo/UI/Screens/Wallet/WalletView.swift`

- [ ] **Step 1: Create VaultViewModel**

```swift
import Foundation

@MainActor
final class VaultViewModel: ObservableObject {

    @Published var vaultBalance: String?
    @Published var amount = ""
    @Published var isLoading = false
    @Published var isBalanceLoading = false
    @Published var txHash: String?
    @Published var error: String?

    private let repository: XionRepositoryProtocol

    init(repository: XionRepositoryProtocol) {
        self.repository = repository
        loadVaultBalance()
    }

    func loadVaultBalance() {
        Task {
            isBalanceLoading = true
            do {
                let info = try await repository.getVaultBalance()
                vaultBalance = info.amount
            } catch {
                // Non-critical
            }
            isBalanceLoading = false
        }
    }

    func deposit() {
        guard !amount.isEmpty else { return }
        let microAmount = CoinFormatter.displayToMicro(amount, decimals: Constants.decimals)

        Task {
            isLoading = true
            error = nil
            txHash = nil
            do {
                let result = try await repository.vaultDeposit(amount: microAmount)
                if result.success {
                    txHash = result.txHash
                    amount = ""
                    loadVaultBalance()
                } else {
                    self.error = result.rawLog
                }
            } catch {
                self.error = error.localizedDescription
            }
            isLoading = false
        }
    }

    func withdraw() {
        guard !amount.isEmpty else { return }
        let microAmount = CoinFormatter.displayToMicro(amount, decimals: Constants.decimals)

        Task {
            isLoading = true
            error = nil
            txHash = nil
            do {
                let result = try await repository.vaultWithdraw(amount: microAmount)
                if result.success {
                    txHash = result.txHash
                    amount = ""
                    loadVaultBalance()
                } else {
                    self.error = result.rawLog
                }
            } catch {
                self.error = error.localizedDescription
            }
            isLoading = false
        }
    }

    func withdrawAll() {
        Task {
            isLoading = true
            error = nil
            txHash = nil
            do {
                let result = try await repository.vaultWithdrawAll()
                if result.success {
                    txHash = result.txHash
                    amount = ""
                    loadVaultBalance()
                } else {
                    self.error = result.rawLog
                }
            } catch {
                self.error = error.localizedDescription
            }
            isLoading = false
        }
    }

    func clearError() { error = nil }
    func resetState() { txHash = nil; error = nil; amount = "" }
}
```

- [ ] **Step 2: Create VaultView**

```swift
import SwiftUI

struct VaultView: View {
    @ObservedObject var viewModel: VaultViewModel
    let onDone: () -> Void

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                // Vault balance card
                HStack {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Vault Balance")
                            .font(.system(size: 14))
                            .foregroundStyle(Color.subtitleText)

                        if viewModel.isBalanceLoading {
                            ProgressView().tint(.xionOrange)
                        } else {
                            Text(viewModel.vaultBalance.map { CoinFormatter.formatWithDenom($0) } ?? "0 XION")
                                .font(.system(size: 28, weight: .bold))
                                .foregroundStyle(Color.greetingText)
                        }
                    }
                    Spacer()
                    Image(systemName: "lock.fill")
                        .font(.system(size: 24))
                        .foregroundStyle(Color.subtitleText)
                }
                .padding(24)
                .background(Color.cardBackground)
                .clipShape(RoundedRectangle(cornerRadius: 20))
                .shadow(color: Color.cardShadow, radius: 2, y: 1)

                if let txHash = viewModel.txHash {
                    // Success state
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Transaction Successful")
                            .font(.system(size: 16, weight: .semibold))
                            .foregroundStyle(Color.xionGreen)
                        Text(txHash)
                            .font(.system(size: 12, design: .monospaced))
                            .foregroundStyle(Color.subtitleText)
                            .lineLimit(1)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(16)
                    .background(Color.xionGreen.opacity(0.1))
                    .clipShape(RoundedRectangle(cornerRadius: 12))

                    Button(action: viewModel.resetState) {
                        Text("New Transaction")
                            .font(.system(size: 16, weight: .semibold))
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .background(Color.xionOrange)
                            .foregroundStyle(.white)
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                    }
                    .buttonStyle(.plain)
                } else {
                    // Amount input
                    TextField("Amount (XION)", text: $viewModel.amount)
                        .keyboardType(.decimalPad)
                        .textFieldStyle(.roundedBorder)
                        .disabled(viewModel.isLoading)

                    // Deposit / Withdraw
                    HStack(spacing: 12) {
                        Button(action: viewModel.deposit) {
                            Group {
                                if viewModel.isLoading {
                                    ProgressView().tint(.white)
                                } else {
                                    Text("Deposit")
                                        .font(.system(size: 16, weight: .semibold))
                                }
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .background(Color.xionGreen.opacity(viewModel.amount.isEmpty || viewModel.isLoading ? 0.5 : 1.0))
                            .foregroundStyle(.white)
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                        }
                        .buttonStyle(.plain)
                        .disabled(viewModel.amount.isEmpty || viewModel.isLoading)

                        Button(action: viewModel.withdraw) {
                            Group {
                                if viewModel.isLoading {
                                    ProgressView().tint(.white)
                                } else {
                                    Text("Withdraw")
                                        .font(.system(size: 16, weight: .semibold))
                                }
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .background(Color.mintscanBlue.opacity(viewModel.amount.isEmpty || viewModel.isLoading ? 0.5 : 1.0))
                            .foregroundStyle(.white)
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                        }
                        .buttonStyle(.plain)
                        .disabled(viewModel.amount.isEmpty || viewModel.isLoading)
                    }

                    // Withdraw All
                    Button(action: viewModel.withdrawAll) {
                        Text("Withdraw All")
                            .font(.system(size: 16, weight: .semibold))
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(Color.subtitleText.opacity(0.3), lineWidth: 1)
                            )
                            .foregroundStyle(Color.greetingText)
                    }
                    .buttonStyle(.plain)
                    .disabled(viewModel.isLoading || viewModel.vaultBalance == nil || viewModel.vaultBalance == "0")
                }

                // Error
                ErrorBanner(
                    message: viewModel.error,
                    onDismiss: viewModel.clearError,
                    onRetry: nil
                )
            }
            .padding(24)
        }
        .background(Color.screenBackground)
        .navigationTitle("Vault")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear { viewModel.loadVaultBalance() }
    }
}
```

- [ ] **Step 3: Add vault route to AppNavigation**

In `AppNavigation.swift`, add to `Route` enum:

```swift
case vault
```

Add navigation destination inside `.navigationDestination(for: Route.self)`:

```swift
case .vault:
    VaultView(
        viewModel: VaultViewModel(repository: container.repository),
        onDone: { path.removeLast() }
    )
```

- [ ] **Step 4: Add vault navigation to WalletView**

Add `onNavigateToVault: () -> Void` parameter to `WalletView`. Add a Vault button after the Send Tokens button:

```swift
Button(action: onNavigateToVault) {
    HStack(spacing: 8) {
        Image(systemName: "lock.fill")
            .font(.system(size: 16))
        Text("Vault")
            .font(.system(size: 16, weight: .semibold))
    }
    .frame(maxWidth: .infinity)
    .padding(.vertical, 14)
    .overlay(
        RoundedRectangle(cornerRadius: 12)
            .stroke(Color.subtitleText.opacity(0.3), lineWidth: 1)
    )
    .foregroundStyle(Color.greetingText)
}
.buttonStyle(.plain)
```

Update `AppNavigation.swift` to pass the callback:

```swift
WalletView(
    ...
    onNavigateToVault: { path.append(Route.vault) },
    ...
)
```

- [ ] **Step 5: Commit**

```bash
git add xion-ios/XionDemo/UI/Screens/Vault/ \
       xion-ios/XionDemo/Navigation/AppNavigation.swift \
       xion-ios/XionDemo/UI/Screens/Wallet/WalletView.swift
git commit -m "feat: add vault screen with deposit/withdraw on iOS"
```

---

### Task 10: Verify End-to-End

- [ ] **Step 1: Build Android app**

```bash
cd xion-android-demo && ./gradlew assembleDebug
```

- [ ] **Step 2: Build iOS app**

Open `xion-ios/XionDemo.xcodeproj` in Xcode, build for simulator.

- [ ] **Step 3: Test vault flow on Android**

1. Connect wallet via Abstraxion
2. Verify vault balance shows "0 XION" on wallet screen (or doesn't show if 0)
3. Navigate to Vault screen via button
4. Enter 0.1, tap Deposit → should succeed (if user has XION balance)
5. Vault balance updates
6. Enter 0.05, tap Withdraw → should succeed
7. Tap Withdraw All → empties vault

- [ ] **Step 4: Test vault flow on iOS**

Same flow as Android.

- [ ] **Step 5: Final commit**

```bash
git add -A
git commit -m "feat: vault contract deployed and integrated into Android + iOS demos"
```
