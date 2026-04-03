import Foundation

enum OnrampStep {
    case form
    case processing
    case status
}

@MainActor
final class OnrampViewModel: ObservableObject {

    @Published var amount = ""
    @Published var amountError: String?
    @Published var bankLinked = false
    @Published var bankAddressId: String?
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

    private var initialSbcBalance: Int64 = 0
    private var pollTask: Task<Void, Never>?

    var isFormValid: Bool {
        guard let num = Double(amount), num > 0, amountError == nil, bankLinked else {
            return false
        }
        return true
    }

    init(
        braleRepository: BraleRepositoryProtocol,
        xionRepository: XionRepositoryProtocol,
        secureStorage: SecureStorage
    ) {
        self.braleRepository = braleRepository
        self.xionRepository = xionRepository
        self.secureStorage = secureStorage

        let bankId = secureStorage.getBraleBankAddressId()
        let xionId = secureStorage.getBraleXionAddressId()
        bankLinked = bankId != nil && !bankId!.isEmpty
        bankAddressId = bankId
        xionAddressId = xionId

        if xionId == nil { checkExistingXionAddress() }
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
