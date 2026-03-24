import Combine
import Foundation

@MainActor
final class HistoryViewModel: ObservableObject {

    @Published var transactions: [TransactionResult] = []
    @Published var selectedTransaction: TransactionResult?
    @Published var isLoading = false

    private let repository: XionRepositoryProtocol
    private var cancellables = Set<AnyCancellable>()

    init(repository: XionRepositoryProtocol) {
        self.repository = repository

        // Observe in-memory history for immediate updates
        repository.sessionManager.$transactionHistory
            .receive(on: DispatchQueue.main)
            .sink { [weak self] _ in
                self?.mergeTransactions()
            }
            .store(in: &cancellables)

        loadOnChainHistory()
    }

    private var onChainTransactions: [TransactionResult] = []

    private func loadOnChainHistory() {
        guard let address = repository.sessionManager.walletState.metaAccountAddress else { return }
        isLoading = true
        Task {
            do {
                onChainTransactions = try await repository.getRecentTransactions(address: address)
            } catch {
                // Non-critical
            }
            mergeTransactions()
            isLoading = false
        }
    }

    private func mergeTransactions() {
        let inMemory = repository.sessionManager.transactionHistory
        // Deduplicate by txHash, prefer on-chain version (has enriched data)
        var seen = [String: TransactionResult]()
        for tx in onChainTransactions {
            seen[tx.txHash] = tx
        }
        for tx in inMemory {
            if seen[tx.txHash] == nil {
                seen[tx.txHash] = tx
            }
        }
        transactions = seen.values.sorted { $0.height > $1.height }
    }
}
