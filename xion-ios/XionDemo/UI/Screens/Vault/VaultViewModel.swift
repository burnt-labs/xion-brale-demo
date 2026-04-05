import Foundation

@MainActor
final class VaultViewModel: ObservableObject {

    @Published var vaultBalance: String?
    @Published var walletBalance: String?
    @Published var walletSbcBalance: String?
    @Published var selectedToken: SendToken = .xion
    @Published var amount = ""
    @Published var isLoading = false
    @Published var isBalanceLoading = false
    @Published var txHash: String?
    @Published var error: String?

    private let repository: XionRepositoryProtocol

    init(repository: XionRepositoryProtocol) {
        self.repository = repository
        loadAllBalances()
    }

    func selectToken(_ token: SendToken) {
        selectedToken = token
        amount = ""
        error = nil
    }

    func loadAllBalances() {
        loadVaultBalance()
        loadWalletBalances()
    }

    private func loadVaultBalance() {
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

    private func loadWalletBalances() {
        Task {
            do {
                let info = try await repository.getBalance()
                walletBalance = info.amount
            } catch {}
        }
        Task {
            do {
                let info = try await repository.getSbcBalance()
                walletSbcBalance = info.amount
            } catch {}
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
                let result = try await repository.vaultDeposit(amount: microAmount, denom: selectedToken.denom)
                if result.success {
                    txHash = result.txHash
                    amount = ""
                    loadAllBalances()
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
                let result = try await repository.vaultWithdraw(amount: microAmount, denom: selectedToken.denom)
                if result.success {
                    txHash = result.txHash
                    amount = ""
                    loadAllBalances()
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
                    loadAllBalances()
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
