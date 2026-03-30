import Foundation

enum SendToken: String, CaseIterable {
    case xion = "XION"
    case sbc = "SBC"

    var denom: String {
        switch self {
        case .xion: return Constants.coinDenom
        case .sbc: return Constants.braleSbcOnChainDenom
        }
    }
}

@MainActor
final class SendViewModel: ObservableObject {

    @Published var recipient = ""
    @Published var amount = ""
    @Published var memo = ""
    @Published var selectedToken: SendToken = .xion
    @Published var recipientError: String?
    @Published var amountError: String?
    @Published var isLoading = false
    @Published var txResult: TransactionResult?
    @Published var error: String?

    private let repository: XionRepositoryProtocol

    init(repository: XionRepositoryProtocol) {
        self.repository = repository
    }

    var isFormValid: Bool {
        recipient.hasPrefix(Constants.addressPrefix) &&
        recipient.count >= 39 &&
        CoinFormatter.isValidAmount(amount)
    }

    func updateRecipient(_ value: String) {
        recipient = value
        validateRecipient()
    }

    func updateAmount(_ value: String) {
        amount = value
        validateAmount()
    }

    func updateMemo(_ value: String) {
        memo = value
    }

    func validateRecipient() {
        if recipient.isEmpty {
            recipientError = nil
        } else if !recipient.hasPrefix(Constants.addressPrefix) {
            recipientError = "Address must start with '\(Constants.addressPrefix)'"
        } else if recipient.count < 39 {
            recipientError = "Address too short"
        } else {
            recipientError = nil
        }
    }

    func validateAmount() {
        if amount.isEmpty {
            amountError = nil
        } else if !CoinFormatter.isValidAmount(amount) {
            amountError = "Enter a valid amount"
        } else {
            amountError = nil
        }
    }

    func send() {
        let microAmount = CoinFormatter.displayToMicro(amount)

        Task {
            isLoading = true
            error = nil
            do {
                let result = try await repository.send(
                    toAddress: recipient,
                    amount: microAmount,
                    memo: memo,
                    denom: selectedToken.denom
                )
                txResult = result
            } catch {
                self.error = error.localizedDescription
            }
            isLoading = false
        }
    }

    func clearError() {
        error = nil
    }

    func resetState() {
        recipient = ""
        amount = ""
        memo = ""
        selectedToken = .xion
        recipientError = nil
        amountError = nil
        isLoading = false
        txResult = nil
        error = nil
    }
}
