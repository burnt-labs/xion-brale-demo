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
