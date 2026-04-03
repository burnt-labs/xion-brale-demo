# Link Bank Account Screen Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Extract the bank linking form from the Buy Stablecoins screen into its own dedicated screen, accessible from the wallet. Disable Buy and Cash Out buttons until a bank account is linked.

**Architecture:** The bank linking form (name, email, phone, DOB fields + Plaid Link flow) moves to a new `LinkBankScreen`/`LinkBankView`. The wallet screen checks SecureStorage for a linked bank and shows a "Link Bank Account" card when not linked, while disabling Buy/Cash Out. The onramp/offramp screens no longer contain linking UI — they assume a bank is already linked.

**Tech Stack:** Kotlin/Compose (Android), SwiftUI (iOS), Hilt DI (Android), manual DI (iOS)

---

## File Structure

### Android
| File | Action | Responsibility |
|------|--------|---------------|
| `ui/screens/linkbank/LinkBankScreen.kt` | Create | Full-screen bank linking UI with form + Plaid |
| `ui/screens/linkbank/LinkBankViewModel.kt` | Create | Form state, validation, Plaid flow, persistence |
| `ui/screens/wallet/WalletScreen.kt` | Modify | Add bank status card, disable Buy/Cash Out when unlinked |
| `ui/screens/wallet/WalletViewModel.kt` | Modify | Add `bankLinked` state from SecureStorage |
| `ui/screens/brale/OnrampScreen.kt` | Modify | Remove bank linking form section |
| `ui/screens/brale/OnrampViewModel.kt` | Modify | Remove form fields, validation, Plaid methods |
| `navigation/NavGraph.kt` | Modify | Add `LINK_BANK` route |

### iOS
| File | Action | Responsibility |
|------|--------|---------------|
| `UI/Screens/LinkBank/LinkBankView.swift` | Create | Full-screen bank linking UI with form + Plaid |
| `UI/Screens/LinkBank/LinkBankViewModel.swift` | Create | Form state, validation, Plaid flow, persistence |
| `UI/Screens/Wallet/WalletView.swift` | Modify | Add bank status card, disable Buy/Cash Out when unlinked |
| `UI/Screens/Wallet/WalletViewModel.swift` | Modify | Add `bankLinked` state from SecureStorage |
| `UI/Screens/Brale/OnrampView.swift` | Modify | Remove bank linking form section |
| `UI/Screens/Brale/OnrampViewModel.swift` | Modify | Remove form fields, validation, Plaid methods |
| `Navigation/AppNavigation.swift` | Modify | Add `.linkBank` route |

---

### Task 1: Create Android LinkBankViewModel

**Files:**
- Create: `xion-android-demo/app/src/main/java/com/burnt/xiondemo/ui/screens/linkbank/LinkBankViewModel.kt`

This extracts the form state, validation, and Plaid flow from `OnrampViewModel` into a standalone ViewModel.

- [ ] **Step 1: Create LinkBankViewModel**

```kotlin
package com.burnt.xiondemo.ui.screens.linkbank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burnt.xiondemo.data.repository.BraleRepository
import com.burnt.xiondemo.security.SecureStorage
import com.burnt.xiondemo.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LinkBankUiState(
    val userName: String = "",
    val userEmail: String = "",
    val userPhone: String = "",
    val userDob: String = "",
    val userNameError: String? = null,
    val userEmailError: String? = null,
    val userPhoneError: String? = null,
    val userDobError: String? = null,
    val bankLinked: Boolean = false,
    val bankAddressId: String? = null,
    val bankName: String? = null,
    val plaidLinkToken: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LinkBankViewModel @Inject constructor(
    private val braleRepository: BraleRepository,
    private val secureStorage: SecureStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(LinkBankUiState())
    val uiState: StateFlow<LinkBankUiState> = _uiState.asStateFlow()

    init {
        val bankId = secureStorage.getString(Constants.PREF_BRALE_BANK_ADDRESS_ID)
        val savedName = secureStorage.getString(Constants.PREF_BRALE_USER_NAME) ?: ""
        val savedEmail = secureStorage.getString(Constants.PREF_BRALE_USER_EMAIL) ?: ""
        val savedPhone = secureStorage.getString(Constants.PREF_BRALE_USER_PHONE) ?: ""
        val savedDob = secureStorage.getString(Constants.PREF_BRALE_USER_DOB) ?: ""
        _uiState.value = _uiState.value.copy(
            bankLinked = bankId != null,
            bankAddressId = bankId,
            userName = savedName,
            userEmail = savedEmail,
            userPhone = savedPhone,
            userDob = savedDob
        )
    }

    fun updateUserName(value: String) {
        val error = if (value.isBlank()) "Name is required" else null
        _uiState.value = _uiState.value.copy(userName = value, userNameError = error)
    }

    fun updateUserEmail(value: String) {
        val error = when {
            value.isBlank() -> "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches() -> "Enter a valid email address"
            else -> null
        }
        _uiState.value = _uiState.value.copy(userEmail = value, userEmailError = error)
    }

    fun updateUserPhone(value: String) {
        val error = when {
            value.isBlank() -> "Phone number is required"
            !value.startsWith("+") -> "Must start with + (e.g. +15551234567)"
            !value.drop(1).all { it.isDigit() } -> "Only digits after +"
            value.length < 11 -> "Enter full number with country code"
            else -> null
        }
        _uiState.value = _uiState.value.copy(userPhone = value, userPhoneError = error)
    }

    fun updateUserDob(value: String) {
        val error = when {
            value.isBlank() -> "Date of birth is required"
            !value.matches(Regex("""\d{4}-\d{2}-\d{2}""")) -> "Use YYYY-MM-DD format"
            else -> null
        }
        _uiState.value = _uiState.value.copy(userDob = value, userDobError = error)
    }

    val isLinkFormValid: Boolean
        get() {
            val state = _uiState.value
            return state.userName.isNotBlank()
                && state.userNameError == null
                && state.userEmail.isNotBlank()
                && state.userEmailError == null
                && state.userPhone.isNotBlank()
                && state.userPhoneError == null
                && state.userDob.isNotBlank()
                && state.userDobError == null
        }

    fun requestPlaidLinkToken() {
        val state = _uiState.value
        val name = state.userName.trim()
        val email = state.userEmail.trim()
        val phone = state.userPhone.trim()
        val dob = state.userDob.trim()
        secureStorage.putString(Constants.PREF_BRALE_USER_NAME, name)
        secureStorage.putString(Constants.PREF_BRALE_USER_EMAIL, email)
        secureStorage.putString(Constants.PREF_BRALE_USER_PHONE, phone)
        secureStorage.putString(Constants.PREF_BRALE_USER_DOB, dob)

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = braleRepository.createPlaidLinkToken(name = name, email = email, phone = phone, dob = dob)
                _uiState.value = _uiState.value.copy(
                    plaidLinkToken = response.linkToken,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to create Plaid link",
                    isLoading = false
                )
            }
        }
    }

    fun onPlaidSuccess(publicToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, plaidLinkToken = null)
            try {
                val addressId = braleRepository.registerBankAccount(publicToken)
                secureStorage.putString(Constants.PREF_BRALE_BANK_ADDRESS_ID, addressId)
                _uiState.value = _uiState.value.copy(
                    bankLinked = true,
                    bankAddressId = addressId,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to register bank account",
                    isLoading = false
                )
            }
        }
    }

    fun onPlaidCancelled() {
        _uiState.value = _uiState.value.copy(plaidLinkToken = null, isLoading = false)
    }

    fun onPlaidExit(errorMessage: String?) {
        _uiState.value = _uiState.value.copy(
            plaidLinkToken = null,
            isLoading = false,
            error = errorMessage ?: "Plaid Link was closed"
        )
    }

    fun unlinkBank() {
        secureStorage.remove(Constants.PREF_BRALE_BANK_ADDRESS_ID)
        _uiState.value = _uiState.value.copy(
            bankLinked = false,
            bankAddressId = null,
            bankName = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add xion-android-demo/app/src/main/java/com/burnt/xiondemo/ui/screens/linkbank/LinkBankViewModel.kt
git commit -m "feat: create LinkBankViewModel for standalone bank linking"
```

---

### Task 2: Create Android LinkBankScreen

**Files:**
- Create: `xion-android-demo/app/src/main/java/com/burnt/xiondemo/ui/screens/linkbank/LinkBankScreen.kt`

Full-screen bank linking UI — extracted from OnrampScreen's form section. Includes Plaid Link launcher, form fields, status card, and unlink button.

- [ ] **Step 1: Create LinkBankScreen.kt**

The screen reuses the same form UI that was in OnrampScreen (fields, validation, Link button), plus a success state showing the linked bank with an Unlink option and a Done button.

Key structure:
- Scaffold with "Link Bank Account" title and back button
- If not linked: form with 4 fields + Link Bank Account button
- If linked: success card with bank name + Unlink + Done button
- Plaid Link launcher (same pattern as OnrampScreen)
- Error banner overlay

- [ ] **Step 2: Commit**

```bash
git add xion-android-demo/app/src/main/java/com/burnt/xiondemo/ui/screens/linkbank/LinkBankScreen.kt
git commit -m "feat: create LinkBankScreen UI for standalone bank linking"
```

---

### Task 3: Update Android WalletScreen and WalletViewModel

**Files:**
- Modify: `xion-android-demo/app/src/main/java/com/burnt/xiondemo/ui/screens/wallet/WalletViewModel.kt`
- Modify: `xion-android-demo/app/src/main/java/com/burnt/xiondemo/ui/screens/wallet/WalletScreen.kt`

Add `bankLinked` state to WalletViewModel (read from SecureStorage). On WalletScreen, add a "Link Bank Account" card above Buy/Cash Out when not linked, and disable the buttons.

- [ ] **Step 1: Add bankLinked to WalletViewModel**

Add to `WalletUiState`:
```kotlin
val bankLinked: Boolean = false
```

In `WalletViewModel`, inject `SecureStorage` and check bank ID on `refresh()`:
```kotlin
private fun checkBankLinked() {
    val bankId = secureStorage.getString(Constants.PREF_BRALE_BANK_ADDRESS_ID)
    _uiState.value = _uiState.value.copy(bankLinked = bankId != null)
}
```

Call `checkBankLinked()` inside `refresh()` so it updates every time the wallet screen appears (including return from LinkBank screen).

- [ ] **Step 2: Update WalletScreen**

Add `onNavigateToLinkBank: () -> Unit` parameter.

Before the Buy/Cash Out row, add a bank status card:
- When `!uiState.bankLinked`: show a card with building icon, "Link Bank Account" text, subtitle "Required for buying and selling stablecoins", and a "Link" button that calls `onNavigateToLinkBank`.
- When `uiState.bankLinked`: show a small "Bank Linked" indicator with a checkmark.

Disable Buy and Cash Out buttons when `!uiState.bankLinked`:
```kotlin
Button(
    onClick = onNavigateToOnramp,
    enabled = uiState.bankLinked,  // ADD THIS
    // ... existing styling
)
```
Use `.copy(alpha = 0.5f)` on colors when disabled for visual clarity.

- [ ] **Step 3: Commit**

```bash
git add xion-android-demo/app/src/main/java/com/burnt/xiondemo/ui/screens/wallet/WalletViewModel.kt \
      xion-android-demo/app/src/main/java/com/burnt/xiondemo/ui/screens/wallet/WalletScreen.kt
git commit -m "feat: add bank link status to wallet, disable Buy/Cash Out when unlinked"
```

---

### Task 4: Update Android Navigation and Clean Up Onramp

**Files:**
- Modify: `xion-android-demo/app/src/main/java/com/burnt/xiondemo/navigation/NavGraph.kt`
- Modify: `xion-android-demo/app/src/main/java/com/burnt/xiondemo/ui/screens/brale/OnrampScreen.kt`
- Modify: `xion-android-demo/app/src/main/java/com/burnt/xiondemo/ui/screens/brale/OnrampViewModel.kt`

- [ ] **Step 1: Add LINK_BANK route to NavGraph**

Add `const val LINK_BANK = "link_bank"` to `Routes`.

Add composable:
```kotlin
composable(Routes.LINK_BANK) {
    LinkBankScreen(onDone = { navController.popBackStack() })
}
```

Wire from WalletScreen:
```kotlin
onNavigateToLinkBank = { navController.navigate(Routes.LINK_BANK) },
```

- [ ] **Step 2: Remove bank linking from OnrampScreen**

Remove the entire bank status Card section (lines 131-302 containing form fields, validation display, and Link button). The onramp now starts directly with the amount input.

Remove the `if (!uiState.bankLinked)` conditional — the form section is gone.

Keep: amount input, fee card, Buy Stablecoins button, processing/status views.

- [ ] **Step 3: Remove bank linking from OnrampViewModel**

Remove: `userName`, `userEmail`, `userPhone`, `userDob` and their error properties from `OnrampUiState`.

Remove: `updateUserName()`, `updateUserEmail()`, `updateUserPhone()`, `updateUserDob()`, `isLinkFormValid`, `requestPlaidLinkToken()`, `onPlaidSuccess()`, `onPlaidCancelled()`, `onPlaidExit()`, `unlinkBank()`.

Keep: `amount`, `amountError`, `bankLinked`, `bankAddressId`, `xionAddressId`, `isFormValid`, `submitOnramp()`, `pollForTokenArrival()`, `reset()`, `clearError()`.

Remove Plaid Link launcher from OnrampScreen since it's no longer needed there.

- [ ] **Step 4: Commit**

```bash
git add xion-android-demo/app/src/main/java/com/burnt/xiondemo/navigation/NavGraph.kt \
      xion-android-demo/app/src/main/java/com/burnt/xiondemo/ui/screens/brale/OnrampScreen.kt \
      xion-android-demo/app/src/main/java/com/burnt/xiondemo/ui/screens/brale/OnrampViewModel.kt
git commit -m "feat: add link-bank route, remove bank linking from onramp screen"
```

---

### Task 5: Create iOS LinkBankViewModel

**Files:**
- Create: `xion-ios/XionDemo/UI/Screens/LinkBank/LinkBankViewModel.swift`

Mirror of Android LinkBankViewModel. Extracts form state, validation, and Plaid flow from OnrampViewModel.

- [ ] **Step 1: Create LinkBankViewModel.swift**

Same structure as Android: form fields with validation, Plaid link token request, onPlaidSuccess/onPlaidCancelled, unlinkBank, persistence via SecureStorage. Uses `PlaidLinkService` for Plaid flow.

- [ ] **Step 2: Commit**

```bash
git add xion-ios/XionDemo/UI/Screens/LinkBank/LinkBankViewModel.swift
git commit -m "feat: create iOS LinkBankViewModel for standalone bank linking"
```

---

### Task 6: Create iOS LinkBankView

**Files:**
- Create: `xion-ios/XionDemo/UI/Screens/LinkBank/LinkBankView.swift`

Full-screen bank linking UI matching Android. Uses `OnrampTextField` component (move or duplicate from OnrampView).

- [ ] **Step 1: Create LinkBankView.swift**

Same layout as Android: navigation bar with back button, form fields when not linked, success state when linked, error banner. Reuse the `OnrampTextField` component — move it to a shared location or keep in OnrampView and import.

- [ ] **Step 2: Commit**

```bash
git add xion-ios/XionDemo/UI/Screens/LinkBank/LinkBankView.swift
git commit -m "feat: create iOS LinkBankView for standalone bank linking"
```

---

### Task 7: Update iOS WalletView and WalletViewModel

**Files:**
- Modify: `xion-ios/XionDemo/UI/Screens/Wallet/WalletViewModel.swift`
- Modify: `xion-ios/XionDemo/UI/Screens/Wallet/WalletView.swift`

- [ ] **Step 1: Add bankLinked to WalletViewModel**

Add `@Published var bankLinked = false` and inject `SecureStorage`.

In `refresh()`, check:
```swift
let bankId = secureStorage.getBraleBankAddressId()
bankLinked = bankId != nil && !bankId!.isEmpty
```

- [ ] **Step 2: Update WalletView**

Add `onNavigateToLinkBank` callback parameter.

Before the Buy/Cash Out HStack, add a bank status card (same design as Android). Disable both buttons when `!viewModel.bankLinked`.

- [ ] **Step 3: Commit**

```bash
git add xion-ios/XionDemo/UI/Screens/Wallet/WalletViewModel.swift \
      xion-ios/XionDemo/UI/Screens/Wallet/WalletView.swift
git commit -m "feat: add bank link status to iOS wallet, disable Buy/Cash Out when unlinked"
```

---

### Task 8: Update iOS Navigation and Clean Up Onramp

**Files:**
- Modify: `xion-ios/XionDemo/Navigation/AppNavigation.swift`
- Modify: `xion-ios/XionDemo/UI/Screens/Brale/OnrampView.swift`
- Modify: `xion-ios/XionDemo/UI/Screens/Brale/OnrampViewModel.swift`

- [ ] **Step 1: Add .linkBank route**

Add `case linkBank` to `Route` enum.

Wire navigation in AppNavigation:
```swift
case .linkBank:
    LinkBankView(
        viewModel: LinkBankViewModel(
            braleRepository: container.braleRepository,
            secureStorage: container.secureStorage,
            plaidLinkService: container.plaidLinkService
        ),
        onDone: { path.removeLast() }
    )
```

Pass to WalletView:
```swift
onNavigateToLinkBank: { path.append(Route.linkBank) },
```

- [ ] **Step 2: Remove bank linking from OnrampView**

Remove the entire bank status VStack (lines 56-155). The view now starts with the amount input.

Move `OnrampTextField` to a shared file or keep it in OnrampView if LinkBankView defines its own (preferred for independence).

- [ ] **Step 3: Remove bank linking from OnrampViewModel**

Remove: `userName`, `userEmail`, `userPhone`, `userDob` and error properties.

Remove: `updateUserName()`, `updateUserEmail()`, `updateUserPhone()`, `updateUserDob()`, `isLinkFormValid`, `requestPlaidLinkToken()`, `onPlaidSuccess()`, `onPlaidCancelled()`, `unlinkBank()`.

Keep: `amount`, `amountError`, `bankLinked`, `bankAddressId`, `xionAddressId`, `isFormValid`, `submitOnramp()`, `pollForTokenArrival()`, `reset()`, `clearError()`.

- [ ] **Step 4: Commit**

```bash
git add xion-ios/XionDemo/Navigation/AppNavigation.swift \
      xion-ios/XionDemo/UI/Screens/Brale/OnrampView.swift \
      xion-ios/XionDemo/UI/Screens/Brale/OnrampViewModel.swift
git commit -m "feat: add link-bank route, remove bank linking from iOS onramp"
```

---

### Task 9: Build, Test, and Push

- [ ] **Step 1: Build Android**

```bash
cd xion-android-demo && ./gradlew assembleDebug
```

- [ ] **Step 2: Build iOS**

```bash
cd xion-ios && xcodegen generate && xcodebuild -project XionDemo.xcodeproj -scheme XionDemo \
  -destination 'platform=iOS Simulator,name=iPhone 16 Pro' -derivedDataPath build build
```

- [ ] **Step 3: Manual verification on both platforms**

1. Wallet screen: verify "Link Bank Account" card appears when no bank linked
2. Buy and Cash Out buttons are visually disabled and non-tappable
3. Tap "Link" on wallet → navigates to Link Bank Account screen
4. Fill form, tap Link Bank Account → Plaid Link opens
5. Complete Plaid flow → bank shows as linked, Done navigates back
6. Wallet screen: Buy and Cash Out now enabled, bank status shows linked
7. Tap Buy → goes to onramp, no bank linking form visible, just amount + fee + Buy button
8. Tap Cash Out → goes to offramp, works as before

- [ ] **Step 4: Final commit and push**

```bash
git push
```
