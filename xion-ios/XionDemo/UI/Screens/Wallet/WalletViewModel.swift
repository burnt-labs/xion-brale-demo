import Combine
import Foundation

@MainActor
final class WalletViewModel: ObservableObject {

    @Published var address: String?
    @Published var sessionAddress: String?
    @Published var connectionType = "Unknown"
    @Published var grantsActive = true
    @Published var balance: String?
    @Published var sbcBalance: String?
    @Published var vaultBalance: String?
    @Published var isBalanceLoading = false
    @Published var blockHeight: Int64?
    @Published var chainId = Constants.chainId
    @Published var error: String?
    @Published var sessionExpiryWarning = false
    @Published var isDisconnected = false
    @Published var transactions: [TransactionResult] = []
    @Published var bankLinked = false

    private let repository: XionRepositoryProtocol
    private let secureStorage: SecureStorage
    private var cancellables = Set<AnyCancellable>()
    private var expiryTimer: Timer?

    init(repository: XionRepositoryProtocol, secureStorage: SecureStorage) {
        self.repository = repository
        self.secureStorage = secureStorage

        repository.sessionManager.$walletState
            .receive(on: DispatchQueue.main)
            .sink { [weak self] state in
                self?.handleStateChange(state)
            }
            .store(in: &cancellables)

        startExpiryCheck()
        refresh()
    }

    func refresh() {
        checkBankLinked()
        loadBalance()
        loadSbcBalance()
        loadVaultBalance()
        loadBlockHeight()
        loadTransactions()
    }

    private func checkBankLinked() {
        let bankId = secureStorage.getBraleBankAddressId()
        bankLinked = bankId != nil && !bankId!.isEmpty
    }

    func disconnect() {
        repository.disconnect()
    }

    func clearError() {
        error = nil
    }

    private func loadBalance() {
        Task {
            isBalanceLoading = true
            do {
                let info = try await repository.getBalance()
                balance = info.amount
            } catch {
                self.error = error.localizedDescription
            }
            isBalanceLoading = false
        }
    }

    private func loadSbcBalance() {
        Task {
            do {
                let info = try await repository.getSbcBalance()
                sbcBalance = info.amount
            } catch {
                // Non-critical — SBC balance failure should not show error
            }
        }
    }

    private func loadVaultBalance() {
        Task {
            do {
                let info = try await repository.getVaultBalance()
                vaultBalance = info.amount
            } catch {
                // Non-critical
            }
        }
    }

    private func loadBlockHeight() {
        Task {
            do {
                blockHeight = try await repository.getBlockHeight()
            } catch {
                // Non-critical
            }
        }
    }

    private func loadTransactions() {
        guard let addr = address else { return }
        Task {
            do {
                transactions = try await repository.getRecentTransactions(address: addr)
            } catch {
                // Non-critical — don't show error for history failure
            }
        }
    }

    private func handleStateChange(_ state: WalletState) {
        switch state {
        case .connected(let meta, let session, _, let grants, _):
            address = meta
            sessionAddress = session
            connectionType = "Meta Account"
            grantsActive = grants
            loadTransactions()
        case .disconnected:
            isDisconnected = true
        case .connecting:
            break
        }
    }

    private func startExpiryCheck() {
        expiryTimer = Timer.scheduledTimer(withTimeInterval: 60, repeats: true) { [weak self] _ in
            Task { @MainActor in
                self?.checkSessionExpiry()
            }
        }
    }

    private func checkSessionExpiry() {
        let state = repository.sessionManager.walletState
        guard state.isConnected else { return }
        let now = Int64(Date().timeIntervalSince1970)
        let remaining = state.sessionExpiresAt - now
        if remaining <= 0 {
            repository.disconnect()
        } else if remaining < 300 {
            sessionExpiryWarning = true
        }
    }

    deinit {
        expiryTimer?.invalidate()
    }
}
