import Foundation
import UIKit

struct PlaidDiagnostic: Identifiable {
    let id = UUID()
    let timestamp: Date
    let phone: String
    let outcome: String
    let tokenRequestId: String?
    let linkSessionId: String?
    let exitRequestId: String?
    let errorMessage: String?
}

@MainActor
final class LinkBankViewModel: ObservableObject {

    // Form fields
    @Published var userName = ""
    @Published var userEmail = ""
    @Published var userPhone = ""
    @Published var userDob = ""
    @Published var userNameError: String?
    @Published var userEmailError: String?
    @Published var userPhoneError: String?
    @Published var userDobError: String?

    // Bank state
    @Published var bankLinked = false
    @Published var bankAddressId: String?
    @Published var bankName: String?
    @Published var isLoading = false
    @Published var error: String?

    // Existing linked banks (skip Plaid re-link when a bank is already on the account)
    @Published var existingBanks: [BraleAddress] = []
    @Published var isLoadingBanks = false

    // Plaid diagnostics — last 10 sessions, newest first, in-memory only
    @Published var diagnostics: [PlaidDiagnostic] = []

    private let braleRepository: BraleRepositoryProtocol
    private let secureStorage: SecureStorage
    private let plaidLinkService: PlaidLinkService

    // Only the phone is collected/sent — it's the field that drives Plaid's prefill,
    // so requiring the user's own prevents Brale from falling back to the shared
    // account owner's number (which would leak that person's phone to others).
    var isPhoneValid: Bool {
        !userPhone.trimmingCharacters(in: .whitespaces).isEmpty && userPhoneError == nil
    }

    init(
        braleRepository: BraleRepositoryProtocol,
        secureStorage: SecureStorage,
        plaidLinkService: PlaidLinkService
    ) {
        self.braleRepository = braleRepository
        self.secureStorage = secureStorage
        self.plaidLinkService = plaidLinkService

        // Restore cached bank ID and user data from secure storage
        let bankId = secureStorage.getBraleBankAddressId()
        bankLinked = bankId != nil && !bankId!.isEmpty
        bankAddressId = bankId
        userPhone = secureStorage.getBraleUserPhone() ?? ""

        if let bankId, !bankId.isEmpty {
            Task { await resolveBankName(bankId) }
        }
    }

    /// Resolves the linked bank's display name + masked number from the address
    /// list so the "Bank Account Linked" screen says which account it is.
    private func resolveBankName(_ addressId: String) async {
        guard let banks = try? await braleRepository.getLinkedBankAddresses(),
              let match = banks.first(where: { $0.id == addressId }) else { return }
        let masked = match.accountNumber.map { " · \($0)" } ?? ""
        bankName = (match.name ?? "Bank account") + masked
    }

    // MARK: - Validation

    func updateUserName(_ value: String) {
        userName = value
        userNameError = value.trimmingCharacters(in: .whitespaces).isEmpty ? "Name is required" : nil
    }

    func updateUserEmail(_ value: String) {
        userEmail = value
        let trimmed = value.trimmingCharacters(in: .whitespaces)
        if trimmed.isEmpty {
            userEmailError = "Email is required"
        } else if !trimmed.contains("@") || !trimmed.contains(".") {
            userEmailError = "Enter a valid email address"
        } else {
            userEmailError = nil
        }
    }

    func updateUserPhone(_ value: String) {
        userPhone = value
        let trimmed = value.trimmingCharacters(in: .whitespaces)
        // The UI fixes a "+1" prefix and the user types only the 10 local digits.
        let localDigits = trimmed.hasPrefix("+1")
            ? trimmed.dropFirst(2).filter(\.isNumber)
            : trimmed.filter(\.isNumber)
        if trimmed.isEmpty {
            userPhoneError = "Phone number is required"
        } else if localDigits.count != 10 {
            userPhoneError = "Enter a 10-digit US phone number"
        } else {
            userPhoneError = nil
        }
    }

    func updateUserDob(_ value: String) {
        userDob = value
        let trimmed = value.trimmingCharacters(in: .whitespaces)
        if trimmed.isEmpty {
            userDobError = "Date of birth is required"
        } else if trimmed.range(of: #"^\d{4}-\d{2}-\d{2}$"#, options: .regularExpression) == nil {
            userDobError = "Use YYYY-MM-DD format"
        } else {
            userDobError = nil
        }
    }

    // MARK: - Plaid Link

    func requestPlaidLinkToken() {
        let phone = userPhone.trimmingCharacters(in: .whitespaces)
        guard !phone.isEmpty, userPhoneError == nil else { return }
        secureStorage.saveBraleUserPhone(phone)

        Task {
            isLoading = true
            error = nil
            var tokenRequestId: String? = nil
            do {
                // Phone only — name/email/dob are intentionally omitted (see isPhoneValid).
                let response = try await braleRepository.createPlaidLinkToken(
                    name: "", email: "", phone: phone, dob: nil
                )
                tokenRequestId = response.requestId
                let result = try await plaidLinkService.openLink(token: response.linkToken)
                switch result {
                case .success(let info):
                    recordDiagnostic(
                        phone: phone,
                        outcome: "Linked",
                        tokenRequestId: tokenRequestId,
                        linkSessionId: info.linkSessionId
                    )
                    onPlaidSuccess(publicToken: info.publicToken, accountMask: info.accountMask)
                case .cancelled(let info):
                    recordDiagnostic(
                        phone: phone,
                        outcome: "Cancelled",
                        tokenRequestId: tokenRequestId,
                        linkSessionId: info.linkSessionId,
                        exitRequestId: info.requestId
                    )
                    onPlaidCancelled()
                }
            } catch let plaidError as PlaidLinkError {
                if case .linkKitError(let info) = plaidError {
                    recordDiagnostic(
                        phone: phone,
                        outcome: "Error",
                        tokenRequestId: tokenRequestId,
                        linkSessionId: info.linkSessionId,
                        exitRequestId: info.requestId,
                        errorMessage: info.errorMessage
                    )
                } else {
                    recordDiagnostic(
                        phone: phone,
                        outcome: "Error",
                        tokenRequestId: tokenRequestId,
                        linkSessionId: nil,
                        errorMessage: plaidError.localizedDescription
                    )
                }
                self.error = plaidError.localizedDescription
                isLoading = false
            } catch {
                recordDiagnostic(
                    phone: phone,
                    outcome: "Error",
                    tokenRequestId: tokenRequestId,
                    linkSessionId: nil,
                    errorMessage: error.localizedDescription
                )
                self.error = error.localizedDescription
                isLoading = false
            }
        }
    }

    private func recordDiagnostic(
        phone: String,
        outcome: String,
        tokenRequestId: String?,
        linkSessionId: String?,
        exitRequestId: String? = nil,
        errorMessage: String? = nil
    ) {
        let entry = PlaidDiagnostic(
            timestamp: Date(),
            phone: phone,
            outcome: outcome,
            tokenRequestId: tokenRequestId,
            linkSessionId: linkSessionId,
            exitRequestId: exitRequestId,
            errorMessage: errorMessage
        )
        diagnostics.insert(entry, at: 0)
        if diagnostics.count > 10 {
            diagnostics = Array(diagnostics.prefix(10))
        }
        print("[PlaidLink] diag phone=\(phone) outcome=\(outcome) tokenReqId=\(tokenRequestId ?? "nil") sessionId=\(linkSessionId ?? "nil") exitReqId=\(exitRequestId ?? "nil")")
    }

    func copyDiagnosticsToClipboard() {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        let lines = diagnostics.map { d -> String in
            """
            [\(formatter.string(from: d.timestamp))] \(d.outcome)
              phone:        \(d.phone)
              token req_id: \(d.tokenRequestId ?? "<not provided>")
              session_id:   \(d.linkSessionId ?? "<not provided>")
              exit req_id:  \(d.exitRequestId ?? "<n/a>")
              error:        \(d.errorMessage ?? "<none>")
            """
        }
        UIPasteboard.general.string = lines.joined(separator: "\n\n")
    }

    func clearDiagnostics() {
        diagnostics.removeAll()
    }

    func onPlaidSuccess(publicToken: String, accountMask: String?) {
        Task {
            isLoading = true
            error = nil
            do {
                let addressId = try await braleRepository.registerBankAccount(
                    publicToken: publicToken,
                    accountMask: accountMask
                )
                secureStorage.saveBraleBankAddressId(addressId)
                bankLinked = true
                bankAddressId = addressId
                isLoading = false
                await resolveBankName(addressId)
            } catch {
                // Brale returns an opaque 500 when re-registering a bank that's already
                // on the account. It may be linked under this wallet (shown in the
                // picker) or under a different one. Reload and give an account-aware hint.
                loadExistingBanks()
                self.error = "That bank is already linked — possibly under a different account. Check \"Use a linked bank\", or try a different bank."
                isLoading = false
            }
        }
    }

    func onPlaidCancelled() {
        isLoading = false
    }

    // MARK: - Existing Banks

    func loadExistingBanks() {
        Task {
            isLoadingBanks = true
            do {
                existingBanks = try await braleRepository.getLinkedBankAddresses()
            } catch {
                // Non-fatal: the form is still available as a fallback
                existingBanks = []
            }
            isLoadingBanks = false
        }
    }

    func useExistingBank(_ bank: BraleAddress) {
        braleRepository.useExistingBankAddress(bank.id)
        bankAddressId = bank.id
        let masked = bank.accountNumber.map { " · \($0)" } ?? ""
        bankName = (bank.name ?? "Bank account") + masked
        bankLinked = true
    }

    func unlinkBank() {
        secureStorage.deleteBraleBankAddressId()
        bankLinked = false
        bankAddressId = nil
        bankName = nil
    }

    func clearError() {
        error = nil
    }
}
