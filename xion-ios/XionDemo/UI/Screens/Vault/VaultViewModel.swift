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

    // MARK: - Confirmation State

    enum PendingAction {
        case deposit, withdraw, withdrawAll
    }

    @Published var pendingAction: PendingAction?

    func requestDeposit() {
        guard !amount.isEmpty else { return }
        pendingAction = .deposit
    }

    func requestWithdraw() {
        guard !amount.isEmpty else { return }
        pendingAction = .withdraw
    }

    func requestWithdrawAll() {
        pendingAction = .withdrawAll
    }

    func cancelPending() {
        pendingAction = nil
    }

    func confirmPending() {
        guard let action = pendingAction else { return }
        pendingAction = nil
        switch action {
        case .deposit: deposit()
        case .withdraw: withdraw()
        case .withdrawAll: withdrawAll()
        }
    }

    var confirmTitle: String {
        switch pendingAction {
        case .deposit: return "Confirm Deposit"
        case .withdraw: return "Confirm Withdrawal"
        case .withdrawAll: return "Confirm Withdraw All"
        case .none: return ""
        }
    }

    var confirmDetails: [(String, String)] {
        switch pendingAction {
        case .deposit:
            return [
                ("Action", "Deposit"),
                ("Token", selectedToken.rawValue),
                ("Amount", "\(amount) \(selectedToken.rawValue)"),
            ]
        case .withdraw:
            return [
                ("Action", "Withdraw"),
                ("Token", selectedToken.rawValue),
                ("Amount", "\(amount) \(selectedToken.rawValue)"),
            ]
        case .withdrawAll:
            return [
                ("Action", "Withdraw All"),
                ("Token", "All deposited tokens"),
            ]
        case .none:
            return []
        }
    }

    // MARK: - Execute Actions

    private func deposit() {
        let microAmount = CoinFormatter.displayToMicro(amount)

        Task {
            isLoading = true
            error = nil
            txHash = nil
            do {
                let result = try await repository.vaultDeposit(amount: microAmount, denom: selectedToken.denom)
                if result.success {
                    txHash = result.txHash
                    amount = ""
                    delayedBalanceReload()
                } else {
                    self.error = result.rawLog
                }
            } catch {
                self.error = (error as? MobError).flatMap { e -> String? in
                        if case .Network(let msg) = e { return msg }
                        if case .Transaction(let msg) = e { return msg }
                        if case .InsufficientFunds(let msg) = e { return msg }
                        return nil
                    } ?? error.localizedDescription
            }
            isLoading = false
        }
    }

    private func withdraw() {
        let microAmount = CoinFormatter.displayToMicro(amount)

        Task {
            isLoading = true
            error = nil
            txHash = nil
            do {
                let result = try await repository.vaultWithdraw(amount: microAmount, denom: selectedToken.denom)
                if result.success {
                    txHash = result.txHash
                    amount = ""
                    delayedBalanceReload()
                } else {
                    self.error = result.rawLog
                }
            } catch {
                self.error = (error as? MobError).flatMap { e -> String? in
                        if case .Network(let msg) = e { return msg }
                        if case .Transaction(let msg) = e { return msg }
                        if case .InsufficientFunds(let msg) = e { return msg }
                        return nil
                    } ?? error.localizedDescription
            }
            isLoading = false
        }
    }

    private func withdrawAll() {
        Task {
            isLoading = true
            error = nil
            txHash = nil
            do {
                let result = try await repository.vaultWithdrawAll()
                if result.success {
                    txHash = result.txHash
                    amount = ""
                    delayedBalanceReload()
                } else {
                    self.error = result.rawLog
                }
            } catch {
                self.error = (error as? MobError).flatMap { e -> String? in
                        if case .Network(let msg) = e { return msg }
                        if case .Transaction(let msg) = e { return msg }
                        if case .InsufficientFunds(let msg) = e { return msg }
                        return nil
                    } ?? error.localizedDescription
            }
            isLoading = false
        }
    }

    private func delayedBalanceReload() {
        Task {
            try? await Task.sleep(nanoseconds: 3_000_000_000) // 3 seconds
            loadAllBalances()
        }
    }

    func clearError() { error = nil }
    func resetState() { txHash = nil; error = nil; amount = "" }
}
