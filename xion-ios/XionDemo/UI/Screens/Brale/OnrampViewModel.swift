import Foundation

enum OnrampStep {
    case form
    case processing
    case status
}

@MainActor
final class OnrampViewModel: ObservableObject {

    // Form fields
    @Published var userName = ""
    @Published var userEmail = ""
    @Published var userPhone = ""
    @Published var userDob = ""
    @Published var userNameError: String?
    @Published var userEmailError: String?
    @Published var userPhoneError: String?
    @Published var userDobError: String?
    @Published var amount = ""
    @Published var amountError: String?

    // Bank/transfer state
    @Published var bankLinked = false
    @Published var bankAddressId: String?
    @Published var bankName: String?
    @Published var xionAddressId: String?
    @Published var isLoading = false
    @Published var transfer: BraleTransfer?
    @Published var tokensReceived = false
    @Published var receivedAmount: String?
    @Published var error: String?
    @Published var step: OnrampStep = .form

    private let braleRepository: BraleRepositoryProtocol
    private let xionRepository: XionRepositoryProtocol
    private let secureStorage: SecureStorage
    private let plaidLinkService: PlaidLinkService

    private var initialSbcBalance: Int64 = 0
    private var pollTask: Task<Void, Never>?

    var isLinkFormValid: Bool {
        !userName.trimmingCharacters(in: .whitespaces).isEmpty
            && userNameError == nil
            && !userEmail.trimmingCharacters(in: .whitespaces).isEmpty
            && userEmailError == nil
            && !userPhone.trimmingCharacters(in: .whitespaces).isEmpty
            && userPhoneError == nil
            && !userDob.trimmingCharacters(in: .whitespaces).isEmpty
            && userDobError == nil
    }

    var isFormValid: Bool {
        guard let num = Double(amount), num > 0, amountError == nil, bankLinked else {
            return false
        }
        return true
    }

    init(
        braleRepository: BraleRepositoryProtocol,
        xionRepository: XionRepositoryProtocol,
        secureStorage: SecureStorage,
        plaidLinkService: PlaidLinkService
    ) {
        self.braleRepository = braleRepository
        self.xionRepository = xionRepository
        self.secureStorage = secureStorage
        self.plaidLinkService = plaidLinkService

        // Restore cached IDs and user data from secure storage
        let bankId = secureStorage.getBraleBankAddressId()
        let xionId = secureStorage.getBraleXionAddressId()
        bankLinked = bankId != nil && !bankId!.isEmpty
        bankAddressId = bankId
        xionAddressId = xionId
        userName = secureStorage.getBraleUserName() ?? ""
        userEmail = secureStorage.getBraleUserEmail() ?? ""
        userPhone = secureStorage.getBraleUserPhone() ?? ""
        userDob = secureStorage.getBraleUserDob() ?? ""

        if xionId == nil { checkExistingXionAddress() }
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
        if trimmed.isEmpty {
            userPhoneError = "Phone number is required"
        } else if !trimmed.hasPrefix("+") {
            userPhoneError = "Must start with + (e.g. +15551234567)"
        } else if !trimmed.dropFirst().allSatisfy(\.isNumber) {
            userPhoneError = "Only digits after +"
        } else if trimmed.count < 11 {
            userPhoneError = "Enter full number with country code"
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

    func updateAmount(_ value: String) {
        amount = value
        if value.isEmpty {
            amountError = nil
        } else if let num = Double(value) {
            if num <= 0 {
                amountError = "Amount must be positive"
            } else if num > 50000 {
                amountError = "ACH limit is $50,000"
            } else {
                amountError = nil
            }
        } else {
            amountError = "Enter a valid amount"
        }
    }

    // MARK: - Plaid Link

    func requestPlaidLinkToken() {
        let name = userName.trimmingCharacters(in: .whitespaces)
        let email = userEmail.trimmingCharacters(in: .whitespaces)
        let phone = userPhone.trimmingCharacters(in: .whitespaces)
        let dob = userDob.trimmingCharacters(in: .whitespaces)

        // Persist user data
        secureStorage.saveBraleUserName(name)
        secureStorage.saveBraleUserEmail(email)
        secureStorage.saveBraleUserPhone(phone)
        secureStorage.saveBraleUserDob(dob)

        Task {
            isLoading = true
            error = nil
            do {
                let response = try await braleRepository.createPlaidLinkToken(
                    name: name, email: email, phone: phone, dob: dob
                )
                let result = try await plaidLinkService.openLink(token: response.linkToken)
                switch result {
                case .success(let publicToken):
                    onPlaidSuccess(publicToken: publicToken)
                case .cancelled:
                    onPlaidCancelled()
                }
            } catch {
                self.error = error.localizedDescription
                isLoading = false
            }
        }
    }

    func onPlaidSuccess(publicToken: String) {
        Task {
            isLoading = true
            error = nil
            do {
                let addressId = try await braleRepository.registerBankAccount(publicToken: publicToken)
                secureStorage.saveBraleBankAddressId(addressId)
                bankLinked = true
                bankAddressId = addressId
                isLoading = false
            } catch {
                self.error = error.localizedDescription
                isLoading = false
            }
        }
    }

    func onPlaidCancelled() {
        isLoading = false
    }

    func unlinkBank() {
        secureStorage.deleteBraleBankAddressId()
        bankLinked = false
        bankAddressId = nil
        bankName = nil
    }

    // MARK: - Submit

    func submitOnramp() {
        guard let bankId = bankAddressId else { return }

        Task {
            isLoading = true
            error = nil
            step = .processing

            do {
                initialSbcBalance = await getCurrentSbcBalance()

                let resolvedXionId: String
                if let existingId = xionAddressId {
                    resolvedXionId = existingId
                } else {
                    guard let walletAddress = xionRepository.sessionManager.walletState.metaAccountAddress else {
                        throw OnrampError.walletNotConnected
                    }
                    let addr = try await braleRepository.registerXionAddress(walletAddress: walletAddress)
                    secureStorage.saveBraleXionAddressId(addr.id)
                    xionAddressId = addr.id
                    resolvedXionId = addr.id
                }

                let transfer = try await braleRepository.createOnrampTransfer(
                    amount: amount,
                    bankAddressId: bankId,
                    xionAddressId: resolvedXionId
                )
                self.transfer = transfer
                isLoading = false
                pollForTokenArrival()
            } catch {
                self.error = error.localizedDescription
                isLoading = false
                step = .form
            }
        }
    }

    func clearError() {
        error = nil
    }

    func reset() {
        amount = ""
        amountError = nil
        isLoading = false
        transfer = nil
        tokensReceived = false
        receivedAmount = nil
        error = nil
        step = .form
        pollTask?.cancel()
        pollTask = nil
    }

    // MARK: - Private

    private func checkExistingXionAddress() {
        Task {
            do {
                guard let walletAddress = xionRepository.sessionManager.walletState.metaAccountAddress else { return }
                if let existing = try await braleRepository.findExistingXionAddress(walletAddress: walletAddress) {
                    secureStorage.saveBraleXionAddressId(existing.id)
                    xionAddressId = existing.id
                }
            } catch {
                // Non-critical
            }
        }
    }

    private func getCurrentSbcBalance() async -> Int64 {
        do {
            let info = try await xionRepository.getSbcBalance()
            return Int64(info.amount) ?? 0
        } catch {
            return 0
        }
    }

    private func pollForTokenArrival() {
        pollTask = Task {
            for _ in 0..<60 {
                try? await Task.sleep(nanoseconds: 3_000_000_000)
                guard !Task.isCancelled else { return }

                let currentBalance = await getCurrentSbcBalance()
                if currentBalance > initialSbcBalance {
                    let received = currentBalance - initialSbcBalance
                    tokensReceived = true
                    receivedAmount = "\(received)"
                    step = .status

                    await xionRepository.appendTransaction(TransactionResult(
                        txHash: transfer?.id ?? "",
                        success: true,
                        gasUsed: "0",
                        gasWanted: "0",
                        height: 0,
                        rawLog: "",
                        timestamp: transfer?.createdAt ?? "",
                        fee: "0",
                        txType: "Buy SBC",
                        amount: "\(received)",
                        amountDenom: Constants.sbcDisplayDenom,
                        recipient: ""
                    ))
                    return
                }
            }
            step = .status
        }
    }
}

enum OnrampError: LocalizedError {
    case walletNotConnected

    var errorDescription: String? {
        switch self {
        case .walletNotConnected: return "Wallet not connected"
        }
    }
}
