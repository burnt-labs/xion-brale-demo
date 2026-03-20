import Combine
import Foundation

@MainActor
final class HistoryViewModel: ObservableObject {

    @Published var transactions: [TransactionResult] = []
    @Published var selectedTransaction: TransactionResult?

    private let repository: XionRepositoryProtocol
    private var cancellables = Set<AnyCancellable>()

    init(repository: XionRepositoryProtocol) {
        self.repository = repository

        repository.sessionManager.$transactionHistory
            .receive(on: DispatchQueue.main)
            .sink { [weak self] txList in
                self?.transactions = txList.reversed()
            }
            .store(in: &cancellables)
    }
}
