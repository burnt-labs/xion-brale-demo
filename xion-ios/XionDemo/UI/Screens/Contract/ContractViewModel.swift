import Foundation

@MainActor
final class ContractViewModel: ObservableObject {

    @Published var contractAddress = Constants.sampleContractAddress
    @Published var message = Constants.sampleContractMsg
    @Published var funds = ""
    @Published var isLoading = false
    @Published var txHash: String?
    @Published var error: String?

    private let repository: XionRepositoryProtocol

    init(repository: XionRepositoryProtocol) {
        self.repository = repository
    }

    var isFormValid: Bool {
        contractAddress.hasPrefix(Constants.addressPrefix) &&
        contractAddress.count >= 39 &&
        !message.isEmpty &&
        isValidJson(message)
    }

    func execute() {
        let fundsValue = funds.isEmpty ? nil : funds

        Task {
            isLoading = true
            error = nil
            do {
                let result = try await repository.executeContract(
                    contractAddress: contractAddress,
                    msg: message,
                    funds: fundsValue
                )
                txHash = result.txHash
            } catch {
                self.error = error.localizedDescription
            }
            isLoading = false
        }
    }

    func reset() {
        contractAddress = Constants.sampleContractAddress
        message = Constants.sampleContractMsg
        funds = ""
        txHash = nil
        error = nil
    }

    func clearError() {
        error = nil
    }

    private func isValidJson(_ json: String) -> Bool {
        let trimmed = json.trimmingCharacters(in: .whitespacesAndNewlines)
        return trimmed.hasPrefix("{") && trimmed.hasSuffix("}")
    }
}
