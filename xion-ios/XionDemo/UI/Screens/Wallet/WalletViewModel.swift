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
    @Published var isBalanceLoading = false
    @Published var blockHeight: Int64?
    @Published var chainId = Constants.chainId
    @Published var error: String?
    @Published var sessionExpiryWarning = false
    @Published var isDisconnected = false
    @Published var transactions: [TransactionResult] = []
    @Published var bankLinked = false

    private let repository: XionRepositoryProtocol
    private let braleRepository: BraleRepositoryProtocol
    private let secureStorage: SecureStorage
    private var cancellables = Set<AnyCancellable>()
    private var expiryTimer: Timer?

    init(
        repository: XionRepositoryProtocol,
        braleRepository: BraleRepositoryProtocol,
        secureStorage: SecureStorage
    ) {
        self.repository = repository
        self.braleRepository = braleRepository
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
        guard let addr = address else {
            NSLog("[WalletVM] loadTransactions skipped — address is nil")
            return
        }
        Task {
            // Buy/Cash Out come from Brale (they carry pending/processing/complete
            // status, and a pending onramp has no on-chain record yet). On-chain history
            // contributes XION and *peer* SBC transfers (Send/Received) — but the SBC
            // legs of a Brale ramp (mint from the issuer, deposit to a Brale custodial
            // address) are dropped, since the Brale transfer already represents them.
            // (If the custodial-address fetch fails, an offramp deposit leg may briefly
            // show alongside its Brale "Cash Out" entry — cosmetic, self-heals on refresh.)
            async let onChainAsync = (try? await repository.getRecentTransactions(address: addr)) ?? []
            async let custodialAsync = (try? await braleRepository.getInternalAddresses()) ?? []
            let onChain = await onChainAsync
            let braleParties = Set(await custodialAsync.compactMap { $0.address }).union([Constants.braleSbcIssuer])
            let peerOnChain = onChain.filter { tx in
                guard tx.amountDenom.lowercased().contains("sbc") else { return true } // keep all XION
                return !braleParties.contains(tx.counterparty) // keep peer SBC, drop ramp legs
            }
            let braleTxs = await loadBraleTransfers()
            let merged = (braleTxs + peerOnChain)
                .sorted { $0.timestamp > $1.timestamp }
                .prefix(5)
            transactions = Array(merged)
            NSLog("[WalletVM] %d brale + %d on-chain tx", braleTxs.count, peerOnChain.count)
        }
    }

    private func loadBraleTransfers() async -> [TransactionResult] {
        guard let transfers = try? await braleRepository.getRecentTransfers() else { return [] }
        return transfers.compactMap { Self.mapBraleTransfer($0) }
    }

    /// Maps a Brale transfer to a wallet row and surfaces its pending/complete status.
    /// Classify by BOTH legs: an ACH buy pays USD for SBC, a cash out sells SBC for
    /// USD, and an SBC→SBC transfer is someone sending you stablecoin (not a buy).
    private static func mapBraleTransfer(_ t: BraleTransfer) -> TransactionResult? {
        let src = t.source?.valueType.uppercased() ?? ""
        let dst = t.destination?.valueType.uppercased() ?? ""
        let label: String
        switch (src, dst) {
        case ("USD", "SBC"): label = "Buy"       // ACH onramp
        case ("SBC", "USD"): label = "Cash Out"  // ACH offramp
        case ("SBC", "SBC"): label = "Received"  // someone sent you SBC
        default: return nil
        }

        let status: String
        switch t.status.lowercased() {
        case "pending": status = "Pending"
        case "processing": status = "Processing"
        case "complete", "completed": status = "Completed"
        case "canceled", "cancelled": status = "Canceled"
        case "failed": status = "Failed"
        default: status = t.status.capitalized
        }

        let isDone = status == "Completed"
        let isFailed = status == "Failed" || status == "Canceled"
        return TransactionResult(
            txHash: t.id,
            success: !isFailed,
            gasUsed: "0",
            gasWanted: "0",
            height: 0,
            rawLog: "",
            timestamp: t.createdAt ?? "",
            fee: "",
            txType: label,
            amount: "",
            amountDenom: "SBC",
            recipient: "",
            status: status,
            displayAmount: "$\(t.amount.value)",
            inProgress: !isDone && !isFailed
        )
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
