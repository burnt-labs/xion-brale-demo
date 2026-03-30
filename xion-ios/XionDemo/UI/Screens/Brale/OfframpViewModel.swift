import Foundation

enum OfframpStep {
    case form
    case depositing
    case processing
    case status
}

@MainActor
final class OfframpViewModel: ObservableObject {

    @Published var amount = ""
    @Published var amountError: String?
    @Published var bankLinked = false
    @Published var bankAddressId: String?
    @Published var bankName: String?
    @Published var custodialAddress: BraleAddress?
    @Published var isLoading = false
    @Published var depositTxHash: String?
    @Published var depositConfirmed = false
    @Published var transfer: BraleTransfer?
    @Published var error: String?
    @Published var step: OfframpStep = .form

    private let braleRepository: BraleRepositoryProtocol
    private let xionRepository: XionRepositoryProtocol
    private let secureStorage: SecureStorage

    var isFormValid: Bool {
        guard let num = Double(amount), num > 0, amountError == nil, bankLinked, custodialAddress != nil else {
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

        // Restore cached bank ID
        let bankId = secureStorage.getBraleBankAddressId()
        bankLinked = bankId != nil
        bankAddressId = bankId

        if bankId == nil { checkExistingBankAddress() }
        loadCustodialAddress()
    }

    func updateAmount(_ value: String) {
        amount = value
        if value.isEmpty {
            amountError = nil
        } else if let num = Double(value) {
            if num <= 0 {
                amountError = "Amount must be positive"
            } else {
                amountError = nil
            }
        } else {
            amountError = "Enter a valid amount"
        }
    }

    func submitOfframp() {
        guard let bankId = bankAddressId,
              let custodial = custodialAddress,
              let custodialWallet = custodial.address else {
            error = "No custodial deposit address found"
            return
        }

        Task {
            isLoading = true
            error = nil
            step = .depositing

            do {
                // Step 1: Send SBC stablecoins to Brale custodial address on-chain
                let microAmount = CoinFormatter.displayToMicro(amount)
                let sendResult = try await xionRepository.send(
                    toAddress: custodialWallet,
                    amount: microAmount,
                    memo: "Brale offramp deposit",
                    denom: Constants.braleSbcOnChainDenom
                )

                let txHash = sendResult.txHash
                depositTxHash = txHash
                depositConfirmed = true
                step = .processing

                // Step 2: Create offramp transfer via Brale
                let transfer = try await braleRepository.createOfframpTransfer(
                    amount: amount,
                    custodialAddressId: custodial.id,
                    bankAddressId: bankId
                )
                self.transfer = transfer
                isLoading = false
                step = .status

                // Add to transaction history
                await xionRepository.appendTransaction(TransactionResult(
                    txHash: txHash,
                    success: true,
                    gasUsed: "0",
                    gasWanted: "0",
                    height: 0,
                    rawLog: "",
                    timestamp: transfer.createdAt ?? "",
                    fee: "0",
                    txType: "Cash Out",
                    amount: microAmount,
                    amountDenom: Constants.sbcDisplayDenom,
                    recipient: custodialWallet
                ))
            } catch {
                self.error = error.localizedDescription
                isLoading = false
                step = depositConfirmed ? .status : .form
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
        depositTxHash = nil
        depositConfirmed = false
        transfer = nil
        error = nil
        step = .form
    }

    // MARK: - Private

    private func checkExistingBankAddress() {
        Task {
            do {
                if let existing = try await braleRepository.findExistingBankAddress() {
                    secureStorage.saveBraleBankAddressId(existing.id)
                    bankLinked = true
                    bankAddressId = existing.id
                    bankName = existing.name
                }
            } catch {
                // Non-critical
            }
        }
    }

    private func loadCustodialAddress() {
        Task {
            do {
                let internals = try await braleRepository.getInternalAddresses()
                // Find the custodial address that supports xion_testnet specifically
                let custodial = internals.first { addr in
                    addr.transferTypes?.contains(Constants.braleTransferType) == true
                } ?? internals.first
                custodialAddress = custodial
            } catch {
                // Non-critical
            }
        }
    }
}
