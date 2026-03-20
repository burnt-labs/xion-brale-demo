import Foundation

@MainActor
final class SendViewModel: ObservableObject {

    @Published var recipient = ""
    @Published var amount = ""
    @Published var memo = ""
    @Published var recipientError: String?
    @Published var amountError: String?
    @Published var isLoading = false
    @Published var txHash: String?
    @Published var error: String?
    @Published var showConfirmation = false

    private let repository: XionRepositoryProtocol

    init(repository: XionRepositoryProtocol) {
        self.repository = repository
    }

    var isFormValid: Bool {
        recipient.hasPrefix(Constants.addressPrefix) &&
        recipient.count >= 39 &&
        CoinFormatter.isValidAmount(amount)
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

    func confirmSend() {
        showConfirmation = true
    }

    func send() {
        showConfirmation = false
        let microAmount = CoinFormatter.displayToMicro(amount)

        Task {
            isLoading = true
            error = nil
            do {
                let result = try await repository.send(
                    toAddress: recipient,
                    amount: microAmount,
                    memo: memo
                )
                txHash = result.txHash
            } catch {
                self.error = error.localizedDescription
            }
            isLoading = false
        }
    }

    func clearError() {
        error = nil
    }
}
